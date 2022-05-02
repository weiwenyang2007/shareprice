package org.easystogu.runner;

import org.easystogu.config.ConfigurationService;
import org.easystogu.config.DBConfigurationService;
import org.easystogu.easymoney.runner.OverAllZiJinLiuAndDDXRunner;
import org.easystogu.sina.runner.DailyStockPriceDownloadAndStoreDBRunner2;

//only download stockprice and zijinliu, ddx, 
//no other ind counted,
//this will be run on aliyun
public class DailyUpdateStockPriceAndDDXRunner implements Runnable {
    private ConfigurationService config = DBConfigurationService.getInstance();
	private boolean fetchAllZiJinLiu = false;

	public boolean isFetchAllZiJinLiu() {
		return fetchAllZiJinLiu;
	}

	public void setFetchAllZiJinLiu(boolean fetchAllZiJinLiu) {
		this.fetchAllZiJinLiu = fetchAllZiJinLiu;
	}

	public void run() {
		long st = System.currentTimeMillis();
		// daily price (download all stockIds price)
		DailyStockPriceDownloadAndStoreDBRunner2.main(null);
		// daily zijinliu and ddx for all
		if(config.getBoolean("count_zijin_and_ddx", false)) {
		  OverAllZiJinLiuAndDDXRunner zijinliuRunner = new OverAllZiJinLiuAndDDXRunner();
		  zijinliuRunner.run();
		}
		System.out.println("stop using " + (System.currentTimeMillis() - st) / 1000 + " seconds");
	}

	public static void main(String[] args) {
		// run today stockprice anaylse
		new DailyUpdateStockPriceAndDDXRunner().run();
	}
}
