package org.easystogu.sina.runner.history;

public class StockPriceHistoryOverAllRunner implements Runnable {
	private String startDate;
	private String endDate;
	private String stockId;

	public StockPriceHistoryOverAllRunner(String _startDate, String _endDate) {
		this.startDate = _startDate;
		this.endDate = _endDate;
	}
	public StockPriceHistoryOverAllRunner(String stockId, String _startDate, String _endDate) {
		this.stockId = stockId;
		this.startDate = _startDate;
		this.endDate = _endDate;
	}

	public void run() {
		// had better clean all the data from DB
		// history stock price
		HistoryStockPriceDownloadAndStoreDBRunner.main(new String[] { startDate, endDate, stockId });
		// hou fuquan history price
		// HistoryHouFuQuanStockPriceDownloadAndStoreDBRunner.main(args);
		// qian fuquan history price
		HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner.main(null);
		// count week price
		HistoryWeekStockPriceCountAndSaveDBRunner.main(null);

	}
}
