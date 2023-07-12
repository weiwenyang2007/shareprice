package org.easystogu.db.vo.table;

public class CandleStickPatternVO {
  public String stockId;
  public String date;
  public String pattern;

  public String getStockId() {
    return stockId;
  }

  public void setStockId(String stockId) {
    this.stockId = stockId;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
