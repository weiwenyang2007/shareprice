package org.easystogu.report.comparator;

import java.util.Comparator;

import org.easystogu.report.RangeHistoryReportVO;

public class ZiJinLiuComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		RangeHistoryReportVO vo1 = (RangeHistoryReportVO) arg0;
		RangeHistoryReportVO vo2 = (RangeHistoryReportVO) arg1;

		return (vo1.currentSuperVO.getAllZiJinLiuVOMajorNetPer() >= vo2.currentSuperVO.getAllZiJinLiuVOMajorNetPer()) ? 0
				: 1;
	}
}
