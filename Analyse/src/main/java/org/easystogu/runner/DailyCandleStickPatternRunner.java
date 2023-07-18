package org.easystogu.runner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.easystogu.db.access.table.CandleStickPatternTableHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.CandleStickPatternVO;
import org.easystogu.db.vo.table.StockPriceVO;

public class DailyCandleStickPatternRunner implements Runnable{
  private StockPriceTableHelper stockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
  private CandleStickPatternTableHelper candlePatternTable = CandleStickPatternTableHelper.getInstance();
  protected AtomicInteger counter = new AtomicInteger();
  public static final List<String> patterns_up =
      Arrays.asList(new String[]{"morningStar","shootingStarBullish","bullishHarami","bullishEngulfing","bullishReversal","piercingLineBullish","hangingManBullish","3WhiteSoldiers","mathold","onneck","piercing"});
  public static final List<String> patterns_down =
      Arrays.asList(new String[]{"eveningStar","shootingStarBearish","bearishHarami","bearishEngulfing","bearishReversal","hangingManBearish","darkCloudCover","3BlackCrows"});

  @Override
  public void run() {
    List<String> stockIds = stockPriceTable.getDistinctStockIDs();
    stockIds.parallelStream().forEach(stockId -> {
      List<StockPriceVO> spList = stockPriceTable.getStockPriceById(stockId);//order by date
      if (spList.size() >= 3) {
        StockPriceVO curVO = spList.get(spList.size() - 1);//current day
        StockPriceVO pre1VO = spList.get(spList.size() - 2);//yesterday
        StockPriceVO pre2VO = spList.get(spList.size() - 3);//before yesterday
        //CandleStickPattern checking:
        //refer to shareprice/AI/pattern_detect_all_stocks.py
        //https://www.ig.com/en/trading-strategies/16-candlestick-patterns-every-trader-should-know-180615
        //https://blog.elearnmarkets.com/35-candlestick-patterns-in-stock-market/
        double O_0,H_0,L_0,C_0=0.0;
        double O_1,H_1,L_1,C_1=0.0;
        double O_2,H_2,L_2,C_2=0.0;

        O_0=curVO.open;H_0=curVO.high;L_0=curVO.low;C_0=curVO.close;
        O_1=pre1VO.open;H_1=pre1VO.high;L_1=pre1VO.low;C_1=pre1VO.close;
        O_2=pre2VO.open;H_2=pre2VO.high;L_2=pre2VO.low;C_2=pre2VO.close;

        double DojiSize = 0.1;

        boolean doji=(Math.abs(O_0 - C_0) <= (H_0 - L_0) * DojiSize);

        boolean hammer=(((H_0 - L_0)>3*(O_0 -C_0)) &&  ((C_0 - L_0)/(.001 + H_0 - L_0) > 0.6) && ((O_0 - L_0)/(.001 + H_0 - L_0) > 0.6));

        boolean invertedHammer=(((H_0 - L_0)>3*(O_0 -C_0)) &&  ((H_0 - C_0)/(.001 + H_0 - L_0) > 0.6) && ((H_0 - O_0)/(.001 + H_0 - L_0) > 0.6));

        boolean bullishReversal= (O_2 > C_2)&&(O_1 > C_1)&&doji;

        boolean bearishReversal= (O_2 < C_2)&&(O_1 < C_1)&&doji;

        boolean eveningStar=(C_2 > O_2) && (Math.min(O_1, C_1) > C_2) && (O_0 < Math.min(O_1, C_1)) && (C_0 < O_0 );

        boolean morningStar=(C_2 < O_2) && (Math.min(O_1, C_1) < C_2) && (O_0 > Math.min(O_1, C_1)) && (C_0 > O_0 );

        boolean shootingStarBearish=(O_1 < C_1) && (O_0 > C_1) && ((H_0 - Math.max(O_0, C_0)) >= Math.abs(O_0 - C_0) * 3) && ((Math.min(C_0, O_0) - L_0 )<= Math.abs(O_0 - C_0)) && invertedHammer;

        boolean shootingStarBullish=(O_1 > C_1) && (O_0 < C_1) && ((H_0 - Math.max(O_0, C_0)) >= Math.abs(O_0 - C_0) * 3) && ((Math.min(C_0, O_0) - L_0 )<= Math.abs(O_0 - C_0)) && invertedHammer;

        boolean bearishHarami=(C_1 > O_1) && (O_0 > C_0) && (O_0 <= C_1) && (O_1 <= C_0) && ((O_0 - C_0) < (C_1 - O_1 ));

        boolean bullishHarami=(O_1 > C_1) && (C_0 > O_0) && (C_0 <= O_1) && (C_1 <= O_0) && ((C_0 - O_0) < (O_1 - C_1));

        boolean bearishEngulfing=((C_1 > O_1) && (O_0 > C_0)) && ((O_0 >= C_1) && (O_1 >= C_0)) && ((O_0 - C_0) > (C_1 - O_1 ));

        boolean bullishEngulfing=(O_1 > C_1) && (C_0 > O_0) && (C_0 >= O_1) && (C_1 >= O_0) && ((C_0 - O_0) > (O_1 - C_1 ));

        boolean piercingLineBullish=(C_1 < O_1) && (C_0 > O_0) && (O_0 < L_1) && (C_0 > C_1)&& (C_0>((O_1 + C_1)/2)) && (C_0 < O_1);

        boolean hangingManBullish=(C_1 < O_1) && (O_0 < L_1) && (C_0>((O_1 + C_1)/2)) && (C_0 < O_1) && hammer;

        boolean hangingManBearish=(C_1 > O_1) && (C_0>((O_1 + C_1)/2)) && (C_0 < O_1) && hammer;

        boolean darkCloudCover=((C_1 > O_1) && (((C_1 + O_1) / 2) > C_0) && (O_0 > C_0) && (O_0 > C_1) && (C_0 > O_1) && ((O_0 - C_0) / (.001 + (H_0 - L_0)) > 0.6));

        String strCandle="";

        if(doji)
          strCandle="doji" + ",";
        if(eveningStar)
          strCandle=strCandle + "eveningStar" + ",";
        if(morningStar)
          strCandle=strCandle + "morningStar" + ",";
        if(shootingStarBearish)
          strCandle=strCandle + "shootingStarBearish" + ",";
        if(shootingStarBullish)
          strCandle=strCandle + "shootingStarBullish" + ",";
        if(hammer)
          strCandle=strCandle + "hammer" + ",";
        if(invertedHammer)
          strCandle=strCandle + "invertedHammer" + ",";
        if(bearishHarami)
          strCandle=strCandle + "bearishHarami" + ",";
        if(bullishHarami)
          strCandle=strCandle + "bullishHarami" + ",";
        if(bearishEngulfing)
          strCandle=strCandle + "bearishEngulfing" + ",";
        if(bullishEngulfing)
          strCandle=strCandle + "bullishEngulfing" + ",";
        if(bullishReversal)
          strCandle=strCandle + "bullishReversal" + ",";
        if(bearishReversal)
          strCandle=strCandle + "bearishReversal" + ",";
        if(piercingLineBullish)
          strCandle=strCandle + "piercingLineBullish" + ",";
        if(hangingManBearish)
          strCandle=strCandle + "hangingManBearish" + ",";
        if(hangingManBullish)
          strCandle=strCandle + "hangingManBullish" + ",";
        if(darkCloudCover)
          strCandle=strCandle + "darkCloudCover" + ",";

        if(strCandle.trim().length() > 0) {
          strCandle = strCandle.substring(0, strCandle.length() - 1);//trim the last comma

          int score = patternsScore(strCandle);
          //System.out.println("stockId="+ stockId + ", date="+curVO.date + ", score=" + score + ", p=" + strCandle);
          CandleStickPatternVO cvo = new CandleStickPatternVO();
          cvo.setStockId(stockId);
          cvo.setPattern(strCandle);
          cvo.setDate(curVO.date);
          cvo.setScore(score);
          //cvo.setScoreRoll();//how to???
          candlePatternTable.delete(stockId, curVO.date);
          candlePatternTable.insert(cvo);
        }
      }

      int current = this.counter.incrementAndGet();
      if (current %50 == 0) {
        System.out.println("candleStickPattern complete:" + current + "/" + stockIds.size());
      }
    });
  }

  //pattern is a single name of pattern, such as darkCloudCover
  private int patternScore(String pattern) {
    if (patterns_up.contains(pattern))
      return 1;
    if (patterns_down.contains(pattern))
      return -1;
    return 0;
  }

  //patterns is a list of strings of pattern, separated by commas, for example: darkCloudCover,hangingManBearish
  private int patternsScore(String patterns) {
    Set<String> patternSet = new HashSet<String>(Arrays.asList(patterns.split(",")));
    Iterator<String> it = patternSet.iterator();
    int score = 0;
    while(it.hasNext()) {
      score += patternScore(it.next());
    }
    return score;
  }

  public static void main(String[] args) {
    DailyCandleStickPatternRunner runner = new DailyCandleStickPatternRunner();
    runner.run();
  }
}
