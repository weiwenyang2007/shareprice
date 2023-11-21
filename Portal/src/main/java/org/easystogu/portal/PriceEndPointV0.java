
package org.easystogu.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.easystogu.analyse.util.ProcessRequestParmsInPostBody;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.utils.Strings;
import com.google.gson.Gson;
import org.easystogu.cache.ConfigurationServiceCache;
import org.easystogu.config.Constants;

//v0, stockprice (no chuquan)
public class PriceEndPointV0 {
	protected ConfigurationServiceCache config = ConfigurationServiceCache.getInstance();
	protected String accessControlAllowOrgin = config.getString("Access-Control-Allow-Origin", "");
	protected static String HHmmss = "00:00:00";
	private StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
	protected String dateRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
	protected String fromToRegex = dateRegex + "_" + dateRegex;
	
	private Gson gson = new Gson();

	@GET
	@Path("/{stockId}/{date}")
	@Produces("application/json")
	public String queryDayPriceById(@PathParam("stockId") String stockIdParm,
			@PathParam("date") String dateParm, @Context HttpServletResponse response) {
		response.addHeader("Access-Control-Allow-Origin", accessControlAllowOrgin);

		List<StockPriceVO> spList = new ArrayList<StockPriceVO>();

		if (Pattern.matches(fromToRegex, dateParm)) {
			String date1 = dateParm.split("_")[0];
			String date2 = dateParm.split("_")[1];
			List<StockPriceVO> cacheSpList = stockPriceTable.queryByStockId(stockIdParm);
			for (Object obj : cacheSpList) {
				StockPriceVO spvo = (StockPriceVO)obj;
				if (Strings.isDateSelected(date1 + " " + HHmmss, date2 + " " + HHmmss, spvo.date + " " + HHmmss)) {
					spList.add(spvo);
				}
			}
		} else if (Pattern.matches(dateRegex, dateParm) || Strings.isEmpty(dateParm)) {
			spList.add(stockPriceTable.getStockPriceByIdAndDate(stockIdParm, dateParm));
		}

		return gson.toJson(spList);
	}
}
