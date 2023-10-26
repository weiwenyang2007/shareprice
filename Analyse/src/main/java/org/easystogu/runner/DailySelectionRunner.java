package org.easystogu.runner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.easystogu.analyse.CombineAnalyseHelper;
import org.easystogu.analyse.util.IndProcessHelper;
import org.easystogu.analyse.util.LocalStaticCache;
import org.easystogu.checkpoint.DailyCombineCheckPoint;
import org.easystogu.config.ConfigurationService;
import org.easystogu.config.DBConfigurationService;
import org.easystogu.db.access.table.CheckPointDailySelectionTableHelper;
import org.easystogu.db.access.table.CheckPointDailyStatisticsTableHelper;
import org.easystogu.db.access.table.FavoritesStockHelper;
import org.easystogu.db.access.table.ScheduleActionTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.access.table.StockSuperVOHelper;
import org.easystogu.db.access.table.WeekStockSuperVOHelper;
import org.easystogu.db.access.table.ZhuLiJingLiuRuTableHelper;
import org.easystogu.db.access.table.ZiJinLiu3DayTableHelper;
import org.easystogu.db.access.table.ZiJinLiu5DayTableHelper;
import org.easystogu.db.access.table.ZiJinLiuTableHelper;
import org.easystogu.db.vo.table.CheckPointDailySelectionVO;
import org.easystogu.db.vo.table.CheckPointDailyStatisticsVO;
import org.easystogu.db.vo.table.ScheduleActionVO;
import org.easystogu.db.vo.table.StockSuperVO;
import org.easystogu.db.vo.table.ZiJinLiuVO;
import org.easystogu.easymoney.helper.RealTimeZiJinLiuFatchDataHelper;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.report.HistoryAnalyseReport;
import org.easystogu.report.HistoryReportDetailsVO;
import org.easystogu.report.RangeHistoryReportVO;
import org.easystogu.report.ReportTemplate;
import org.easystogu.report.comparator.ZiJinLiuComparator;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.slf4j.Logger;

// daily select stock that checkpoint is satisfied
public class DailySelectionRunner implements Runnable {
  private static Logger logger = LogHelper.getLogger(DailySelectionRunner.class);
  private ConfigurationService config = DBConfigurationService.getInstance();
  private StockSuperVOHelper stockOverAllHelper = new StockSuperVOHelper();
  private WeekStockSuperVOHelper weekStockOverAllHelper = new WeekStockSuperVOHelper();
  private CheckPointDailySelectionTableHelper checkPointDailySelectionTable =
      CheckPointDailySelectionTableHelper.getInstance();
  private CheckPointDailyStatisticsTableHelper checkPointDailyStatisticsTable =
      CheckPointDailyStatisticsTableHelper.getInstance();
  private RealTimeZiJinLiuFatchDataHelper realTimeZiJinLiuHelper =
      RealTimeZiJinLiuFatchDataHelper.getInstance();
  private ZiJinLiuTableHelper ziJinLiuTableHelper = ZiJinLiuTableHelper.getInstance();
  private ZhuLiJingLiuRuTableHelper zhuLiJingLiuRuTableHelper =
      ZhuLiJingLiuRuTableHelper.getInstance();
  private ZiJinLiu3DayTableHelper ziJinLiu3DayTableHelper = ZiJinLiu3DayTableHelper.getInstance();
  private ZiJinLiu5DayTableHelper ziJinLiu5DayTableHelper = ZiJinLiu5DayTableHelper.getInstance();
  private ScheduleActionTableHelper scheduleActionTableHelper =
      ScheduleActionTableHelper.getInstance();
  private HistoryAnalyseReport historyReportHelper = new HistoryAnalyseReport();
  private CombineAnalyseHelper combineAnalyserHelper = new CombineAnalyseHelper();
  private boolean doHistoryAnalyzeInDailySelection =
      config.getBoolean("do_History_Analyze_In_Daily_Selection", true);
  private String[] specifySelectCheckPoints =
      config.getString("specify_Select_CheckPoint", "").split(";");
  private String[] specifyDependCheckPoints =
      config.getString("specify_Depend_CheckPoint", "").split(";");
  private String[] generalCheckPoints = config.getString("general_CheckPoint", "").split(";");
  private StringBuffer recommandStr = new StringBuffer();
  // StockPriceVO, CheckPoint list
  private Map<StockSuperVO, List<DailyCombineCheckPoint>> selectedMaps =
      new java.util.concurrent.ConcurrentHashMap<StockSuperVO, List<DailyCombineCheckPoint>>();
  private Map<String, ZiJinLiuVO> realTimeZiJinLiuMap = new java.util.concurrent.ConcurrentHashMap<String, ZiJinLiuVO>();
  private Map<DailyCombineCheckPoint, List<String>> generalCheckPointGordonMap =
      new java.util.concurrent.ConcurrentHashMap<DailyCombineCheckPoint, List<String>>();
  private boolean fetchRealTimeZiJinLiu = false;

  // below are for daily and history
  protected FavoritesStockHelper favoritesStockHelper = FavoritesStockHelper.getInstance();
  protected CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
  protected StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
  protected String latestDate = stockPriceTable.getLatestStockDate();
  protected boolean addToScheduleActionTable = true;
  protected boolean checkDayPriceEqualWeekPrice = true;
  protected AtomicInteger counter = new AtomicInteger();

  private void doAnalyse(String stockId) {
    try {
      List<StockSuperVO> overDayList = stockOverAllHelper.getAllStockSuperVO(stockId);
      List<StockSuperVO> overWeekList = weekStockOverAllHelper.getAllStockSuperVO(stockId);

      if (addToScheduleActionTable && overDayList.size() == 0) {
        logger.debug("No stockprice data for " + stockId + ", add to Schedule Action.");
        // next action should be fetch all the data from web, it must be
        // a new board id
        ScheduleActionVO vo = new ScheduleActionVO();
        vo.setActionDo(ScheduleActionVO.ActionDo.refresh_history_stockprice.name());
        vo.setStockId(stockId);
        vo.setCreateDate(this.latestDate);
        vo.setRunDate(WeekdayUtil.nextNDateString(latestDate, 20));
        this.scheduleActionTableHelper.deleteIfExistAndThenInsert(vo);
        return;
      }

      if (addToScheduleActionTable && overWeekList.size() == 0) {
        logger.debug("No stockprice data for " + stockId + ", add to Schedule Action.");
        // next action should be fetch all the data from web, it must be
        // a new board id
        ScheduleActionVO vo = new ScheduleActionVO();
        vo.setActionDo(ScheduleActionVO.ActionDo.refresh_history_stockprice.name());
        vo.setStockId(stockId);
        vo.setCreateDate(this.latestDate);
        vo.setRunDate(WeekdayUtil.nextNDateString(latestDate, 20));
        this.scheduleActionTableHelper.deleteIfExistAndThenInsert(vo);
        return;
      }
      
      List<StockSuperVO> overDayListTmp = null;
      List<StockSuperVO> overWeekListTmp = null;

      int dayListLen = this.getDateIndex(this.latestDate, overDayList) + 1;
      if (dayListLen >= 120)
        overDayListTmp = overDayList.subList(dayListLen - 120, dayListLen);

      int weekListLen = overWeekList.size();
      if (weekListLen >= 24) {
        //bug: the weekList is not exactly equal to the latestDate in history runner
        //any analyse using weekList in isConditionSatisfy is wrong here
        overWeekListTmp = overWeekList.subList(weekListLen - 24, weekListLen);
      }
      
      if(overDayListTmp == null || overWeekListTmp == null) {
        return;
      }
      if(overWeekListTmp.size() == 0 || overDayListTmp.size() == 0) {
        return;
      }

      // so must reverse in date order
      // Collections.reverse(overDayList);
      // Collections.reverse(overWeekList);

      IndProcessHelper.processDayList(overDayListTmp);
      IndProcessHelper.processWeekList(overWeekListTmp);

      StockSuperVO superVO = overDayListTmp.get(overDayListTmp.size() - 1);
      StockSuperVO weekSuperVO = overWeekListTmp.get(overWeekListTmp.size() - 1);

      if (checkDayPriceEqualWeekPrice && !superVO.priceVO.date.equals(weekSuperVO.priceVO.date)) {
        logger.debug(stockId + " DayPrice VO date (" + superVO.priceVO.date
            + ") is not equal WeekPrice VO date (" + weekSuperVO.priceVO.date + ")");
        return;
      }

      // exclude ting pai
      if (!superVO.priceVO.date.equals(latestDate)) {
        logger.debug(stockId + " priveVO date (" + superVO.priceVO.date
            + " ) is not equal latestDate (" + latestDate + ")");
        return;
      }

      // check all combine check point
      for (DailyCombineCheckPoint checkPoint : DailyCombineCheckPoint.values()) {
        // System.out.println("checkpoint=" + checkPoint);
        if (this.isSelectedCheckPoint(checkPoint)) {
          if (combineAnalyserHelper.isConditionSatisfy(checkPoint, overDayListTmp, overWeekListTmp)) {
            this.setZiJinLiuVO(superVO);
            this.saveToCheckPointSelectionDB(superVO, checkPoint);
            //this.addToConditionMapForReportDisplay(superVO, checkPoint);
          }
        } else if (this.isDependCheckPoint(checkPoint)) {
          if (combineAnalyserHelper.isConditionSatisfy(checkPoint, overDayListTmp, overWeekListTmp)) {
            this.setZiJinLiuVO(superVO);
            // search if other checkpoint already happen in recent
            // days
            CheckPointDailySelectionVO latestCheckPointSelection = checkPointDailySelectionTable
                .getDifferentLatestCheckPointSelection(stockId, checkPoint.toString());
            if (latestCheckPointSelection != null
                && !latestCheckPointSelection.checkPoint.equals(checkPoint.toString())
                && !latestCheckPointSelection.date.equals(superVO.priceVO.date)) {
              // check if day is between 10 days
              String lastNDate = stockPriceTable.getLastNDate(stockId, 10);
              if (latestCheckPointSelection.date.compareTo(lastNDate) >= 0) {
                this.saveToCheckPointSelectionDB(superVO, checkPoint);
                //this.addToConditionMapForReportDisplay(superVO, checkPoint);
              }
            }
          }
        }
        if (this.isGeneralCheckPoint(checkPoint)) {
          if (combineAnalyserHelper.isConditionSatisfy(checkPoint, overDayListTmp, overWeekListTmp)) {
            // only save QSDD Bottom to daily selection table and
            // display
            if (checkPoint.compareTo(DailyCombineCheckPoint.AiTrend_Bottom_Area) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.AiTrend_Bottom_Gordon) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.AiTrend_Top_Area) == 0//add to DB? more top area
                || checkPoint.compareTo(DailyCombineCheckPoint.QSDD_Bottom_Area) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.QSDD_Bottom_Gordon) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.QSDD_Top_Area) == 0//add to DB? more top area
                || checkPoint.compareTo(DailyCombineCheckPoint.WR_Bottom_Area) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.WR_Bottom_Gordon) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.WR_Top_Area) == 0//add to DB? more top area
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_GordonO_MA43_DownCross_MA86) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_GordonI_MA19_UpCross_MA43) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_GordonII_MA19_UpCross_MA86) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_DeadI_MA43_UpCross_MA86) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_DeadII_MA19_DownCross_MA43) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.LuZao_DeadIII_MA43_DownCross_MA86) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.ShenXian_Gordon) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.ShenXian_Dead) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.MACD_Gordon) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.MACD_Dead) == 0            
                || checkPoint.compareTo(DailyCombineCheckPoint.WR_DI_BeiLi) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.MAGIC_NIGHT_DAYS_SHANG_ZHANG) == 0
                || checkPoint.compareTo(DailyCombineCheckPoint.MAGIC_NIGHT_DAYS_XIA_DIE) == 0) {
              this.saveToCheckPointSelectionDB(superVO, checkPoint);
              //this.addToConditionMapForReportDisplay(superVO, checkPoint);
            }
            this.addToGeneralCheckPointGordonMap(checkPoint, stockId);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Exception for " + stockId);
      e.printStackTrace();
    }
  }

  private void setZiJinLiuVO(StockSuperVO superVO) {

    boolean justReturn = true;
    if (justReturn) {
      return;
    }

    // if real time zijinliu is not collect, then find it from total range
    // zijinliu (59 pages)
    if (this.fetchRealTimeZiJinLiu) {
      String stockId = superVO.priceVO.stockId;
      ZiJinLiuVO realTimeVO = null;
      if (!this.realTimeZiJinLiuMap.containsKey(stockId)) {
        realTimeVO = realTimeZiJinLiuHelper.fetchDataFromWeb(stockId);
        this.realTimeZiJinLiuMap.put(stockId, realTimeVO);
      } else {
        realTimeVO = this.realTimeZiJinLiuMap.get(stockId);
      }

      // put ziJinLiu VO to list
      superVO.putZiJinLiuVO(ZiJinLiuVO.RealTime, realTimeVO);
    }

    // also get zijinliu from DB if exist
    superVO.putZiJinLiuVO(ZiJinLiuVO._1Day,
        ziJinLiuTableHelper.getZiJinLiu(superVO.priceVO.stockId, latestDate));
    superVO.putZiJinLiuVO(ZiJinLiuVO._3Day,
        ziJinLiu3DayTableHelper.getZiJinLiu(superVO.priceVO.stockId, latestDate));
    superVO.putZiJinLiuVO(ZiJinLiuVO._5Day,
        ziJinLiu5DayTableHelper.getZiJinLiu(superVO.priceVO.stockId, latestDate));

    // get zhuLiJingLiuRu from DB
    superVO.setZhuLiJingLiuRuVO(
        zhuLiJingLiuRuTableHelper.getZhuLiJingLiuRu(superVO.priceVO.stockId, latestDate));
  }

  private void saveToCheckPointSelectionDB(StockSuperVO superVO,
      DailyCombineCheckPoint checkPoint) {
    CheckPointDailySelectionVO vo = new CheckPointDailySelectionVO();
    vo.setStockId(superVO.priceVO.stockId);
    vo.setDate(superVO.priceVO.date);
    vo.setCheckPoint(checkPoint.toString());
    this.checkPointDailySelectionTable.insertIfNotExist(vo);
  }

  private void addToConditionMapForReportDisplay(StockSuperVO superVO,
      DailyCombineCheckPoint checkPoint) {
    List<DailyCombineCheckPoint> checkPointList = selectedMaps.get(superVO);
    if (checkPointList == null) {
      checkPointList = new ArrayList<DailyCombineCheckPoint>();
      checkPointList.add(checkPoint);
      selectedMaps.put(superVO, checkPointList);
    } else {
      checkPointList.add(checkPoint);
    }
  }

  private void addToGeneralCheckPointGordonMap(DailyCombineCheckPoint checkPoint, String stockId) {
    List<String> stockIds = this.generalCheckPointGordonMap.get(checkPoint);
    if (stockIds == null) {
      stockIds = new ArrayList<String>();
      this.generalCheckPointGordonMap.put(checkPoint, stockIds);
    }
    stockIds.add(stockId);
  }

  private boolean isSelectedCheckPoint(DailyCombineCheckPoint checkPoint) {
    if (specifySelectCheckPoints != null && specifySelectCheckPoints.length > 0) {
      for (String cp : specifySelectCheckPoints) {
        if (cp.equals(checkPoint.toString())) {
          return true;
        }
      }
    } else if (checkPoint.isSatisfyMinEarnPercent()) {
      return true;
    }
    return false;
  }

  private boolean isDependCheckPoint(DailyCombineCheckPoint checkPoint) {
    for (String cp : specifyDependCheckPoints) {
      if (cp.equals(checkPoint.toString())) {
        return true;
      }
    }
    return false;
  }

  private boolean isGeneralCheckPoint(DailyCombineCheckPoint checkPoint) {
    // System.out.println("g:" + config.getString("general_CheckPoint"));
    for (String cp : generalCheckPoints) {
      if (cp.equals(checkPoint.toString())) {
        // System.out.println(checkPoint + " is meet");
        return true;
      }
    }
    return false;
  }

  private void addGeneralCheckPointStatisticsResultToDB() {
    logger.debug("==================General CheckPoint Statistics=====================");
    Set<DailyCombineCheckPoint> keys = this.generalCheckPointGordonMap.keySet();
    Iterator<DailyCombineCheckPoint> keysIt = keys.iterator();
    while (keysIt.hasNext()) {
      DailyCombineCheckPoint checkPoint = keysIt.next();
      List<String> stockIds = this.generalCheckPointGordonMap.get(checkPoint);

      CheckPointDailyStatisticsVO cpdsvo = new CheckPointDailyStatisticsVO();
      cpdsvo.date = latestDate;
      cpdsvo.checkPoint = checkPoint.name();
      cpdsvo.count = stockIds.size();
      //count the stock company has deal at that date
      int totalCompanyDeal = this.stockPriceTable.countByDate(cpdsvo.date);
      cpdsvo.rate = Strings.convert2ScaleDecimal(cpdsvo.count * 1.0 / totalCompanyDeal, 4);
      
      // update
      String previousDate = this.stockPriceTable.getPreviousStockDate(latestDate);
      CheckPointDailyStatisticsVO lastVO =
          checkPointDailyStatisticsTable.getByCheckPointAndDate(previousDate, cpdsvo.checkPoint);
      checkPointDailyStatisticsTable.delete(cpdsvo.date, cpdsvo.checkPoint);
      checkPointDailyStatisticsTable.insert(cpdsvo);

      String diff = "N/A";
      if (lastVO != null) {
        diff = Integer.toString(cpdsvo.count - lastVO.count);
      }
      logger.debug(cpdsvo.checkPoint + " = " + cpdsvo.count + " (Diff: " + diff + ")");
    }
  }

  // get the index which is equal date, if date == LatestDate, it should
  // return size - 1, overList is ORDER BY DATE
  private int getDateIndex(String date, List<StockSuperVO> overList) {

    for (int index = 0; index < overList.size(); index++) {
      StockSuperVO vo = overList.get(index);
      if (vo.priceVO.date.equals(date)) {
        return index;
      }
    }
    return overList.size() - 1;
  }

  public void setFetchRealTimeZiJinLiu(boolean fetchRealTimeZiJinLiu) {
    this.fetchRealTimeZiJinLiu = fetchRealTimeZiJinLiu;
  }

  public void runForStockIds(List<String> stockIds) {
    latestDate = stockPriceTable.getLatestStockDate();
    logger.debug("DailySelection runForStockIds start for date " + this.latestDate);
    this.counter.set(0);
    stockIds.parallelStream().forEach(stockId -> {
      this.doAnalyse(stockId);
      int current = this.counter.incrementAndGet();
      if (current %50 == 0) {
        logger.debug("doAnalyse complete:" + current + "/" + stockIds.size());
      }
    });

    addGeneralCheckPointStatisticsResultToDB();
    
    logger.debug("DailySelection runForStockIds stop for date " + this.latestDate);
  }

  public void run() {
    List<String> stockIds = stockConfig.getAllStockId();
    this.runForStockIds(stockIds);
  }

  //run for CHECKPOINT_DAILY_STATISTICS when all zero statistics for date
  public void runForDate(String date, List<String> stockIds) {
    this.latestDate = date;
    this.addToScheduleActionTable = false;
    this.checkDayPriceEqualWeekPrice = false;
    this.runForStockIds(stockIds);
  }
}
