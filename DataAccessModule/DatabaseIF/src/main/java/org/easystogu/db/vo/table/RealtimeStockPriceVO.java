package org.easystogu.db.vo.table;

public class RealtimeStockPriceVO {
  public String stockId;
  public String name;
  public String datetime;
  public double open;
  public double high;
  public double low;
  public double close;
  public double shenxian_buy;
  public double shenxian_sell;

  public String getStockId() {
    return stockId;
  }

  public void setStockId(String stockId) {
    this.stockId = stockId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDatetime() {
    return datetime;
  }

  public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  public double getOpen() {
    return open;
  }

  public void setOpen(double open) {
    this.open = open;
  }

  public double getHigh() {
    return high;
  }

  public void setHigh(double high) {
    this.high = high;
  }

  public double getLow() {
    return low;
  }

  public void setLow(double low) {
    this.low = low;
  }

  public double getClose() {
    return close;
  }

  public void setClose(double close) {
    this.close = close;
  }

  public double getShenxian_buy() {
    return shenxian_buy;
  }

  public void setShenxian_buy(double shenxian_buy) {
    this.shenxian_buy = shenxian_buy;
  }

  public double getShenxian_sell() {
    return shenxian_sell;
  }

  public void setShenxian_sell(double shenxian_sell) {
    this.shenxian_sell = shenxian_sell;
  }

  public RealtimeStockPriceVO(){
    this.name = "";
    this.stockId = "";
    this.datetime = "";
    this.open = 0;
    this.close = 0;
    this.low = 0;
    this.high = 0;
    this.shenxian_buy = 0;
    this.shenxian_sell = 0;
  }

  public RealtimeStockPriceVO copy(){
    RealtimeStockPriceVO copy = new RealtimeStockPriceVO();
    copy.close = this.close;
    copy.datetime = this.datetime;
    copy.high = this.high;
    copy.low = this.low;
    copy.name = this.name;
    copy.open = this.open;
    copy.stockId = this.stockId;
    copy.shenxian_buy = this.shenxian_buy;
    copy.shenxian_sell = this.shenxian_sell;
    return copy;
  }

  public static RealtimeStockPriceVO copyFrom(StockPriceVO spvo, String datetime){
    RealtimeStockPriceVO copy = new RealtimeStockPriceVO();
    copy.close = spvo.close;
    copy.datetime = datetime;
    copy.high = spvo.high;
    copy.low = spvo.low;
    copy.name = spvo.name;
    copy.open = spvo.open;
    copy.stockId = spvo.stockId;
    return copy;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer("RealtimeStockPriceVO: {");
    sb.append("stockId:" + stockId);
    sb.append(", name:" + name);
    sb.append(", datetime:" + datetime);
    sb.append(", open:" + open);
    sb.append(", high:" + high);
    sb.append(", low:" + low);
    sb.append(", close:" + close);
    sb.append(", shenxian_buy:" + shenxian_buy);
    sb.append(", shenxian_sell:" + shenxian_sell);
    sb.append("}");
    return sb.toString();
  }

  public boolean isValidated() {
    return this.close > 0 ? true : false;
  }
}
