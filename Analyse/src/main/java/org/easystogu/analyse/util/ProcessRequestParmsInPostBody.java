package org.easystogu.analyse.util;

import org.easystogu.config.Constants;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.util.MergeNDaysPriceUtil;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.sina.runner.history.HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner;
import org.easystogu.trendmode.TrendModeLoader;
import org.easystogu.trendmode.vo.SimplePriceVO;
import org.easystogu.trendmode.vo.TrendModeVO;
import org.easystogu.utils.Strings;
import org.easystogu.utils.WeekdayUtil;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//process request parms in post request body, it only apply for IndicatorEndPointV3 and PriceEndPoint
//parms such as: append trendMode, merge nDays into one
//the parms has priority: nDays > trendMode

//@Component
public class ProcessRequestParmsInPostBody {
	private static Logger logger = LogHelper.getLogger(ProcessRequestParmsInPostBody.class);
	protected StockPriceTableHelper qianFuQuanStockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
	protected StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();
	protected CompanyInfoFileHelper companyInfoHelper = CompanyInfoFileHelper.getInstance();
	protected MergeNDaysPriceUtil mergeNdaysPriceHeloer = new MergeNDaysPriceUtil();
	protected HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner historyQianFuQuanRunner = new HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner();
	//@Autowired
	protected TrendModeLoader trendModeLoader = TrendModeLoader.getInstance();

	//just for legacy using, instead of using @Component
	private static ProcessRequestParmsInPostBody instance = null;
	private ProcessRequestParmsInPostBody() {

	}

	public static ProcessRequestParmsInPostBody getInstance(){
		if(instance == null){
			instance = new ProcessRequestParmsInPostBody();
		}
		return instance;
	}

	//根据trendMode预计算
	public List<StockPriceVO> updateStockPriceAccordingToRequest(String stockId, JSONObject jsonParm) {

		List<StockPriceVO> spList = fetchAllPrices(stockId);
		logger.debug("before updateStockPriceAccordingToRequest, curVo: {}", spList.get(spList.size() - 1));
		if (jsonParm == null) {
			return spList;
		}

		try {
			// parms has process priority, do not change the order
			int repeatTimes = 1;
			if(jsonParm.has("repeatTimes")) {
				String repeatTimesParms = jsonParm.getString("repeatTimes");
				if (Strings.isNotEmpty(repeatTimesParms) && Strings.isNumeric(repeatTimesParms)) {
					repeatTimes = Integer.parseInt(repeatTimesParms);
				}
			}

			if(jsonParm.has("trendModeName")) {
				String trendModeName = jsonParm.getString("trendModeName");
				if (Strings.isNotEmpty(trendModeName)) {
					spList = this.appendTrendModePrice(trendModeName, repeatTimes, spList);
				}
			}

			if(jsonParm.has("nDays")) {
				String nDays = jsonParm.getString("nDays");
				if (Strings.isNotEmpty(nDays) && Strings.isNumeric(nDays)) {
					spList = this.mergeNDaysPrice(Integer.parseInt(nDays), spList);
				}
			}

			// based currently realtime stock price data (O,H,L,C),
			// mock to change the current Close price and predict today's Buy,Sell indicator
			if(jsonParm.has("mockCurPriceAndPredictTodayBSInd")) {
				String mockCurPriceAndPredictTodayBSInd = jsonParm
						.getString("mockCurPriceAndPredictTodayBSInd");
				if (Strings.isNotEmpty(mockCurPriceAndPredictTodayBSInd)) {
					//close price change percentage
					//increase percentage such as: "0.0", "0.010", "-0.010", "0.015", "-0.015","0.020", "-0.020", "0.025", "-0.025"
					double mockClosePricePercent = Double.parseDouble(mockCurPriceAndPredictTodayBSInd);
					StockPriceVO curVo = spList.get(spList.size() - 1).copy();
					logger.debug("before mockCurPriceAndPredictTodayBSInd="+mockCurPriceAndPredictTodayBSInd + ", update vo="+curVo.toString());
					if (WeekdayUtil.currentDate().equals(curVo.date) && curVo.open > 0 && curVo.close > 0 && curVo.lastClose > 0) {
						double dynamicPrice = curVo.lastClose * (1.0 + mockClosePricePercent);
						//keep open price, since at this time the open price had been known
						//adjust the high and low price accordingly
						if (dynamicPrice > curVo.high) {
							curVo.high = dynamicPrice;
						}
						if (dynamicPrice < curVo.low) {
							curVo.low = dynamicPrice;
						}
						//override it
						spList.set(spList.size() - 1, curVo);
						logger.debug("after mockCurPriceAndPredictTodayBSInd="+mockCurPriceAndPredictTodayBSInd + ", update vo="+curVo.toString());
					} else {
						logger.warn(
								"mockCurPriceAndPredictTodayBSInd error, either today is not a trader day, nor the stock price is not updated");
					}
				}
			}

		}catch(org.json.JSONException e){
			e.printStackTrace();
		}
		// finally return the updated spList
		logger.debug("after updateStockPriceAccordingToRequest, curVo: {}", spList.get(spList.size() - 1));
		return spList;
	}

	private List<StockPriceVO> mergeNDaysPrice(int nDays, List<StockPriceVO> spList) {
		return mergeNdaysPriceHeloer.generateNDaysPriceVOInDescOrder(nDays, spList);
	}

	private List<StockPriceVO> appendTrendModePrice(String trendModeName, int repeatTimes, List<StockPriceVO> spList) {
		// parse the forecast body and add back to spList
		StockPriceVO curSPVO = spList.get(spList.size() - 1);
		TrendModeVO tmo = trendModeLoader.loadTrendMode(trendModeName).copy();

		if (tmo.prices.size() == 0)
			return spList;

		List<SimplePriceVO> origList = tmo.getPricesByCopy();

		// append the repeat times of forecast
		for (int i = 1; i < repeatTimes; i++) {
			tmo.prices.addAll(origList);
		}

		// if the now the time is at the transaction time
		// 如果当前正是交易时间，即时的价格已经在schedule每5分钟更新一次，在此基础上再进行预测
		if (WeekdayUtil.isNowAtWorkingDayAndTransactionTime() && curSPVO.date.equals(WeekdayUtil.currentDate())) {
			// delete the first one, since it is already happens
			SimplePriceVO firstSPVO = tmo.prices.get(0);
			tmo.prices.remove(0);

			// update the stock price based on the forecast tmo
			curSPVO.setClose(Strings.convert2ScaleDecimal(curSPVO.lastClose * (1.0 + firstSPVO.getClose() / 100.0)));
			// adjust the realtime high and realtime low if the forecast close
			// is changed
			if (curSPVO.high < curSPVO.close) {
				curSPVO.high = curSPVO.close;
			}
			if (curSPVO.low > curSPVO.close) {
				curSPVO.low = curSPVO.close;
			}
		}

		List<String> nextWorkingDateList = WeekdayUtil.nextWorkingDateList(curSPVO.date, tmo.prices.size());

		for (int i = 0; i < tmo.prices.size(); i++) {
			SimplePriceVO svo = tmo.prices.get(i);
			StockPriceVO spvo = new StockPriceVO();
			spvo.setDate(nextWorkingDateList.get(i));
			spvo.setStockId(curSPVO.stockId);
			spvo.setLastClose(curSPVO.close);
			spvo.setOpen(Strings.convert2ScaleDecimal(spvo.lastClose * (1.0 + svo.getOpen() / 100.0)));
			spvo.setClose(Strings.convert2ScaleDecimal(spvo.lastClose * (1.0 + svo.getClose() / 100.0)));
			spvo.setLow(Strings.convert2ScaleDecimal(spvo.lastClose * (1.0 + svo.getLow() / 100.0)));
			spvo.setHigh(Strings.convert2ScaleDecimal(spvo.lastClose * (1.0 + svo.getHigh() / 100.0)));
			spvo.setVolume((long) (curSPVO.volume * svo.getVolume()));

			spList.add(spvo);
			curSPVO = spvo;
		}

		return spList;
	}

	// common function to fetch price from stockPrice table
	private List<StockPriceVO> fetchAllPrices(String stockid) {
		List<StockPriceVO> spList = new ArrayList<StockPriceVO>();
		List<StockPriceVO> tmpList = this.qianFuQuanStockPriceTable.queryByStockId(stockid);
		for (Object obj : tmpList) {
			spList.add((StockPriceVO) obj);
		}
		return spList;
	}

	private String appendTrendModeDateToDateRange(JSONObject jsonParm, String date) {
		String fromDate = WeekdayUtil.currentDate();
		String endDate = WeekdayUtil.currentDate();

		if (Pattern.matches(Constants.fromToRegex, date)) {
			fromDate = date.split("_")[0];
			endDate = date.split("_")[1];

			// if postBody contains the trendMode, then get the dateLengh from
			// it
			// and append the last date to dateRange
			if (jsonParm != null) {
				try{
					int repeatTimes = 1;
					if(jsonParm.has("repeatTimes")) {
						String repeatTimesParms = jsonParm.getString("repeatTimes");
						if (Strings.isNotEmpty(repeatTimesParms) && Strings.isNumeric(repeatTimesParms)) {
							repeatTimes = Integer.parseInt(repeatTimesParms);
						}
					}

					if(jsonParm.has("trendModeName")) {
						String trendModeName = jsonParm.getString("trendModeName");
						if (Strings.isNotEmpty(trendModeName)) {
							TrendModeVO tmo = trendModeLoader.loadTrendMode(trendModeName).copy();

							List<SimplePriceVO> origList = tmo.getPricesByCopy();
							for (int i = 1; i < repeatTimes; i++) {
								tmo.prices.addAll(origList);
							}

							if (tmo.prices.size() > 0) {
								String newEndDate = WeekdayUtil.nextNWorkingDate(endDate, tmo.prices.size());
								return fromDate + "_" + newEndDate;
							}
						}
					}
				}catch(org.json.JSONException e){
					e.printStackTrace();
				}
			}
		}

		// return the default data range
		return date;
	}

	public boolean isStockDateSelected(JSONObject jsonParm, String date, String aDate) {

		String newDate = this.appendTrendModeDateToDateRange(jsonParm, date);

		if (Pattern.matches(Constants.fromToRegex, newDate)) {
			String date1 = newDate.split("_")[0];
			String date2 = newDate.split("_")[1];
			return Strings.isDateSelected(date1 + " " + Constants.HHmmss, date2 + " " + Constants.HHmmss,
					aDate + " " + Constants.HHmmss);
		}
		if (Pattern.matches(Constants.dateRegex, newDate) || Strings.isEmpty(newDate)) {
			return aDate.equals(newDate);
		}
		return false;
	}
}
