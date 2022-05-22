package org.easystogu.analyse;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Doubles;
import org.easystogu.analyse.util.ProcessRequestParmsInPostBody;
import org.easystogu.analyse.vo.ShenXianUIVO;
import org.easystogu.db.vo.table.BBIVO;
import org.easystogu.db.vo.table.LuZaoVO;
import org.easystogu.db.vo.table.MacdVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.indicator.*;
import org.easystogu.indicator.runner.utils.StockPriceFetcher;
import org.easystogu.utils.Strings;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShenXianSellAnalyseHelper {
    protected MACDHelper macdHelper = new MACDHelper();
    protected ShenXianHelper shenXianHelper = new ShenXianHelper();
    protected BBIHelper bbiHelper = new BBIHelper();
    protected LuZaoHelper luzaoHelper = new LuZaoHelper();
    @Autowired
    protected ProcessRequestParmsInPostBody postParmsProcess;
    @Autowired
    FlagsAnalyseHelper flagsAnalyseHelper;

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
}
