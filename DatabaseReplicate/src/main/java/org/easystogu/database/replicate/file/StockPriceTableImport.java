package org.easystogu.database.replicate.file;

import java.util.List;

import net.sf.json.JSONArray;

import org.easystogu.db.access.table.StockPriceTableHelper;
import org.easystogu.db.vo.table.StockPriceVO;
import org.easystogu.file.TextFileSourceHelper;

public class StockPriceTableImport {
    private TextFileSourceHelper fileSource = TextFileSourceHelper.getInstance();
    private StockPriceTableHelper stockPriceTable = StockPriceTableHelper.getInstance();

    public void importFromFile(String date) {
        try {
            String file = "stockPrice/" + date + ".json";
            JSONArray jsonArray = JSONArray.fromObject(fileSource.loadContent(file));
            List<StockPriceVO> list = (List<StockPriceVO>) JSONArray.toCollection(jsonArray, StockPriceVO.class);
            for (StockPriceVO vo : list) {
                System.out.println("Update stockPrice for " + vo);
                stockPriceTable.delete(vo.stockId, date);
                stockPriceTable.insert(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        StockPriceTableImport ins = new StockPriceTableImport();
        ins.importFromFile("2016-04-05");
    }
}
