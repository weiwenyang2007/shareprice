package org.easystogu.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easystogu.analyse.ShenXianSellAnalyseHelper;
import org.easystogu.analyse.vo.ShenXianUIVO;
import org.easystogu.cache.ConfigurationServiceCache;
import org.easystogu.cache.runner.AllCacheRunner;
import org.easystogu.checkpoint.DailyCombineCheckPoint;
import org.easystogu.config.Constants;
import org.easystogu.db.access.table.RealTimeStockPriceTableHelper;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.runner.AllDailyIndCountAndSaveDBRunner;
import org.easystogu.log.LogHelper;
import org.easystogu.report.HistoryAnalyseReport;
import org.easystogu.runner.DailyCandleStickPatternRunner;
import org.easystogu.runner.DailyOverAllRunner;
import org.easystogu.runner.DailySelectionRunner;
import org.easystogu.runner.DailyViewAnalyseRunner;
import org.easystogu.runner.DataBaseSanityCheck;
import org.easystogu.sina.runner.DailyStockPriceDownloadAndStoreDBRunner2;
import org.easystogu.sina.runner.DailyWeeklyStockPriceCountAndSaveDBRunner;
import org.easystogu.sina.runner.history.HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner;
import org.easystogu.sina.runner.history.HistoryStockPriceDownloadAndStoreDBRunner;
import org.easystogu.sina.runner.history.HistoryWeekStockPriceCountAndSaveDBRunner;
import org.easystogu.sina.runner.history.StockPriceHistoryOverAllRunner;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class DailyScheduler implements SchedulingConfigurer {
	private static Logger logger = LogHelper.getLogger(DailyScheduler.class);
	private ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
	private String zone = config.getString("zone", Constants.ZONE_HOME);
	private boolean dailyUpdateStockPriceByBatch = config.getBoolean(Constants.DailyUpdateStockPriceByBatch, false);
	private CompanyInfoFileHelper companyInfoHelper = CompanyInfoFileHelper.getInstance();
	private DataBaseSanityCheck sanityCheck = new DataBaseSanityCheck();
	private CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
	private ShenXianSellAnalyseHelper shenXianSellAnalyseHelper = ShenXianSellAnalyseHelper.getInstance();
	private DailyStockPriceDownloadAndStoreDBRunner2 dailyStockPriceDownloadAndStoreDBRunner2 = new DailyStockPriceDownloadAndStoreDBRunner2();
	private RealTimeStockPriceTableHelper realTimeStockPriceTableHelper = RealTimeStockPriceTableHelper.getInstance();

	@Autowired
	@Qualifier("taskScheduler")
	private TaskScheduler taskScheduler;

	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler);
	}

	// refer to:
	// http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger

	// every 2 mins from 9:25 to 9:40, Monday to Friday
	//@Scheduled(cron = "0 0/2 09 * * MON-FRI")
	public void updateStockPriceOnlyEvery2Mins() {
		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
			String time = WeekdayUtil.currentTime();
			if ((time.compareTo("09-25-00") >= 0 && time.compareTo("09-40-00") <= 0)) {
				logger.info("Start updateStockPriceOnlyEvery2Mins");
				long startTs = System.currentTimeMillis();
				updateRealtimeStockPriceForEasyTrader();
				logger.info("End updateStockPriceOnlyEvery2Mins, spent " + (System.currentTimeMillis() - startTs)/1000 + " seconds");
			}
		}
	}

	// every 1 mins from 9:40 to 15:00, Monday to Friday
	@Scheduled(cron = "0 0/1 09,10,11,13,14 * * MON-FRI")
	public void updateStockPriceOnlyEvery5Mins() {
		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
			String time = WeekdayUtil.currentTime();
			if ((time.compareTo("09-25-00") >= 0 && time.compareTo("11-32-00") <= 0)
					|| (time.compareTo("13-00-00") >= 0 && time.compareTo("15-02-00") <= 0)) {
				logger.info("Start updateStockPriceOnlyEvery5Mins");
				long startTs = System.currentTimeMillis();
				updateRealtimeStockPriceForEasyTrader();
				logger.info("End updateStockPriceOnlyEvery5Mins, spent " + (System.currentTimeMillis() - startTs)/1000 + " seconds");
			}
		}
	}

	public void updateRealtimeStockPriceForEasyTrader() {
		// day (download all stockIds price from sina realtime stock price)
		// download specific stockIds realTime price, the url is save into WSFCONFIG
		String today = WeekdayUtil.currentDate();
		String datetime = WeekdayUtil.currentDateTime();
		String pages = config.getString("realtime_stock_quota_service_page_number_list");//example: 7 or 7,10
		String stockIds = config.getString("easytrader_stock_list");//example: 600547 or 600547,300059
		List<String> stocks = Arrays.asList(stockIds.split(","));
		if(Strings.isNotEmpty(pages)) {
			String[] page = pages.split(",");
			for(int i=0; i<page.length; i++){
				dailyStockPriceDownloadAndStoreDBRunner2.downloadDataAndSaveIntoDB(today, datetime, Integer.parseInt(page[i]));
			}
		}
		// update indicators for part of the stockIds
		if(stocks != null && stocks.size() > 0) {
			// day ind
			new AllDailyIndCountAndSaveDBRunner().runDailyIndForStockIds(stocks);
			// week
			new DailyWeeklyStockPriceCountAndSaveDBRunner().countAndSave(stocks);
			// week ind
			new AllDailyIndCountAndSaveDBRunner().runDailyWeekIndForStockIds(stocks);
			// update cache
			AllCacheRunner cacheRunner = new AllCacheRunner();
			cacheRunner.refreshAll();
		}
		//update shenxian buy sell indicator
		stocks.forEach(stockId -> {
			//Buy
			String buyOrSell = "B";
			double shenxian_buy = 0.0;
			ShenXianUIVO rtnBuy = shenXianSellAnalyseHelper.mockCurPriceAndPredictTodayInd(stockId, today, buyOrSell);
			if (rtnBuy!=null && rtnBuy.sellFlagsTitle.contains(buyOrSell)){
				shenxian_buy = rtnBuy.hc6;
			}
			//Sell
			buyOrSell = "S";
			double shenxian_sell = 0.0;
			ShenXianUIVO rtnSell = shenXianSellAnalyseHelper.mockCurPriceAndPredictTodayInd(stockId, today, buyOrSell);
			if (rtnSell!=null && rtnSell.sellFlagsTitle.contains(buyOrSell)){
				shenxian_sell = rtnBuy.hc5;
			}
			//
			if(shenxian_buy > 0.0 && shenxian_sell > 0.0) {
				realTimeStockPriceTableHelper
						.updateShenxianBuySell(stockId, datetime, shenxian_buy, shenxian_sell);
			}
		});
	}

	// run at 15:10 DailyOverAllRunner
	// @Scheduled(cron = "0 10 15 * * MON-FRI")
	public void _1_DailyOverAllRunner() {
		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
			boolean isGetZiJinLiu = false;
			this.DailyOverAllRunner(isGetZiJinLiu);
		}
	}

	// run at 21:10 DailyOverAllRunner
	@Scheduled(cron = "0 10 21 * * MON-FRI")
	public void _3_DailyOverAllRunner() {
		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
		  Thread t = new Thread(new Runnable() {
	        public void run() {
	        	//This will use sohu history stock data, sohu is much late to update today's data, so do not run it earlier
						//-10 means that it will update the stock price from 10 days ago, and will make sure all the price are up to date
	          new StockPriceHistoryOverAllRunner(WeekdayUtil.nDayBeforeToday(-10), WeekdayUtil.currentDate()).run();
	          
	          List<String> allStockIds = stockConfig.getAllStockId();
	          // day ind
	          new AllDailyIndCountAndSaveDBRunner().runDailyIndForStockIds(allStockIds);
	          // week
	          new DailyWeeklyStockPriceCountAndSaveDBRunner().countAndSave(allStockIds);
	          // week ind
	          new AllDailyIndCountAndSaveDBRunner().runDailyWeekIndForStockIds(allStockIds);

	          // analyse by java code
	          DailySelectionRunner dailySelectionRunner = new DailySelectionRunner();
	          dailySelectionRunner.setFetchRealTimeZiJinLiu(false);
	          dailySelectionRunner.runForStockIds(allStockIds);

	          // alaylse by view names
	          new DailyViewAnalyseRunner().run();
	          // candle pattern
						new DailyCandleStickPatternRunner().run();
	          //
	          new AllCacheRunner().refreshAll();
			  		//
			  		ShenXianSellAnalyseHelper.main(null);
	        }
	      });
	      t.start();
		}
	}

	// run at 23:00
	@Scheduled(cron = "0 0 23 * * MON-FRI")
	public void _0_DataBaseSanityCheck() {
		// only run at office, since at aliyun, there is daily santy after price
		// update
		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
			logger.info("DataBaseSanityCheck already running.");
			Thread t = new Thread(new DataBaseSanityCheck());
			t.start();
		}
	}

	private void DailyOverAllRunner(boolean isGetZiJinLiu) {
		logger.info("DailyOverAllRunner already running, please check DB result.");
		//This will use sina realtime stock data
		DailyOverAllRunner runner = new DailyOverAllRunner(isGetZiJinLiu);
		Thread t = new Thread(runner);
		t.start();
	}

	// sometime the stockprice has problem and will miss data or chuquan is not
	// 每周更新一下stockprice，每次选择一部分
	// please do not change the SAT-SUN, will impact the selected stockId
	// 请不要随意更改这个时间，跟选出的stockid算法有关。
	// run at 02:00 every day
	//@Scheduled(cron = "0 00 02 * * ?")
	private void DailyUpdateStockPriceByBatch() {
		if (!dailyUpdateStockPriceByBatch) {
			logger.info("dailyUpdateStockPriceByBatch is false, not run.");
			return;
		}

		if (Constants.ZONE_HOME.equalsIgnoreCase(zone)) {
			logger.info("DailyUpdateStockPriceByBatch already running, please check DB result.");
			List<String> allStockIds = companyInfoHelper.getAllStockId();
			List<String> stockIds = new ArrayList<String>();
			// dayN is 1 ~ 31
			int dayN = Integer.parseInt(WeekdayUtil.currentDay());

			// each day will fetch about 1/30 of all stockIds
			int[] lastTwoDigs = { ((dayN - 1) * 3), ((dayN - 1) * 3 + 1), ((dayN - 1) * 3 + 2) };
			String[] strLastTwoDigs = { "" + lastTwoDigs[0], "" + lastTwoDigs[1], "" + lastTwoDigs[2] };

			// convert '1' to '01', '2' to '02' etc
			for (int i = 0; i < lastTwoDigs.length; i++) {
				if (lastTwoDigs[i] <= 9) {
					strLastTwoDigs[i] = "0" + lastTwoDigs[i];
				}
			}

			for (String stockId : allStockIds) {
				String lastTwoDig = stockId.substring(4);
				if (lastTwoDig.equals(strLastTwoDigs[0]) || lastTwoDig.equals(strLastTwoDigs[0])
						|| lastTwoDig.equals(strLastTwoDigs[2])) {
					stockIds.add(stockId);
				}

				// hardocde to add missing stockIds
				if (dayN == 1 && (lastTwoDig.equals("93") || lastTwoDig.equals("94") || lastTwoDig.equals("95")
						|| lastTwoDig.equals("96") || lastTwoDig.equals("97") || lastTwoDig.equals("98")
						|| lastTwoDig.equals("99"))) {
					stockIds.add(stockId);
				}
			}

			logger.info("DailyUpdateStockPriceByBatch select stockId with last dig: " + strLastTwoDigs[0] + ", "
					+ strLastTwoDigs[1] + ", " + strLastTwoDigs[2] + ", size: " + stockIds.size());
			new HistoryStockPriceDownloadAndStoreDBRunner().countAndSave(stockIds);
			new HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner().countAndSave(stockIds);
			new HistoryWeekStockPriceCountAndSaveDBRunner().countAndSave(stockIds);

			// after update price, do the sanity test
			new DataBaseSanityCheck().run();
		}
	}

	// just run onece
	//@Scheduled(cron = "0 00 21 * * ?")
	public void JustRunOnce() {
		String time = WeekdayUtil.currentDate();
		if (time.equals("2023-06-17")) {
			logger.info("start HistoryAnalyseReport");
			HistoryAnalyseReport reporter = new HistoryAnalyseReport();
			reporter.searchAllStockIdStatisticsCheckPoint(DailyCombineCheckPoint.AiTrend_Top_Area);
			reporter.searchAllStockIdStatisticsCheckPoint(DailyCombineCheckPoint.AiTrend_Bottom_Area);
			reporter.searchAllStockIdStatisticsCheckPoint(DailyCombineCheckPoint.AiTrend_Bottom_Gordon);
			logger.info("stop HistoryAnalyseReport");
		}
	}
}
