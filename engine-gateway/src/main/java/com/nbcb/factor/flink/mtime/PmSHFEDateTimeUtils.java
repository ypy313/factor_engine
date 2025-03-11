package com.nbcb.factor.flink.mtime;

import com.nbcb.factor.common.DateUtil;
import com.nbcb.factor.enums.PeriodEnum;
import com.nbcb.factor.job.HolidayManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 上清所
 */
@Slf4j
public class PmSHFEDateTimeUtils extends ReuterDateTimeUtils{
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

    public static LocalDateTime findBeginTime(LocalDateTime dateTime, PeriodEnum period) {
        //金交所融合行情的时间段为[20:00,02:30),[09:00,15:30),过滤开始时间不属于以上时间段的行情
        boolean filterFlg = (dateTime.getHour() >= 21) || (dateTime.getHour() < 2)
                || (dateTime.getHour() == 2 && dateTime.getMinute() < 30)
                || (dateTime.getHour()==9)
                || (dateTime.getHour() == 10 && dateTime.getMinute() < 15)
                || (dateTime.getHour() == 10 && dateTime.getMinute() >30)
                || (dateTime.getHour() == 11 && dateTime.getMinute() <30)
                || (dateTime.getHour() == 13 && dateTime.getMinute() >=30)
                ||(dateTime.getHour() == 14);
        if (!filterFlg) {
            return null;
        }
        //获取前一天是否为节假日（除周六周日
        LocalDateTime localDateTime = dateTime.minusDays(1);
        List<String> holidayList = HolidayManager.getHolidayList();
        boolean isHoliday = holidayList.contains(DateUtil.localDateTimeToString(localDateTime, DateUtil.DAYSTR));
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        boolean isWeek = dayOfWeek.getValue() == 6 || dayOfWeek.getValue() == 7;
        boolean isHolidayWeek = isHoliday && !isWeek;

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
                return beginDayHandler(dateTime,step,isHolidayWeek);
            case WEEKS:
                //1Week
                return beginWeekHandler(dateTime,step,isHolidayWeek);
            case MONTHS:
                return beginMonthsHandler(dateTime,step,isHolidayWeek);
            default:
                log.info("该识别单位不明确：{}",unit.name());
                break;
        }
        throw new UnsupportedOperationException(unit.name()+"不支持");
    }

    /**
     * 对日期和时间处理（月行情）
     *
     * @param dateTime 输入的日期时间
     * @param step     月份步长，用于确定目标月份（例如，step=3表示每三个月处理一次）
     * @param isHolidayWeek 是否为节假日周，节假日（除周六，周日）后第一个交易日的时间处理不同
     * @return 处理后的日期时间
     */
    public static LocalDateTime beginMonthsHandler(LocalDateTime dateTime, int step, boolean isHolidayWeek) {
        // 计算目标月份的第一天，通过除以step再乘以step的方式实现向下取整到最近的step倍数月份
        int month = dateTime.getMonthValue() / step * step;

        // 根据是否为节假日周，返回不同的日期时间
        if (!isHolidayWeek) {
            // 普通交易日，开始时间时间戳为上一日盘后21:00的时间
            // 先构造目标月份的第一天21:00的时间
            LocalDateTime localDate = LocalDateTime.of(dateTime.getYear(), month, 1,
                    21, 0, 0);
            // 然后减去一天，得到上一日盘后的时间
            return localDate.minusDays(1);
        } else {
            // 节假日周，后第一个交易日的时间就是当天日盘开盘时间9:00
            return LocalDateTime.of(dateTime.getYear(), month, 1,
                    9, 0, 0);
        }
    }

    /**
     * 周末开始时间处理
     *
     * @param dateTime 当前时间
     * @param step     步长，用于确定周几作为基准点，例如7代表每周一
     * @param isHolidayWeek 是否为节假日周
     * @return LocalDateTime 根据条件返回的开始时间
     */
    public static LocalDateTime beginWeekHandler(LocalDateTime dateTime, int step, boolean isHolidayWeek) {
        // 获取当前时间所在周的周一
        //普通交易日开始时间戳为上一日夜盘21：00的时间，节假日（除周六，周日）后第一个交易日1D的时间戳是当天日盘开盘时间9：00
        LocalDateTime thisWeekMonday = getThisWeekMonday(dateTime);

        // 根据步长计算基准周几，例如step为7时，weekDay为当前周所在的周一
        // 但由于后续逻辑中是对weekDay进行日期设置，因此这里计算的是目标周的周一日期（未减天数）
        int weekDay = thisWeekMonday.getDayOfMonth() / step * step;

        if (!isHolidayWeek) {
            // 普通交易日，开始时间戳为上一周日夜盘21:00的时间
            // 先构造目标周周一的21:00时间，再减去一天得到上一周日夜盘21:00的时间
            LocalDateTime dateTimeInfo = LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), weekDay,
                    21, 0, 0);
            return dateTimeInfo.minusDays(1);
        } else {
            // 节假日周（除周六，周日）后第一个交易日，时间戳是当天日盘开盘时间9:00
            // 直接构造目标周周一的9:00时间作为结果
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), weekDay,
                    9, 0, 0);
        }
    }

    /**
     * 天行情开始时间的处理
     *
     * @param dateTime 日期时间
     * @param step 步长
     * @param isHolidayWeek 是否为非交易日周
     * @return 处理后的日期时间
     */
    public static LocalDateTime beginDayHandler(LocalDateTime dateTime, int step, boolean isHolidayWeek) {
        int day = dateTime.getDayOfMonth() / step * step;
        if (!isHolidayWeek) {
            LocalDateTime localDate = LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), day,
                    21, 0, 0);
            return localDate.minusDays(1);
        } else {
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), day,
                    9, 0, 0);
        }
    }

    /**
     * 获取结束时间
     *
     * @param beginTime 开始时间
     * @param period    周期枚举类型，包含时间单位、步长及代码
     * @return 结束时间
     */
    public static LocalDateTime findEndTime(LocalDateTime beginTime, PeriodEnum period) {
        int step = period.getValue(); // 获取周期步长
        ChronoUnit unit = period.getUnit(); // 获取时间单位
        String code = period.getCode(); // 获取周期代码（可能用于特定处理逻辑）

        switch (unit) {
            case SECONDS:
                // 直接在开始时间基础上增加秒数
                return beginTime.plusSeconds(step);
            case MINUTES:
                // 调用分钟处理函数，可能涉及特定逻辑处理
                return endMinuteHandler(beginTime, step, unit, code);
            case HOURS:
                // 调用小时处理函数，可能涉及特定逻辑处理
                return endHourHandler(beginTime, step, unit, code);
            case DAYS:
                // 调用天处理函数，可能涉及日期变更的逻辑
                return endDayHandler(beginTime);
            case WEEKS:
            case MONTHS:
                // 调用月或周处理函数，处理更复杂的周期
                return endMonthsWeekHandler(beginTime, step, unit);
            default:
                // 如果时间单位不明确，记录日志
                log.info("该识别单位不明确: {}", unit.name());
                break;
        }
        // 抛出异常，表示不支持的时间单位
        throw new UnsupportedOperationException(unit.name() + "不支持");
    }


    /**
     * 天结束时间融合
     *
     * @param beginTime 开始时间
     * @return 结束时间
     */
    public static LocalDateTime endDayHandler(LocalDateTime beginTime) {
        // 普通交易日开始时间时间戳为上一日夜盘21:00的时间，结束时间为开始时间后一天的15:00
        // 节假日（除周六，周日）后第一个交易日的时间戳是当天日盘开盘时间9:00，结束时间为当天的15:00
        if (beginTime.getHour() == 21) {
            LocalDateTime resultTime = beginTime.plusDays(1);
            return LocalDateTime.of(resultTime.getYear(), resultTime.getMonth(), resultTime.getDayOfMonth(),
                    15, 0, 0);
        } else if (beginTime.getHour() == 9) {
            return LocalDateTime.of(beginTime.getYear(), beginTime.getMonth(), beginTime.getDayOfMonth(),
                    15, 0, 0);
        }
        return null;
    }

    /**
     * 分钟结束时间融合
     *
     * @param beginTime 开始时间
     * @param step      步长
     * @param unit      时间单位
     * @param period    时间段标识，例如"30Min"表示30分钟一根bar
     * @return 结束时间
     */
    public static LocalDateTime endMinuteHandler(LocalDateTime beginTime, int step, ChronoUnit unit, String period) {
        if ("30Min".equals(period) && beginTime.getMinute() == 0 && beginTime.getHour() == 10) {
            // 30分钟时间段，10:00~10:15为一根bar
            return LocalDateTime.of(beginTime.getYear(), beginTime.getMonth(), beginTime.getDayOfMonth()
                    , beginTime.getHour(), 15, 0);
        } else {
            return beginTime.plus(step, unit);
        }
    }



    /**
     * 小时endTime处理
     * @param dateTime 当前时间
     * @param step     步长，用于确定周几作为基准点，例如7代表每周一
     * @param unit      时间单位
     * @param period    时间段标识
     */
    public static LocalDateTime endHourHandler(LocalDateTime dateTime, int step, ChronoUnit unit, String period) {
        if ("1H".equals(period) && dateTime.getHour() == 2) {
            // 1H夜盘21:00-2:30最后半小时的融合区间为2:00-2:30
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    dateTime.getHour(), 30, 0);
        } else if ("1H".equals(period) && dateTime.getHour() == 11) {
            // 1H[11:00,11:30)
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    dateTime.getHour(), 30, 0);
        } else if ("1H".equals(period) && dateTime.getHour() == 13) {
            // 1H[13:00,14:30)
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    14, 30, 0);
        } else if ("1H".equals(period) && dateTime.getHour() == 14) {
            // 1H[14:30,15:00)
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    15, 0, 0);
        } else if ("4H".equals(period) && (dateTime.getHour() == 21)) {
            // 4H夜盘21:00-2:30最后区间为21：00-1：00 1：00-2：30
            LocalDateTime localDateTime = dateTime.plusDays(1);
            return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth(),
                    1, 0, 0);
        } else if ("4H".equals(period) && dateTime.getHour() == 1) {
            // 4H夜盘1:00-2:30
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    2, 30, 0);
        } else if ("4H".equals(period) && dateTime.getHour() == 9) {
            // 4H日盘9:00-11:30
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    11, 30, 0);
        } else if ("4H".equals(period) && dateTime.getHour() == 13 && dateTime.getMinute() == 30) {
            // 4H日盘13:30-15:00
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    15, 0, 0);
        } else {
            return dateTime.plus(step, unit);
        }
    }

    /**
     * 月份与周结束时间融合
     * @param beginTime 开始时间
     * @param step 步长
     * @param unit 时间周期
     */
    private static LocalDateTime endMonthsWeekHandler(LocalDateTime beginTime, int step, ChronoUnit unit) {
        // 实现周和月处理的逻辑
        if (unit == ChronoUnit.WEEKS) {
            return beginTime.plusWeeks(step); // 示例，实际可能更复杂
        } else if (unit == ChronoUnit.MONTHS) {
            return beginTime.plusMonths(step); // 示例，实际可能更复杂
        }
        throw new IllegalArgumentException("Invalid unit for months/weeks handler: " + unit);
    }

    /**
     * 小时行情开始时间的处理
     * @param dateTime 时间
     * @param step 格数
     * @param code 周期
     * @return 开始时间
     */
    public static LocalDateTime beginHourHandler(LocalDateTime dateTime, int step, String code) {
        int hourTime = dateTime.getHour();
        int minuteTime = dateTime.getMinute();

        if ("lH".equals(code) && (hourTime >= 21 || hourTime <= 11)) {
            // 1H
            int hour = dateTime.getHour() / step * step;
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    hour, 0, 0);
        } else if ("1H".equals(code) && ((hourTime == 13 && minuteTime >= 30) || (hourTime == 14 && minuteTime < 30))) {
            // 1H[13:30-14:30)
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    13, 30, 0);
        } else if ("1H".equals(code) && (hourTime == 14 && minuteTime >= 30)) {
            // 1H[14:30-15:00)
            return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                    14, 30, 0);
        } else if ("4H".equals(code)) {
            // 4小时行情融合时间段[21:00,01:00） [01:00,02:30) [09:00,11:30) [13:30,15:00)
            if (hourTime >= 21) {
                // 4小时行情融合时间段[21：00,01：00)
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                        21, 0, 0);
            } else if (hourTime < 1) {
                // 4小时行情融合时间段[21：00,01：00)
                // [00:00,01:00]点的属于上一天的开始日期，需要减一天
                LocalDateTime localDateTime = dateTime.minusDays(1);
                return LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth(),
                        21, 0, 0);
            } else if (hourTime == 1 || (hourTime == 2 && minuteTime < 30)) {
                // [01:00,02:30)
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                        1, 0, 0);
            } else if ((hourTime >= 9 && hourTime < 11) || (hourTime == 11 && minuteTime < 30)) {
                // 4小时行情融合时间段[09：00,11：30)
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                        9, 0, 0);
            } else if ((hourTime == 13 && minuteTime >= 30) || (hourTime == 14)) {
                // 4小时行情融合时间段[13：30,15：00]
                return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(),
                        13, 30, 0);
            }
        }
        return null;
    }


}
