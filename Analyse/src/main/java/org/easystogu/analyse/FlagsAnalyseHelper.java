package org.easystogu.analyse;

import com.google.common.primitives.Doubles;
import org.easystogu.cache.CheckPointDailySelectionTableCache;
import org.easystogu.db.vo.table.*;
import org.easystogu.indicator.IND;
import org.easystogu.indicator.MAHelper;
import org.easystogu.log.LogHelper;
import org.easystogu.analyse.vo.CheckPointFlagsVO;
import org.easystogu.analyse.vo.ShenXianUIVO;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class FlagsAnalyseHelper {
  private static Logger logger = LogHelper.getLogger(FlagsAnalyseHelper.class);
  private CheckPointDailySelectionTableCache checkPointDailySelectionCache =
      CheckPointDailySelectionTableCache.getInstance();
  private static String[] zijinliuViewnames =
      {"luzao_phaseII_zijinliu_top300", "luzao_phaseIII_zijinliu_top300",
          "luzao_phaseII_ddx_bigger_05", "luzao_phaseIII_ddx_bigger_05",
          "luzao_phaseII_zijinliu_3_days_top300", "luzao_phaseII_zijinliu_3_of_5_days_top300",
          "luzao_phaseII_ddx_2_of_5_days_bigger_05", "luzao_phaseIII_zijinliu_3_days_top300",
          "luzao_phaseIII_zijinliu_3_of_5_days_top300", "luzao_phaseIII_ddx_2_of_5_days_bigger_05"};

  //just for legacy using, instead of using @Component
  private static FlagsAnalyseHelper instance = null;
  private FlagsAnalyseHelper(){

  }
  public static FlagsAnalyseHelper getInstance(){
    if(instance == null){
      instance = new FlagsAnalyseHelper();
    }
    return instance;
  }
  public List<ShenXianUIVO> shenXianBuySellFlagsAnalyse(List<StockPriceVO> spList,
      List<ShenXianUIVO> sxList, List<MacdVO> macdList, List<BBIVO> bbiList,
      List<LuZaoVO> luzaoList) {

    StockPriceVO spvoF = spList.get(spList.size() - 1);
    List<VolumeVO> volumeList = getMAVolumeList(spList);
    List<CheckPointDailySelectionVO> checkPoints = getCheckPoints(spvoF.stockId);
    //
    for (int index = 0; index < spList.size(); index++) {
      StockPriceVO spvo = spList.get(index);
      ShenXianUIVO sxvo = getShenXianIndVOByDate(spvo.date, sxList);
      MacdVO macdvo = getMacdIndVOByDate(spvo.date, macdList);
      BBIVO bbivo = getBBIIndVOByDate(spvo.date, bbiList);
      LuZaoVO luzaovo = getLuzaoIndVOByDate(spvo.date, luzaoList);
      VolumeVO volumevo = getVolumeVOByDate(spvo.date, volumeList);
      // below can be search from table checkpoint_daily_selection
      CheckPointFlagsVO cpfvo = this.checkPoints(spvo.date, checkPoints);

      if (sxvo != null && macdvo != null && bbivo != null && luzaovo != null && volumevo != null) {

        String macdStr = macdvo.dif < 0 ? "??????" : "??????";

        // ??????
        if (isMacdGordon(spvo.date, macdList) && isShenXianGordon(spvo.date, sxList)) {
          sxvo.setDuoFlagsTitle("G2");
          sxvo.setDuoFlagsText(macdStr + "Macd??????, ????????????");
        } else if (isMacdGordon(spvo.date, macdList)) {
          sxvo.setDuoFlagsTitle("G");
          sxvo.setDuoFlagsText(macdStr + "Macd??????");
        } else if (isShenXianGordon(spvo.date, sxList)) {
          sxvo.setDuoFlagsTitle("G");
          sxvo.setDuoFlagsText("????????????");
        }

        // ??????
        if (isMacdDead(spvo.date, macdList) && isShenXianDead(spvo.date, sxList)) {
          sxvo.setDuoFlagsTitle("D2");
          sxvo.setDuoFlagsText(macdStr + "Macd????????????");
        } else if (isMacdDead(spvo.date, macdList)) {
          sxvo.setDuoFlagsTitle("D");
          sxvo.setDuoFlagsText(macdStr + "Macd??????");
        } else if (isShenXianDead(spvo.date, sxList)) {
          sxvo.setDuoFlagsTitle("D");
          sxvo.setDuoFlagsText("????????????");
        }

        // macd ??????: price is higher but macd is lower
        if (this.isMacdYouDuo(spvo.date, macdList, spList)) {
          sxvo.appendDuoFlagsTitle("YD");
          sxvo.appendDuoFlagsText("??????");
        }

        // macd ??????: price is lower but macd is higher
        if (this.isMacdYouKong(spvo.date, macdList, spList)) {
          sxvo.appendDuoFlagsTitle("YK");
          sxvo.appendDuoFlagsText("??????");
        }

        // ??????
        if (isBBIDead(spvo.date, bbiList)) {
          if (isMacdDead(spvo.date, macdList) && isShenXianDead(spvo.date, sxList)) {
            sxvo.setSellFlagsTitle("???D2");
            sxvo.setSellFlagsText(spvo.close + " " + macdStr + "Macd????????????,??????2/3");
          } else if (isMacdDead(spvo.date, macdList)) {
            sxvo.setSellFlagsTitle("???D");
            sxvo.setSellFlagsText(spvo.close + " " + macdStr + "Macd??????,??????2/3");
          } else if (isShenXianDead(spvo.date, sxList)) {
            sxvo.setSellFlagsTitle("???D");
            sxvo.setSellFlagsText(spvo.close + " ????????????,??????2/3");
          } else {
            // do not print it since too many occurs
            // sxvo.setSellFlagsTitle("???");
          }
        }

        // ??????
        if (isBBIGordon(spvo.date, bbiList)) {
          if (isMacdGordon(spvo.date, macdList) && isShenXianGordon(spvo.date, sxList)) {
            sxvo.setDuoFlagsTitle("??????2");
            sxvo.setDuoFlagsText(macdStr + " Macd????????????,??????1/3");
          } else if (isShenXianGordon(spvo.date, sxList)) {
            sxvo.setDuoFlagsTitle("??????");
            sxvo.setDuoFlagsText("????????????,??????1/3");
          } else if (isMacdGordon(spvo.date, macdList)) {
            sxvo.setDuoFlagsTitle("??????");
            sxvo.setDuoFlagsText(macdStr + " MACD??????,??????1/3");
          } else {
            // most of time come here
            // do not print it since too many occurs
            // sxvo.setDuoFlagsTitle("???");
            // sxvo.setDuoFlagsText("");
          }
        }

        // ????????????
        // Gordon
        if (this.isLuZaoGordonI(spvo.date, spList, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        } else if (this.isLuZaoGordonII(spvo.date, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        } else if (this.isLuZaoGordonIII(spvo.date, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        }

        // Dead
        if (this.isLuZaoDeadI(spvo.date, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        } else if (this.isLuZaoDeadII(spvo.date, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        } else if (this.isLuZaoDeadIII(spvo.date, luzaoList)) {
          String title =
              sxvo.getDuoFlagsTitle().trim().length() > 0 ? sxvo.getDuoFlagsTitle() + "???" : "???";
          String text =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " ????????????"
                  : "????????????";
          sxvo.setDuoFlagsTitle(title);
          sxvo.setDuoFlagsText(text);
        }

        // ?????????T
        // if ((sxvo.h1 >= sxvo.h2)) {
        // buy point
        if (spvo.low <= sxvo.hc6) {
          sxvo.setBuyFlagsTitle("B");
          sxvo.setBuyFlagsText(sxvo.hc6 + " HC6??????,??????1/3");
        }

        // sell point
        if (spvo.high >= sxvo.hc5) {
          sxvo.setSellFlagsTitle("S");
          sxvo.setSellFlagsText(sxvo.hc5 + " HC5??????, ??????1/3");
        }
        // }

        // 86?????????
        if (volumevo.maVolume < volumevo.hhvVolume / 10) {
          sxvo.setSuoFlagsTitle("???");
          sxvo.setSuoFlagsText("????????????");
        }

        // append if volume and bottom area or bottom gordon
        if (cpfvo.bottomAreaTitle.toString().trim().length() > 0) {
          sxvo.setSuoFlagsTitle(
              sxvo.getSuoFlagsTitle().trim() + cpfvo.bottomAreaTitle.toString().trim());
          sxvo.setSuoFlagsText(
              sxvo.getSuoFlagsText().trim() + cpfvo.bottomAreaText.toString().trim());
        }
        if (cpfvo.bottomGordonTitle.toString().trim().length() > 0) {
          sxvo.setSuoFlagsTitle(
              sxvo.getSuoFlagsTitle().trim() + cpfvo.bottomGordonTitle.toString().trim());
          sxvo.setSuoFlagsText(
              sxvo.getSuoFlagsText().trim() + cpfvo.bottomGordonText.toString().trim());
        }

        // append if it has weekGordon
        if (cpfvo.weekGordonTitle.toString().trim().length() > 0) {
          sxvo.setDuoFlagsTitle(cpfvo.weekGordonTitle.toString().trim() + sxvo.getDuoFlagsTitle());
          sxvo.setDuoFlagsText(cpfvo.weekGordonText.toString().trim() + sxvo.getDuoFlagsText());
        }

        // append if it has W Botton and Twice MACD Gordon
        if (cpfvo.wbottomMacdTwiceGordonTitle.toString().trim().length() > 0) {
          sxvo.setDuoFlagsTitle(
              cpfvo.wbottomMacdTwiceGordonTitle.toString().trim() + sxvo.getDuoFlagsTitle());
          sxvo.setDuoFlagsText(
              cpfvo.wbottomMacdTwiceGordonText.toString().trim() + sxvo.getDuoFlagsText());
        }

        // append if it has zijinliu
        if (cpfvo.ziJinLiuRuText.toString().trim().length() > 0) {
          sxvo.setDuoFlagsTitle(sxvo.getDuoFlagsTitle() + "???");
          String info =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " " : "";
          sxvo.setDuoFlagsText(info + "????????????");
        }

        // ??????????????????, 86????????? high * 0.618 = low
        if (this.isGordonLowPrice(spList, index)) {
          sxvo.setDuoFlagsTitle(sxvo.getDuoFlagsTitle() + "???");
          String info =
              sxvo.getDuoFlagsText().trim().length() > 0 ? sxvo.getDuoFlagsText() + " " : "";
          sxvo.setDuoFlagsText(info + "?????????????????????");
        }

      }

    }
    return sxList;
  }

  private ShenXianUIVO getShenXianIndVOByDate(String date, List<ShenXianUIVO> indList) {
    for (ShenXianUIVO vo : indList) {
      if (vo.date.equals(date))
        return vo;
    }
    return null;
  }

  private MacdVO getMacdIndVOByDate(String date, List<MacdVO> indList) {
    for (MacdVO vo : indList) {
      if (vo.date.equals(date))
        return vo;
    }
    return null;
  }

  private LuZaoVO getLuzaoIndVOByDate(String date, List<LuZaoVO> indList) {
    for (LuZaoVO vo : indList) {
      if (vo.date.equals(date))
        return vo;
    }
    return null;
  }

  private BBIVO getBBIIndVOByDate(String date, List<BBIVO> indList) {
    for (BBIVO vo : indList) {
      if (vo.date.equals(date))
        return vo;
    }
    return null;
  }

  // ???????????? close >= MA19
  private boolean isLuZaoGordonI(String date, List<StockPriceVO> spList, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      StockPriceVO curspvo = spList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          StockPriceVO prespvo = spList.get(index - 1);
          if (curspvo.close >= curvo.ma19 && prespvo.close < prevo.ma19) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ???????????? MA19 >= MA43
  private boolean isLuZaoGordonII(String date, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          if (curvo.ma19 >= curvo.ma43 && prevo.ma19 < prevo.ma43) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ???????????? MA19 >= MA86
  private boolean isLuZaoGordonIII(String date, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          if (curvo.ma19 >= curvo.ma86 && prevo.ma19 < prevo.ma86) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ???????????? MA43 >= MA86
  private boolean isLuZaoDeadI(String date, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          if (curvo.ma43 >= curvo.ma86 && prevo.ma43 < prevo.ma86) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ???????????? MA19 <= MA43
  private boolean isLuZaoDeadII(String date, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          if (curvo.ma19 < curvo.ma43 && prevo.ma19 >= prevo.ma43) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ???????????? MA43 <= MA86
  private boolean isLuZaoDeadIII(String date, List<LuZaoVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      LuZaoVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          LuZaoVO prevo = indList.get(index - 1);
          if (curvo.ma43 < curvo.ma86 && prevo.ma43 >= prevo.ma86) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isMacdGordon(String date, List<MacdVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      MacdVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          MacdVO prevo = indList.get(index - 1);
          if (curvo.macd >= 0 && prevo.macd < 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isMacdDead(String date, List<MacdVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      MacdVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          MacdVO prevo = indList.get(index - 1);
          if (curvo.macd <= 0 && prevo.macd > 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ??????
  private boolean isMacdYouDuo(String date, List<MacdVO> indList, List<StockPriceVO> spList) {
    for (int index = 0; index < indList.size(); index++) {
      MacdVO curvo = indList.get(index);
      StockPriceVO curPvo = spList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          MacdVO prevo = indList.get(index - 1);
          StockPriceVO prePvo = spList.get(index - 1);
          if (curvo.macd < 0) {
            if (curvo.macd < prevo.macd && curPvo.close > prePvo.close) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  // ??????
  private boolean isMacdYouKong(String date, List<MacdVO> indList, List<StockPriceVO> spList) {
    for (int index = 0; index < indList.size(); index++) {
      MacdVO curvo = indList.get(index);
      StockPriceVO curPvo = spList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          MacdVO prevo = indList.get(index - 1);
          StockPriceVO prePvo = spList.get(index - 1);
          if (curvo.macd > 0) {
            if (curvo.macd > prevo.macd && curPvo.close < prePvo.close) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean isBBIGordon(String date, List<BBIVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      BBIVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          BBIVO prevo = indList.get(index - 1);
          if (curvo.close >= curvo.bbi && prevo.close < prevo.bbi) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isBBIDead(String date, List<BBIVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      BBIVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          BBIVO prevo = indList.get(index - 1);
          if (curvo.close <= curvo.bbi && prevo.close > prevo.bbi) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isShenXianGordon(String date, List<ShenXianUIVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      ShenXianUIVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          ShenXianUIVO prevo = indList.get(index - 1);
          if (curvo.h1 >= curvo.h2 && prevo.h1 < prevo.h2) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isShenXianDead(String date, List<ShenXianUIVO> indList) {
    for (int index = 0; index < indList.size(); index++) {
      ShenXianUIVO curvo = indList.get(index);
      if (curvo.date.equals(date)) {
        if (index - 1 >= 0) {
          ShenXianUIVO prevo = indList.get(index - 1);
          if (curvo.h1 <= curvo.h2 && prevo.h1 > prevo.h2) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // ??????????????????, 86????????? high * 0.618 = low, current price is low
  private boolean isGordonLowPrice(List<StockPriceVO> spList, int currentIndex) {
    StockPriceVO highSpvo = spList.get(currentIndex);
    StockPriceVO currentSpvo = spList.get(currentIndex);
    int foundIndex = -1;
    for (int i = currentIndex - 1; i > 0 && i > (currentIndex - 86); i--) {
      StockPriceVO spvo = spList.get(i);
      if (spvo.low < currentSpvo.low) {
        // current spvo is not the lowest
        return false;
      }

      if (spvo.high > highSpvo.high) {
        highSpvo = spvo;
        foundIndex = i;
      }
    }

    if ((currentIndex - foundIndex) < 43) {
      // the high and low is too near, not meet the min of 43 days
      return false;
    }

    // check if the lowest price is around with high * 0.618
    if (highSpvo.high * 0.64 >= currentSpvo.low && highSpvo.high * 0.6 <= currentSpvo.low) {
      return true;
    }

    return false;
  }

  private List<VolumeVO> getMAVolumeList(List<StockPriceVO> spList) {
    List<VolumeVO> volumesList = new ArrayList<VolumeVO>();
    List<Double> volumes = new ArrayList<Double>();
    for (StockPriceVO spvo : spList) {
      volumes.add(spvo.volume + 0.0);
    }

    double[] maVolumes = new MAHelper().getMAList(Doubles.toArray(volumes), 3);
    double[] hhvVolumes = new IND().HHV(maVolumes, 86);

    for (int i = 0; i < spList.size(); i++) {
      StockPriceVO spvo = spList.get(i);
      VolumeVO vvo = new VolumeVO();
      vvo.stockId = spvo.stockId;
      vvo.date = spvo.date;
      vvo.volume = spvo.volume;
      vvo.maVolume = (long) maVolumes[i];
      vvo.hhvVolume = (long) hhvVolumes[i];

      volumesList.add(vvo);
    }

    return volumesList;
  }

  private VolumeVO getVolumeVOByDate(String date, List<VolumeVO> indList) {
    for (VolumeVO vo : indList) {
      if (vo.date.equals(date))
        return vo;
    }
    return null;
  }

  private CheckPointFlagsVO checkPoints(String date, List<CheckPointDailySelectionVO> checkPoints) {
    CheckPointFlagsVO cpfvo = new CheckPointFlagsVO();
    for (CheckPointDailySelectionVO cpvo : checkPoints) {
      if (cpvo.date.equals(date)) {
        // check zijinliu
        for (String cp : zijinliuViewnames) {
          if (cpvo.checkPoint.toUpperCase().equals(cp.toUpperCase())) {
            cpfvo.ziJinLiuRuTitle.append("???");
            cpfvo.ziJinLiuRuText.append("????????????");
            break;
          }
        }

        // check week Gordon
        if (cpvo.checkPoint.toUpperCase().contains("MACD_WEEK_GORDON_MACD_DAY_DIF_CROSS_0")) {
          cpfvo.weekGordonTitle.append("MD???");
          cpfvo.weekGordonText.append("MACD????????????DIF????????????");
        }
        if (cpvo.checkPoint.toUpperCase().contains("MACD_WEEK_GORDON_KDJ_WEEK_GORDON")) {
          cpfvo.weekGordonTitle.append("MK???");
          cpfvo.weekGordonText.append("MACD??????????????????KDJ??????");
        }

        // check buttom
        if (cpvo.checkPoint.toUpperCase().equals("WR_Bottom_Area".toUpperCase())) {
          cpfvo.bottomAreaTitle.append("W???");
          cpfvo.bottomAreaText.append("WR????????????");
        }
        if (cpvo.checkPoint.toUpperCase().equals("QSDD_Bottom_Area".toUpperCase())) {
          cpfvo.bottomAreaTitle.append("Q???");
          cpfvo.bottomAreaText.append("QSDD????????????");
        }

        // check buttom gordon

        if (cpvo.checkPoint.toUpperCase().equals("WR_Bottom_Gordon".toUpperCase())) {
          cpfvo.bottomGordonTitle.append("W???");
          cpfvo.bottomGordonText.append("WR????????????");
        }
        if (cpvo.checkPoint.toUpperCase().equals("QSDD_Bottom_Gordon".toUpperCase())) {
          cpfvo.bottomGordonTitle.append("Q???");
          cpfvo.bottomGordonText.append("QSDD????????????");
        }

        // macd???????????????W??????MACD??????
        if (cpvo.checkPoint.toUpperCase()
            .contains("MACD_TWICE_GORDON_W_Botton_MACD_DI_BEILI".toUpperCase())) {
          cpfvo.wbottomMacdTwiceGordonTitle.append("W???");
          cpfvo.wbottomMacdTwiceGordonText.append("MACD????????????,W?????????");
        }
      }

    }
    return cpfvo;
  }

  private List<CheckPointDailySelectionVO> getCheckPoints(String stockId) {
    return checkPointDailySelectionCache.getCheckPointByStockId(stockId);
  }
}
