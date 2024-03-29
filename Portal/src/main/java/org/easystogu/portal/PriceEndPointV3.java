package org.easystogu.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.easystogu.analyse.util.ProcessRequestParmsInPostBody;
import org.easystogu.config.ConfigurationService;
import org.easystogu.config.DBConfigurationService;
import org.easystogu.cache.ConfigurationServiceCache;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.trendmode.TrendModeLoader;
import org.easystogu.trendmode.vo.TrendModeVO;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.Gson;

//v3, with forecast data, query qian fuquan stockprice and count in real time
public class PriceEndPointV3 {
	private ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
	private String accessControlAllowOrgin = config.getString("Access-Control-Allow-Origin", "");
	protected CompanyInfoFileHelper companyInfoHelper = CompanyInfoFileHelper.getInstance();
	protected ProcessRequestParmsInPostBody postParmsProcess = ProcessRequestParmsInPostBody.getInstance();
	protected TrendModeLoader trendModeLoader = TrendModeLoader.getInstance();

	private Gson gson = new Gson();
	
	@POST
	@Path("/{stockId}/{date}")
	@Produces("application/json")
	public String queryDayPriceByIdWithForecastPrice(@PathParam("stockId") String stockIdParm,
			@PathParam("date") String dateParm, String postBody, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);
		List<StockPriceVO> rtnSpList = new ArrayList<StockPriceVO>();
		JSONObject jsonParm = null;
		try {
			if (Strings.isNotEmpty(postBody)) {
				jsonParm = new JSONObject(postBody);
			}
		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		List<StockPriceVO> spList = postParmsProcess.updateStockPriceAccordingToRequest(stockIdParm, jsonParm);
		for (StockPriceVO vo : spList) {
			if (postParmsProcess.isStockDateSelected(jsonParm, dateParm, vo.date)) {
				rtnSpList.add(vo);
			}
		}

		return gson.toJson(rtnSpList);
	}
}
