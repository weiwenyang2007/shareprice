package org.easystogu.analyse;

import static org.easystogu.checkpoint.DailyCombineCheckPoint.AiTrend_Bottom_Area;
import static org.easystogu.checkpoint.DailyCombineCheckPoint.AiTrend_Top_Area;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easystogu.checkpoint.DailyCombineCheckPoint;
import org.easystogu.db.access.table.AiTrendPredictTableHelper;
import org.easystogu.db.access.table.CheckPointDailySelectionTableHelper;
import org.easystogu.db.access.table.FavoritesFilterStockHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.CheckPointDailySelectionVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.db.vo.view.FavoritesFilterStockVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.utils.Strings;
import org.slf4j.Logger;

//analyse whether ai trend predicate is better for each stockId
public class AiTrendBuySellPointAnalyse {
  private static Logger logger = LogHelper.getLogger(AiTrendBuySellPointAnalyse.class);
  private AiTrendPredictTableHelper aiTrendPredictTableHelper = AiTrendPredictTableHelper.getInstance();
  private CheckPointDailySelectionTableHelper checkPointDailySelectionTable = CheckPointDailySelectionTableHelper
      .getInstance();
  private StockPriceTableHelper stockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
  protected CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
  private FavoritesFilterStockHelper favoritesFilterTable = FavoritesFilterStockHelper.getInstance();

  public void analyze(String stockId){
    double buyClose = 0.0; int buyCount = 0;
    double selClose = 0.0; int selCount = 0;

    List<CheckPointDailySelectionVO>  ckpList = checkPointDailySelectionTable.getCheckPointByStockID(stockId);
    for (CheckPointDailySelectionVO ckpVo : ckpList) {
      StockPriceVO spvo = stockPriceTable.getStockPriceByIdAndDate(ckpVo.stockId, ckpVo.getDate());
      if(AiTrend_Top_Area.name().equals(ckpVo.getCheckPoint())) {
        selClose += spvo.close;
        selCount++;
      }else if(AiTrend_Bottom_Area.name().equals(ckpVo.getCheckPoint())) {
        buyClose += spvo.close;
        buyCount++;
      }
    }
    double buyAvg = (buyClose/buyCount);
    double selAvg = (selClose/selCount);
    double earn =  (selAvg/buyAvg);//盈利百分比
    //due to AI spend much time for prediction, filter out the stock by Earning, limit the number of stockId to about 1250 (instead of all 4800)
    if((buyCount >= 60 && earn >= 1.15) || (buyCount >= 30 && earn >= 1.20) || (buyCount >= 20 && earn >= 1.25)) {
      logger.debug("stockId " + stockId + ", buyCount=" + buyCount + ", buyAvg=" + Strings
          .convert2ScaleDecimalStr(buyAvg, 2) + ", selCount=" + selCount + ", selAvg=" + Strings
          .convert2ScaleDecimalStr(selAvg, 2) + ", earn=" + Strings
          .convert2ScaleDecimalStr(earn, 2));
      favoritesFilterTable.delete(stockId, "AI_Earn");
      favoritesFilterTable.insert(stockId, "AI_Earn");
    }
  }

  public void processAll() {
    for (String stockId : stockConfig.getAllStockId()) {
      this.analyze(stockId);
    }
  }
}
