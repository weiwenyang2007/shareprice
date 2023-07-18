package org.easystogu.db.access.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.table.CandleStickPatternVO;
import org.easystogu.log.LogHelper;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class CandleStickPatternTableHelper {
  private static Logger logger = LogHelper.getLogger(CandleStickPatternTableHelper.class);
  private static CandleStickPatternTableHelper instance = null;
  private static CandleStickPatternTableHelper georedInstance = null;
  protected String tableName = "CANDLESTICK_PATTERN";
  // please modify this SQL in all subClass
  protected String INSERT_SQL = "INSERT INTO " + tableName
      + " (stockId, date, pattern, score, score_roll) VALUES (:stockId, :date, :pattern, :score, :score_roll)";
  protected String QUERY_BY_STOCKID_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId ORDER BY date";
  protected String QUERY_BY_STOCKID_AND_DATE_SQL = "SELECT * FROM " + tableName + " WHERE stockId = :stockId AND date = :date";
  protected String DELETE_BY_STOCKID_AND_DATE_SQL = "DELETE FROM " + tableName + " WHERE stockId = :stockId AND date = :date";

  protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private CandleStickPatternTableHelper(javax.sql.DataSource datasource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  public static CandleStickPatternTableHelper getInstance() {
    if (instance == null) {
      instance = new CandleStickPatternTableHelper(PostgreSqlDataSourceFactory.createDataSource());
    }
    return instance;
  }

  public static CandleStickPatternTableHelper getGeoredInstance() {
    if (georedInstance == null) {
      georedInstance = new CandleStickPatternTableHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
    }
    return georedInstance;
  }

  private static final class CandleStickPatternVOMapper implements RowMapper<CandleStickPatternVO> {
    public CandleStickPatternVO mapRow(ResultSet rs, int rowNum) throws SQLException {
      CandleStickPatternVO vo = new CandleStickPatternVO();
      vo.setStockId(rs.getString("stockId"));
      vo.setDate(rs.getString("date"));
      vo.setPattern(rs.getString("pattern"));
      vo.setScore(rs.getInt("score"));
      vo.setScoreRoll(rs.getInt("score_roll"));
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

  public void insert(CandleStickPatternVO vo) {
    logger.debug("insert for {}", vo);

    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", vo.getStockId());
      namedParameters.addValue("date", vo.getDate());
      namedParameters.addValue("pattern", vo.getPattern());
      namedParameters.addValue("score", vo.getScore());
      namedParameters.addValue("score_roll", vo.getScoreRoll());

      namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters, new CandleStickPatternTableHelper.DefaultPreparedStatementCallback());
    } catch (Exception e) {
      logger.error("exception meets for insert vo: " + vo, e);
      e.printStackTrace();
    }
  }


  public void insert(List<CandleStickPatternVO> list) throws Exception {
    for (CandleStickPatternVO vo : list) {
      this.insert(vo);
    }
  }

  public List<CandleStickPatternVO> getByStockId(String stockId) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);

      List<CandleStickPatternVO> list = this.namedParameterJdbcTemplate.query(QUERY_BY_STOCKID_SQL, namedParameters,
          new CandleStickPatternTableHelper.CandleStickPatternVOMapper());

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<CandleStickPatternVO>();
  }

  public CandleStickPatternVO getByStockIdAndDate(String stockId, String date) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("date", date);

      CandleStickPatternVO vo = this.namedParameterJdbcTemplate.queryForObject(QUERY_BY_STOCKID_AND_DATE_SQL, namedParameters,
          new CandleStickPatternTableHelper.CandleStickPatternVOMapper());

      return vo;
    } catch (Exception e) {
      //e.printStackTrace();
    }
    return null;
  }

  public void delete(String stockId, String date) {
    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("date", date);
      namedParameterJdbcTemplate.execute(DELETE_BY_STOCKID_AND_DATE_SQL, namedParameters,
          new CandleStickPatternTableHelper.DefaultPreparedStatementCallback());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    CandleStickPatternTableHelper ins = CandleStickPatternTableHelper.getInstance();
    try {
      System.out.println(ins.getByStockId("000673"));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
