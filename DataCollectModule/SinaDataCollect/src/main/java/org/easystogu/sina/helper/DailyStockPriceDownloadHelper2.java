package org.easystogu.sina.helper;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;

import org.easystogu.config.ConfigurationService;
import org.easystogu.config.Constants;
import org.easystogu.config.FileConfigurationService;
import org.easystogu.db.access.table.CompanyInfoTableHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.sina.common.SinaQuoteStockPriceVO;
import org.easystogu.utils.Strings;
import org.slf4j.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

//get real time stock price from http://vip.stock.finance.sina.com.cn/quotes_service/api/
//it will get all the stockId from the web, including the new on board stockId

//another api to get realtime stock price is :
//https://vip.stock.finance.sina.com.cn/quotes_service/view/vML_DataList.php?asc=j&symbol=sh600547&num=5
public class DailyStockPriceDownloadHelper2 {
	private static Logger logger = LogHelper.getLogger(DailyStockPriceDownloadHelper2.class);
	private CompanyInfoTableHelper companyInfoTable = CompanyInfoTableHelper.getInstance();
	// currently total stock number is less then 3000, if increase, then enlarge
	// the numberPage
	private static final int numberPerPage = 100;//can not larger than 100 per times
	private int totalNumberPage = this.companyInfoTable.getAllCompanyInfo().size() / numberPerPage + 1;
	private static final String baseUrl = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num="
			+ numberPerPage + "&sort=symbol&asc=1&node=hs_a";
	private static ConfigurationService configure = FileConfigurationService.getInstance();

	private static RestTemplate restTemplate = null;

	static {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(20000);
		requestFactory.setReadTimeout(20000);

		if (Strings.isNotEmpty(configure.getString(Constants.httpProxyServer))) {
			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(configure.getString(Constants.httpProxyServer),
					configure.getInt(Constants.httpProxyPort)));
			requestFactory.setProxy(proxy);
		}

		restTemplate = new RestTemplate(requestFactory);
	}

	public List<SinaQuoteStockPriceVO> fetchAllStockPriceFromWeb() {
		long beginTs = System.currentTimeMillis();
		List<SinaQuoteStockPriceVO> list = new ArrayList<SinaQuoteStockPriceVO>();
		for (int pageNumber = 1; pageNumber <= totalNumberPage; pageNumber++) {
			list.addAll(this.fetchAPageDataFromWeb(pageNumber));
		}
		long endTs = System.currentTimeMillis();
		logger.debug("End fetchAllStockPriceFromWeb, Total fetch size=" + list.size() + ", spent seconds=" + (endTs - beginTs)/1000);
		return list;
	}

	public List<SinaQuoteStockPriceVO> fetchAPageDataFromWeb(int page) {
		List<SinaQuoteStockPriceVO> list = new ArrayList<SinaQuoteStockPriceVO>();
		try {

			String url = baseUrl.replaceFirst("page=1", "page=" + page);
			logger.debug("Fetch Sina Daily Data for page= " + page);

			String contents = restTemplate.getForObject(url.toString(), String.class);

			if (Strings.isEmpty(contents)) {
				logger.debug("Contents is empty");
				return list;
			}

			// convert json to vo list
			JSONArray jsonArray = JSONArray.fromObject(contents);
			JSONObject jsonObject;
			Object pojoValue;

			for (int i = 0; i < jsonArray.size(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				pojoValue = JSONObject.toBean(jsonObject, SinaQuoteStockPriceVO.class);
				SinaQuoteStockPriceVO sqspvo = (SinaQuoteStockPriceVO) pojoValue;
				if (sqspvo.high > 0 && sqspvo.low > 0 && sqspvo.open > 0) {
					list.add(sqspvo);
				}
			}

			logger.debug(", result size= " + jsonArray.size());

		} catch (JSONException e) {
			logger.debug("JSONException at fetchAPageDataFromWeb : " + e.getMessage());
		} catch (Exception e) {
			logger.error("fetchAPageDataFromWeb exception", e.getMessage());
		}
		return list;
	}

	public static void main(String[] args) {
		DailyStockPriceDownloadHelper2 ins = new DailyStockPriceDownloadHelper2();
		List<SinaQuoteStockPriceVO> list = ins.fetchAllStockPriceFromWeb();
		SinaQuoteStockPriceVO vo = list.get(list.size() - 1);
		System.out.println(list.size());
		System.out.println(vo);
	}
}
