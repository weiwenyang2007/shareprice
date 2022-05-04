package org.easystogu.ai.tf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.easystogu.config.Constants;
import org.easystogu.db.access.facde.DBAccessFacdeFactory;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.helper.IF.IndicatorDBHelperIF;
import org.easystogu.db.vo.table.KDJVO;
import org.easystogu.db.vo.table.MacdVO;
import org.easystogu.db.vo.table.QSDDVO;
import org.easystogu.db.vo.table.ShenXianVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.db.vo.table.WRVO;
import org.easystogu.file.CSVFileHelper;

// pls refer to:
// https://github.com/lyshello123/stock_predict_with_LSTM/blob/master/stock_predict_2.py
// python script is: TF_LSTM_Predict_StockPrice.py
public class LstmPredictStockPriceDataPrepare {
  private static QianFuQuanStockPriceTableHelper qianFuQuanStockPriceTableHelper =
      QianFuQuanStockPriceTableHelper.getInstance();
  protected static IndicatorDBHelperIF shenXianTable =
      DBAccessFacdeFactory.getInstance(Constants.indShenXian);
  protected static IndicatorDBHelperIF macdTable =
      DBAccessFacdeFactory.getInstance(Constants.indMacd);
  protected static IndicatorDBHelperIF kdjTable =
      DBAccessFacdeFactory.getInstance(Constants.indKDJ);
  protected static IndicatorDBHelperIF qsddTable =
      DBAccessFacdeFactory.getInstance(Constants.indQSDD);
  protected static IndicatorDBHelperIF wrTable = DBAccessFacdeFactory.getInstance(Constants.indWR);
  protected static Map<String, Double> minMap = new HashMap<String,Double>();
  protected static Map<String, Double> maxMap = new HashMap<String,Double>();

  private static String filePath = "C:/Users/eyaweiw/github/shareprice/AI/mytest/exampleData/";
  private static String csvHeader = "date,open,close,low,high,volume,change,h1,h2,h3,dea,dif,macd,k,d,j,qsddS,qsddM,qsddL,wrS,wrM,wrL,label1";

  private static void prepareStockSimpleDateForLstm(String stockId) {
    String startDate= "2018-01-01";
    String endDate= "2022-05-01";
    List<StockPriceVO> spList = qianFuQuanStockPriceTableHelper.getStockPriceByIdAndBetweenDate(stockId, startDate, endDate);
    List<ShenXianVO> sxList = shenXianTable.getByIdAndBetweenDate(stockId, startDate, endDate);
    List<MacdVO> macdList = macdTable.getByIdAndBetweenDate(stockId, startDate, endDate);
    List<KDJVO> kdjList = kdjTable.getByIdAndBetweenDate(stockId, startDate, endDate);
    List<QSDDVO> qsddList = qsddTable.getByIdAndBetweenDate(stockId, startDate, endDate);
    List<WRVO> wrList = wrTable.getByIdAndBetweenDate(stockId, startDate, endDate);

    //count nextClose
    for (int index = 0; index < spList.size() - 1; index++) {
      StockPriceVO currVo = spList.get(index);
      StockPriceVO nextVo = spList.get(index + 1);
      currVo.nextClose = nextVo.close;
      currVo.nextHigh = nextVo.high;
      currVo.nextLow = nextVo.low;
    }
    //count the min-max value
    double[][] array = new double[21][spList.size()];//21 is the total number of indicator
    for (int index = 0; index < spList.size() - 1; index++) {
      array[0][index] = spList.get(index).open;
      array[1][index] = spList.get(index).high;
      array[2][index] = spList.get(index).close;
      array[3][index] = spList.get(index).low;
      array[4][index] = spList.get(index).volume;
      array[5][index] = (spList.get(index).close - spList.get(index).lastClose)/spList.get(index).lastClose;//change
    }
    for (int index = 0; index < spList.size() - 1; index++) {
      array[6][index] = sxList.get(index).h1;
      array[7][index] = sxList.get(index).h2;
      array[8][index] = sxList.get(index).h3;
    }
    for (int index = 0; index < spList.size() - 1; index++) {
      array[9][index] = macdList.get(index).dea;
      array[10][index] = macdList.get(index).dif;
      array[11][index] = macdList.get(index).macd;
    }
    for (int index = 0; index < spList.size() - 1; index++) {
      array[12][index] = kdjList.get(index).k;
      array[13][index] = kdjList.get(index).d;
      array[14][index] = kdjList.get(index).j;
    }
    for (int index = 0; index < spList.size() - 1; index++) {
      array[15][index] = qsddList.get(index).shoTerm;
      array[16][index] = qsddList.get(index).midTerm;
      array[17][index] = qsddList.get(index).lonTerm;
    }
    for (int index = 0; index < spList.size() - 1; index++) {
      array[18][index] = wrList.get(index).shoTerm;
      array[19][index] = wrList.get(index).midTerm;
      array[20][index] = wrList.get(index).lonTerm;
    }

    countMinMaxMap("open", array, 0);
    countMinMaxMap("high", array, 1);
    countMinMaxMap("close", array, 2);
    countMinMaxMap("low", array, 3);
    countMinMaxMap("volume", array, 4);
    countMinMaxMap("change", array, 5);
    countMinMaxMap("h1", array, 6);
    countMinMaxMap("h2", array, 7);
    countMinMaxMap("h3", array, 8);
    countMinMaxMap("dea", array, 9);
    countMinMaxMap("dif", array, 10);
    countMinMaxMap("macd", array, 11);
    countMinMaxMap("k", array, 12);
    countMinMaxMap("d", array, 13);
    countMinMaxMap("j", array, 14);
    countMinMaxMap("qsddS", array, 15);
    countMinMaxMap("qsddM", array, 16);
    countMinMaxMap("qsddL", array, 17);
    countMinMaxMap("wrS", array, 18);
    countMinMaxMap("wrM", array, 19);
    countMinMaxMap("wrL", array, 20);

    //write to csv
    List<String[]> contents = new ArrayList();
    for (int index = 0; index < spList.size(); index++) {
      if(!spList.get(index).date.equals(sxList.get(index).date)
      ||!spList.get(index).date.equals(macdList.get(index).date)
      ||!spList.get(index).date.equals(kdjList.get(index).date)
      ||!spList.get(index).date.equals(qsddList.get(index).date)
      ||!spList.get(index).date.equals(wrList.get(index).date)){
        System.err.print("date is not equals, pls run Sanity first");
        System.exit(1);
      }
      contents.add(toCsvPredictString(spList.get(index), sxList.get(index), macdList.get(index),
          kdjList.get(index), qsddList.get(index), wrList.get(index)).split(","));
    }

    // write to csv file
    String fileName = filePath + stockId + "_stockPrice.csv";
    CSVFileHelper.write(fileName, csvHeader.split(","), contents);
  }

  private static void countMinMaxMap(String key, double[][] array, int index) {
    minMap.put(key, Arrays.stream(array[index]).min().getAsDouble());
    maxMap.put(key, Arrays.stream(array[index]).max().getAsDouble());
  }

  public static String toCsvPredictString(StockPriceVO spvo, ShenXianVO sxvo, MacdVO macdvo,
      KDJVO kdjvo, QSDDVO qsddvo, WRVO wrvo) {
    StringBuffer sb = new StringBuffer();
    sb.append(spvo.date);
    //input: (value - minValue)/(maxValue - minValue)
    sb.append("," + (spvo.open - minMap.get("open"))/(maxMap.get("open") - minMap.get("open")));
    sb.append("," + (spvo.close - minMap.get("close"))/(maxMap.get("close") - minMap.get("close")));
    sb.append("," + (spvo.low - minMap.get("low"))/(maxMap.get("low") - minMap.get("low")));
    sb.append("," + (spvo.high - minMap.get("high"))/(maxMap.get("high") - minMap.get("high")));
    sb.append("," + (spvo.volume - minMap.get("volume"))/(maxMap.get("volume") - minMap.get("volume")));
    double change = (spvo.close - spvo.lastClose) / spvo.lastClose;
    sb.append("," + (change - minMap.get("change"))/(maxMap.get("change") - minMap.get("change")));
    
    sb.append("," + (sxvo.h1 - minMap.get("h1"))/(maxMap.get("h1") - minMap.get("h1")));
    sb.append("," + (sxvo.h2 - minMap.get("h2"))/(maxMap.get("h2") - minMap.get("h2")));
    sb.append("," + (sxvo.h3 - minMap.get("h3"))/(maxMap.get("h3") - minMap.get("h3")));
    
    sb.append("," + (macdvo.dea - minMap.get("dea"))/(maxMap.get("dea") - minMap.get("dea")));
    sb.append("," + (macdvo.dif - minMap.get("dif"))/(maxMap.get("dif") - minMap.get("dif")));
    sb.append("," + (macdvo.macd - minMap.get("macd"))/(maxMap.get("macd") - minMap.get("macd")));
    
    sb.append("," + (kdjvo.k - minMap.get("k"))/(maxMap.get("k") - minMap.get("k")));
    sb.append("," + (kdjvo.d - minMap.get("d"))/(maxMap.get("d") - minMap.get("d")));
    sb.append("," + (kdjvo.j - minMap.get("j"))/(maxMap.get("j") - minMap.get("j")));
    
    sb.append("," + (qsddvo.shoTerm - minMap.get("qsddS"))/(maxMap.get("qsddS") - minMap.get("qsddS")));
    sb.append("," + (qsddvo.midTerm - minMap.get("qsddM"))/(maxMap.get("qsddM") - minMap.get("qsddM")));
    sb.append("," + (qsddvo.lonTerm - minMap.get("qsddL"))/(maxMap.get("qsddL") - minMap.get("qsddL")));
    
    sb.append("," + (wrvo.shoTerm - minMap.get("wrS"))/(maxMap.get("wrS") - minMap.get("wrS")));
    sb.append("," + (wrvo.midTerm - minMap.get("wrM"))/(maxMap.get("wrM") - minMap.get("wrM")));
    sb.append("," + (wrvo.lonTerm - minMap.get("wrL"))/(maxMap.get("wrL") - minMap.get("wrL")));
    
    //output
    sb.append("," + spvo.nextClose);// label1
    // sb.append("," + vo.nextHigh);// label2
    return sb.toString();
  }

  public static void main(String[] args) {
    prepareStockSimpleDateForLstm("600036");
  }

}

