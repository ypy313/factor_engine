package com.changtian.factor.flink.mtime;

import com.changtian.factor.common.DateUtil;
import com.changtian.factor.enums.PeriodEnum;
import com.changtian.factor.job.HolidayManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 贵金属金交所
 */
@Slf4j
public class PmSGEDateTimeUtils extends ReuterDateTimeUtils{

    /**
     * [beginTime,endTime）
     * @param beginTime 20221130 15:25:59 800
     * @param period 1Min
     * @return bar所在的[beginTime,endTime]
     */
    public static Tuple2<String,String> getTimeWindow(String beginTime, PeriodEnum period){
        LocalDateTime begin = ReuterDateTimeUtils.parse(beginTime);
        //查找最近可以被step 整除的点
        LocalDateTime start1 = findBeginTime(begin, period);
        //开始时间为null,则不需要计算结束时间
        if (start1==null) {
            return null;
        }
        //计算结束时间
        LocalDateTime end1 = findEndTime(start1, period);
        String resBegin = ReuterDateTimeUtils.format(start1);
        String resEnd = ReuterDateTimeUtils.format(end1);
        return Tuple2.of(resBegin,resEnd);
    }

    /**
     * 获取结束时间
     * @param beginTime 开始时间
     * @param period 1Min
     */
    public static LocalDateTime findEndTime(LocalDateTime beginTime, PeriodEnum period) {
        int step = period.getValue();
        ChronoUnit unit = period.getUnit();
        switch (unit) {
            case SECONDS:
            case MINUTES:
                return beginTime.plus(step, unit);
            case HOURS:
                return endHourHandler(beginTime, period);
            case DAYS:
                return endDayHandler(beginTime);
            case WEEKS:
            case MONTHS:
                return endMonthsWeekHandler(beginTime, step, unit);
            default:
                log.info("该识别单位不明确：{}", unit.name());
                break;
        }
        throw new UnsupportedOperationException(unit.name() + "不支持");
    }

    /**
     * 月份与周期结束时间融合
     * @param beginTime beginTime
     * @param step step
     * @param unit unit
     */
    public static LocalDateTime endMonthsWeekHandler(LocalDateTime beginTime,int step,ChronoUnit unit) {
        //普通交易日开始时间时间戳为上一日夜盘21：00的时间，结束时间为开始时间后一天的15：00
        //节假日（除周六，周日）后第一个交易日1D的时间戳是当天日盘开始时间9：00，结束时间为当天的15：00
        if (beginTime.getHour()==21) {
            LocalDateTime resultTime = beginTime.plusDays(1).plus(step, unit);
            return LocalDateTime.of(resultTime.getYear(),resultTime.getMonth(),resultTime.getDayOfMonth(),
                    15,30,0);
        }else if (beginTime.getHour()==9){
            LocalDateTime resultTime = beginTime.plus(step, unit);
            return LocalDateTime.of(resultTime.getYear(),resultTime.getMonth(),resultTime.getDayOfMonth(),
                    15,30,0);
        }else {
            return null;
        }
    }
    /**
     * 天结束时间融合
     *
     * @param beginTime beginTime
     */
    public static LocalDateTime endDayHandler(LocalDateTime beginTime) {
        //普通交易日开始时间戳为上一日夜盘21：00时间，结束时间为开始时间够一天的15：00
        //节假日（除周六，周日）后第一个交易日1D的时间戳是当天日盘开盘时间9：00，结束时间为当天的15：00
        if (beginTime.getHour() == 21) {
            LocalDateTime resultTime = beginTime.plusDays(1);
            return LocalDateTime.of(resultTime.getYear(), resultTime.getMonth(), resultTime.getDayOfMonth(),
                    15, 30, 0);
        } else if (beginTime.getHour() == 9) {
            return LocalDateTime.of(beginTime.getYear(), beginTime.getMonth(), beginTime.getDayOfMonth(),
                    15, 30, 0);
        } else {
            return null;
        }
    }

    /**
     * 小时endTime处理
     *
     * @param dateTime 时间
     * @param period   周期
     * @return 结束时间
     */
    public static LocalDateTime endHourHandler(LocalDateTime dateTime, PeriodEnum period) {
        int step = period.getValue();
        ChronoUnit unit = period.getUnit();
        String code = period.getCode();
        if ("1H".equals(code) && (dateTime.getHour() == 2 || dateTime.getHour() == 15)) {
            //1H 夜盘20：00 -2：30 最后半小时的融合区间为2：00-2：30 25：00-15：30
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(),
                    dateTime.getDayOfMonth(), dateTime.getHour(), 30, 0);
        } else if ("4H".equals(code) && dateTime.getHour() == 0) {
            //4H夜盘20：00-2：30 最后区间为20：00-0：00 0：00-2：30
            LocalDateTime localDateTime = dateTime.plusDays(1);
            return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth()
                    , localDateTime.getDayOfMonth(), 2, 30, 0);
        } else if ("4H".equals(code) && dateTime.getHour() == 13) {
            //4H日盘最后一个时间段为13：00-15：30
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth()
                    , dateTime.getDayOfMonth(), 15, 30, 0);
        } else {
            return dateTime.plus(step, unit);
        }
    }

    public static LocalDateTime findBeginTime(LocalDateTime dateTime, PeriodEnum period) {
        //金交所融合行情的时间段为[20:00,02:30),[09:00,15:30),过滤开始时间不属于以上时间段的行情
        boolean filterFlg = (dateTime.getHour() >= 20)
                || (dateTime.getHour() < 2)
                || (dateTime.getHour() == 2 && dateTime.getMinute() < 30)
                || (dateTime.getHour() >= 9 && dateTime.getHour() < 15)
                || (dateTime.getHour() == 15 && dateTime.getMinute() < 30);
        if (!filterFlg) {
            return null;
        }
        //获取前一天是否为节假日（除周六周日
        LocalDateTime localDateTime = dateTime.minusDays(1);
        List<String> holidayList = HolidayManager.getHolidayList();
        boolean isHoliday = holidayList.contains(DateUtil.localDateTimeToString(localDateTime, DateUtil.DAYSTR));
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        boolean isWeek = dayOfWeek.getValue() == 6 || dayOfWeek.getValue() == 7;
        boolean holidayFlag = isHoliday && !isWeek;

        int step = period.getValue();
        ChronoUnit unit = period.getUnit();
        String code = period.getCode();
        switch (unit) {
            case SECONDS:
                //tick
                int seconds = dateTime.getSecond() / step * step;
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth()
                        , dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), seconds);
            case MINUTES:
                //1Min 5Min  15Min 30Min
                int minutes = dateTime.getMinute() / step * step;
                return LocalDateTime.of(dateTime.getYear()
                        , dateTime.getMonth(), dateTime.getDayOfMonth()
                        , dateTime.getHour(), minutes, 0);
            case HOURS:
                //1H 4H
                return beginHourHandler(dateTime, step, code);
            case DAYS:
                //1D
                //普通交易日开始时间时间戳为上一日夜盘21：00的时间，节假日（除周六周日）后第一个交易日1D的时间戳是当天日盘开盘时间9：00
                int day = dateTime.getDayOfMonth()/step*step;
                if (!holidayFlag) {
                    LocalDateTime localDate = LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),day,
                            21,0,0);
                    return localDate.minusDays(1);
                }else {
                    return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),day,
                            9,0,0);
                }
            case WEEKS:
                //1Week
                //普通交易日开始时间时间戳为上一日夜盘21：00的时间，节假日（除周六，周日）后第一个交易日1D的时间戳是当天日盘开盘时间9：00
                LocalDateTime thisWeekMonday = getThisWeekMonday(dateTime);
                int weekDay = thisWeekMonday.getDayOfMonth() / step * step;
                if (!holidayFlag) {
                    LocalDateTime localDate = LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),weekDay,
                            21,0,0);
                    return localDate.minusDays(1);
                }else {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(),weekDay,
                            9,0,0);
                }
            case MONTHS:
                //1Month
                //普通交易日开始时间戳为上一日夜盘21：00的时间
                //节假日（除周六周日）后第一个交易日1D的时间戳是当天日盘开盘时间09：00
                int month = dateTime.getMonthValue()/step*step;
                if (!holidayFlag) {
                    LocalDateTime localDate = LocalDateTime.of(dateTime.getYear(),month,1,
                            21,0,0);
                    return localDate.minusDays(1);
                }else {
                    return LocalDateTime.of(dateTime.getYear(),month,1,
                            9,0,0);
                }
            default:
                log.info("该识别单位不明确：{}",unit.name());
                break;
        }
        throw new UnsupportedOperationException(unit.name()+"不支持");
    }
    /**
     * 小时行情的处理
     * @param dateTime 时间
     * @param step 格数
     * @param code 周期
     * @return 开始时间
     */
    public static LocalDateTime beginHourHandler(LocalDateTime dateTime, int step, String code) {
        if ("1H".equals(code)) {
            //1H
            int hour = dateTime.getHour() / step * step;
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth()
                    , hour, 0, 0);
        } else if ("4H".equals(code)) {
            //4小时行情融合时间段[20:00,00:00)[00:00,02:30][09:00,13:00)[13:00,15:00]
            //判断时间段的开始时间
            int hourTime = dateTime.getHour();
            int minuteTime = dateTime.getMinute();
            if (hourTime >= 20) {
                //4H中[20:00,00：00）
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth()
                        , dateTime.getDayOfMonth(), 20, 0, 0);
            } else if (hourTime < 2 || (hourTime == 2 && minuteTime < 30)){
                //4H中[00:00,02:30]
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),dateTime.getDayOfMonth(),
                        0,0,0);
            }else if (hourTime >=9 && hourTime < 13) {
                //4H中[09:00,13:00)
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth()
                        , dateTime.getDayOfMonth(), 0, 0, 0);
            } else if ((hourTime >= 13 && hourTime < 15) || (hourTime == 15 && minuteTime <= 30)) {
                //4H[13:00,15:30]
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth()
                        ,dateTime.getDayOfMonth(),13,0,0);
            }
        }
        return null;
    }
}
