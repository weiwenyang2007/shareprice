package org.easystogu.analyse;

import static org.easystogu.checkpoint.DailyCombineCheckPoint.AiTrend_Bottom_Area;
import static org.easystogu.checkpoint.DailyCombineCheckPoint.AiTrend_Top_Area;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easystogu.checkpoint.DailyCombineCheckPoint;
import org.easystogu.db.access.table.AiTrendPredictTableHelper;
import org.easystogu.db.access.table.CheckPointDailySelectionTableHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.CheckPointDailySelectionVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.utils.Strings;

//analyse whether ai trend predicate is better for each stockId
public class AiTrendBuySellPointAnalyse {
  private AiTrendPredictTableHelper aiTrendPredictTableHelper = AiTrendPredictTableHelper.getInstance();
  private CheckPointDailySelectionTableHelper checkPointDailySelectionTable = CheckPointDailySelectionTableHelper
      .getInstance();
  private StockPriceTableHelper stockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
  protected CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();

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
    double earn =  (selAvg/buyAvg);
    if(buyCount >= 30 && earn >= 1.20) {
      System.out.println("stockId " + stockId + ", buyCount=" + buyCount + ", buyAvg=" + Strings
          .convert2ScaleDecimalStr(buyAvg, 2) + ", selCount=" + selCount + ", selAvg=" + Strings
          .convert2ScaleDecimalStr(selAvg, 2) + ", earn=" + Strings
          .convert2ScaleDecimalStr(earn, 2));
    }
  }

  public void processAll() {
    for (String stockId : stockConfig.getAllStockId()) {
      this.analyze(stockId);
    }
  }
}
