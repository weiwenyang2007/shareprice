package org.easystogu.db.vo.table;

public class AiTrendPredictVO {
  public String stockId;
  public String date;
  public double result;
  public static final double buyPoint = 0.75;// range is from 0 to 1.0

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

  public double getResult() {
    return result;
  }

  public void setResult(double result) {
    this.result = result;
  }

  public String toString() {
    return "AiTrendPredictVO: " + this.getStockId() + ", " + this.getDate() + ", " + this.getResult();
  }
}
