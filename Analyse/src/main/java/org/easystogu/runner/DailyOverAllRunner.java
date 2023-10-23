package org.easystogu.runner;

import org.easystogu.cache.runner.AllCacheRunner;

public class DailyOverAllRunner implements Runnable {

	public boolean isGetZiJinLiu = false;

	public DailyOverAllRunner(boolean isGetZiJinLiu) {
		this.isGetZiJinLiu = isGetZiJinLiu;
	}
	
	public void runForSchedule() {
	  run();
	}

	public void run() {
		new DailyScheduleActionRunner().run();
		new DailyUpdateAllStockRunner(this.isGetZiJinLiu).run();
		//new RecentlySelectionRunner().run();
		new AllCacheRunner().refreshAll();
	}

	public static void main(String[] args) {
		new DailyOverAllRunner(false).run();
	}
}
