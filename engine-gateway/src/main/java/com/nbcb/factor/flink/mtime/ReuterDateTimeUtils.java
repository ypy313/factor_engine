package com.nbcb.factor.flink.mtime;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.enums.PeriodEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.nbcb.factor.common.Constants.TA_PATTERN;

/**
 * 行情平台中外汇与贵金属时间间隔融合
 */
@Slf4j
public abstract class ReuterDateTimeUtils {

    public static LocalDateTime findBeginTime(LocalDateTime dateTime, PeriodEnum period) {
        int step = period.getValue();
        ChronoUnit unit = period.getUnit();
        String code = period.getCode();
        switch (unit) {
            case SECONDS:
                int second = dateTime.getSecond()/step*step;
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),dateTime.getDayOfMonth(),
                        dateTime.getHour(),dateTime.getMinute(),second);
            case MINUTES:
                int minute = dateTime.getMinute()/step*step;
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),dateTime.getDayOfMonth(),
                        dateTime.getHour(),minute,0);
            case HOURS:
                return hourHandler(dateTime, step,code);
            case DAYS:
                int day = dateTime.getDayOfMonth()/step*step;
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),day,
                        0,0,0);
            case WEEKS:
                //获取本周-日期
                LocalDateTime thisWeekMonday = getThisWeekMonday(dateTime);
                int weekDay = thisWeekMonday.getDayOfMonth()/step*step;
                return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),weekDay,
                        0,0,0);
            case MONTHS:
                int month = dateTime.getMonthValue() /step * step;
                return LocalDateTime.of(dateTime.getYear(),month,1,
                        0,0,0);
            default:
                log.info("该识别单位不明确：{}", unit.name());
                break;
        }
        throw new UnsupportedOperationException(unit.name() + "不支持");
    }

    /**
     * 转日期
     */
    public static LocalDateTime parse(String str) {
        return DateUtil.parse(str,TA_PATTERN);
    }

    public static long toTimestamp(String str){
        if (!StringUtils.hasLength(str)) {
            return -1L;
        }
        return DateUtil.toTimestamp(DateUtil.parse(str,TA_PATTERN));
    }

    /**
     * 转字符串
     */
    public static String format(long timestamp){
        return DateUtil.format(timestamp,TA_PATTERN);
    }

    public static String format(LocalDateTime time){
        return DateUtil.format(time,TA_PATTERN);
    }

    public static LocalDateTime hourHandler(LocalDateTime dateTime, int step, String code){
        if ("1H".equals(code)) {
            //1H
            int hour = dateTime.getHour()/step*step;
            return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),dateTime.getDayOfMonth(),
                    hour,0,0);
        }else if("4H".equals(code)){
            //判断星期一，星期六，星期日
            DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
            int week = dayOfWeek.getValue();
            int hourTime = dateTime.getHour();
            if((week == 1 && hourTime<5)||(week == 6 && hourTime<4)|| (week ==7)){
                //星期一 0:00-5:00 ||星期六，超过04:00||星期天 行情都不融合
                return null;
            }
            //星期一，融合的k线时间段
            if (week == 1) {
                if (hourTime < 9) {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                            5, 0, 0);
                } else if (hourTime < 13) {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                            9, 0, 0);
                } else if (hourTime < 17) {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                            13, 0, 0);
                } else if (hourTime < 21) {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                            17, 0, 0);
                } else if (hourTime < 24) {
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                            21, 0, 0);
                }
            }
            int hour = dateTime.getHour()/step*step;
            return LocalDateTime.of(dateTime.getYear(),dateTime.getMonth(),dateTime.getDayOfMonth(),
                    hour,0,0);
        }
        return null;
    }

    /**
     * [beginTime,endTime)
     * @param beginTime 20221130 15:25:59 800
     * @param period 1Min
     * @return bar所在的[beginTime,endTime]
     */
    public static Tuple2<String,String> getTimeWindow(String beginTime, PeriodEnum period){
        LocalDateTime begin = ReuterDateTimeUtils.parse(beginTime);

        int step = period.getValue();
        ChronoUnit unit = period.getUnit();

        //查找最近可以被step整除的点
        LocalDateTime start1 = ReuterDateTimeUtils.findBeginTime(begin,period);
        //开始时间为null,则不需要计算结束时间
        if (start1 == null) {
            return null;
        }
        //计算结束时间
        LocalDateTime end1;
        if ("4H".equals(period.getCode()) && begin.getDayOfWeek().getValue() == 1 && start1.getHour() >=21) {
            //星期一 周期为4H 的最后一根bar时间间隔只有3小时
            end1 = start1.plus(3,unit);
        }else {
            end1 = start1.plus(step,unit);
        }
        String resBegin = ReuterDateTimeUtils.format(start1);
        String resEnd = ReuterDateTimeUtils.format(end1);
        return Tuple2.of(resBegin,resEnd);
    }

    /**
     * 获取本周星期一为那一天
     */
    public static LocalDateTime getThisWeekMonday(LocalDateTime localDateTime) {
        // 将 LocalDateTime 转换为 Calendar
        Calendar localCalendar = GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));

        // 获取当前星期是一个星期的第几天
        int dayWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
        if (dayWeek == Calendar.SUNDAY) {
            dayWeek = 8;
        }

        // 设置一个星期的第一天为星期一
        localCalendar.setFirstDayOfWeek(Calendar.MONDAY);

        // 当前日期减去星期几天数差值
        localCalendar.add(Calendar.DATE, localCalendar.getFirstDayOfWeek() - dayWeek);

        // 对时分秒清0
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        // 将 Calendar 转换回 LocalDateTime
        return LocalDateTime.ofInstant(localCalendar.toInstant(), ZoneId.systemDefault());
    }
}
