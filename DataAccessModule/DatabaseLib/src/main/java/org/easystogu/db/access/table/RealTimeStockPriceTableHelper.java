package org.easystogu.db.access.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.table.RealtimeStockPriceVO;
import org.easystogu.log.LogHelper;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class RealTimeStockPriceTableHelper {
  private static Logger logger = LogHelper.getLogger(RealTimeStockPriceTableHelper.class);
  private static RealTimeStockPriceTableHelper instance = null;
  private static RealTimeStockPriceTableHelper georedInstance = null;
  protected String tableName = "REALTIME_STOCKPRICE";
  // please modify this SQL in all subClass
  protected String INSERT_SQL = "INSERT INTO " + tableName
      + " (stockId, datetime, open, high, low, close) VALUES (:stockId, :datetime, :open, :high, :low, :close)";
  protected String UPDATE_SHENXIAN_SQL = "UPDATE " + tableName
      + " SET shenxian_buy = :shenxian_buy, shenxian_sell = :shenxian_sell"
      + " WHERE stockId = :stockId AND datetime = :datetime";
  protected String QUERY_LATEST_REALTIME_PRICE_BY_STOCKID_SQL =
      "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY datetime DESC LIMIT 1";

  protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  protected RealTimeStockPriceTableHelper(javax.sql.DataSource datasource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  public static RealTimeStockPriceTableHelper getInstance() {
    if (instance == null) {
      instance = new RealTimeStockPriceTableHelper(PostgreSqlDataSourceFactory.createDataSource());
    }
    return instance;
  }

  public static RealTimeStockPriceTableHelper getGeoredInstance() {
    if (georedInstance == null) {
      georedInstance =
          new RealTimeStockPriceTableHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
    }
    return georedInstance;
  }

  private static final class RealtimeStockPriceVOMapper implements RowMapper<RealtimeStockPriceVO> {
    public RealtimeStockPriceVO mapRow(ResultSet rs, int rowNum) throws SQLException {
      RealtimeStockPriceVO vo = new RealtimeStockPriceVO();
      vo.setStockId(rs.getString("stockId"));
      vo.setDatetime(rs.getString("datetime"));
      vo.setClose(rs.getDouble("close"));
      vo.setHigh(rs.getDouble("high"));
      vo.setLow(rs.getDouble("low"));
      vo.setOpen(rs.getDouble("open"));
      vo.setShenxian_buy(rs.getDouble("shenxian_buy"));
      vo.setShenxian_sell(rs.getDouble("shenxian_sell"));
      return vo;
    }
  }

  private static final class DoubleVOMapper implements RowMapper<Double> {
    public Double mapRow(ResultSet rs, int rowNum) throws SQLException {
      return rs.getDouble("rtn");
    }
  }

  private static final class StringVOMapper implements RowMapper<String> {
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
      return rs.getString("rtn");
    }
  }

  private static final class IntVOMapper implements RowMapper<Integer> {
    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
      return rs.getInt("rtn");
    }
  }

  private static final class DefaultPreparedStatementCallback
      implements PreparedStatementCallback<Integer> {
    public Integer doInPreparedStatement(PreparedStatement ps)
        throws SQLException, DataAccessException {
      return ps.executeUpdate();
    }
  }

  public void insert(RealtimeStockPriceVO vo) {
    logger.debug("insert for {}", vo);

    if (!vo.isValidated()) {
      logger.warn(vo.getStockId() + " is not validated, skip. vo= {}", vo);
      return;
    }

    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", vo.getStockId());
      namedParameters.addValue("datetime", vo.getDatetime());
      namedParameters.addValue("open", vo.getOpen());
      namedParameters.addValue("high", vo.getHigh());
      namedParameters.addValue("low", vo.getLow());
      namedParameters.addValue("close", vo.getClose());

      namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters,
          new RealTimeStockPriceTableHelper.DefaultPreparedStatementCallback());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void insert(List<RealtimeStockPriceVO> list) throws Exception {
    for (RealtimeStockPriceVO vo : list) {
      this.insert(vo);
    }
  }

  public void updateShenxianBuySell(String stockId, String datetime, double shenxian_buy, double shenxian_sell) {
    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("datetime", datetime);
      namedParameters.addValue("shenxian_buy", shenxian_buy);
      namedParameters.addValue("shenxian_sell", shenxian_sell);

      namedParameterJdbcTemplate.execute(UPDATE_SHENXIAN_SQL, namedParameters,
          new RealTimeStockPriceTableHelper.DefaultPreparedStatementCallback());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public RealtimeStockPriceVO getLatestRealtimePrice(String stockId) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);

      RealtimeStockPriceVO vo = this.namedParameterJdbcTemplate.queryForObject(
          QUERY_LATEST_REALTIME_PRICE_BY_STOCKID_SQL, namedParameters, new RealtimeStockPriceVOMapper());

      return vo;
    } catch (EmptyResultDataAccessException ee) {
      return null;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
