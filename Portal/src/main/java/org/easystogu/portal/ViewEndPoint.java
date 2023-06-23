package org.easystogu.portal;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.easystogu.config.ConfigurationService;
import org.easystogu.config.DBConfigurationService;
import org.easystogu.db.access.table.AiTrendPredictTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.access.view.CommonViewHelper;
import org.easystogu.db.vo.table.AiTrendPredictVO;
import org.easystogu.db.vo.table.CheckPointDailySelectionVO;
import org.easystogu.db.vo.view.CommonViewVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.cache.CheckPointDailySelectionTableCache;
import org.easystogu.cache.CommonViewCache;
import org.easystogu.cache.ConfigurationServiceCache;
import org.easystogu.utils.Strings;
import org.slf4j.Logger;
import com.google.gson.Gson;

public class ViewEndPoint {
	private ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
	private String accessControlAllowOrgin = config.getString("Access-Control-Allow-Origin", "");
	private static Logger logger = LogHelper.getLogger(ViewEndPoint.class);
	private CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
	private CheckPointDailySelectionTableCache checkPointDailySelectionCache = CheckPointDailySelectionTableCache
			.getInstance();
	protected StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
	private CommonViewCache commonViewCache = CommonViewCache.getInstance();
	private AiTrendPredictTableHelper aiTrendPredictTableHelper  = AiTrendPredictTableHelper.getInstance();
	private CompanyInfoFileHelper companyInfoHelper = CompanyInfoFileHelper.getInstance();
	
	private Gson gson = new Gson();

	@GET
	@Path("/{viewname}")
	@Produces("application/json")
	public String queryDayPriceByIdFromAnalyseViewAtRealTime(@PathParam("viewname") String viewname,
			@Context HttpServletRequest request, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		String date = request.getParameter("date");
		String cixin = request.getParameter("cixin");
		logger.debug("viewName=" + viewname + ",date=" + date + ",cixin=" + cixin);

		if(Strings.isEmpty(viewname)){
			return gson.toJson(new ArrayList<CommonViewVO>());
		}

		if ("luzao_phaseII_zijinliu_top300".equals(viewname) || "luzao_phaseIII_zijinliu_top300".equals(viewname)
				|| "luzao_phaseII_ddx_bigger_05".equals(viewname) || "luzao_phaseIII_ddx_bigger_05".equals(viewname)) {
			// get result from view directory, since they are fast
			String searchViewName = viewname + "_Details";
			List<CommonViewVO> list = this.commonViewCache
					.queryByDateForViewDirectlySearch(date, searchViewName);

			return gson.toJson(this.fliterCiXinGu(cixin, list));
		}

		//get all the stockIds if the predication day (9999-01-01) is Bottom
		if ("AiTrend_Bottom_Area".equals(viewname) && date.equals("9999-01-01")) {
			List<CommonViewVO> list = new ArrayList<CommonViewVO>();
			List<AiTrendPredictVO> aiList = aiTrendPredictTableHelper.getByDateAndResultBottom(date);
			for (AiTrendPredictVO aiVO : aiList){
				List<AiTrendPredictVO> vos = aiTrendPredictTableHelper.getByStockId(aiVO.getStockId());
				if(vos.size() < 3){
					continue;
				}
				AiTrendPredictVO vo9999 = vos.get(vos.size() - 1);
				AiTrendPredictVO voToday = vos.get(vos.size() - 2);
				AiTrendPredictVO voYesterday = vos.get(vos.size() - 3);
				if("9999-01-01".equals(vo9999.getDate())){
					//Bottom
					if(vo9999.getResult() >= AiTrendPredictVO.buyPoint
							&& voToday.getResult() < AiTrendPredictVO.buyPoint
							&& voYesterday.getResult() < AiTrendPredictVO.buyPoint){
						CommonViewVO acvo = new CommonViewVO();
						acvo.date = vo9999.getDate();
						acvo.stockId = vo9999.getStockId();
						acvo.name = companyInfoHelper.getStockName(vo9999.getStockId());
						list.add(acvo);
					}
				}
			}
			return gson.toJson(list);
		}

		//get all the stockIds if the predication day (9999-01-01) is Top
		if ("AiTrend_Top_Area".equals(viewname) && date.equals("9999-01-01")) {
			List<CommonViewVO> list = new ArrayList<CommonViewVO>();
			List<AiTrendPredictVO> aiList = aiTrendPredictTableHelper.getByDateAndResultTop(date);
			for (AiTrendPredictVO aiVO : aiList){
				List<AiTrendPredictVO> vos = aiTrendPredictTableHelper.getByStockId(aiVO.getStockId());
				if(vos.size() < 3){
					continue;
				}
				AiTrendPredictVO vo9999 = vos.get(vos.size() - 1);
				AiTrendPredictVO voToday = vos.get(vos.size() - 2);
				AiTrendPredictVO voYesterday = vos.get(vos.size() - 3);
				if("9999-01-01".equals(vo9999.getDate())){
					//Top
					if(vo9999.getResult() < AiTrendPredictVO.buyPoint
							&& voToday.getResult() >= AiTrendPredictVO.buyPoint
							&& voYesterday.getResult() >= AiTrendPredictVO.buyPoint){
						CommonViewVO acvo = new CommonViewVO();
						acvo.date = vo9999.getDate();
						acvo.stockId = vo9999.getStockId();
						acvo.name = companyInfoHelper.getStockName(vo9999.getStockId());
						list.add(acvo);
					}
				}
			}
			return gson.toJson(list);
		}

		if(viewname.contains("_AITrendBuy")){
			String newViewName = viewname.split("_AITrendBuy")[0];
			List<CommonViewVO> list = getAllCheckPointDailySelection(newViewName, date);
			List<CommonViewVO> rtnList = new ArrayList<CommonViewVO>();
			//filter the list that match the ai trund buy point
			for (CommonViewVO vo : list) {
				AiTrendPredictVO aiVo = aiTrendPredictTableHelper.getByStockIdAndDate(vo.getStockId(), vo.getDate());
				if (aiVo !=null && aiVo.getResult() >= AiTrendPredictVO.buyPoint){
					rtnList.add(vo);
				}
			}
			return gson.toJson(this.fliterCiXinGu(cixin, rtnList));
		}

		if(viewname.contains("_AITrendSell")){
			String newViewName = viewname.split("_AITrendSell")[0];
			List<CommonViewVO> list = getAllCheckPointDailySelection(newViewName, date);
			List<CommonViewVO> rtnList = new ArrayList<CommonViewVO>();
			//filter the list that match the ai trund buy point
			for (CommonViewVO vo : list) {
				AiTrendPredictVO aiVo = aiTrendPredictTableHelper.getByStockIdAndDate(vo.getStockId(), vo.getDate());
				if (aiVo !=null && aiVo.getResult() < AiTrendPredictVO.buyPoint){
					rtnList.add(vo);
				}
			}
			return gson.toJson(this.fliterCiXinGu(cixin, rtnList));
		}


		// else get result for checkpoint data, since they are analyse daily and
		// save to daily table
		return gson.toJson(this.fliterCiXinGu(cixin, getAllCheckPointDailySelection(viewname, date)));
	}

	private List<CommonViewVO> getAllCheckPointDailySelection(String viewName, String date){
		List<CommonViewVO> list = new ArrayList<CommonViewVO>();
		List<CheckPointDailySelectionVO> cps = checkPointDailySelectionCache
				.queryByDateAndCheckPoint(date, viewName);
		for (CheckPointDailySelectionVO cp : cps) {
			CommonViewVO cvo = new CommonViewVO();
			cvo.stockId = cp.stockId;
			cvo.name = stockConfig.getStockName(cp.stockId);
			cvo.date = date;

			list.add(cvo);
		}

		return list;
	}

	// fliter cixin
	private List<CommonViewVO> fliterCiXinGu(String cixin, List<CommonViewVO> originList) {
		if ("True".equalsIgnoreCase(cixin) && originList.size() > 0) {
			List<CommonViewVO> cixinList = new ArrayList<CommonViewVO>();
			int cixinStockLen = config.getInt("cixin_Stock_Length", 86 * 2);

			for (CommonViewVO cvo : originList) {
				int spLength = stockPriceTable.countByStockId(cvo.stockId);
				if (spLength <= cixinStockLen) {
					cixinList.add(cvo);
				}
			}
			return cixinList;
		}

		return originList;
	}
}
