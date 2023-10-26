package org.easystogu.runner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.easystogu.cache.runner.AllCacheRunner;
import org.easystogu.config.Constants;
import org.easystogu.db.access.facde.DBAccessFacdeFactory;
import org.easystogu.db.access.table.CheckPointDailyStatisticsTableHelper;
import org.easystogu.db.access.table.IndMATableHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.access.table.WeekStockPriceTableHelper;
import org.easystogu.db.helper.IF.IndicatorDBHelperIF;
import org.easystogu.db.vo.table.MacdVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.runner.history.HistoryBollCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryKDJCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryMACountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryMacdCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryQSDDCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryShenXianCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryWRCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryWeeklyKDJCountAndSaveDBRunner;
import org.easystogu.indicator.runner.history.HistoryWeeklyMacdCountAndSaveDBRunner;
import org.easystogu.log.LogHelper;
import org.easystogu.sina.runner.history.HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner;
import org.easystogu.utils.Strings;
import org.slf4j.Logger;

public class DataBaseSanityCheck implements Runnable {
  private static Logger logger = LogHelper.getLogger(DataBaseSanityCheck.class);
  protected StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
  protected QianFuQuanStockPriceTableHelper qianfuquanStockPriceTable =
      QianFuQuanStockPriceTableHelper.getInstance();
  protected IndicatorDBHelperIF macdTable = DBAccessFacdeFactory.getInstance(Constants.indMacd);
  protected IndicatorDBHelperIF kdjTable = DBAccessFacdeFactory.getInstance(Constants.indKDJ);
  protected IndicatorDBHelperIF bollTable = DBAccessFacdeFactory.getInstance(Constants.indBoll);
  protected IndicatorDBHelperIF shenXianTable =
      DBAccessFacdeFactory.getInstance(Constants.indShenXian);
  protected IndicatorDBHelperIF qsddTable = DBAccessFacdeFactory.getInstance(Constants.indQSDD);
  protected IndicatorDBHelperIF wrTable = DBAccessFacdeFactory.getInstance(Constants.indWR);

  protected IndMATableHelper maTable = IndMATableHelper.getInstance();
  protected HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner historyQianFuQuanRunner =
      new HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner();
  protected CheckPointDailyStatisticsTableHelper checkPointDailyStatisticsTable =
      CheckPointDailyStatisticsTableHelper.getInstance();

  protected WeekStockPriceTableHelper weekStockPriceTable = WeekStockPriceTableHelper.getInstance();
  protected IndicatorDBHelperIF weekMacdTable =
      DBAccessFacdeFactory.getInstance(Constants.indWeekMacd);
  protected IndicatorDBHelperIF weekKdjTable =
      DBAccessFacdeFactory.getInstance(Constants.indWeekKDJ);

  protected HistoryMacdCountAndSaveDBRunner macdRunner = new HistoryMacdCountAndSaveDBRunner();
  protected HistoryKDJCountAndSaveDBRunner kdjRunner = new HistoryKDJCountAndSaveDBRunner();
  protected HistoryBollCountAndSaveDBRunner boolRunner = new HistoryBollCountAndSaveDBRunner();
  protected HistoryShenXianCountAndSaveDBRunner shenxianRunner =
      new HistoryShenXianCountAndSaveDBRunner();
  protected HistoryQSDDCountAndSaveDBRunner qsddRunner = new HistoryQSDDCountAndSaveDBRunner();
  protected HistoryMACountAndSaveDBRunner maRunner = new HistoryMACountAndSaveDBRunner();
  protected HistoryWRCountAndSaveDBRunner wrRunner = new HistoryWRCountAndSaveDBRunner();

  protected HistoryWeeklyMacdCountAndSaveDBRunner weekMacdRunner =
      new HistoryWeeklyMacdCountAndSaveDBRunner();
  protected HistoryWeeklyKDJCountAndSaveDBRunner weekKdjRunner =
      new HistoryWeeklyKDJCountAndSaveDBRunner();
  protected AtomicInteger counter = new AtomicInteger();

  public void sanityDailyCheck(List<String> stockIds) {
    logger.debug("sanityDailyCheck start.");
    this.counter.set(0);
    stockIds.parallelStream().forEach(stockId -> {
      this.sanityDailyCheck(stockId);
      int current = this.counter.incrementAndGet();
      if (current %50 == 0) {
        logger.debug("sanityDailyCheck complete:" + current + "/" + stockIds.size());
      }
    });

    // int index = 0;
    // for (String stockId : stockIds) {
    // if (index++ % 100 == 0) {
    // System.out.println("Processing " + index + "/" + stockIds.size());
    // }
    // this.sanityDailyCheck(stockId);
    // }
    logger.debug("sanityDailyCheck completed.");
  }

  public void sanityDailyCheck(String stockId) {

    List<StockPriceVO> spList = stockPriceTable.getStockPriceById(stockId);
    int qianfuquan_spList = qianfuquanStockPriceTable.getCount(stockId);
    int macdList = macdTable.getCount(stockId);
    int kdjList = kdjTable.getCount(stockId);
    int bollList = bollTable.getCount(stockId);
    int shenXianList = shenXianTable.getCount(stockId);
    int qsddList = qsddTable.getCount(stockId);
    int maList = maTable.getCount(stockId);
    int wrList = wrTable.getCount(stockId);

    for (StockPriceVO vo : spList) {
      if (vo.close == 0 || vo.open == 0 || vo.high == 0 || vo.low == 0
          || Strings.isEmpty(vo.date)) {
        logger.error("Error Sanity Delete StockPrice " + vo);
        this.stockPriceTable.delete(vo.stockId, vo.date);
      }
    }

    if (spList.size() != qianfuquan_spList) {
      logger.debug(stockId + " StockPrice Length is not equal to Qian FuQuan StockPrice");
      this.historyQianFuQuanRunner.countAndSave(stockId);
    }

    if ((spList.size() != macdList)) {
      logger.debug(
          stockId + " size of macd is not equal:" + spList.size() + "!=" + macdList);

      // figureOutDifferenceDate(spList, macdList);

      //macdTable.delete(stockId);
      macdRunner.countAndSaved(stockId);
    }
    if ((spList.size() != kdjList)) {
      logger.debug(stockId + " size of kdj is not equal:" + spList.size() + "!=" + kdjList);
      //kdjTable.delete(stockId);
      kdjRunner.countAndSaved(stockId);
    }
    if ((spList.size() != bollList)) {
      logger.debug(
          stockId + " size of boll is not equal:" + spList.size() + "!=" + bollList);
      //bollTable.delete(stockId);
      boolRunner.countAndSaved(stockId);
    }
    if ((spList.size() != shenXianList)) {
      logger.debug(
          stockId + " size of shenXian is not equal:" + spList.size() + "!=" + shenXianList);
      //shenXianTable.delete(stockId);
      shenxianRunner.countAndSaved(stockId);
    }
    if ((spList.size() != qsddList)) {
      logger.debug(
          stockId + " size of QSDD is not equal:" + spList.size() + "!=" + qsddList);
      //qsddTable.delete(stockId);
      qsddRunner.countAndSaved(stockId);
    }
    if ((spList.size() != maList)) {
      logger.debug(stockId + " size of MA is not equal:" + spList.size() + "!=" + maList);
      //maTable.delete(stockId);
      maRunner.countAndSaved(stockId);
    }
    if ((spList.size() != wrList)) {
      logger.debug(stockId + " size of WR is not equal:" + spList.size() + "!=" + wrList);
      //wrTable.delete(stockId);
      wrRunner.countAndSaved(stockId);
    }

  }

  public void sanityWeekCheck(List<String> stockIds) {
    logger.debug("sanityWeekCheck completed.");
    this.counter.set(0);
    stockIds.parallelStream().forEach(stockId -> {
      this.sanityWeekCheck(stockId);
      int current = this.counter.incrementAndGet();
      if (current %50 == 0) {
        logger.debug("sanityWeekCheck complete:" + current + "/" + stockIds.size());
      }
    });

    // int index = 0;
    // for (String stockId : stockIds) {
    // if (index++ % 100 == 0) {
    // System.out.println("Processing week " + index + "/" + stockIds.size());
    // }
    // this.sanityWeekCheck(stockId);
    // }
    logger.debug("sanityWeekCheck completed.");
  }

  public void sanityWeekCheck(String stockId) {
    List<StockPriceVO> spList = weekStockPriceTable.getStockPriceById(stockId);
    int macdList = weekMacdTable.getCount(stockId);
    int kdjList = weekKdjTable.getCount(stockId);

    boolean refresh = false;
    for (StockPriceVO vo : spList) {
      if (vo.close == 0 || vo.open == 0 || vo.high == 0 || vo.low == 0
          || Strings.isEmpty(vo.date)) {
        logger.debug("Sanity Delete WeekStockPrice " + vo);
        this.weekStockPriceTable.delete(vo.stockId, vo.date);
        refresh = true;
      }
    }
    // refresh if above delete vo
    if (refresh)
      spList = weekStockPriceTable.getStockPriceById(stockId);

    if ((spList.size() != macdList)) {
      logger.debug(
          stockId + " size of week macd is not equal:" + spList.size() + "!=" + macdList);

      figureOutDifferenceDate(spList, weekMacdTable.getAll(stockId));

      //weekMacdTable.delete(stockId);
      weekMacdRunner.countAndSaved(stockId);
    }
    if ((spList.size() != kdjList)) {
      logger.debug(
          stockId + " size of week kdj is not equal:" + spList.size() + "!=" + kdjList);
      //weekKdjTable.delete(stockId);
      weekKdjRunner.countAndSaved(stockId);
    }

  }

  public void sanityDailyStatisticsCheck(List<String> stockIds) {
    logger.debug("sanityDailyStatisticsCheck start. Only run for date >= 2022-01-01");
    List<String> dates = stockPriceTable.getAllDealDate("999999");
    for (String date : dates) {
      // do not care the count date before 2000 year
      if (date.compareTo("2022-01-01") >= 0) {
        int rtn = checkPointDailyStatisticsTable.countByDate(date);
        if (rtn == 0) {
          logger.debug("Daily Statistics is all zero for date " + date + ", try to re-count it.");
          DailySelectionRunner dailySelectionRunner = new DailySelectionRunner();
          dailySelectionRunner.runForDate(date, stockIds);
        }
      }
    }
    logger.debug("sanityDailyStatisticsCheck completed.");
  }

  public void sanityHistoryStatisticsCheck(List<String> stockIds, String startDate, String endDate) {
    logger.debug("sanityHistoryStatisticsCheck start. startDate=" + startDate + ", endDate="+endDate);
    List<String> dates = stockPriceTable.getAllDealDate("999999");
    for (String date : dates) {
      // count history date between
      if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
        int rtn = checkPointDailyStatisticsTable.countByDate(date);
        if (rtn == 0) {
          logger.debug("Daily Statistics is all zero for date " + date + ", try to re-count it.");
          DailySelectionRunner dailySelectionRunner = new DailySelectionRunner();
          dailySelectionRunner.runForDate(date, stockIds);
        }
      }
    }
    logger.debug("sanityDailyStatisticsCheck completed.");
  }

  public void figureOutDifferenceDate(List<StockPriceVO> spList, List<MacdVO> macdList) {
    int minLen = Math.min(spList.size(), macdList.size());
    int index = 0;
    for (; index < minLen; index++) {
      StockPriceVO spvo = spList.get(index);
      MacdVO macdvo = macdList.get(index);
      if (!spvo.date.equals(macdvo.date)) {
        logger.debug("spList date != macdList @" + spvo.date);
      }
    }
    if (index == spList.size()) {
      logger.debug("spList has, but macdList do not have @" + macdList.get(index).date);
    }

    if (index == macdList.size()) {
      logger.debug("macdList has, but spList do not have @" + spList.get(index).date);
    }
  }

  public void run() {
    // TODO Auto-generated method stub
    CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();

    List<String> stockIds = stockConfig.getAllStockId();
    
    sanityDailyCheck(stockIds);
    sanityWeekCheck(stockIds);
    sanityDailyStatisticsCheck(stockIds);
  }

  public void runForIndicatorOnly() {
    // TODO Auto-generated method stub
    CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();

    List<String> stockIds = stockConfig.getAllStockId();

    sanityDailyCheck(stockIds);
    sanityWeekCheck(stockIds);
  }
  
  public void runForHistoryStatisticsCheck() {
    // TODO Auto-generated method stub
    CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
    DataBaseSanityCheck check = new DataBaseSanityCheck();
    check.sanityDailyStatisticsCheck(stockConfig.getAllStockId());
  }
  
  public void runForHistoryStatisticsCheck(String startDate, String endDate) {
    // TODO Auto-generated method stub
    CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
    DataBaseSanityCheck check = new DataBaseSanityCheck();
    check.sanityHistoryStatisticsCheck(stockConfig.getAllStockId(), startDate, endDate);
  }

  public static void main(String[] args) {
    new DataBaseSanityCheck().run();
    new AllCacheRunner().refreshAll();
    logger.debug("DataBaseSanityCheck done!");
  }
}
