package org.easystogu.db.access.table;

import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ZiJinLiu3DayTableHelper extends ZiJinLiuTableHelper {
	private static ZiJinLiu3DayTableHelper instance = null;
	private static ZiJinLiu3DayTableHelper georedInstance = null;

	public static ZiJinLiu3DayTableHelper getInstance() {
		if (instance == null) {
			instance = new ZiJinLiu3DayTableHelper(PostgreSqlDataSourceFactory.createDataSource());
		}
		return instance;
	}

	public static ZiJinLiu3DayTableHelper getGeoredInstance() {
		if (georedInstance == null) {
			georedInstance = new ZiJinLiu3DayTableHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
		}
		return georedInstance;
	}

	protected ZiJinLiu3DayTableHelper(javax.sql.DataSource datasource) {
		super(datasource);
		refeshTableSQL();
	}

	private void refeshTableSQL() {
		// please modify this SQL in superClass
		tableName = "ZIJINLIU_3DAY";
		INSERT_SQL = "INSERT INTO " + tableName
				+ " (stockId, date, rate, incPer, majorNetIn, majorNetPer, biggestNetIn, biggestNetPer, bigNetIn, bigNetPer, midNetIn, midNetPer, smallNetIn, smallNetPer) VALUES (:stockId, :date, :rate, :incPer, :majorNetIn, :majorNetPer, :biggestNetIn, :biggestNetPer, :bigNetIn, :bigNetPer, :midNetIn, :midNetPer, :smallNetIn, :smallNetPer)";
		QUERY_BY_ID_AND_DATE_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
		QUERY_ALL_BY_ID_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY date";
		QUERY_LATEST_N_BY_ID_SQL = "SELECT * FROM " + tableName
				+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit";
		DELETE_BY_STOCKID_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId";
		DELETE_BY_STOCKID_AND_DATE_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
		DELETE_BY_DATE_SQL = "DELETE FROM " + tableName + " WHERE date = :date";
		QUERY_BY_DATE_SQL = "SELECT * FROM " + tableName + " WHERE date = :date";
	}
}
