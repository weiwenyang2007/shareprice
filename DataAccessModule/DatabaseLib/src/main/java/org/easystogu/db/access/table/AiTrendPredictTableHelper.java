package org.easystogu.db.access.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.table.AiTrendPredictVO;
import org.easystogu.log.LogHelper;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AiTrendPredictTableHelper {
  private static Logger logger = LogHelper.getLogger(AiTrendPredictTableHelper.class);
  private static AiTrendPredictTableHelper instance = null;
  private static AiTrendPredictTableHelper georedInstance = null;
  protected String tableName = "AI_TREND_PREDICTION";
  // please modify this SQL in all subClass
  protected String INSERT_SQL = "INSERT INTO " + tableName
      + " (stockId, date, result) VALUES (:stockId, :date, :result)";
  protected String QUERY_BY_STOCKID_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY date";
  protected String QUERY_BY_STOCKID_AND_DATE_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
  protected String QUERY_BY_DATE_AND_RESULT_BOTTOM_SQL = "SELECT * FROM " + tableName + " WHERE date = :date AND result >= :result";
  protected String QUERY_BY_DATE_AND_RESULT_TOP_SQL = "SELECT * FROM " + tableName + " WHERE date = :date AND result < :result";

  protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private AiTrendPredictTableHelper(javax.sql.DataSource datasource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  public static AiTrendPredictTableHelper getInstance() {
    if (instance == null) {
      instance = new AiTrendPredictTableHelper(PostgreSqlDataSourceFactory.createDataSource());
    }
    return instance;
  }

  public static AiTrendPredictTableHelper getGeoredInstance() {
    if (georedInstance == null) {
      georedInstance = new AiTrendPredictTableHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
    }
    return georedInstance;
  }

  private static final class AiTrendPredictVOMapper implements RowMapper<AiTrendPredictVO> {
    public AiTrendPredictVO mapRow(ResultSet rs, int rowNum) throws SQLException {
      AiTrendPredictVO vo = new AiTrendPredictVO();
      vo.setStockId(rs.getString("stockId"));
      vo.setDate(rs.getString("date"));
      vo.setResult(rs.getDouble("result"));
      return vo;
    }
  }

  private static final class DefaultPreparedStatementCallback implements
      PreparedStatementCallback<Integer> {
    public Integer doInPreparedStatement(
        PreparedStatement ps) throws SQLException, DataAccessException {
      return ps.executeUpdate();
    }
  }

  public void insert(AiTrendPredictVO vo) {
    logger.debug("insert for {}", vo);

    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", vo.getStockId());
      namedParameters.addValue("date", vo.getDate());
      namedParameters.addValue("result", vo.getResult());

      namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters, new AiTrendPredictTableHelper.DefaultPreparedStatementCallback());
    } catch (Exception e) {
      logger.error("exception meets for insert vo: " + vo, e);
      e.printStackTrace();
    }
  }


  public void insert(List<AiTrendPredictVO> list) throws Exception {
    for (AiTrendPredictVO vo : list) {
      this.insert(vo);
    }
  }

  public List<AiTrendPredictVO> getByStockId(String stockId) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);

      List<AiTrendPredictVO> list = this.namedParameterJdbcTemplate.query(QUERY_BY_STOCKID_SQL, namedParameters,
          new AiTrendPredictVOMapper());

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<AiTrendPredictVO>();
  }

  public AiTrendPredictVO getByStockIdAndDate(String stockId, String date) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("date", date);

      AiTrendPredictVO vo = this.namedParameterJdbcTemplate.queryForObject(QUERY_BY_STOCKID_AND_DATE_SQL, namedParameters,
          new AiTrendPredictVOMapper());

      return vo;
    } catch (Exception e) {
      //e.printStackTrace();
    }
    return null;
  }

  public List<AiTrendPredictVO> getByDateAndResultBottom(String date) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("date", date);
      namedParameters.addValue("result", AiTrendPredictVO.buyPoint);

      List<AiTrendPredictVO> list = this.namedParameterJdbcTemplate.query(QUERY_BY_DATE_AND_RESULT_BOTTOM_SQL, namedParameters,
          new AiTrendPredictVOMapper());

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<AiTrendPredictVO>();
  }

  public List<AiTrendPredictVO> getByDateAndResultTop(String date) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("date", date);
      namedParameters.addValue("result", AiTrendPredictVO.buyPoint);

      List<AiTrendPredictVO> list = this.namedParameterJdbcTemplate.query(QUERY_BY_DATE_AND_RESULT_TOP_SQL, namedParameters,
          new AiTrendPredictVOMapper());

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<AiTrendPredictVO>();
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    AiTrendPredictTableHelper ins = AiTrendPredictTableHelper.getInstance();
    try {
      System.out.println(ins.getByStockId("000673"));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
