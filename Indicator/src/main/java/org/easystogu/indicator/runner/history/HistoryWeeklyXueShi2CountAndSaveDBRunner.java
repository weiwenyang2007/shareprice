package org.easystogu.indicator.runner.history;

import org.easystogu.db.access.table.IndWeekXueShi2TableHelper;
import org.easystogu.db.access.table.WeekStockPriceTableHelper;
import org.easystogu.file.access.CompanyInfoFileHelper;

public class HistoryWeeklyXueShi2CountAndSaveDBRunner extends HistoryXueShi2CountAndSaveDBRunner {
	public HistoryWeeklyXueShi2CountAndSaveDBRunner() {
		xueShi2Table = IndWeekXueShi2TableHelper.getInstance();
		qianFuQuanStockPriceTable = WeekStockPriceTableHelper.getInstance();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
		HistoryWeeklyXueShi2CountAndSaveDBRunner runner = new HistoryWeeklyXueShi2CountAndSaveDBRunner();
		runner.countAndSaved(stockConfig.getAllStockId());
		// runner.countAndSaved("002194");
	}

}
