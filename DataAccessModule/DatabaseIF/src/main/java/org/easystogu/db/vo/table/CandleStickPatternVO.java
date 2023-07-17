package org.easystogu.db.vo.table;

public class CandleStickPatternVO {
  public String stockId;
  public String date;
  public String pattern;
  public int score;
  public int scoreRoll;

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public int getScoreRoll() {
    return scoreRoll;
  }

  public void setScoreRoll(int scoreRoll) {
    this.scoreRoll = scoreRoll;
  }

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

  public String getPatternWithScore() {
    return String.format("s=%d r=%d p=%s", score, scoreRoll, pattern);
  }
}
