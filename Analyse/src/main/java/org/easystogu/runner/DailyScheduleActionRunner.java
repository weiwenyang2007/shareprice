package org.easystogu.runner;

import java.util.List;
import org.easystogu.db.access.table.QianFuQuanStockPriceTableHelper;
import org.easystogu.db.access.table.ScheduleActionTableHelper;
import org.easystogu.db.vo.table.ScheduleActionVO;
import org.easystogu.indicator.runner.history.IndicatorHistortOverAllRunner;
import org.easystogu.sina.runner.history.HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner;
import org.easystogu.sina.runner.history.HistoryStockPriceDownloadAndStoreDBRunner;
import org.easystogu.sina.runner.history.HistoryWeekStockPriceCountAndSaveDBRunner;
import org.easystogu.utils.WeekdayUtil;

public class DailyScheduleActionRunner implements Runnable {
  private String currentDate = WeekdayUtil.currentDate();
  private ScheduleActionTableHelper scheduleActionTable = ScheduleActionTableHelper.getInstance();
  private QianFuQuanStockPriceTableHelper qianfuquanStockPriceTable =
      QianFuQuanStockPriceTableHelper.getInstance();
  private HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner historyQianFuQuanRunner =
      new HistoryQianFuQuanStockPriceDownloadAndStoreDBRunner();
  private IndicatorHistortOverAllRunner indicatorHistoryRunner =
      new IndicatorHistortOverAllRunner();
  private HistoryWeekStockPriceCountAndSaveDBRunner weekPriceHistoryRunner =
      new HistoryWeekStockPriceCountAndSaveDBRunner();
  private HistoryStockPriceDownloadAndStoreDBRunner priceHistoryRunner =
      new HistoryStockPriceDownloadAndStoreDBRunner();

  public void runAllScheduleAction() {
    List<ScheduleActionVO> actions = this.scheduleActionTable.getAllShouldRunDate(currentDate);
    actions.parallelStream().forEach(savo -> {
      // for (ScheduleActionVO savo : actions) {

      if (currentDate.compareTo(savo.getRunDate()) >= 0) {

        if (savo.actionDo.equals(ScheduleActionVO.ActionDo.refresh_history_stockprice.name())) {
          System.out.println("refresh_history_stockprice for " + savo.stockId);
          // fetch original history data
          this.priceHistoryRunner.countAndSave(savo.stockId);
          // for qian fuquan history data
          this.historyQianFuQuanRunner.countAndSave(savo.stockId);
          // delete schedule action if success
          if (this.qianfuquanStockPriceTable.countByStockId(savo.stockId) > 0) {
            this.scheduleActionTable.delete(savo.stockId, savo.actionDo);
          }

          // update week price
          weekPriceHistoryRunner.countAndSave(savo.stockId);
          // update indicator
          indicatorHistoryRunner.countAndSave(savo.stockId);
        } else if (savo.actionDo
            .equals(ScheduleActionVO.ActionDo.refresh_fuquan_history_stockprice.name())) {
          System.out.println("refresh_fuquan_history_stockprice for " + savo.stockId);
          // fetch hou ququan history data
          // this.historyHouFuQuanRunner.countAndSave(savo.stockId);
          // for qian fuquan
          this.historyQianFuQuanRunner.countAndSave(savo.stockId);
          // delete schedule action if success
          if (this.qianfuquanStockPriceTable.countByStockId(savo.stockId) > 0) {
            this.scheduleActionTable.delete(savo.stockId, savo.actionDo);
          }

          // update week price
          weekPriceHistoryRunner.countAndSave(savo.stockId);
          // update indicator
          indicatorHistoryRunner.countAndSave(savo.stockId);
        }
      }
    });
  }

  public void run() {
    this.runAllScheduleAction();
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    DailyScheduleActionRunner runner = new DailyScheduleActionRunner();
    runner.run();
  }
}
