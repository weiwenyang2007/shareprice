package org.easystogu.db.access.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.table.ZhuLiJingLiuRuVO;
import org.easystogu.db.vo.table.ZiJinLiuVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ZhuLiJingLiuRuTableHelper {
	private static Logger logger = LogHelper.getLogger(ZhuLiJingLiuRuTableHelper.class);
	private static ZhuLiJingLiuRuTableHelper instance = null;
	private static ZhuLiJingLiuRuTableHelper georedInstance = null;
	// please modify this SQL in all subClass
	protected String tableName = "ZHULIJINGLIURU";
	protected String INSERT_SQL = "INSERT INTO " + tableName
			+ " (stockId, date, rate, price, majorNetPer, incPer) VALUES (:stockId, :date, :rate, :price, :majorNetPer, :incPer)";
	protected String QUERY_BY_ID_AND_DATE_SQL = "SELECT * FROM " + tableName
			+ " WHERE stockId = :stockId AND date = :date";
	protected String QUERY_ALL_BY_ID_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY date";
	protected String QUERY_LATEST_N_BY_ID_SQL = "SELECT * FROM " + tableName
			+ " WHERE stockId = :stockId ORDER BY date DESC LIMIT :limit";
	protected String DELETE_BY_STOCKID_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId";
	protected String DELETE_BY_STOCKID_AND_DATE_SQL = "DELETE FROM " + tableName
			+ " WHERE stockId = :stockId AND date = :date";
	protected String DELETE_BY_DATE_SQL = "DELETE FROM " + tableName + " WHERE date = :date";
	protected String QUERY_BY_DATE_SQL = "SELECT * FROM " + tableName + " WHERE date = :date";

	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private ZhuLiJingLiuRuTableHelper(javax.sql.DataSource datasource) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
	}
	
	public static ZhuLiJingLiuRuTableHelper getInstance() {
		if (instance == null) {
			instance = new ZhuLiJingLiuRuTableHelper(PostgreSqlDataSourceFactory.createDataSource());
		}
		return instance;
	}

	public static ZhuLiJingLiuRuTableHelper getGeoredInstance() {
		if (georedInstance == null) {
			georedInstance = new ZhuLiJingLiuRuTableHelper(
					PostgreSqlDataSourceFactory.createGeoredDataSource());
		}
		return georedInstance;
	}

	private static final class ZhuLiJingLiuRuVOMapper implements RowMapper<ZhuLiJingLiuRuVO> {
		public ZhuLiJingLiuRuVO mapRow(ResultSet rs, int rowNum) throws SQLException {
			ZhuLiJingLiuRuVO vo = new ZhuLiJingLiuRuVO();
			vo.setStockId(rs.getString("stockId"));
			vo.setDate(rs.getString("date"));
			vo.setRate(rs.getInt("rate"));
			vo.setIncPer(rs.getString("incPer"));
			vo.setPrice(rs.getDouble("price"));
			vo.setMajorNetPer(rs.getDouble("majorNetPer"));

			return vo;
		}
	}

	private static final class DefaultPreparedStatementCallback implements PreparedStatementCallback<Integer> {
		public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
			return ps.executeUpdate();
		}
	}

	public void insert(ZhuLiJingLiuRuVO vo) {
		logger.debug("insert for {}", vo);

		try {
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", vo.getStockId());
			namedParameters.addValue("date", vo.getDate());
			namedParameters.addValue("rate", vo.getRate());
			namedParameters.addValue("incPer", vo.getIncPer());
			namedParameters.addValue("price", vo.getPrice());
			namedParameters.addValue("majorNetPer", vo.getMajorNetPer());

			namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters, new DefaultPreparedStatementCallback());
		} catch (Exception e) {
			logger.error("exception meets for insert vo: " + vo, e);
			e.printStackTrace();
		}
	}

	public void insert(List<ZhuLiJingLiuRuVO> list) throws Exception {
		for (ZhuLiJingLiuRuVO vo : list) {
			this.insert(vo);
		}
	}

	public void insertIfNotExist(ZhuLiJingLiuRuVO vo) {
		if (getZhuLiJingLiuRu(vo.stockId, vo.date) == null) {
			insert(vo);
		}

	}

	public void delete(String stockId) {
		try {
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", stockId);
			namedParameterJdbcTemplate.execute(DELETE_BY_STOCKID_SQL, namedParameters,
					new DefaultPreparedStatementCallback());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(String stockId, String date) {
		try {
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", stockId);
			namedParameters.addValue("date", date);
			namedParameterJdbcTemplate.execute(DELETE_BY_STOCKID_AND_DATE_SQL, namedParameters,
					new DefaultPreparedStatementCallback());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteByDate(String date) {
		try {
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("date", date);
			namedParameterJdbcTemplate.execute(DELETE_BY_DATE_SQL, namedParameters,
					new DefaultPreparedStatementCallback());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ZhuLiJingLiuRuVO getZhuLiJingLiuRu(String stockId, String date) {
		try {

			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", stockId);
			namedParameters.addValue("date", date);

			ZhuLiJingLiuRuVO vo = this.namedParameterJdbcTemplate.queryForObject(QUERY_BY_ID_AND_DATE_SQL,
					namedParameters, new ZhuLiJingLiuRuVOMapper());

			return vo;
		} catch (EmptyResultDataAccessException ee) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ZhuLiJingLiuRuVO> getZhuLiJingLiuRu(String date) {
		try {

			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("date", date);

			List<ZhuLiJingLiuRuVO> vo = this.namedParameterJdbcTemplate.query(QUERY_BY_DATE_SQL, namedParameters,
					new ZhuLiJingLiuRuVOMapper());

			return vo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ZhuLiJingLiuRuVO>();
	}

	public List<ZhuLiJingLiuRuVO> getAllZhuLiJingLiuRu(String stockId) {
		try {

			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", stockId);

			List<ZhuLiJingLiuRuVO> list = this.namedParameterJdbcTemplate.query(QUERY_ALL_BY_ID_SQL, namedParameters,
					new ZhuLiJingLiuRuVOMapper());

			return list;
		} catch (Exception e) {
			logger.error("exception meets for getAllKDJ stockId=" + stockId, e);
			e.printStackTrace();
			return new ArrayList<ZhuLiJingLiuRuVO>();
		}
	}

	// 最近几天的，必须使用时间倒序的SQL
	public List<ZhuLiJingLiuRuVO> getNDayZhuLiJingLiuRu(String stockId, int day) {
		try {

			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue("stockId", stockId);
			namedParameters.addValue("limit", day);

			List<ZhuLiJingLiuRuVO> list = this.namedParameterJdbcTemplate.query(QUERY_LATEST_N_BY_ID_SQL,
					namedParameters, new ZhuLiJingLiuRuVOMapper());

			return list;
		} catch (Exception e) {
			logger.error("exception meets for getAvgClosePrice stockId=" + stockId, e);
			e.printStackTrace();
			return new ArrayList<ZhuLiJingLiuRuVO>();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
		List<String> Ids = stockConfig.getAllStockId();
		Map<String, String> allIds = new HashMap<String, String>();
		ZiJinLiuTableHelper ins = ZiJinLiuTableHelper.getInstance();
		List<ZiJinLiuVO> list = ins.getZiJinLiu("2015-08-11");

		System.out.println("Ids.size=" + Ids.size());
		System.out.println("list.size=" + list.size());

		for (ZiJinLiuVO vo : list) {
			if (!allIds.containsKey(vo.stockId)) {
				allIds.put(vo.stockId, vo.name);
			}
		}

		for (String id : Ids) {
			if (!allIds.containsKey(id)) {
				allIds.put(id, stockConfig.getStockName(id));
			}
		}

		for (Map.Entry<String, String> entry : allIds.entrySet()) {
			if (entry.getValue() != null)
				System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}
}
