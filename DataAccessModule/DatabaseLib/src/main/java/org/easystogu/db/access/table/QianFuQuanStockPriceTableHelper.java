package org.easystogu.db.access.table;

import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.table.StockPriceVO;

public class QianFuQuanStockPriceTableHelper extends StockPriceTableHelper {
	private static QianFuQuanStockPriceTableHelper instance = null;
	private static QianFuQuanStockPriceTableHelper georedInstance = null;

	public static QianFuQuanStockPriceTableHelper getInstance() {
		if (instance == null) {
			instance = new QianFuQuanStockPriceTableHelper(PostgreSqlDataSourceFactory.createDataSource());
		}
		return instance;
	}

	public static QianFuQuanStockPriceTableHelper getGeoredInstance() {
		if (georedInstance == null) {
			georedInstance = new QianFuQuanStockPriceTableHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
		}
		return georedInstance;
	}

	protected QianFuQuanStockPriceTableHelper(javax.sql.DataSource datasource) {
		super(datasource);
		refeshTableSQL();
	}

	private void refeshTableSQL() {
		tableName = "QIAN_FUQUAN_STOCKPRICE";
		// please modify this SQL in superClass
		INSERT_SQL = "INSERT INTO " + tableName
				+ " (stockId, date, open, high, low, close, volume, lastclose) VALUES (:stockId, :date, :open, :high, :low, :close, :volume, :lastclose)";
		SELECT_CLOSE_PRICE_SQL = "SELECT close AS rtn FROM " + tableName + " WHERE stockId = :stockId ORDER BY DATE";
		SELECT_LOW_PRICE_SQL = "SELECT low AS rtn FROM " + tableName + " WHERE stockId = :stockId ORDER BY DATE";
		SELECT_HIGH_PRICE_SQL = "SELECT high AS rtn FROM " + tableName + " WHERE stockId = :stockId ORDER BY DATE";
		QUERY_BY_STOCKID_AND_BETWEEN_DATE = "SELECT * FROM " + tableName
				+ " WHERE stockId = :stockId AND date >= :date1 AND date <= :date2 ORDER BY DATE";
		// macd used this sql
		QUERY_BY_STOCKID_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY DATE";
		// avg price, for example MA5, MA10, MA20, MA30
		AVG_CLOSE_PRICE_SQL = "SELECT avg(close) AS rtn from (SELECT close FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit) AS myma";
		// avg volume, for example MAVOL5, MAVOL10
		AVG_VOLUME_SQL = "SELECT avg(volume) AS rtn from (SELECT volume FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit) AS mymavol";
		// kdj used this: Low(n)
		SELECT_LOW_N_PRICE_SQL = "SELECT min(low) AS rtn from (SELECT low FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit) AS mylown";
		// kdj used this: High(n)
		SELECT_HIGH_N_PRICE_SQL = "SELECT max(high) AS rtn from (SELECT high FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit) AS myhighn";
        
		//AI use this High(n) with date start from
        SELECT_HIGH_N_PRICE_START_DATA_SQL = "SELECT max(high) AS rtn from (SELECT high FROM "
              + tableName + " WHERE stockId = :stockId AND date > :startDate LIMIT :limit) AS myhighn";
        //AI use this Low(n) with date start from
        SELECT_LOW_N_PRICE_START_DATA_SQL = "SELECT min(low) AS rtn from (SELECT low FROM "
              + tableName + " WHERE stockId = :stockId AND date > :startDate LIMIT :limit) AS mylown";
        //AI use this Close(n) with date start from
        SELECT_CLOSE_N_PRICE_START_DATA_SQL = "SELECT min(close) AS rtn from (SELECT close FROM "
              + tableName + " WHERE stockId = :stockId AND date > :startDate LIMIT :limit) AS myclose";
        
		// query price by Id and date
		QUERY_BY_STOCKID_DATE_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
		// query the last date
		GET_LATEST_STOCK_DATE = "SELECT date as rtn FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY DATE DESC limit 1";
		// query the latest N date price
		QUERY_LATEST_PRICE_N_DATE_STOCKID_SQL = "SELECT * FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit";
		// query the low price between date1(not include) and date2(include)
		QUERY_LOW_PRICE_BETWEEN_DATE_SQL = "SELECT min(low) AS rtn from (SELECT low from " + tableName
				+ " WHERE stockId = :stockId AND date > :startDate AND date <= :endDate) AS mylowQuery";
		// query the high price between date1(not include) and date2(include)
		QUERY_HIGH_PRICE_BETWEEN_DATE_SQL = "SELECT max(high) AS rtn from (SELECT high from " + tableName
				+ " WHERE stockId = :stockId AND date > :startDate AND date <= :endDate) AS myHighQuery";
		// query the high price date between date1(not include) and
		// date2(include)
		QUERY_HIGH_PRICE_DATE_BETWEEN_DATE_SQL = "SELECT date AS rtn from " + tableName
				+ " WHERE stockId = :stockId AND high = :high AND date > :startDate AND date <= :endDate";
		DELETE_BY_STOCKID_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId";
		DELETE_BY_STOCKID_AND_DATE_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
		DELETE_BY_STOCKID_AND_AFTER_DATE_SQL = "DELETE FROM " + tableName
				+ " WHERE stockId = :stockId AND date >= :date";
		DELETE_BY_STOCKID_AND_BETWEEN_DATE_SQL = "DELETE FROM " + tableName
				+ " WHERE stockId = :stockId AND date >= :date1 AND date <= :date2";
		DELETE_BY_DATE_SQL = "DELETE FROM " + tableName + " WHERE date = :date";
		COUNT_DAYS_BETWEEN_DATE1_DATE2 = "SELECT COUNT(*) AS rtn FROM " + tableName
				+ " WHERE stockId = :stockId AND DATE >= :date1 AND DATE <= :date2";
		QUERY_DAYS_BETWEEN_DATE1_DATE2 = "SELECT date AS rtn FROM " + tableName
				+ " WHERE stockId = :stockId AND DATE >= :date1 AND DATE <= :date2";
		QUERY_PREVIOUS_DATE_SQL = "SELECT date as rtn FROM " + tableName
				+ " WHERE stockId = :stockId  AND date < :date ORDER BY DATE DESC limit 1";
		// only use for weekPrice, query the weekPrice based on date
		// QUERY_BY_STOCKID_AND_BETWEEN_DATE = "SELECT * FROM " +
		// tableName
		// +
		// " WHERE stockId = :stockId AND DATE >= :date1 AND DATE <= :date2
		// ORDER BY DATE";
		QUERY_BY_STOCKID_AND_BEFORE_DATE = "SELECT * FROM " + tableName
				+ " WHERE stockId = :stockId AND DATE <= :date ORDER BY DATE";
		// update batch price based on gaoSongZhuan and date
		UPDATE_BATCH_PRICE_BASED_ON_CHUQUAN_AND_DATE = "UPDATE " + tableName
				+ " SET open = open*:rate, high = high*:rate, low = low*:rate, close = close*:rate "
				+ " WHERE stockId = :stockId AND DATE < :date";
		// update price based on gaoSongZhuan and date
		UPDATE_PRICE_BASED_ON_CHUQUAN_AND_DATE = "UPDATE " + tableName
				+ " SET open = :open, high = :high, low = :low, close = :close "
				+ " WHERE stockId = :stockId AND DATE = :date";
		// query the latest N date
		QUERY_LATEST_N_DATE_STOCKID_SQL = "SELECT date AS rtn FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit";
		QUERY_ALL_BY_DATE = "SELECT * FROM " + tableName + " WHERE DATE = :date ORDER BY stockId";

		// to check if a date is in deal time, to exclude the holiday
		IS_DATE_IN_DEAL = "SELECT count(*) AS rtn FROM " + tableName + " WHERE stockid='999999' AND DATE = :date";
		COUNT_BY_ID_SQL = "SELECT COUNT(*) AS rtn FROM " + tableName + " WHERE stockId = :stockId";
		COUNT_BY_DATE_SQL = "SELECT COUNT(*) AS rtn FROM " + tableName + " WHERE DATE = :date";
		COUNT_ALL_SQL = "SELECT count(*) AS rtn from " + tableName;
		// select all distinct stockIDs
		QUERY_DISTINCT_ID = "SELECT distinct(stockid) AS rtn FROM " + tableName + " order by stockid";
		QUERY_DEAL_DATE_BY_ID = "SELECT date AS rtn FROM " + tableName + " WHERE stockId = :stockId ORDER BY date DESC";
		COUNT_SQL = "SELECT COUNT(*) AS rtn FROM " + tableName + " WHERE stockId = :stockId";
	}

	public static void main(String[] args) {
		StockPriceTableHelper table = StockPriceTableHelper.getInstance();
		StockPriceVO vo = table.getStockPriceByIdAndDate("000002", "2015-01-06");
		System.out.println(vo.stockId + ";" + vo.close + ";" + vo.date);

		QianFuQuanStockPriceTableHelper qiantable = QianFuQuanStockPriceTableHelper.getInstance();
		StockPriceVO qianvo = qiantable.getStockPriceByIdAndDate("000002", "2015-01-06");
		System.out.println(qianvo.stockId + ";" + qianvo.close + ";" + qianvo.date);
	}
}
