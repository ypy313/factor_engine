package com.changtian.factor.common;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.changtian.factor.entity.riding.*;
import com.changtian.factor.strategy.CurveChangeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.changtian.factor.common.Constants.*;

@Slf4j
public class BondCalculatorUtils {
    private static final String YYYY_MM_DD = "yyyyMMdd";

    /**
     * @param start 开始数
     * @param stop  结束数
     * @param step  步距
     */
    public static BigDecimal[] arange(BigDecimal start, BigDecimal stop, BigDecimal step) {
        List<BigDecimal> result = new ArrayList<>();
        if (stop.subtract(start).signum() != step.signum()) {
            return new BigDecimal[0];
        }
        result.add(start);
        BigDecimal value = start;
        while ((value = value.add(step)).compareTo(stop) < 0) {
            result.add(value);
        }
        if (CollectionUtils.isEmpty(result)) {
            return new BigDecimal[0];
        } else {
            return result.toArray(new BigDecimal[0]);
        }
    }

    /**
     * 计算收益率曲线
     */
    public static BigDecimal[] deriv(BigDecimal[] xAxisVal, BigDecimal[] yAxisVal) {
        BigDecimal[] resultVal = new BigDecimal[xAxisVal.length];
        resultVal[0] = (yAxisVal[1].subtract(yAxisVal[0])).divide(xAxisVal[1]
                .subtract(xAxisVal[0]), 10, RoundingMode.HALF_UP);
        for (int i = 0; i < xAxisVal.length - 1; i++) {
            if (i == xAxisVal.length - 2) {
                resultVal[i + 1] = (yAxisVal[i + 1].subtract(yAxisVal[i]))
                        .divide(xAxisVal[i + 1].subtract(xAxisVal[i]), 10, RoundingMode.HALF_UP);
            }
        }
        return resultVal;
    }

    /**
     * 埃尔米特插值函数
     *
     * @return 结果
     */
    public static BigDecimal[] cubicHermiteInterp(BigDecimal[] xAxisVal, BigDecimal[] yAxisVal,
                                                  BigDecimal[] xDeriv, BigDecimal[] xImterp) {
        int[] xLoc = new int[xImterp.length];
        for (int i = 0; i < xImterp.length; i++) {
            for (int j = 0; j < xAxisVal.length; j++) {
                if (xImterp[i].compareTo(xAxisVal[j]) <= 0) {
                    xLoc[i] = j;
                    if (xLoc[i] == 0) {
                        xLoc[i] = 1;
                    }
                    break;
                }
            }
        }
        BigDecimal[] resList = new BigDecimal[xLoc.length];
        for (int j = 0; j < xLoc.length; j++) {
            int i = xLoc[j];
            double x = xImterp[j].doubleValue();
            double h_i = xAxisVal[i].subtract(xAxisVal[i - 1]).doubleValue();
            double H_i = (1 + 2 * (x - xAxisVal[i - 1].doubleValue()) / h_i) * (Math.pow((x - xAxisVal[i].doubleValue()) / h_i, 2) * yAxisVal[i - 1].doubleValue() +
                    (1 + 2 * (xAxisVal[i].doubleValue() - x) / h_i) * (Math.pow((x - xAxisVal[i - 1].doubleValue()) / h_i, 2)) * yAxisVal[i].doubleValue() +
                    (x - xAxisVal[i - 1].doubleValue()) * (Math.pow((x - xAxisVal[i].doubleValue()) / h_i, 2)) * xDeriv[i - 1].doubleValue() +
                    (x - xAxisVal[i].doubleValue()) * (Math.pow((x - xAxisVal[i - 1].doubleValue()) / h_i, 2)) * xDeriv[i].doubleValue());
            resList[j] = BigDecimal.valueOf(H_i).setScale(10, RoundingMode.HALF_UP);
        }
        return resList;
    }

    /**
     * 计算年付息频率
     *
     * @return 次数
     */
    public static int interestPaymentFrequency(String infoCarryDate, String infoPaymentDate) {
        double daysBetween = daysBetween(infoCarryDate, infoPaymentDate);
        if (daysBetween == 0) {
            return 0;
        }
        return (int) Math.round(365 / daysBetween);
    }

    /**
     * 计算区间付息
     *
     * @param bondCFList 现金流
     * @param days       持有期限天数
     * @return 区间付息
     */
    public static BigDecimal getIntervalInterestPayment(List<BondCF> bondCFList, int days) {
        if (CollectionUtils.isEmpty(bondCFList) || days <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        //根据起息日进行排序
        List<BondCF> bondCfSortist = bondCFList.stream()
                .sorted(Comparator.comparing(BondCF::getInfoCarryDate)).collect(Collectors.toList());
        //计算days天后的日期
        String afterNowData = DateUtil.parseDateToStr(YYYY_MM_DD, DateUtil.getAfterDays(days));
        for (BondCF bondCF : bondCFList) {
            String infoPayment = bondCF.getInfoPaymentDate();
            if (isEffectiveDate(infoPayment,getCurrentDate(), afterNowData)) {
                result = result.add(BigDecimal.valueOf(bondCF.getInfoPaymentInterest()));
            }
        }
        return result;
    }

    /**
     * 计算两日期间隔天数
     *
     * @param startDate 开始时间20200913
     * @param endDate   结束时间20200913
     * @return 间隔天数
     */
    public static int daysBetween(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        Date sDate = null;
        Date eDate = null;
        try {
            sDate = sdf.parse(startDate);
            eDate = sdf.parse(endDate);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        assert sDate != null;
        long startTime = sDate.getTime();
        assert eDate != null;
        long endTime = eDate.getTime();
        long betweenDays = (endTime - startTime) / (1000 * 60 * 60 * 24);
        return (int) betweenDays;
    }

    /**
     * 获取当前日期
     *
     * @return 格式 yyyyMMdd
     */
    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD);
        return formatter.format(new Date());
    }

    /**
     * 结算日至到期兑付日的债券付息次数
     *
     * @return S-单利 C-单例
     */
    public static int interestPaymentsCount(List<BondCF> bondCFList, String settlementDate) {
        //当前日期处于最后一个付息区间时属于单利 否则为复利
        int count = 0;
        if (null == bondCFList || bondCFList.isEmpty()) {
            return count;
        }
        //根据起息日进行倒序排序
        bondCFList = bondCFList.stream()
                .sorted(Comparator.comparing(BondCF::getInfoCarryDate).reversed())
                .filter(b -> settlementDate.compareTo(b.getInfoPaymentDate()) < 0)
                .collect(Collectors.toList());
        if (bondCFList.isEmpty()) {
            return count;
        }
        //取第一条数据，因为倒叙属于最后一次付息区间
        BondCF bondCF = bondCFList.get(0);
        //判断当前时间属于最后一次
        if (isEffectiveDate(settlementDate, bondCF.getInfoCarryDate(), bondCF.getInfoPaymentDate())) {
            //如果是最后一个区间时单例
            count = 1;
        } else {
            count = bondCFList.size();
        }
        return count;
    }

    /**
     * 断当前时间是否在[startTime,endTime] 区间
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 在时间段内返回true，不在返回false
     */
    public static boolean isEffectiveDate(String nowTime, String startTime, String endTime) {
        if (null != nowTime && (nowTime.equals(startTime) || nowTime.equals(endTime))) {
            return true;
        }
        Calendar date = Calendar.getInstance();
        Calendar begin = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
            date.setTime(sdf.parse(nowTime));
            begin.setTime(sdf.parse(startTime));
            end.setTime(sdf.parse(endTime));
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date.after(begin) && date.before(end);
    }

    /**
     * 应计利息
     *
     * @return 返回应计利息
     */
    public static BigDecimal accruedInterest(BigDecimal c, BigDecimal t, BigDecimal ts) {
        return c.multiply(t).divide(ts, 10, RoundingMode.HALF_UP);
    }

    /**
     * 当前区间的利率
     *
     * @return 结果
     */
    public static BigDecimal getNowRate(List<BondCF> bondCFList, String settlementDate) {
        if (CollectionUtils.isEmpty(bondCFList)) {
            return BigDecimal.ZERO;
        }
        bondCFList = bondCFList.stream().sorted(Comparator.comparing(BondCF::getInfoCarryDate)).collect(Collectors.toList());
        for (BondCF bondCF : bondCFList) {
            if (isEffectiveDate(settlementDate, bondCF.getInfoCarryDate(), bondCF.getInfoPaymentDate())) {
                return BigDecimal.valueOf(bondCF.getInfoPaymentInterest());
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 净价
     *
     * @return 计算净价 全价减去应计利息
     */
    public static BigDecimal netPrice(BigDecimal fullPrice, BigDecimal AI) {
        return fullPrice.subtract(AI);
    }

    /**
     * 到期收益率转全价
     *
     * @param ytm 到期收益率
     * @param f   年付息频率
     * @param n   算日至到期兑付日的债券付息次数
     * @param w   d/TS
     * @param d   债券结算日至下一最近付息日之间的实际天数
     * @param TY  当前计息年度的实际天数，算头不算尾
     * @return 返回价格
     */
    public static BigDecimal ytmToFullPrice(BigDecimal ytm, int f, int n, BigDecimal w, BigDecimal d, int TY
            , List<BondCF> bondCFList, String settlementDate) {
        BigDecimal fullPrice;
        //根据起息日进行倒序排序
        bondCFList = bondCFList.stream().sorted(Comparator.comparing(BondCF::getInfoCarryDate))
                .filter(b -> settlementDate.compareTo(b.getInfoPaymentDate()) < 0).collect(Collectors.toList());
        if (n == 1) {
            //单利 1+ytm*d/TY
            BigDecimal decimal = ytm.multiply(d).divide(BigDecimal.valueOf(TY), 10, RoundingMode.HALF_UP);
            BigDecimal bigDecimal = BigDecimal.valueOf(1).add(decimal);
            fullPrice = BigDecimal.valueOf(bondCFList.get(0).getInfoPaymentSum())
                    .divide(bigDecimal, 10, RoundingMode.HALF_UP);
        } else {
            //复利
            //剔除集合中小于某个值
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < bondCFList.size(); i++) {
                BondCF bondCF = bondCFList.get(i);
                BigDecimal baseNumber = BigDecimal.valueOf(1)
                        .add(ytm.divide(BigDecimal.valueOf(f), 10, RoundingMode.HALF_UP));
                double indexNumber = -(w.doubleValue() + i);
                sum = sum.add((BigDecimal.valueOf(bondCF.getInfoPaymentSum())
                        .multiply(BigDecimal.valueOf(Math.pow(baseNumber.doubleValue(), indexNumber)))));
            }
            fullPrice = sum;
        }
        return fullPrice;
    }

    /**
     * 价格转收益率（复利）
     *
     * @param PV         全价
     * @param bondCFList 债券集合
     * @param f          年付息频率
     * @param n          算日至到期兑付日的债券付息次数
     * @param d          债券结算日至下一最近付息日之间的实际天数
     * @param TY         当前计息年度的实际天数，算头不算尾
     * @return 收益率
     */
    public static BigDecimal priceToYtmDichotomy(BigDecimal PV, List<BondCF> bondCFList, int f, int n, BigDecimal w,
                                                 BigDecimal d, int TY, String settlementDate) {
        BigDecimal ytm_l = new BigDecimal(-1);
        BigDecimal ytm_r = new BigDecimal(1);
        while (ytm_r.subtract(ytm_l).compareTo(BigDecimal.valueOf(0.000001)) > 0) {
            BigDecimal ytm_m = ytm_l.add(ytm_r).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
            if (PV.compareTo(ytmToFullPrice(ytm_m, f, n, w, d, TY, bondCFList, settlementDate)) < 0) {
                ytm_l = ytm_m;
            } else if (PV.compareTo(ytmToFullPrice(ytm_m, f, n, w, d, TY, bondCFList, settlementDate)) > 0) {
                ytm_r = ytm_m;
            } else {
                return ytm_m;
            }
        }
        return ytm_l;
    }

    /**
     * 价格转收益率
     *
     * @param PV              全价
     * @param bondCFListOrder 债券集合
     * @param f               年付息频率
     * @param n               算日至到期兑付日的债券付息次数
     * @param w
     * @param d               债券结算日至下一最近付息日之间的实际天数
     * @param TY              当前计息年度的实际太难书，算头不算尾
     * @return 收益率
     */
    public static BigDecimal priceToYtm(BigDecimal PV, List<BondCF> bondCFListOrder, int f
            , int n, BigDecimal w, BigDecimal d, int TY, String settlementDate) {
        BigDecimal ytm;
        if (n == 1) {
            ytm = ((BigDecimal.valueOf(bondCFListOrder.get(0).getInfoPaymentSum()).subtract(PV))
                    .divide(PV, 10, RoundingMode.HALF_UP)).divide(d, 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(TY));
        } else {
            ytm = priceToYtmDichotomy(PV, bondCFListOrder, f, n, w, d, TY, settlementDate);
        }
        return ytm;
    }

    /**
     * 久期计算
     *
     * @return 久期
     */
    public static BigDecimal durationCalculate(BigDecimal ytm, List<BondCF> bondCFList, int f
            , int n, BigDecimal w, BigDecimal pv, BigDecimal deltYtm, BigDecimal d, int ty, String settlementDate) {
        BigDecimal vLeft = ytmToFullPrice(ytm.subtract(deltYtm), f, n, w, d, ty, bondCFList, settlementDate);
        BigDecimal vRight = ytmToFullPrice(ytm.add(deltYtm), f, n, w, d, ty, bondCFList, settlementDate);
        return vLeft.subtract(vRight).divide(BigDecimal.valueOf(2).multiply(pv).multiply(deltYtm), 10, RoundingMode.HALF_UP);
    }

    /**
     * 获取剩余年限
     * 当前计息年度的实际天数，算头不算尾
     * 计息年度是指发行公告中标明的第一个起息日至次一年度对应的同月同日的时间间隔尾第一个计息年度
     *
     * @return 天数 TY 和剩余年限 remainYear
     */
    public static Map<String, BigDecimal> getTyAndRemainYear(List<BondCF> bondCFList, String settlementDate) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(bondCFList)) {
            return resultMap;
        }
        //根据起息日进行倒叙排序
        bondCFList = bondCFList.stream().sorted(Comparator.comparing(BondCF::getInfoCarryDate).reversed())
                .filter(b -> settlementDate.compareTo(b.getInfoPaymentDate()) < 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bondCFList)) {
            return resultMap;
        }
        //获取最后付息日
        String paymentDate = bondCFList.get(0).getInfoPaymentDate();
        String nextPaymentDate = "";
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        if (settlementDate.compareTo(paymentDate) < 0) {
            Calendar date = Calendar.getInstance();
            try {
                nextPaymentDate = paymentDate;
                date.setTime(sdf.parse(paymentDate));
                date.add(Calendar.YEAR, -1);//减一年
                paymentDate = sdf.format(date.getTime());
                count++;
            } catch (ParseException e) {
                log.error(e.getMessage());
            }
        }
        //获取计息年度天数 365 366
        int days = daysBetween(paymentDate, nextPaymentDate);
        //当前计息年度的实际天数
        resultMap.put("TY", new BigDecimal(days));
        BigDecimal currentYear = new BigDecimal(daysBetween(settlementDate, nextPaymentDate))
                .divide(new BigDecimal(days), 10, RoundingMode.HALF_UP);
        //剩余年限
        resultMap.put("remainYear", new BigDecimal(count - 1).add(currentYear));
        return resultMap;
    }

    /**
     * d 获取债券结算日至下一最近付息日之间的实际天数
     * t 债券结算日至上一最近付息日之间的实际天数
     *
     * @param bondCFList 债券
     * @return 返回map key d t TS
     */
    public static Map<String, BigDecimal> getDAndT(String settlementDate, List<BondCF> bondCFList) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        if (null == bondCFList || bondCFList.isEmpty()) {
            return resultMap;
        }
        if (StringUtils.isEmpty(settlementDate)) {
            //如果是空设置当前时间
            settlementDate = getCurrentDate();
        }
        //根据起息日进行倒序排序
        bondCFList = bondCFList.stream()
                .sorted(Comparator.comparing(BondCF::getInfoCarryDate).reversed())
                .collect(Collectors.toList());
        //获取结算日处于哪个付息区间
        String infoCarryDate = "";
        String infoPaymentDate = "";
        for (int i = 0; i < bondCFList.size(); i++) {
            String paymentDate = bondCFList.get(i).getInfoPaymentDate();
            if (i == 0 && settlementDate.compareTo(paymentDate) == 0) {
                return resultMap;
            }
            //当只有一条现金流时
            if (i == bondCFList.size() - 1 && settlementDate.compareTo(paymentDate) < 0) {
                infoPaymentDate = bondCFList.get(i).getInfoPaymentDate();
                infoCarryDate = bondCFList.get(i).getInfoCarryDate();
                break;
            } else if (settlementDate.compareTo(paymentDate) > 0) {
                infoPaymentDate = bondCFList.get(i - 1).getInfoPaymentDate();
                infoCarryDate = bondCFList.get(i - 1).getInfoCarryDate();
                break;
            } else if (settlementDate.compareTo(paymentDate) == 0) {
                infoCarryDate = bondCFList.get(i).getInfoCarryDate();
                infoPaymentDate = bondCFList.get(i).getInfoPaymentDate();
                break;
            }
        }
        resultMap.put("t", BigDecimal.valueOf(daysBetween(infoCarryDate, settlementDate)));
        resultMap.put("d", BigDecimal.valueOf(daysBetween(settlementDate, settlementDate)));
        resultMap.put("TS", BigDecimal.valueOf(daysBetween(infoCarryDate, infoPaymentDate)));
        return resultMap;
    }

    /**
     * 估算算法 carry 计算
     *
     * @param currentYield  当前收益率
     * @param futureYield   未来收益率
     * @param rate          利率（shibor7D 或者 repo 7D）
     * @param holdingPeriod 持有期限
     * @return 结果
     */
    public static BigDecimal algorithmEstimateCarry(BigDecimal currentYield, BigDecimal futureYield
            , BigDecimal rate, BigDecimal holdingPeriod) {
        return ((currentYield.add(futureYield)).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
                .subtract(rate)).multiply(holdingPeriod).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
    }

    /**
     * 估算算法 roll down 计算
     *
     * @param currentYield 当前收益率
     * @param futureYield  未来收益率
     * @param meanDuration 久期均值
     * @return 结果
     */
    public static BigDecimal algorithmEstimateRollDown(BigDecimal currentYield, BigDecimal futureYield, BigDecimal meanDuration) {
        return (currentYield.subtract(futureYield)).multiply(meanDuration).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 精算算法carry计算
     *
     * @param coupon        票面利率
     * @param rate          利率（shibor7D 或者 repo7D）
     * @param holdingPeriod 持有期限 天数
     * @return 结果
     */
    public static BigDecimal algorithmActuaryCarry(BigDecimal coupon, BigDecimal rate, BigDecimal holdingPeriod) {
        return (coupon.subtract(rate)).multiply(holdingPeriod).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
    }

    /**
     * 精算算法综合损益计算
     *
     * @param futureFullPrice         未来全价
     * @param currentFullPrice        当前全价
     * @param intervalInterestPayment 区间付息
     * @param rate                    利率（shibor7D 或者 repo7D）
     * @param holdingPeriod           持有期限
     * @return 结果
     */
    public static BigDecimal algorithmActuaryComprehensive(BigDecimal futureFullPrice, BigDecimal currentFullPrice
            , BigDecimal intervalInterestPayment, BigDecimal rate, BigDecimal holdingPeriod) {
        return (((futureFullPrice.subtract(currentFullPrice)).add(intervalInterestPayment))
                .divide(currentFullPrice, 10, RoundingMode.HALF_UP))
                .subtract((rate.multiply(holdingPeriod).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP)));

    }

    /**
     * 根据拥有期限算天数
     *
     * @param shp 类型
     */
    public static int getDaysToShowHoldPeriod(String shp, String customValue, String settlementDate) {
        int days = 0;
        Date date = DateUtil.stringToDate(settlementDate, DateUtil.DAYSTR);
        if ("1M".equals(shp)) {
            days = DateUtil.differentDaysByMillisecond(date, DateUtil.getAfterMonth(settlementDate, 1));
        } else if ("3M".equals(shp)) {
            days = DateUtil.differentDaysByMillisecond(date, DateUtil.getAfterMonth(settlementDate, 3));
        } else if ("6M".equals(shp)) {
            days = DateUtil.differentDaysByMillisecond(date, DateUtil.getAfterMonth(settlementDate, 6));
        } else if ("9M".equals(shp)) {
            days = DateUtil.differentDaysByMillisecond(date, DateUtil.getAfterMonth(settlementDate, 9));
        } else if ("1Y".equals(shp)) {
            days = DateUtil.differentDaysByMillisecond(date, DateUtil.getAfterMonth(settlementDate, 12));
        } else {
            if (StringUtils.isEmpty(customValue)) {
                return days;
            }
            Date customValueDate = DateUtil.stringToDate(customValue, DateUtil.YYYY_MM_DD);
            days = DateUtil.differentDaysByMillisecond(date, customValueDate);
        }
        return days;
    }

    /**
     * 计算carry,rollDown 综合损益
     *
     * @param bondCFList            现金流
     * @param futureFullPrice       未来全价
     * @param nowFullPrice          当前全价
     * @param algorithmStr          算法 estimate actuary
     * @param infoCouponRate        票面利率
     * @param currentValuationYield 当前收益率
     * @param fundsRate             利率（shibor7D 或者 repo7D）
     * @param futureYield           未来收益率
     * @param meanDuration          久期均值
     * @param days                  持有天数
     * @return map carry rollDown 综合损益
     */
    public static Map<String, BigDecimal> getComprehensive(List<BondCF> bondCFList, BigDecimal futureFullPrice,
                                                           BigDecimal nowFullPrice, String algorithmStr, BigDecimal infoCouponRate,
                                                           BigDecimal currentValuationYield, BigDecimal fundsRate,
                                                           BigDecimal futureYield, BigDecimal meanDuration, int days,
                                                           int f, int n, BigDecimal w, BigDecimal d, int TY, String settlementDate) {
        if (CollectionUtils.isEmpty(bondCFList)) {
            return new HashMap<>();
        }
        //初始化carry rolldown 综合损益 盈亏平衡点
        BigDecimal carry = BigDecimal.ZERO;
        BigDecimal rollDown = BigDecimal.ZERO;
        BigDecimal comprehensive = BigDecimal.ZERO;
        BigDecimal breakEven = BigDecimal.ZERO;
        //对现金流排序
        List<BondCF> bondCfListOrder = bondCFList.stream().sorted(Comparator.comparing(BondCF::getInfoCarryDate).reversed())
                .filter(b -> settlementDate.compareTo(b.getInfoPaymentDate()) < 0).collect(Collectors.toList());
        carry = algorithmEstimateCarry(currentValuationYield, futureYield, fundsRate, BigDecimal.valueOf(days));

        if ("estimate".equals(algorithmStr)) {
            //估算算法
            rollDown = algorithmEstimateRollDown(currentValuationYield, futureYield, meanDuration);
            comprehensive = carry.add(rollDown);
            breakEven = getBreakEvenByEstimate(meanDuration, currentValuationYield, days, fundsRate);
        } else if ("actuary".equals(algorithmStr)) {
            //精算
            //计算区间付息
            BigDecimal intPayment = getIntervalInterestPayment(bondCFList, days);
            comprehensive = algorithmActuaryComprehensive(futureFullPrice, nowFullPrice
                    , intPayment, fundsRate, BigDecimal.valueOf(days));
            rollDown = comprehensive.subtract(carry);
            breakEven = getBreakEvenByActuary(nowFullPrice, intPayment
                    , days, fundsRate, bondCfListOrder, f, n, w, d, TY, settlementDate);
        } else {
            log.error("不支持其他类型算法");
        }
        Map<String, BigDecimal> resultMap = new HashMap<>();
        resultMap.put("carry", carry);
        resultMap.put("rollDown", rollDown);
        resultMap.put("comprehensive", comprehensive);
        resultMap.put("breakEven", breakEven);
        return resultMap;
    }

    /**
     * 计算估算盈亏平衡点
     *
     * @param meanDuration          久期均衡
     * @param currentValuationYield 当前首义路
     * @param days                  天数
     * @param fundsRate             资金利率
     * @return 返回结果
     */
    public static BigDecimal getBreakEvenByEstimate(BigDecimal meanDuration, BigDecimal currentValuationYield,
                                                    int days, BigDecimal fundsRate) {
        BigDecimal denominator = ((BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(365)).multiply(meanDuration).multiply(currentValuationYield))
                .add(BigDecimal.valueOf(days).multiply(currentValuationYield))).subtract(BigDecimal.valueOf(2)
                .multiply(BigDecimal.valueOf(days)).multiply(fundsRate));
        BigDecimal numerator = (BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(365)).multiply(meanDuration))
                .subtract(BigDecimal.valueOf(days));
        return denominator.divide(numerator, 10, RoundingMode.HALF_UP);
    }

    /**
     * 计算精算盈亏平衡点
     *
     * @param nowFullPrice 当前全价
     * @param intPayment   区息付息
     * @param days         天数
     * @param fundsRate    资金利率
     * @return 返回盈亏平衡点
     */
    public static BigDecimal getBreakEvenByActuary(BigDecimal nowFullPrice, BigDecimal intPayment, int days, BigDecimal fundsRate,
                                                   List<BondCF> bondCfListOrder, int f, int n, BigDecimal w, BigDecimal d, int TY, String settlementDate) {
        BigDecimal aa = fundsRate.multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        BigDecimal futureFullPrice = nowFullPrice.subtract(intPayment).add(aa);
        //未来
        return priceToYtm(futureFullPrice, bondCfListOrder, f, n, w, d, TY, settlementDate);
    }

    /**
     * 曲线取点 获取当前未来曲线取点
     * @param curveCnbdList 收益率曲线
     * @param nowRemainYear 当前剩余年限
     * @param futureRemainYear 未来剩余年限
     * @param type 债券类别1232（国债） ，1322（口行债），2142（国开债），2352（农发债）
     * @return
     */
    public static Map<String, BigDecimal> getDeviate(List<CbondCurveCnbd> curveCnbdList, List<RidingAssetPool> voList,
                                                     BigDecimal nowRemainYear, BigDecimal futureRemainYear, String type) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        String maturityCnbd = LoadConfig.getProp().getProperty("maturity.cnbd");
        String step = LoadConfig.getProp().getProperty("maturity.cnbd.step");
        if (StringUtils.isEmpty(maturityCnbd)) {
            return resultMap;
        }
        String[] maturityCnbdArr = maturityCnbd.split(",");
        //原始收益率曲线
        Map<String, BigDecimal[]> oriputPutMap = new HashMap<>();
        //x坐标初始化
        BigDecimal[] xAxis = new BigDecimal[maturityCnbdArr.length];
        for (int i = 0; i < maturityCnbdArr.length; i++) {
            xAxis[i] = new BigDecimal(maturityCnbdArr[i]);
        }
        //根据债券类型进行分组
        Map<String, List<CbondCurveCnbd>> groupList = curveCnbdList.stream()
                .collect(Collectors.groupingBy(CbondCurveCnbd::getAnalCurvenumber));
        //获取当前收益率曲线
        List<CbondCurveCnbd> cbondCurveCnbds = groupList.get(type);
        if (CollectionUtils.isEmpty(cbondCurveCnbds)) {
            return resultMap;
        }
        //now arange
        BigDecimal[] nowResult = arange(nowRemainYear,
                nowRemainYear.add(new BigDecimal(step)), new BigDecimal(step));
        //遍历生成实时的收益率曲线
        cbondCurveCnbds = cbondCurveCnbds.stream().sorted(Comparator.comparing(CbondCurveCnbd::getAnalCurveterm))
                .collect(Collectors.toList());
        BigDecimal[] bigDecimals = new BigDecimal[cbondCurveCnbds.size()];
        for (int i = 0; i < cbondCurveCnbds.size(); i++) {
            bigDecimals[i] = BigDecimal.valueOf(cbondCurveCnbds.get(i).getAnalYield());
        }
        //设置值
        oriputPutMap.put(type, bigDecimals);
        //计算当前曲线利率
        BigDecimal[] k = deriv(xAxis, bigDecimals);
        BigDecimal[] h = cubicHermiteInterp(xAxis, bigDecimals, k, nowResult);
        resultMap.put("nowDeviate", h[0]);
        //计算未来曲线
        if (CollectionUtils.isEmpty(voList)) {
            //如果未来是空不进行计算
            return resultMap;
        }
        for (RidingAssetPool ridingAssetPool : voList) {
            //资产池下所有的收益率曲线属于公共的，取一条即可
            RidingStrategyInstance instance = ridingAssetPool.getStrategyInstanceVoList().get(0);
            if (null == instance) {
                break;
            }
            List<RidingInputConfig> inputConfigList = instance.getInputConfigList();
            if (CollectionUtils.isEmpty(inputConfigList)) {
                break;
            }
            //获取偏离类型
            String curveChang = getPropertyValue(inputConfigList, CURVE_CHANGE_KEY);
            BigDecimal[] futureOutPut = new BigDecimal[maturityCnbdArr.length];
            if (CurveChangeEnum.NOCHANGE.getValue().equals(curveChang)) {
                //不变 值保持与收益率曲线一致
                futureOutPut = oriputPutMap.get(type);
            } else if (CurveChangeEnum.UPPER.getValue().equals(curveChang) ||
                    CurveChangeEnum.DOWN.getValue().equals(curveChang)) {
                //向上 获取向上偏离值
                String curveDeviationValue = getPropertyValue(inputConfigList,CURVE_DEVIATION_VALUE_KEY);
                if (StringUtils.isEmpty(curveDeviationValue)) {
                    break;
                }
                BigDecimal bigDecimal = new BigDecimal(curveDeviationValue)
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                futureOutPut = oriputPutMap.get(type);
                for (int i = 0; i < futureOutPut.length; i++) {
                    if (CurveChangeEnum.UPPER.getValue().equals(curveChang)) {
                        futureOutPut[i] = futureOutPut[i].add(bigDecimal);
                    }else {
                        futureOutPut[i] = futureOutPut[i].subtract(bigDecimal);
                    }
                }
            }else if (CurveChangeEnum.CUSTOM.getValue().equals(curveChang)){
                //获取债务类型
                String bondCategory = getPropertyValue(inputConfigList,BOND_CATEGORY_KEY);
                //自定义
                JSONArray object = JSONUtil.parseArray(getPropertyValue(inputConfigList,bondCategory));
                List<Double> doubleList = JSONUtil.toList(object, Double.class);
                for (int j = 0; j < doubleList.size(); j++) {
                    futureOutPut[j] = BigDecimal.valueOf(doubleList.get(j));
                }
            }else {
                log.error("curveChange:{} error", curveChang);
                break;
            }
            //future arange
            BigDecimal[] futureResult = arange(futureRemainYear,
                    futureRemainYear.add(new BigDecimal(step)),new BigDecimal(step));
            BigDecimal[] fk = deriv(xAxis,futureOutPut);
            BigDecimal[] fh = cubicHermiteInterp(xAxis,futureOutPut,fk,futureResult);
            resultMap.put("futureDeviation", fh[0]);
        }
        return resultMap;
    }

    /**
     * 获取属性值
     * @param inputConfigList 数据源
     * @return 返回结果集
     */
    public static String getPropertyValue(List<RidingInputConfig> inputConfigList,String propertyKey){
        String value = "";
        if (CollectionUtils.isEmpty(inputConfigList)|| StringUtils.isEmpty(propertyKey)) {
            return value;
        }
        for (RidingInputConfig config : inputConfigList) {
            if (propertyKey.equals(config.getPropertyKey())) {
                value = config.getPropertyValue();
                break;
            }
        }
        return value;
    }
}
