package org.easystogu.db.vo.view;

public class FavoritesFilterStockVO {
  public String stockId;
  public String filter;

  public FavoritesFilterStockVO() {

  }

  public FavoritesFilterStockVO(String stockId, String filter) {
    this.stockId = stockId;
    this.filter = filter;
  }

  @Override
  public String toString() {
    return this.stockId + " " + this.filter;
  }

  public String getStockId() {
    return stockId;
  }

  public void setStockId(String stockId) {
    this.stockId = stockId;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }
}
