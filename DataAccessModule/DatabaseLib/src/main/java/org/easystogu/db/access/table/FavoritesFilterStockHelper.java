package org.easystogu.db.access.table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.easystogu.db.ds.PostgreSqlDataSourceFactory;
import org.easystogu.db.vo.view.FavoritesFilterStockVO;
import org.easystogu.log.LogHelper;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class FavoritesFilterStockHelper {
  private static Logger logger = LogHelper.getLogger(FavoritesFilterStockHelper.class);
  private static FavoritesFilterStockHelper instance = null;
  private static FavoritesFilterStockHelper georedInstance = null;
  protected String tableName = "FAVORITES_FILTER_STOCK";
  protected String INSERT_SQL = "INSERT INTO " + tableName + " (stockId, filter) VALUES (:stockId, :filter)";
  protected String QUERY_BY_FILTER_SQL = "SELECT * FROM " + tableName + " WHERE filter = :filter";
  protected String DELETE_BY_STOCKID_AND_FILTER_SQL = "DELETE FROM " + tableName + " WHERE stockId =:stockId AND filter = :filter";

  protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  protected FavoritesFilterStockHelper(javax.sql.DataSource datasource) {
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
  }

  public static FavoritesFilterStockHelper getInstance() {
    if (instance == null) {
      instance = new FavoritesFilterStockHelper(PostgreSqlDataSourceFactory.createDataSource());
    }
    return instance;
  }

  public static FavoritesFilterStockHelper getGeoredInstance() {
    if (georedInstance == null) {
      georedInstance = new FavoritesFilterStockHelper(PostgreSqlDataSourceFactory.createGeoredDataSource());
    }
    return georedInstance;
  }

  private static final class FavoritesStockVOMapper implements RowMapper<FavoritesFilterStockVO> {
    public FavoritesFilterStockVO mapRow(ResultSet rs, int rowNum) throws SQLException {
      FavoritesFilterStockVO vo = new FavoritesFilterStockVO();
      vo.setStockId(rs.getString("stockId"));
      vo.setFilter(rs.getString("filter"));
      return vo;
    }
  }

  private static final class DefaultPreparedStatementCallback implements PreparedStatementCallback<Integer> {
    public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
      return ps.executeUpdate();
    }
  }

  public void insert(FavoritesFilterStockVO vo) {
    logger.debug("insert for {}", vo);

    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", vo.getStockId());
      namedParameters.addValue("filter", vo.getFilter());

      namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters, new DefaultPreparedStatementCallback());
    } catch (Exception e) {
      logger.error("exception meets for insert vo: " + vo, e);
      e.printStackTrace();
    }
  }

  public void insert(String stockId, String filter) {

    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("filter", filter);

      namedParameterJdbcTemplate.execute(INSERT_SQL, namedParameters, new DefaultPreparedStatementCallback());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void insert(List<FavoritesFilterStockVO> list) throws Exception {
    for (FavoritesFilterStockVO vo : list) {
      this.insert(vo);
    }
  }

  public void delete(String stockId, String filter) {
    try {
      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("stockId", stockId);
      namedParameters.addValue("filter", filter);
      namedParameterJdbcTemplate.execute(DELETE_BY_STOCKID_AND_FILTER_SQL, namedParameters,
          new DefaultPreparedStatementCallback());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<FavoritesFilterStockVO> getByFilter(String filter) {
    try {

      MapSqlParameterSource namedParameters = new MapSqlParameterSource();
      namedParameters.addValue("filter", filter);

      List<FavoritesFilterStockVO> list = this.namedParameterJdbcTemplate.query(QUERY_BY_FILTER_SQL, namedParameters,
          new FavoritesStockVOMapper());

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<FavoritesFilterStockVO>();
  }
}
