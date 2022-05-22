package org.easystogu.analyse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.primitives.Doubles;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.easystogu.analyse.util.ProcessRequestParmsInPostBody;
import org.easystogu.analyse.vo.ShenXianUIVO;
import org.easystogu.checkpoint.DailyCombineCheckPoint;
import org.easystogu.db.access.table.CheckPointDailySelectionTableHelper;
import org.easystogu.db.vo.table.*;
import org.easystogu.indicator.*;
import org.easystogu.indicator.runner.utils.StockPriceFetcher;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class ShenXianSellAnalyseHelper {
    protected MACDHelper macdHelper = new MACDHelper();
    protected ShenXianHelper shenXianHelper = new ShenXianHelper();
    protected BBIHelper bbiHelper = new BBIHelper();
    protected LuZaoHelper luzaoHelper = new LuZaoHelper();
    //@Autowired
    protected ProcessRequestParmsInPostBody postParmsProcess = ProcessRequestParmsInPostBody.getInstance();

    private CheckPointDailySelectionTableHelper checkPointDailySelectionTable = CheckPointDailySelectionTableHelper
            .getInstance();
    //@Autowired
    private FlagsAnalyseHelper flagsAnalyseHelper = FlagsAnalyseHelper.getInstance();

    private static final String LUZAO_KEY1 = "ZHENGCHUDONGFANG";//震出东方
    private static final String LUZAO_KEY2 = "SHENGYUELIANGSHAN";//升越良山
    private static final String LUZAO_KEY3 = "SHANYAOCHENGLIANG";//山腰乘凉

    //选择最近发生重要事件，比如底背离和金叉的个股. 参考index.htm的鲁兆-金叉和W底-金叉
    private static String[] checkPoints = {
            //鲁兆-金叉
            "LuZao_KDJ_Gordon_TiaoKongGaoKai",
            "LuZao_PhaseII_MACD_WEEK_GORDON_MACD_DAY_DIF_CROSS_0",
            "LuZao_PhaseII_MACD_WEEK_GORDON_KDJ_WEEK_GORDON",
            "LuZao_PhaseIII_MACD_WEEK_GORDON_KDJ_WEEK_GORDON",
            "LuZao_PhaseIII_MACD_WEEK_GORDON_MACD_DAY_DIF_CROSS_0",
            //W底-金叉
            "MACD_TWICE_GORDON_W_Botton_TiaoKong_ZhanShang_Bull",
            "MACD_TWICE_GORDON_W_Botton_MACD_DI_BEILI"};
    //最终有上述特征的股票经过未来2天的预计算，得出 checkpoint 为 LuZao_PhaseIII_ShanYaoChengLiang_In_Future_2_Days

    //预估后面2天每天涨2个点，计算出是否出现luzao山腰乘凉买点
    private static String postBody = "{\"trendModeName\":\"Zhang2GeDian\",\"nDays\":\"1\",\"repeatTimes\":\"2\"}";
    private static JSONObject jsonParm = null;

    static {
        try {
            if (Strings.isNotEmpty(postBody)) {
                jsonParm = new JSONObject(postBody);
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    //just for legacy using, instead of using @Component
    private static ShenXianSellAnalyseHelper instance = null;
    private ShenXianSellAnalyseHelper(){

    }
    public static ShenXianSellAnalyseHelper getInstance(){
        if(instance == null){
            instance = new ShenXianSellAnalyseHelper();
        }
        return instance;
    }

    //stockIdParm is like: 603999
    //dateParm is like: 2016-11-29_2022-05-22
    //postBody is like: {"trendModeName":"Zhang2GeDian","nDays":"1","repeatTimes":"2"}
    public List<ShenXianUIVO> queryShenXianSellById(String stockIdParm, String dateParm, JSONObject jsonParm) {
        List<ShenXianUIVO> sxList = new ArrayList<ShenXianUIVO>();
        List<MacdVO> macdList = new ArrayList<MacdVO>();
        List<BBIVO> bbiList = new ArrayList<BBIVO>();
        List<LuZaoVO> luzaoList = new ArrayList<LuZaoVO>();

        List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);
        List<Double> close = StockPriceFetcher.getClosePrice(spList);
        List<Double> high = StockPriceFetcher.getHighPrice(spList);
        List<Double> low = StockPriceFetcher.getLowPrice(spList);

        // shenxian
        double[][] shenXian = shenXianHelper.getShenXianSellPointList(Doubles.toArray(close), Doubles.toArray(high),
                Doubles.toArray(low));
        for (int i = 0; i < shenXian[0].length; i++) {
            if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
                ShenXianUIVO vo = new ShenXianUIVO();
                vo.setH1(Strings.convert2ScaleDecimal(shenXian[0][i]));
                vo.setH2(Strings.convert2ScaleDecimal(shenXian[1][i]));
                vo.setHc5(Strings.convert2ScaleDecimal(shenXian[2][i]));
                vo.setHc6(Strings.convert2ScaleDecimal(shenXian[3][i]));
                vo.setStockId(stockIdParm);
                vo.setDate(spList.get(i).date);
                sxList.add(vo);
            }
        }

        // macd
        double[][] macd = macdHelper.getMACDList(Doubles.toArray(close));
        for (int i = 0; i < macd[0].length; i++) {
            if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
                MacdVO vo = new MacdVO();
                vo.setDif(macd[0][i]);
                vo.setDea(macd[1][i]);
                vo.setMacd(macd[2][i]);
                vo.setStockId(stockIdParm);
                vo.setDate(spList.get(i).date);
                macdList.add(vo);
            }
        }

        // bbi
        double[][] bbi = bbiHelper.getBBIList(Doubles.toArray(close));
        for (int i = 0; i < bbi[0].length; i++) {
            if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
                BBIVO vo = new BBIVO();
                vo.setBbi(bbi[0][i]);
                vo.setClose(bbi[1][i]);
                vo.setStockId(stockIdParm);
                vo.setDate(spList.get(i).date);
                bbiList.add(vo);
            }
        }

        // luzhao
        double[][] lz = luzaoHelper.getLuZaoList(Doubles.toArray(close));
        for (int i = 0; i < lz[0].length; i++) {
            if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
                LuZaoVO vo = new LuZaoVO();
                vo.setMa19(lz[0][i]);
                vo.setMa43(lz[1][i]);
                vo.setMa86(lz[2][i]);
                vo.setStockId(stockIdParm);
                vo.setDate(spList.get(i).date);
                luzaoList.add(vo);
            }
        }
        //
        return flagsAnalyseHelper.shenXianBuySellFlagsAnalyse(spList, sxList, macdList, bbiList, luzaoList);
    }

    public void analyseWithPredictStockPrice() {
        //选择最近发生重要事件，比如底背离和金叉的个股. 参考index.htm的鲁兆-金叉和W底-金叉
        //选择交易日期至少是120天前有数据的
        String curDate = WeekdayUtil.currentDate();
        String startDate = WeekdayUtil.nextNDateString(curDate, -120);

        //最近120天内，有重要事件发生的个股
        List<CheckPointDailySelectionVO> checkPointList = this.checkPointDailySelectionTable.getRecentDaysCheckPoint(startDate);
        List<String> filterStockIdsByCP = checkPointList.stream()
                .filter(cp ->  isSelectedCheckPoint(cp.getCheckPoint()))
                .map(cp -> cp.getStockId())
                .collect(Collectors.toList());


        //filter out the duplicated stockIds
        List<String> selectStockIds = filterStockIdsByCP.stream().distinct().collect(Collectors.toList());

        //run for each stockId
        selectStockIds.parallelStream().forEach(stockId
                -> {
            System.out.println("analyseWithPredictStockPrice process "+ stockId);
            List<ShenXianUIVO> shenXianUIVOList = queryShenXianSellById(stockId, startDate+"_"+curDate, jsonParm);
            //flag, date
            Map<String, String> flagMap = new HashMap<>();

            shenXianUIVOList.stream().forEach(svo -> {
                if(svo.getDuoFlagsText().contains("震出东方")){
                    flagMap.put(LUZAO_KEY1, svo.getDate());
                }
                if(svo.getDuoFlagsText().contains("升越良山")){
                    flagMap.put(LUZAO_KEY2, svo.getDate());
                }
                if(svo.getDuoFlagsText().contains("山腰乘凉")){
                    flagMap.put(LUZAO_KEY3, svo.getDate());
                }
            });

            if(flagMap.containsKey(LUZAO_KEY1) && flagMap.containsKey(LUZAO_KEY2) && flagMap.containsKey(LUZAO_KEY3)
                    && WeekdayUtil.isDate1BeforeDate2(flagMap.get(LUZAO_KEY1), flagMap.get(LUZAO_KEY2))
                    && WeekdayUtil.isDate1BeforeDate2(flagMap.get(LUZAO_KEY2), flagMap.get(LUZAO_KEY3))
                    && WeekdayUtil.isDate1BeforeOrEqualDate2(curDate, flagMap.get(LUZAO_KEY3))){
                System.out.println("analyseWithPredictStockPrice process result: " + stockId + " ,event Map:"+flagMap);
                CheckPointDailySelectionVO cpvo = new CheckPointDailySelectionVO();
                cpvo.stockId = stockId;
                cpvo.checkPoint = DailyCombineCheckPoint.LuZao_PhaseIII_ShanYaoChengLiang_In_Future_2_Days.name();
                cpvo.date = curDate;
                checkPointDailySelectionTable.delete(cpvo);
                checkPointDailySelectionTable.insert(cpvo);
            }
        });
    }

    private boolean isSelectedCheckPoint(String checkPoint) {
        for(String cp: checkPoints){
            if(cp.equalsIgnoreCase(checkPoint)){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args){
        ShenXianSellAnalyseHelper ins = new ShenXianSellAnalyseHelper();
        ins.analyseWithPredictStockPrice();
    }
}
