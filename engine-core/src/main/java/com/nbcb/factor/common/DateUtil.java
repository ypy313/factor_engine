package com.nbcb.factor.common;

import cn.hutool.core.date.DateUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final String YYYYMMDD_HH_MM_SS = "yyyyMMdd HH:mm:ss";
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    public static final String YYYY_MM_DD="yyyy-MM-dd";
    public static final String DAYSTR="yyyyMMdd";
    public static final String NOWSTR="yyyyMMddHHmmssSSS";
    public static final String SEND_TIME_DF="yyyyMMdd HH:mm:ss.SSS";
    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DT_FORMAT = "yyyyMMdd-HH:mm:ss.SSS";
    public static final String DT_FORMAT_PATTERN = "yyyyMMdd HH:mm:ss SSS";
    public static final String YYYY_MM_DD_HH_MM_SS_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String WORK_TIME_FORMAT = "yyyyMMdd HH:mm";

    private static final ZoneOffset ZONE8 = ZoneOffset.ofHours(8);

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 计算days天后的日期
     * @param days 天数
     * @return 日期
     */
    public static Date getAfterDays(int days){
        //结束时间 取当前日期
        Date nowDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(nowDate);
        //计算日期
        date.add(Calendar.DATE, days);
        long timeInMillis = date.getTimeInMillis();
        //返回现在时间减去x天的时间
        return stringToDate(longToDateString(timeInMillis),YYYY_MM_DD);
    }

    public static String getAfterDays(String date,int days){
        SimpleDateFormat df = new SimpleDateFormat(DAYSTR);
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(df.parse(date));
            //计算日期
            calendar.add(Calendar.DATE,days);
            return df.format(calendar.getTime());
        }catch (ParseException e){
            log.info(e.getMessage());
        }
        return null;
    }

    /**
     * String 转date
     */
    public static Date stringToDate(String time,String formatPatter){
        Date date = new Date();
        try {
            date = new SimpleDateFormat(formatPatter).parse(time);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return date;
    }

    public static String longToDateString(long time){
        Date date = new Date(time);
        SimpleDateFormat sd = new SimpleDateFormat(YYYY_MM_DD);
        return sd.format(date);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2){
        return Math.abs((int)((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24)));
    }

    /**
     * 计算几月之后的日期
     */
    public static Date getAfterMonth(String settlementDate,int month){
        Calendar date = Calendar.getInstance();
        date.setTime(stringToDate(settlementDate,DAYSTR));
        //计算日期
        date.add(Calendar.MONTH,month);
        long timeInMillis = date.getTimeInMillis();
        //返回现在时间减去x天的时间
        return stringToDate(longToDateString(timeInMillis),YYYY_MM_DD);
    }

    public static Date getAfterMoth(int month){
        Date nowDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(nowDate);
        //计算日期
        date.add(Calendar.MONTH,month);
        long timeInMillis = date.getTimeInMillis();
        return stringToDate(longToDateString(timeInMillis),YYYY_MM_DD);
    }
    /**
     * 获取现在的日期字符串，精度到毫秒
     * 日期格式：yyyyMMddHHmmssSSS
     */
    public static String getNowstr(){
        return format(new Date(),NOWSTR);
    }

    /**
     * 获取现在的日期字符串，精度到毫秒
     * 日期格式 yyyyMMdd-HH:mm:ss.SSS
     */
    public static String getSendingTime(){
        return format(new Date(),NOWSTR);
    }

    /**
     * 日期格式
     * @param date 日期
     * @param pattern 格式类型
     * @return 格式化字符串
     */
    public static String format(Date date,String pattern){
        if (date == null || pattern == null) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String format(long timestamp, DateTimeFormatter pattern){
        return format(parse(timestamp),pattern);
    }
    public static LocalDateTime parse(long timestamp){
        return Instant.ofEpochMilli(timestamp).atZone(ZONE8).toLocalDateTime();
    }

    public static String format(LocalDateTime time,DateTimeFormatter pattern){
        if(time == null){
            return "";
        }
        return time.format(pattern);
    }

    /**
     * 获取现在的日期字符串，精度到日期
     * 日期格式：yyyyMMdd
     * @return 字符串
     */
    public static String getDaystr(){
        return format(new Date(),DAYSTR);
    }

    /***
     * 获取现在的日期字符串，精度到毫秒
     * 日期格式：yyyyMMdd HH:mm:ss.SSS
     */
    public static String getSendTimeStr(){
        return format(new Date(),SEND_TIME_DF);
    }

    public static String timestampFormat(String timestamp,DateTimeFormatter pattern){
        if(StringUtils.isBlank(timestamp)){
            return "";
        }
        long ts = Long.parseLong(timestamp);
        return format(ts,pattern);
    }

    /**
     * 将string 日期格式转为另一种string 日期格式
     */
    public static String convertDateStr(String time,String sdfSourceFormat,String sdfTargetFormat){
        try {
            SimpleDateFormat sdfSource = new SimpleDateFormat(sdfSourceFormat);
            SimpleDateFormat sdtSource = new SimpleDateFormat(sdfTargetFormat);
            String formatDate =  sdtSource.format(sdfSource.parse(time));
            return formatDate;
        } catch (ParseException e) {
            log.error("时间格式转化失败");
            return null;
        }
    }

    public static String longToFormatDateString(Long time,String format){
        Date date = new Date(time);
        SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.format(date);
    }

    public static LocalDateTime parse(String dateStr,DateTimeFormatter pattern){
        return LocalDateTime.parse(dateStr,pattern);
    }

    /**
     * 转时间戳
     */
    public static long toTimestamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZONE8).toEpochMilli();
    }

    /**
     * LocalDateTime ->String
     * @param dateTime dateTime
     * @param formatString formatString
     * @return date(String)
     */
    public static String localDateTimeToString(LocalDateTime dateTime,String formatString){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatString);
        return dateTime.format(dateTimeFormatter);
    }

    /**
     * 计算两时间的时间间隔
     * @param startTimeString 开始时间
     * @param endTimeString 结束时间
     * @param pattern 时间格式化形式
     * @param TimeType 计算的时间结果类型（毫秒，秒，分钟等）
     */
    public static long timeDiffer(String startTimeString, String endTimeString, String pattern, DateUnit TimeType){
        try{
            Date endDate = DateUtils.parseDate(endTimeString, pattern);
            Date startDate = DateUtils.parseDate(startTimeString, pattern);
            //计算两者间隔分钟
            return cn.hutool.core.date.DateUtil.between(startDate,endDate,TimeType);
        } catch (ParseException e) {
            log.error("String ->Date 失败，其中开始时间为：{}，结束时间为：{}",startTimeString,endTimeString);
            throw new RuntimeException("时间格式不正确");
        }
    }

    /**
     * 获取现在的日期字符串
     * @return
     */
    public static String getForexTime(){
        return format(new Date(),YYYY_MM_DD_HH_MM_SS_SS);
    }

    /**
     * 推送时间String->拼接转化为Date
     * @param pushEndTime
     * @return
     */
    public static Date workTimeTra(String pushEndTime){
        String pushEnd = DateUtil.getDaystr()+" "+pushEndTime;
        return DateUtil.stringToDate(pushEnd,DateUtil.WORK_TIME_FORMAT);
    }

    /**
     * 计算x天之前的日期
     */
    public static Date calcDaysBefore(int days){
        //结束时间，取当前日期
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        //计算日期
        date.set(Calendar.DATE,date.get(Calendar.DATE)-days);
        //返回现在时间减去x天的时间
        return date.getTime();
    }

    public static Date transferLongToDate(String format, Long millSec) {
        Date date = new Date(millSec);
        return dateToDateFormat(date,format);
    }

    public static Date dateToDateFormat(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String formatString = dateFormat.format(date);
        Date data=null;
        try {
            data=dateFormat.parse(formatString);
        } catch (ParseException e) {
            log.error("data -> date 格式转换有误");
        }
        return date;
    }

    /**
     * 判断是否是星期日
     */
    public static boolean isSunDay(String date){
        boolean flag = false;
        SimpleDateFormat df = new SimpleDateFormat(DAYSTR);
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(df.parse(date));
            int week =calendar.get(Calendar.DAY_OF_WEEK);
            if(week == Calendar.SUNDAY){
                flag = true;
            }
        }catch (ParseException e){
            log.info(e.getMessage());
        }
        return flag;
    }

    /**
     * 判断参数的格式是否为yyyyMMdd格式的合法日期字符串
     */
    public static boolean isValidDate(String str){
        try{
            if (str!=null && !str.equals("") && str.length()==8) {
                //闰年标志
                boolean isLeapYear = false;
                String year = str.substring(0,4);
                String month = str.substring(4, 6);
                String day = str.substring(6, 8);
                int vYear = Integer.parseInt(year);
                //判断年份是否合法
                if (vYear<1900||vYear>2200) {
                    return false;
                }
                //判断是否为闰年
                if (vYear%4==0 && vYear %100 !=0 || vYear%400 == 0) {
                    isLeapYear = true;
                }
                //判断月份
                if (!isValidMonth(month,day,isLeapYear)) {
                    return false;
                }
                //判断日期
                if (!isValidDate(day)) {
                    return false;
                }
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //判断月份
    private static boolean isValidMonth(String month,String day,boolean isLeapYear){
        //判断月份
        if (month.startsWith("0")) {
            String units4Month = month.substring(1,2);
            int vUnits4Month = Integer.parseInt(units4Month);
            if (vUnits4Month == 0) {
                return false;
            }
            if (vUnits4Month==2) {
                //判断2月的天数
                int vDays4February = Integer.parseInt(day);
                if (isLeapYear) {
                    if (vDays4February>29) {
                        return false;
                    }
                }else {
                    if(vDays4February>28){
                        return false;
                    }
                }
            }
        }else {
            //判断非0打头的月份是否合法
            int vMonth = Integer.parseInt(month);
            if(vMonth != 10 && vMonth!=11 && vMonth !=12){
                return false;
            }
        }
        return true;
    }

    /**
     * 获取现在的日期字符串，精度到毫秒
     */
    public static String getSendingTimeStr(){
        return format(new Date(),SEND_TIME_DF);
    }
}

