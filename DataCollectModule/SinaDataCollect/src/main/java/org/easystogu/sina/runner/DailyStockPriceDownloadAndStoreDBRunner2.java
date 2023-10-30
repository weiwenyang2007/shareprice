package org.easystogu.sina.runner;

import java.util.List;

import org.easystogu.cache.ConfigurationServiceCache;
import org.easystogu.db.access.table.CompanyInfoTableHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.CompanyInfoVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.sina.common.SinaQuoteStockPriceVO;
import org.easystogu.sina.helper.DailyStockPriceDownloadHelper2;
import org.easystogu.sina.runner.history.HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner;
import org.easystogu.sina.runner.history.HistoryStockPriceDownloadAndStoreDBRunner;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.slf4j.Logger;

//daily get real time stock price from http://vip.stock.finance.sina.com.cn/quotes_service/api/
//it will get all the stockId from the web, including the new on board stockId
public class DailyStockPriceDownloadAndStoreDBRunner2 implements Runnable {

    private static Logger logger = LogHelper.getLogger(DailyStockPriceDownloadAndStoreDBRunner2.class);
    private CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
    private StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
    private ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
    //private HouFuQuanStockPriceTableHelper houfuquanStockPriceTable = HouFuQuanStockPriceTableHelper.getInstance();
    private QianFuQuanStockPriceTableHelper qianfuquanStockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
    //private ScheduleActionTableHelper scheduleActionTable = ScheduleActionTableHelper.getInstance();
    private CompanyInfoTableHelper companyInfoTable = CompanyInfoTableHelper.getInstance();
    //private DailyStockPriceDownloadAndStoreDBRunner runner1 = new DailyStockPriceDownloadAndStoreDBRunner();
    private DailyStockPriceDownloadHelper2 sinaHelper2 = new DailyStockPriceDownloadHelper2();
    private HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner historyQianFuQuanRunner = new HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner();
    private String latestDate = "";

    // first download szzs, szcz, cybz,
    // must record the latest date time
    public void downloadMainBoardIndicator() {
        //List<String> stockIds = new ArrayList<String>();

        //stockIds.add(stockConfig.getSZZSStockIdForSina());
        //stockIds.add(stockConfig.getSZCZStockIdForSina());
        //stockIds.add(stockConfig.getCYBZStockIdForSina());

        //runner1.downloadDataAndSaveIntoDB(stockIds);
        HistoryStockPriceDownloadAndStoreDBRunner.getCurrentDayMajorIndicatorStockPrice();
        // important: this json do not contain date information,
        // just time is not enough, so we must get it form hq.sinajs.cn
        // then query the database and get the latest deal date
        this.latestDate = stockPriceTable.getLatestStockDate();
    }

    //download specific stockIds realTime price, the url is save into WSFCONFIG
    public void downloadTradeTodayRealTimePriceAndSave2DB(String pages){
        String today = WeekdayUtil.currentDate();
            String[] str = pages.split(",");
            for(int i=0; i<str.length; i++){
                downloadDataAndSaveIntoDB(today, Integer.parseInt(str[i]));
        }
    }

    //specify the latestDate and pageNumber to get the specify stockId's realtime price from sina (result contains many stockId's price, should filter them by code)
    //for example: https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=7&num=100&sort=symbol&asc=1&node=hs_a

    //another api to get realtime stock price is :
    //https://vip.stock.finance.sina.com.cn/quotes_service/view/vML_DataList.php?asc=j&symbol=sh600547&num=5
    public void downloadDataAndSaveIntoDB(String today, int page) {
        logger.debug("Get stock price for today=" + today + ", page=" + page);

        List<SinaQuoteStockPriceVO> sqsList = sinaHelper2.fetchAPageDataFromWeb(page);
        for (SinaQuoteStockPriceVO sqvo : sqsList) {
            // to check if the stockId is a new on board one, if so, insert to
            // companyInfo table
            CompanyInfoVO companyInfo = companyInfoTable.getCompanyInfoByStockId(sqvo.code);
            if (companyInfo == null) {
                CompanyInfoVO cinvo = new CompanyInfoVO(sqvo.code, sqvo.name);
                companyInfoTable.insert(cinvo);
                logger.debug("New company on board " + sqvo.code + " " + sqvo.name);
            } else if (Strings.isNotEmpty(companyInfo.name) && !companyInfo.name.equals(sqvo.name)) {
                //update the company name
                logger.debug("Company change name from " + companyInfo.name + " to " + sqvo.name);
                companyInfo.name = sqvo.name;
                companyInfoTable.updateName(companyInfo);
            }
            // convert to stockprice and save to DB
            this.saveIntoDBWithoutChuQuanEventChecking(sqvo, today);
        }
    }

    public void downloadDataAndSaveIntoDB() {

        if (Strings.isEmpty(this.latestDate)) {
            logger.error("Fatel Error, the latestDate is null! Return.");
            return;
        }

        logger.debug("Get stock price for latestDate=" + this.latestDate);

        List<SinaQuoteStockPriceVO> sqsList = sinaHelper2.fetchAllStockPriceFromWeb();
        for (SinaQuoteStockPriceVO sqvo : sqsList) {
            // to check if the stockId is a new on board one, if so, insert to
            // companyInfo table
            CompanyInfoVO companyInfo = companyInfoTable.getCompanyInfoByStockId(sqvo.code);
            if (companyInfo == null) {
                CompanyInfoVO cinvo = new CompanyInfoVO(sqvo.code, sqvo.name);
                companyInfoTable.insert(cinvo);
                logger.debug("New company on board " + sqvo.code + " " + sqvo.name);
            } else if (Strings.isNotEmpty(companyInfo.name) && !companyInfo.name.equals(sqvo.name)) {
                //update the company name
                logger.debug("Company change name from " + companyInfo.name + " to " + sqvo.name);
                companyInfo.name = sqvo.name;
                companyInfoTable.updateName(companyInfo);
            }
            // convert to stockprice and save to DB
            this.saveIntoDB(sqvo);
        }
    }

    private void saveIntoDB(SinaQuoteStockPriceVO sqvo) {
        try {
            // update stockprice into table
            StockPriceVO spvo = new StockPriceVO();
            spvo.stockId = sqvo.code;
            spvo.name = sqvo.name;
            // important: this json do not contain date information,
            // just time is not enough, so we must get it form hq.sinajs.cn
            spvo.date = this.latestDate;
            spvo.close = sqvo.trade;
            spvo.open = sqvo.open;
            spvo.low = sqvo.low;
            spvo.high = sqvo.high;
            // sina data is 100 larger then sohu history data
            spvo.volume = sqvo.volume / 100;
            spvo.lastClose = sqvo.trade - sqvo.pricechange;

            // delete if today old data is exist
            this.stockPriceTable.delete(spvo.stockId, spvo.date);
            List<StockPriceVO> nDaySpList = this.stockPriceTable.getNdateStockPriceById(spvo.stockId, 1);

            this.stockPriceTable.insert(spvo);
            // also insert the qian fuquan stockprice
            this.qianfuquanStockPriceTable.delete(spvo.stockId, spvo.date);
            this.qianfuquanStockPriceTable.insert(spvo);

            // check if chu quan event exist
            if (nDaySpList.size() > 0) {
                StockPriceVO prevo = nDaySpList.get(0);
                if (spvo.lastClose != 0 && prevo.close != 0 && spvo.lastClose != prevo.close) {
                    double rate = prevo.close / spvo.lastClose;
                    if (rate <= 0.95 || rate >= 1.05) {
                        // chu quan event
                        logger.debug("Chu Quan happens for " + spvo.stockId + ", rate=" + rate);
                        this.historyQianFuQuanRunner.countAndSave(spvo.stockId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveIntoDBWithoutChuQuanEventChecking(SinaQuoteStockPriceVO sqvo, String today) {
        try {
            // update stockprice into table
            StockPriceVO spvo = new StockPriceVO();
            spvo.stockId = sqvo.code;
            spvo.name = sqvo.name;
            // important: this json do not contain date information,
            // just time is not enough, so we must get it form hq.sinajs.cn
            spvo.date = today;
            spvo.close = sqvo.trade;
            spvo.open = sqvo.open;
            spvo.low = sqvo.low;
            spvo.high = sqvo.high;
            // sina data is 100 larger then sohu history data
            spvo.volume = sqvo.volume / 100;
            spvo.lastClose = sqvo.trade - sqvo.pricechange;

            // delete if today old data is exist
            this.stockPriceTable.delete(spvo.stockId, spvo.date);
            this.stockPriceTable.insert(spvo);
            // also insert the qian fuquan stockprice
            this.qianfuquanStockPriceTable.delete(spvo.stockId, spvo.date);
            this.qianfuquanStockPriceTable.insert(spvo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        downloadMainBoardIndicator();
        downloadDataAndSaveIntoDB();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DailyStockPriceDownloadAndStoreDBRunner2 runner = new DailyStockPriceDownloadAndStoreDBRunner2();
        runner.run();
    }
}
