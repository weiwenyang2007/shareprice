package org.easystogu.indicator.runner.history;

import java.util.List;

import org.easystogu.config.Constants;
import org.easystogu.db.access.facde.DBAccessFacdeFactory;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.helper.IF.IndicatorDBHelperIF;
import org.easystogu.db.vo.table.BollVO;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.BOLLHelper;
import org.easystogu.utils.Strings;

//计算数据库中所有boll值，包括最新和历史的，一次性运行
public class HistoryBollCountAndSaveDBRunner {

	protected IndicatorDBHelperIF bollTable = DBAccessFacdeFactory.getInstance(Constants.indBoll);
	protected StockPriceTableHelper qianFuQuanStockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
	private BOLLHelper bollHelper = new BOLLHelper();

	public void deleteBoll(String stockId) {
		bollTable.delete(stockId);
	}

	public void deleteBoll(List<String> stockIds) {
		int index = 0;
		for (String stockId : stockIds) {
			this.deleteBoll(stockId);
		}
	}

	public void countAndSaved(String stockId) {
        this.deleteBoll(stockId);
        
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

			double[][] boll = bollHelper.getBOLLList(close, 20, 2, 2);

			for (index = priceList.size() - 1; index >= 0; index--) {
				double up = Strings.convert2ScaleDecimal(boll[0][index], 3);
				double mb = Strings.convert2ScaleDecimal(boll[1][index], 3);
				double dn = Strings.convert2ScaleDecimal(boll[2][index], 3);

				BollVO bollVO = new BollVO();
				bollVO.setStockId(stockId);
				bollVO.setDate(priceList.get(index).date);
				bollVO.setMb(mb);
				bollVO.setUp(up);
				bollVO.setDn(dn);

				// if (bollVO.date.compareTo("2015-06-29") >= 0)
				// if (bollTable.getBoll(bollVO.stockId, bollVO.date) == null) {
				bollTable.insert(bollVO);
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
		HistoryBollCountAndSaveDBRunner runner = new HistoryBollCountAndSaveDBRunner();
		runner.countAndSaved(stockConfig.getAllStockId());
		// runner.countAndSaved("600750");
	}

}
