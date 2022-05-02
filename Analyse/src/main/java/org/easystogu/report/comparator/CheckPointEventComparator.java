package org.easystogu.report.comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.easystogu.db.vo.table.CheckPointDailySelectionVO;
import org.easystogu.db.vo.table.ZhuLiJingLiuRuVO;
import org.easystogu.db.vo.table.ZiJinLiuVO;

public class CheckPointEventComparator {
	public static Map<String, List<CheckPointDailySelectionVO>> sortMapByValue(final List<String> lastNDates,
			Map<String, List<CheckPointDailySelectionVO>> oriMap, final Map<String, List<ZiJinLiuVO>> ziJinLius,
			final Map<String, Integer> liuTongShiZhis, final Map<String, List<ZhuLiJingLiuRuVO>> zhuLiJingLiuRus) {
		Map<String, List<CheckPointDailySelectionVO>> sortedMap = new LinkedHashMap<String, List<CheckPointDailySelectionVO>>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, List<CheckPointDailySelectionVO>>> entryList = new ArrayList<Map.Entry<String, List<CheckPointDailySelectionVO>>>(
					oriMap.entrySet());
			Collections.sort(entryList, new Comparator<Map.Entry<String, List<CheckPointDailySelectionVO>>>() {
				public int compare(Entry<String, List<CheckPointDailySelectionVO>> entry1,
						Entry<String, List<CheckPointDailySelectionVO>> entry2) {
					List<CheckPointDailySelectionVO> cpList1 = null;
					List<CheckPointDailySelectionVO> cpList2 = null;
					try {
						cpList1 = entry1.getValue();
						cpList2 = entry2.getValue();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}

					String stockId1 = entry1.getKey();
					String stockId2 = entry2.getKey();
					// count checkPoint number & ZiJinLiu number
					int s1 = cpList1.size()
							+ countLiuTongShiZhi(stockId1, liuTongShiZhis)
							+ (countZiJinLiuVONumber(lastNDates, stockId1, ziJinLius) + countZhuLiJingLiuRuVONumber(
									lastNDates, stockId1, zhuLiJingLiuRus));
					int s2 = cpList2.size()
							+ countLiuTongShiZhi(stockId2, liuTongShiZhis)
							+ (countZiJinLiuVONumber(lastNDates, stockId2, ziJinLius) + countZhuLiJingLiuRuVONumber(
									lastNDates, stockId2, zhuLiJingLiuRus));

					int ddx1 = (int) getTotalDDX(stockId1, lastNDates, ziJinLius, liuTongShiZhis);
					int ddx2 = (int) getTotalDDX(stockId2, lastNDates, ziJinLius, liuTongShiZhis);

					return s2 - s1;
					// return ddx2 - ddx1;
				}
			});
			//
			Iterator<Map.Entry<String, List<CheckPointDailySelectionVO>>> iter = entryList.iterator();
			Map.Entry<String, List<CheckPointDailySelectionVO>> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return sortedMap;
	}

	private static int countZiJinLiuVONumber(List<String> lastNDates, String stockId,
			Map<String, List<ZiJinLiuVO>> ziJinLius) {
		int count = 0;
		List<ZiJinLiuVO> list = ziJinLius.get(stockId);
		if (list == null) {
			return 0;
		}
		for (ZiJinLiuVO vo : list) {
			for (int index = 0; index < lastNDates.size(); index++) {
				if (vo.date.equals(lastNDates.get(index))) {
					count += (lastNDates.size() - index + 1);
				}
			}
		}
		return count;
	}

	private static int countZhuLiJingLiuRuVONumber(List<String> lastNDates, String stockId,
			Map<String, List<ZhuLiJingLiuRuVO>> zhuLiJingLiuRus) {
		int count = 0;
		List<ZhuLiJingLiuRuVO> list = zhuLiJingLiuRus.get(stockId);
		if (list == null) {
			return 0;
		}
		for (ZhuLiJingLiuRuVO vo : list) {
			for (int index = 0; index < lastNDates.size(); index++) {
				if (vo.date.equals(lastNDates.get(index))) {
					count += (lastNDates.size() - index + 1);
				}
			}
		}
		return count;
	}

	private static int countLiuTongShiZhi(String stockId, Map<String, Integer> liuTongShiZhis) {
		int liuTongShiZhi = liuTongShiZhis.get(stockId);
		if (liuTongShiZhi <= 50) {
			return 3;
		}
		if (liuTongShiZhi <= 100) {
			return 2;
		}
		if (liuTongShiZhi <= 200) {
			return 1;
		}
		return 0;
	}

	private static double getTotalDDX(String stockId, List<String> lastNDates, Map<String, List<ZiJinLiuVO>> ziJinLius,
			Map<String, Integer> liuTongShiZhis) {
		List<ZiJinLiuVO> zjlList = ziJinLius.get(stockId);
		int liuTongShiZhi = liuTongShiZhis.get(stockId);
		double ddx = 0.0;
		for (ZiJinLiuVO zjl : zjlList) {
			for (int index = 0; index < lastNDates.size(); index++) {
				if (zjl.date.equals(lastNDates.get(index))) {
					ddx += zjl.getMajorNetIn() / liuTongShiZhi;
				}
			}
		}
		return ddx;
	}

	public static Map<String, List<CheckPointDailySelectionVO>> sortMapByEvent(
			Map<String, List<CheckPointDailySelectionVO>> oriMap) {
		Map<String, List<CheckPointDailySelectionVO>> sortedMap = new LinkedHashMap<String, List<CheckPointDailySelectionVO>>();
		if (oriMap != null && !oriMap.isEmpty()) {
			List<Map.Entry<String, List<CheckPointDailySelectionVO>>> entryList = new ArrayList<Map.Entry<String, List<CheckPointDailySelectionVO>>>(
					oriMap.entrySet());
			Collections.sort(entryList, new Comparator<Map.Entry<String, List<CheckPointDailySelectionVO>>>() {
				public int compare(Entry<String, List<CheckPointDailySelectionVO>> entry1,
						Entry<String, List<CheckPointDailySelectionVO>> entry2) {
					List<CheckPointDailySelectionVO> cpList1 = null;
					List<CheckPointDailySelectionVO> cpList2 = null;
					try {
						cpList1 = entry1.getValue();
						cpList2 = entry2.getValue();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					return cpList2.size() - cpList1.size();
				}
			});
			//
			Iterator<Map.Entry<String, List<CheckPointDailySelectionVO>>> iter = entryList.iterator();
			Map.Entry<String, List<CheckPointDailySelectionVO>> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}
		}
		return sortedMap;
	}
}
