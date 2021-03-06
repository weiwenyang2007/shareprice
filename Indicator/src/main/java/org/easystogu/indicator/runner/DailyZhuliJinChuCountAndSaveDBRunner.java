package org.easystogu.indicator.runner;

import java.util.List;

import org.easystogu.db.access.table.IndZhuliJinChuTableHelper;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.db.vo.table.ZhuliJinChuVO;
import org.easystogu.file.access.CompanyInfoFileHelper;
import org.easystogu.indicator.ZhuliJinChuHelper;
import org.easystogu.utils.Strings;

public class DailyZhuliJinChuCountAndSaveDBRunner implements Runnable {

    protected StockPriceTableHelper qianFuQuanStockPriceTable = QianFuQuanStockPriceTableHelper.getInstance();
    protected IndZhuliJinChuTableHelper zhuliJinChuTable = IndZhuliJinChuTableHelper.getInstance();
    protected ZhuliJinChuHelper zhuliJinChuHelper = new ZhuliJinChuHelper();
    protected CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();

    public DailyZhuliJinChuCountAndSaveDBRunner() {

    }

    public void deleteZhuliJinChu(String stockId, String date) {
        zhuliJinChuTable.delete(stockId, date);
    }

    public void deleteZhuliJinChu(String stockId) {
        zhuliJinChuTable.delete(stockId);
    }

    public void deleteMai1Mai2(List<String> stockIds) {
        int index = 0;
        for (String stockId : stockIds) {
            System.out.println("Delete ZhuliJinChu for " + stockId + " " + (++index) + "/" + stockIds.size());
            this.deleteZhuliJinChu(stockId);
        }
    }

    public void countAndSaved(String stockId) {
        List<StockPriceVO> priceList = qianFuQuanStockPriceTable.getStockPriceById(stockId);

        if (priceList.size() <= 34) {
            // System.out.println("StockPrice data is less than 34, skip " +
            // stockId);
            return;
        }

        // list is order by date
        int length = priceList.size();
        double[] var1 = new double[length];
        int index = 0;
        for (StockPriceVO vo : priceList) {
            var1[index++] = (2 * vo.close + vo.high + vo.low) / 4;
        }

        double[][] zhuliJinChu = zhuliJinChuHelper.getZhuliJinChuList(var1);

        ZhuliJinChuVO vo = new ZhuliJinChuVO();
        vo.setDuofang(Strings.convert2ScaleDecimal(zhuliJinChu[0][length - 1]));
        vo.setKongfang(Strings.convert2ScaleDecimal(zhuliJinChu[1][length - 1]));
        vo.setStockId(stockId);
        vo.setDate(priceList.get(length - 1).date);

        this.deleteZhuliJinChu(stockId, vo.date);
        zhuliJinChuTable.insert(vo);

    }

    public void countAndSaved(List<String> stockIds) {
        int index = 0;
        for (String stockId : stockIds) {
            if (index++ % 500 == 0) {
                System.out.println("ZhuliJinChu countAndSaved: " + stockId + " " + (index) + "/" + stockIds.size());
            }
            this.countAndSaved(stockId);
        }
    }

    public void run() {
    }

    // TODO Auto-generated method stub
    public static void main(String[] args) {
        CompanyInfoFileHelper stockConfig = CompanyInfoFileHelper.getInstance();
        DailyZhuliJinChuCountAndSaveDBRunner runner = new DailyZhuliJinChuCountAndSaveDBRunner();
        runner.countAndSaved(stockConfig.getAllStockId());
        // runner.countAndSaved("600000");
    }

}
