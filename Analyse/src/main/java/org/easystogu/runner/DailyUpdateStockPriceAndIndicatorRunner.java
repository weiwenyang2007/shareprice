package org.easystogu.runner;

import java.util.List;

import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.runner.AllDailyIndCountAndSaveDBRunner;
import org.easystogu.log.LogHelper;
import org.easystogu.sina.runner.DailyStockPriceDownloadAndStoreDBRunner2;
import org.easystogu.sina.runner.DailyWeeklyStockPriceCountAndSaveDBRunner;
import org.slf4j.Logger;

public class DailyUpdateStockPriceAndIndicatorRunner implements Runnable {
	private static Logger logger = LogHelper.getLogger(DailyUpdateStockPriceAndIndicatorRunner.class);
	private CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
	private List<String> allStockIds = stockConfig.getAllStockId();

	public DailyUpdateStockPriceAndIndicatorRunner() {
	}

	public void run() {
		String[] args = null;
		long st = System.currentTimeMillis();
		// day (download all stockIds price)
		DailyStockPriceDownloadAndStoreDBRunner2.main(args);
		// day ind
		new AllDailyIndCountAndSaveDBRunner().runDailyIndForStockIds(allStockIds);
		// week
		new DailyWeeklyStockPriceCountAndSaveDBRunner().countAndSave(allStockIds);
		// week ind
		new AllDailyIndCountAndSaveDBRunner().runDailyWeekIndForStockIds(allStockIds);

		logger.debug("stop using " + (System.currentTimeMillis() - st) / 1000 + " seconds");
	}

	public static void main(String[] args) {
		// run today stockprice anaylse
		new DailyUpdateStockPriceAndIndicatorRunner().run();
	}
}
