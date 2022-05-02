package org.easystogu.analyse.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easystogu.db.vo.table.StockSuperVO;

public class LocalStaticCache {
  private static Map<String, List<StockSuperVO>> dailyCache = new HashMap<String, List<StockSuperVO>>();
  private static Map<String, List<StockSuperVO>> weeklyCache = new HashMap<String, List<StockSuperVO>>();
  
  public static void addToDailyCache(String stockId, List<StockSuperVO> list) {
    if (dailyCache.containsKey(stockId)) {
      dailyCache.put(stockId, list);
    }
  }
  
  public static List<StockSuperVO> getFromDailyCache(String stockId) {
    return dailyCache.get(stockId);
  }
  
  public static void addToWeeklyCache(String stockId, List<StockSuperVO> list) {
    if (!weeklyCache.containsKey(stockId)) {
      weeklyCache.put(stockId, list);
    }
  }
  
  public static List<StockSuperVO> getFromWeeklyCache(String stockId) {
    return weeklyCache.get(stockId);
  }
  
  public static void emptyAllCache() {
    dailyCache.clear();
    weeklyCache.clear();
    System.out.println("LocalStaticCache emptyAllCache completed.");
  }
}
