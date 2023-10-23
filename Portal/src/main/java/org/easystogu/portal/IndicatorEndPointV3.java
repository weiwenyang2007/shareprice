package org.easystogu.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.easystogu.analyse.FlagsAnalyseHelper;
import org.easystogu.analyse.ShenXianSellAnalyseHelper;
import org.easystogu.analyse.util.ProcessRequestParmsInPostBody;
import org.easystogu.db.vo.table.BollVO;
import org.easystogu.db.vo.table.KDJVO;
import org.easystogu.db.vo.table.LuZaoVO;
import org.easystogu.db.vo.table.MacdVO;
import org.easystogu.db.vo.table.QSDDVO;
import org.easystogu.db.vo.table.ShenXianVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.db.vo.table.WRVO;
import org.easystogu.indicator.BBIHelper;
import org.easystogu.indicator.BOLLHelper;
import org.easystogu.indicator.KDJHelper;
import org.easystogu.indicator.LuZaoHelper;
import org.easystogu.indicator.MACDHelper;
import org.easystogu.indicator.QSDDHelper;
import org.easystogu.indicator.ShenXianHelper;
import org.easystogu.indicator.WRHelper;
import org.easystogu.indicator.runner.utils.StockPriceFetcher;
import org.easystogu.trendmode.TrendModeLoader;
import org.easystogu.analyse.vo.ShenXianUIVO;
import org.easystogu.utils.Strings;
import org.json.JSONObject;
import org.easystogu.cache.ConfigurationServiceCache;
import com.google.common.primitives.Doubles;
import com.google.gson.Gson;

//V3, with forecast data, query from qian fuquan stock price and count in real time
public class IndicatorEndPointV3 {
	private ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
	protected String accessControlAllowOrgin = config.getString("Access-Control-Allow-Origin", "");
	protected static String HHmmss = "00:00:00";
	protected MACDHelper macdHelper = new MACDHelper();
	protected KDJHelper kdjHelper = new KDJHelper();
	protected ShenXianHelper shenXianHelper = new ShenXianHelper();
	protected QSDDHelper qsddHelper = new QSDDHelper();
	protected WRHelper wrHelper = new WRHelper();
	protected BOLLHelper bollHelper = new BOLLHelper();
	protected BBIHelper bbiHelper = new BBIHelper();
	protected LuZaoHelper luzaoHelper = new LuZaoHelper();
	protected ProcessRequestParmsInPostBody postParmsProcess = ProcessRequestParmsInPostBody.getInstance();
	protected TrendModeLoader trendModeLoader = TrendModeLoader.getInstance();
	FlagsAnalyseHelper flagsAnalyseHelper = FlagsAnalyseHelper.getInstance();
	ShenXianSellAnalyseHelper shenXianSellAnalyseHelper = ShenXianSellAnalyseHelper.getInstance();
	
	private Gson gson = new Gson();

	@POST
	@Path("/macd/{stockId}/{date}")
	@Produces("application/json")
	public String queryMACDById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<MacdVO> list = new ArrayList<MacdVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		double[][] macd = macdHelper.getMACDList(Doubles.toArray(close));
		for (int i = 0; i < macd[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				MacdVO vo = new MacdVO();
				vo.setDif(Strings.convert2ScaleDecimal(macd[0][i]));
				vo.setDea(Strings.convert2ScaleDecimal(macd[1][i]));
				vo.setMacd(Strings.convert2ScaleDecimal(macd[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}
		return gson.toJson(list);
	}

	@POST
	@Path("/kdj/{stockId}/{date}")
	@Produces("application/json")
	public String queryKDJById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<KDJVO> list = new ArrayList<KDJVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		List<Double> low = StockPriceFetcher.getLowPrice(spList);
		List<Double> high = StockPriceFetcher.getHighPrice(spList);
		double[][] kdj = kdjHelper.getKDJList(Doubles.toArray(close), Doubles.toArray(low), Doubles.toArray(high));
		for (int i = 0; i < kdj[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				KDJVO vo = new KDJVO();
				vo.setK(Strings.convert2ScaleDecimal(kdj[0][i]));
				vo.setD(Strings.convert2ScaleDecimal(kdj[1][i]));
				vo.setJ(Strings.convert2ScaleDecimal(kdj[2][i]));
				vo.setRsv(Strings.convert2ScaleDecimal(kdj[3][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}
		return gson.toJson(list);
	}

	@POST
	@Path("/boll/{stockId}/{date}")
	@Produces("application/json")
	public String queryBollById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<BollVO> list = new ArrayList<BollVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		double[][] boll = bollHelper.getBOLLList(Doubles.toArray(close), 20, 2.0, 2.0);
		for (int i = 0; i < boll[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				BollVO vo = new BollVO();
				vo.setUp(Strings.convert2ScaleDecimal(boll[0][i]));
				vo.setMb(Strings.convert2ScaleDecimal(boll[1][i]));
				vo.setDn(Strings.convert2ScaleDecimal(boll[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}

		return gson.toJson(list);
	}

	@POST
	@Path("/shenxian/{stockId}/{date}")
	@Produces("application/json")
	public String queryShenXianById(@PathParam("stockId") String stockIdParm,
			@PathParam("date") String dateParm, String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<ShenXianVO> list = new ArrayList<ShenXianVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		double[][] shenXian = shenXianHelper.getShenXianList(Doubles.toArray(close));
		for (int i = 0; i < shenXian[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				ShenXianVO vo = new ShenXianVO();
				vo.setH1(Strings.convert2ScaleDecimal(shenXian[0][i]));
				vo.setH2(Strings.convert2ScaleDecimal(shenXian[1][i]));
				vo.setH3(Strings.convert2ScaleDecimal(shenXian[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}

		return gson.toJson(list);
	}

	@POST
	@Path("/shenxianSell/{stockId}/{date}")
	@Produces("application/json")
	public String queryShenXianSellById(@PathParam("stockId") String stockIdParm,
			@PathParam("date") String dateParm, String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		//stockIdParm is like: 603999
		//dateParm is like: 2016-11-29_2022-05-22
		//postBody is like: {"trendModeName":"Zhang2GeDian","nDays":"1","repeatTimes":"2"}
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<ShenXianUIVO> rtnList = shenXianSellAnalyseHelper.queryShenXianSellById(stockIdParm, dateParm, jsonParm);
		return gson.toJson(rtnList);
	}

	@POST
	@Path("/predictTodayBuySell/{stockId}/{date}")
	@Produces("application/json")
	public String mockCurPriceAndPredictTodayBSInd(@PathParam("stockId") String stockIdParm,
			@PathParam("date") String dateParm, String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		//stockIdParm is like: 603999
		//dateParm is like: 2016-11-29_2022-05-22
		//postBody is like: {"mockCurPriceAndPredictTodayBSInd":"0.02"}

		Map<String, ShenXianUIVO> buySellMap = new HashMap<String, ShenXianUIVO>();
		boolean buyPointFind = false;
		boolean sellPointFind = false;

		String bodyTemplate = "{'mockCurPriceAndPredictTodayBSInd':'changeTmpl'}";
		String[] percent = {"0.0", "0.010", "-0.010", "0.015", "-0.015","0.020", "-0.020", "0.025", "-0.025","0.030", "-0.030", "0.035", "-0.035","0.040", "-0.040", "0.045", "-0.045", "0.050", "-0.050"};
		//loop from the minimum price change to a larger change, only return the first occurrence
		for(int i=0; i < percent.length; i++){
					String change = percent[i];
					String realPostBody = bodyTemplate.replaceFirst("changeTmpl",change);
					JSONObject jsonParm = null;
					try {
							jsonParm = new JSONObject(realPostBody);
					}catch(org.json.JSONException e){
						e.printStackTrace();
					}
					List<ShenXianUIVO> rtnList = shenXianSellAnalyseHelper.queryShenXianSellById(stockIdParm, dateParm, jsonParm);
					ShenXianUIVO curVo = rtnList.get(rtnList.size() - 1);
					//System.out.println("change="+change + ", curVo="+curVo.toString());
					if(!buyPointFind && (curVo.sellFlagsTitle.contains("B") || curVo.buyFlagsTitle.contains("B"))){
						buyPointFind = true;
						buySellMap.put("Buy@"+change,  curVo);
					}
					if(!sellPointFind && (curVo.sellFlagsTitle.contains("S") || curVo.buyFlagsTitle.contains("S"))){
						sellPointFind = true;
						buySellMap.put("Sell@"+change,  curVo);
					}
					if(buyPointFind && sellPointFind){
						break;
					}
		}
		return gson.toJson(buySellMap);
	}

	@POST
	@Path("/luzao/{stockId}/{date}")
	@Produces("application/json")
	public String queryLuZaoById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<LuZaoVO> list = new ArrayList<LuZaoVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		double[][] lz = luzaoHelper.getLuZaoList(Doubles.toArray(close));
		for (int i = 0; i < lz[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				LuZaoVO vo = new LuZaoVO();
				vo.setMa19(Strings.convert2ScaleDecimal(lz[0][i]));
				vo.setMa43(Strings.convert2ScaleDecimal(lz[1][i]));
				vo.setMa86(Strings.convert2ScaleDecimal(lz[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}

		return gson.toJson(list);
	}

	@POST
	@Path("/qsdd/{stockId}/{date}")
	@Produces("application/json")
	public String queryQSDDById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<QSDDVO> list = new ArrayList<QSDDVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		List<Double> low = StockPriceFetcher.getLowPrice(spList);
		List<Double> high = StockPriceFetcher.getHighPrice(spList);
		double[][] qsdd = qsddHelper.getQSDDList(Doubles.toArray(close), Doubles.toArray(low), Doubles.toArray(high));
		for (int i = 0; i < qsdd[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				QSDDVO vo = new QSDDVO();
				vo.setLonTerm(Strings.convert2ScaleDecimal(qsdd[0][i]));
				vo.setShoTerm(Strings.convert2ScaleDecimal(qsdd[1][i]));
				vo.setMidTerm(Strings.convert2ScaleDecimal(qsdd[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}

		return gson.toJson(list);
	}

	@POST
	@Path("/wr/{stockId}/{date}")
	@Produces("application/json")
	public String queryWRById(@PathParam("stockId") String stockIdParm, @PathParam("date") String dateParm,
			String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<WRVO> list = new ArrayList<WRVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);

		List<Double> close = StockPriceFetcher.getClosePrice(spList);
		List<Double> low = StockPriceFetcher.getLowPrice(spList);
		List<Double> high = StockPriceFetcher.getHighPrice(spList);
		double[][] wr = wrHelper.getWRList(Doubles.toArray(close), Doubles.toArray(low), Doubles.toArray(high), 19, 43,
				86);
		for (int i = 0; i < wr[0].length; i++) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, spList.get(i).date)) {
				WRVO vo = new WRVO();
				vo.setLonTerm(Strings.convert2ScaleDecimal(wr[0][i]));
				vo.setShoTerm(Strings.convert2ScaleDecimal(wr[1][i]));
				vo.setMidTerm(Strings.convert2ScaleDecimal(wr[2][i]));
				vo.setStockId(stockIdParm);
				vo.setDate(spList.get(i).date);
				list.add(vo);
			}
		}

		return gson.toJson(list);
	}
}
