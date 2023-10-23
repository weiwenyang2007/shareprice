package org.easystogu.indicator.runner.history;

import java.util.List;

import org.easystogu.config.Constants;
import org.easystogu.db.access.facde.DBAccessFacdeFactory;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.helper.IF.IndicatorDBHelperIF;
import org.easystogu.db.vo.table.MacdVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.MACDHelper;
import org.easystogu.utils.Strings;

//计算数据库中所有macd值，包括最新和历史的，一次性运行
public class HistoryMacdCountAndSaveDBRunner {
	protected IndicatorDBHelperIF macdTable = DBAccessFacdeFactory.getInstance(Constants.indMacd);
	protected StockPriceTableHelper qianFuQuanStockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
	protected MACDHelper macdHelper = new MACDHelper();

	public void deleteMacd(String stockId) {
		macdTable.delete(stockId);
	}

	public void deleteMacd(List<String> stockIds) {
		int index = 0;
		for (String stockId : stockIds) {
			this.deleteMacd(stockId);
		}
	}

	public void countAndSaved(String stockId) {
		this.deleteMacd(stockId);

		try {
		    List<StockPriceVO> priceList = qianFuQuanStockPriceTable.getStockPriceById(stockId);

			int length = priceList.size();

			if (length < 1) {
				return;
			}

			double[] close = new double[length];
			int index = 0;
			for (StockPriceVO vo : priceList) {
				close[index++] = vo.close;
			}

			double[][] macd = macdHelper.getMACDList(close);

			for (index = priceList.size() - 1; index >= 0; index--) {
				double dif = macd[0][index];
				double dea = macd[1][index];
				double macdRtn = macd[2][index];

				MacdVO macdVo = new MacdVO();
				macdVo.setStockId(stockId);
				macdVo.setDate(priceList.get(index).date);
				macdVo.setDif(Strings.convert2ScaleDecimal(dif, 3));
				macdVo.setDea(Strings.convert2ScaleDecimal(dea, 3));
				macdVo.setMacd(Strings.convert2ScaleDecimal(macdRtn, 3));

				// if (macdVo.date.compareTo("2015-06-29") >= 0)
				// if (macdTable.getMacd(macdVo.stockId, macdVo.date) == null) {
				macdTable.insert(macdVo);
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void countAndSaved(List<String> stockIds) {
	  stockIds.parallelStream().forEach(stockId -> {
	    this.countAndSaved(stockId);
	  });
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
		HistoryMacdCountAndSaveDBRunner runner = new HistoryMacdCountAndSaveDBRunner();
		runner.countAndSaved(stockConfig.getAllStockId());
		// runner.countAndSaved("600750");
	}
}
