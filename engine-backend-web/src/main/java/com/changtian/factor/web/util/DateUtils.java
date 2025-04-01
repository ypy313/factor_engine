package com.changtian.factor.web.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间工具类
 *
 * @author wu.zhou
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static final String DT_FORMAT_PATTERN = "yyyyMMdd HH:mm:ss SSS";
    public static final String DT_FORMAT_PATTERN_POINT = "yyyyMMdd HH:mm:ss.SSS";
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";
    public static String HH_mm_ss = "HH:mm:ss";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss SSS";

    public static String DT_FORMAT = "yyyyMMdd-HH:mm:ss.SSS";

    public final static String DAYSTR = "yyyyMMdd";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM","yyyyMMdd-HH:mm:ss.SSS",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM","yyyyMMdd","yyyyMMdd HH:mm:ss"};
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String getHtime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS_SSS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String getDateTimeNow() {
        return dateTimeNow(DAYSTR);
    }

    /**
     * 获取现在的日期字符串，精度到毫秒
     * 日期格式 yyyyMMdd-HH:mm:ss.SSS
     *
     * @return 字符串
     */
    public static final String getSendingTime() {
        return dateTimeNow(DT_FORMAT);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 计算x天之前的日期
     */
    public static Date calcDaysBefore(int days, Date time) {
        //结束时间 取当前日期
        Calendar date = Calendar.getInstance();
        date.setTime(time);
        //计算日期
        date.set(Calendar.DATE, date.get(Calendar.DATE) - days);
        //返回现在时间减去x天的时间
        return date.getTime();
    }

    /**
     * 获取当前long时间
     *
     * @return
     */
    public static long getCurrTime() {
        return new Date().getTime();
    }

    public static String convertDateStr(String time, String sdfSourceFormat, String sdfTragetFormat) throws ParseException {
        SimpleDateFormat sdfSource = new SimpleDateFormat(sdfSourceFormat);
        SimpleDateFormat sdTSource = new SimpleDateFormat(sdfTragetFormat);
        String format = sdTSource.format(sdfSource.parse(time));
        return format;
    }

    /**
     * 计算量时间的时间间隔
     *
     * @param startTimeString
     * @param endTimeString
     * @param pattern
     * @param timeType
     * @return
     */
    public static long timeDiffer(String startTimeString, String endTimeString, String pattern, DateUnit timeType) {
        try {
            Date startDate = DateUtils.parseDate(startTimeString, pattern);
            Date endDate = DateUtils.parseDate(endTimeString, pattern);
            //计算两者间隔分钟
            return DateUtil.between(startDate, endDate, timeType, false);
        } catch (ParseException e) {
            log.error("String ->Date 失败， 其中开始时间为:{},结束时间为{}", startTimeString, endTimeString);
            throw new RuntimeException("时间格式不正确");
        }
    }

    public static Date getAfterMoth(int month, Date time) {
        //结束时间 取当前日期
        Calendar date = Calendar.getInstance();
        date.setTime(time);
        //计算日期
        date.add(Calendar.MONTH, month);
        long timeInMillis = date.getTimeInMillis();
        //返回现在时间减去x天的时间
        return stringToDate(longToDateString(timeInMillis, DateUtils.YYYY_MM_DD), YYYY_MM_DD);
    }

    public static Date stringToDate(String time, String formatPatter) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat(formatPatter).parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String longToDateString(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.format(date);
    }

    public static String timeConver(String time, String inputPattern, String outputPattern) {
        try {
            Date timeInfo = DateUtils.parseDate(time, inputPattern);
            String timeResult = DateUtils.parseDateToStr(outputPattern, timeInfo);
            return timeResult;
        } catch (ParseException e) {
            log.error("时间转换失败");
            throw new RuntimeException(e);
        }
    }

    public static String getAfterDay(long day, Date time) {
        long beginTime = time.getTime();
        long intervalTime = day * 24 * 60 * 60 * 1000;
        long lastTime = beginTime + intervalTime;
        return longToDateString(lastTime, DateUtils.YYYY_MM_DD);
    }

    /**
     * 获取当前日期的开始时间和结束时间。
     * <p>
     * 该方法计算当前日期的第一时刻（00:00:00.000）和最后一刻（23:59:59.999），并以字符串格式返回。
     *
     * @return 包含开始时间和结束时间字符串的Map，键分别为 "start" 和 "end"。
     */
    public static Map<String, String> getStartAndEndOfCurrentDay() {
        // 定义所需字符串格式的格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 创建当天的开始时间和结束时间
        LocalDateTime startOfDay = currentDate.atStartOfDay();
        LocalDateTime endOfDay = currentDate.atTime(23, 59, 59, 999_000_000); // Java 使用纳秒

        // 将 LocalDateTime 对象格式化为字符串
        String startTimeStr = startOfDay.format(formatter);
        String endTimeStr = endOfDay.format(formatter);

        // 将结果放入 Map 中返回
        Map<String, String> result = new HashMap<>();
        result.put("start", startTimeStr);
        result.put("end", endTimeStr);

        return result;
    }

    public static String timeAddMinute(String time, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(stringToDate(time, HH_mm_ss));
        calendar.add(Calendar.MINUTE, minute);
        return parseDateToStr(HH_mm_ss, calendar.getTime());
    }

    /**
     * 计算x月之前的日期
     */
    public static Date calcMothsBefore(int month){
        //结束日期 取当前日期
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        //计算日期
        date.set(Calendar.MONTH,date.get(Calendar.MONTH)-month);
        //返回现在时间减去x天的时间
        return date.getTime();

    }}
