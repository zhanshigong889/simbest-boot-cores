package com.simbest.boot.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些有用的日期时间工具类
 *
 * @author lishuyi
 *
 */
public final class DateUtil {

	public static final String datePattern1 = "yyyy-MM-dd";
	public static final String datePattern2 = "yyyyMMdd";
    public static final String datePattern3 = "yyyy/MM/dd";
    public static final String datePattern4 = "yyMMdd";
	public static final String timestampPattern1 = "yyyy-MM-dd HH:mm:ss";
    public static final String timestampPattern2 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String timestampPattern3 = "yyyy-MM-dd HH:mm:ss.SSS Z";
	public static final String timePattern = "HH:mm:ss";

	//匹配2017年9月20日 09:30  取出其中的2017  9  30  09 30
    public static final Pattern pattern = Pattern.compile( "\\d+" );

    public static final DateTimeFormatter fullDateTimeFormatter = new DateTimeFormatterBuilder().append(null, //because no printing is required
				 new DateTimeParser[]{
                         DateTimeFormat.forPattern(timePattern).getParser(),
                         DateTimeFormat.forPattern(datePattern1).getParser(),
                         DateTimeFormat.forPattern(datePattern2).getParser(),
                         DateTimeFormat.forPattern(datePattern3).getParser(),
                         DateTimeFormat.forPattern(timestampPattern1).getParser(),
                         DateTimeFormat.forPattern(timestampPattern2).getParser(),
                         DateTimeFormat.forPattern(timestampPattern3).getParser()
                 }).toFormatter();

    private DateUtil() {
        //not called
    }

	public static void main(String[] args) throws ParseException {
		System.out.println(getYesterday("2014-01-02 14:32:55"));
        System.out.println(getNextMonthLastDay());
		System.out.println("您好！");
		System.out.print("Xmx=");
		System.out.println(Runtime.getRuntime().maxMemory()/1024.0/1024+"M");

		System.out.print("free mem=");
		System.out.println(Runtime.getRuntime().freeMemory()/1024.0/1024+"M");

		System.out.print("total mem=");
		System.out.println(Runtime.getRuntime().totalMemory()/1024.0/1024+"M");
		System.gc();


        Matcher matcher = pattern.matcher( "2017年9月20日 09:30" );
        while ( matcher.find() ){
            System.out.println( matcher.group() );
        }
        System.out.println( getDateStrNumByGroups("2017年9月20日 09:30"));

        System.out.println(getDate(removeDate(new Date())));

        Date nowTime1 = parseTimestamp("2014-01-02 11:32:55");
        Date nowTime2 = parseTimestamp("2014-01-02 15:32:55");
        Date nowTime3 = parseTimestamp("2014-01-02 19:32:55");
        Date beginTime = parseTimestamp("2019-01-02 14:30:55");
        Date endTime = parseTimestamp("2019-01-02 18:29:58");
        System.out.println(belongTimeZone(nowTime1, beginTime, endTime));
        System.out.println(belongTimeZone(nowTime2, beginTime, endTime));
        System.out.println(belongTimeZone(nowTime3, beginTime, endTime));
        long[] times = timeBetweenDates(beginTime, endTime);
        System.out.println(times[0]);
        System.out.println(times[1]);
        System.out.println(times[2]);
        System.out.println(times[3]);
        System.out.println(dateToWeek(new Date()));
        System.out.println(dateToWeek(parseDate("2019-09-19")));
    }

    /**
     *
     * @return 当前时间戳
     */
	public static long getNow(){
		return System.currentTimeMillis();
	}

    /**
     *
     * @return 当前日期对象
     */
	public static Date getCurrent(){
		return new Date(System.currentTimeMillis());
	}

    /**
     *
     * @return 当前日期字符串 2018-03-14
     */
    public static String getCurrentStr() {
        return getDate(getCurrent());
    }

    /**
     *
     * @return 当前时间戳字符串 2018-03-14 17:13:08
     */
    public static String getCurrentTimestamp() {
        return getDate(getCurrent(), timestampPattern1);
    }

    /**
     *
     * @param pattern 指定类型
     * @return 当前日期字符串 2018-03-14
     */
    public static String getDateStr(String pattern) {
        return getDate(getCurrent(), pattern);
    }

    /**
     *
     * @return 获取今天的开始时间：比如：2014-06-19 00:00:00
     */
	public static Date getTodayTimestamp() {
		return DateUtil.startTimeOfDay(DateUtil.getCurrent()).toDate();
	}

    /**
     *
     * @return 当前昨日日期字符串 2018-03-14
     */
	public static String getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
	    return getDate(cal.getTime());
	}

    /**
     *
     * @param pattern 指定类型
     * @return 当前昨日日期字符串 2018-03-14
     */
	public static String getYesterday(String pattern) {
		Calendar cal = Calendar.getInstance();  
		cal.add(Calendar.DATE, -1);
	    return getDate(cal.getTime(), pattern);
	}
	
	public static String getDate(Date date) {
        DateTime dt = new DateTime(date);
        return dt.toString(datePattern1);
	}

	public static String getDate(Date date, String pattern) {
        DateTime dt = new DateTime(date);
        return dt.toString(pattern);
	}

	public static String getTime(Date date) {
        DateTime dt = new DateTime(date);
        return dt.toString(timePattern);
	}

	public static String getTimestamp(Date date) {
        DateTime dt = new DateTime(date);
        return dt.toString(timestampPattern1);
	}

	public static String getTimestamp(Date date, String pattern) {
        DateTime dt = new DateTime(date);
        return dt.toString(pattern);
	}

	// ===========================字符串转换时间==================================
	public static Date parseDate(String source){
	    if(StringUtils.isEmpty(source))
	        return null;
        else
            return fullDateTimeFormatter.parseDateTime(source).toDate();
	}

	public static Date parseTimestamp(String source){
        return fullDateTimeFormatter.parseDateTime(source).toDate();
	}

	public static Date parseCustomDate(String source, String pattern){
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        return dtf.parseDateTime(source).toDate();
	}

	public static int compareDate(Date src, Date desc) {
		String str1 = getDate(src);
		String str2 = getDate(desc);
		return str1.compareTo(str2);
	}

	public static int compareTimestamp(Date src, Date desc) {
		String str1 = getTimestamp(src);
		String str2 = getTimestamp(desc);
		return str1.compareTo(str2);
	}

	public static int compareTime(Date src, Date desc) {
		String str1 = getTime(src);
		String str2 = getTime(desc);
		return str1.compareTo(str2);
	}

    /**
     * 比较时间区间(只关心时间，不区分年月日)
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public static boolean belongTimeZone(Date nowTime, Date startTime, Date endTime) {
        nowTime = removeDate(nowTime);
        startTime = removeDate(startTime);
        endTime = removeDate(endTime);
        return belongDate(nowTime, startTime, endTime);
    }

    /**
     * 比较时间区间(完整时间)
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public static boolean belongDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

	public static int compareTime(String src, String desc) {
		return src.compareTo(desc);
	}

	// ===========================时间计算==================================
	/**
	 * 当前年
	 * @return 2014
	 */
	public static String getCurrYear() {
		Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.MONTH, 0);
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy");
	    return sdf.format(cal.getTime());
	}

    /**
     * 当前月 2014-08
     * @return
     */
	public static String getCurrMonth() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM");
	    return sdf.format(cal.getTime());
	}

    /**
     *
     * @return 当前月08
     */
	public static String getCurrSimpleMonth() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf =  new SimpleDateFormat("MM");
	    return sdf.format(cal.getTime());
	}

    /**
     *
     * @return 当前日28
     */
    public static String getCurrSimpleDay() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf =  new SimpleDateFormat("dd");
        return sdf.format(cal.getTime());
    }

	/**
	 * 上一个月
	 * @return 2014-08
	 */
	public static String getLastMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM");
	    return sdf.format(cal.getTime());
	}

	/**
	 * 下一个月
	 * @return 2014-08
	 */
	public static String getNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM");
	    return sdf.format(cal.getTime());
	}

	/**
	 * 当前月第一天
	 * @return 2014-08-01
	 */
	public static String getCurrMonthFirstDay() {
        DateTime dt = new DateTime();
        DateTime firstday = dt.dayOfMonth().withMinimumValue();
        return firstday.toString(datePattern1);
	}

	/**
	 * 当前月最后一天
	 * @return 2014-08-31
	 */
	public static String getCurrMonthLastDay() {
        DateTime dt = new DateTime();
        DateTime lastday = dt.dayOfMonth().withMaximumValue();
        return lastday.toString(datePattern1);
	}

	/**
	 * 上月第一天
	 * @return 2014-08-01
	 */
	public static String getLastMonthFirstDay() {
        DateTime dt = new DateTime();
        dt = dt.minusMonths(1);
        DateTime firstday = dt.dayOfMonth().withMinimumValue();
        return firstday.toString(datePattern1);
	}

	/**
	 * 上月最后一天
	 * @return 2014-08-31
	 */
	public static String getLastMonthLastDay() {
        DateTime dt = new DateTime();
        dt = dt.minusMonths(1);
        DateTime lastday = dt.dayOfMonth().withMaximumValue();
        return lastday.toString(datePattern1);
	}

	/**
	 * 下月第一天
	 * @return 2014-08-01
	 */
	public static String getNextMonthFirstDay() {
        DateTime dt = new DateTime();
        dt = dt.plusMonths(1);
        DateTime firstday = dt.dayOfMonth().withMinimumValue();
        return firstday.toString(datePattern1);
	}

	/**
	 * 下月最后一天
	 * @return 2014-08-31
	 */
	public static String getNextMonthLastDay() {
        DateTime dt = new DateTime();
        dt = dt.plusMonths(1);
        DateTime lastday = dt.dayOfMonth().withMaximumValue();
        return lastday.toString(datePattern1);
	}

    /**
     * 本周周第一天
     */
    public static String getCurrWeekFirstDay() {
        DateTime dt = new DateTime();
        DateTime firstday = dt.dayOfWeek().withMinimumValue();
        return firstday.toString(datePattern1);
    }

    /**
     * 本周最后一天
     */
    public static String getCurrWeekLastDay() {
        DateTime dt = new DateTime();
        DateTime lastday = dt.dayOfWeek().withMaximumValue();
        return lastday.toString(datePattern1);
    }

    /**
     * 下周第一天
     */
    public static String getNextWeekFirstDay() {
        DateTime dt = new DateTime();
        dt = dt.plusWeeks(1);
        DateTime firstday = dt.dayOfWeek().withMinimumValue();
        return firstday.toString(datePattern1);
    }

    /**
     * 下周最后一天
     */
    public static String getNextWeekLastDay() {
        DateTime dt = new DateTime();
        dt = dt.plusWeeks(1);
        DateTime lastday = dt.dayOfWeek().withMaximumValue();
        return lastday.toString(datePattern1);
    }

	/**
	 * 当前时间向前增加天数
	 * @param days
	 * @return
	 */
	public static Date addDays(int days) {
		DateTime dateTime = new DateTime();
		dateTime = dateTime.plusDays(days);
		return dateTime.toDate();
	}

	/**
	 * 当前时间向后减少天数
	 * @param days
	 * @return
	 */
	public static Date subDays(int days) {
		DateTime dateTime = new DateTime();
		dateTime = dateTime.minusDays(days);
		return dateTime.toDate();
	}

	/**
	 * 指定时间向前增加天数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date addDays(Date date, int days) {
		DateTime dateTime = new DateTime(date);
		dateTime = dateTime.plusDays(days);
		return dateTime.toDate();
	}

	/**
	 * 指定时间向后减少天数
	 * @param date
	 * @param days
	 * @return
	 */
	public static Date subDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days * -1);
	    return cal.getTime();
	}

	/**
	 * 在当前时间增加时间
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, minutes);
	    return cal.getTime();
	}

	/**
	 * 在当前时间向后减少时间
	 * @param minutes
	 * @return
	 */
	public static Date subMinutes(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, minutes * -1);
	    return cal.getTime();
	}

	/**
	 * 指定时间增加时间
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(Date date, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes);
	    return cal.getTime();
	}

	/**
	 * 在指定时间向后减少
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date subMinutes(Date date, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes * -1);
	    return cal.getTime();
	}

	/**
	 * 时间置零
	 * @param date
	 * @return
	 */
	public static Date removeTime(Date date) {
		return DateUtil.parseDate(DateUtil.getDate(date));
	}

    /**
     * 日期置零
     * @param date
     * @return
     */
    public static Date removeDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar target = Calendar.getInstance();
        target.setTime(new Date(0));
        target.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        target.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        target.set(Calendar.SECOND, cal.get(Calendar.SECOND));
        return target.getTime();
    }

	/**
	 * 获取今天的开始时间：比如：2014-06-19 00:00:00
	 * @param date
	 * @return
	 */
	public static DateTime startTimeOfDay(Date date){
		DateTime nowTime = new DateTime(date.getTime());
        return nowTime.withTimeAtStartOfDay();
	}

	/**
	 * 获取今天的结束时间：比如：2014-06-19 23:59:59
	 * @param date
	 * @return
	 */
	public static DateTime endTimeOfDay(Date date){
		DateTime nowTime = new DateTime(date.getTime());
        return nowTime.millisOfDay().withMaximumValue();
	}

	/**
	 * 获取现在距离今天结束还有多长时间
	 * @return
	 */
	public static long overTimeOfToday(){
		DateTime nowTime = new DateTime();
		DateTime endOfDay = nowTime.millisOfDay().withMaximumValue();
		return endOfDay.getMillis()-nowTime.getMillis();
	}

	/**
	 * 得到两个日期之间相差的天数
	 *
	 * @param startDate 2006-03-01
	 * @param endDate 2006-05-01
	 * @return n 61
	 */
	public static int daysBetweenDates(Date startDate, Date endDate) {
		DateTime startTime = new DateTime(startDate.getTime());
		DateTime endTime = new DateTime(endDate.getTime());
        return Days.daysBetween(startTime, endTime).getDays();
	}

	/**
	 * 计算两个时间相差的分钟数
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long minuteBetweenDates(Date startDate, Date endDate) {
	    long seconds = (endDate.getTime() - startDate.getTime()) / 1000;
        return seconds / 60;
	}

    /**
     * 计算两个时间相差的秒数
     * @param startDate
     * @param endDate
     * @return
     */
    public static long secondBetweenDates(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / 1000L;
    }

	/**
	 * 计算两个时间相差的天数、小时数、分数
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long[] timeBetweenDates(Date startDate, Date endDate) {
		  long diff = endDate.getTime() - startDate.getTime(); //这样得到的差值是微秒级别
		  long days = diff / (1000 * 60 * 60 * 24);
		  long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
		  long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
          long seconds = ((diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60)- minutes*(1000* 60)))/1000;
		  return new long[]{days, hours, minutes, seconds};
	}

    /**
     * 返回两个时间相差的天数、小时数、分数的中文描述
     * @param startDate
     * @param endDate
     * @return
     */
    public static String descBetweenDates(Date startDate, Date endDate) {
        long[] requestTimeDurations = timeBetweenDates(startDate, endDate);
        String desc = String.format("%s天%s小时%s分钟%s秒", requestTimeDurations[0],
                requestTimeDurations[1], requestTimeDurations[2], requestTimeDurations[3]);
        if(desc.startsWith("0")) {
            if (desc.startsWith("0天0小时0分钟")) {
                desc = StringUtils.removeStart(desc, "0天0小时0分钟");
            } else if (desc.startsWith("0天0小时")) {
                desc = StringUtils.removeStart(desc, "0天0小时");
            } else if (desc.startsWith("0天")) {
                desc = StringUtils.removeStart(desc, "0天");
            }
        }
        if(desc.endsWith("天0小时0分钟0秒")){
            desc = StringUtils.removeEnd(desc, "0小时0分钟0秒");
        }
        else if(desc.endsWith("小时0分钟0秒")){
            desc = StringUtils.removeEnd(desc, "0分钟0秒");
        }
        else if(desc.endsWith("分钟0秒")){
            desc = StringUtils.removeEnd(desc, "0秒");
        }
        return desc;
    }

    /**
     * 获取日期在一个星期的周几
     * @param date
     * @return
     */
    public static String dateToWeek(Date date) {
        String[] weekDays = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

	public static DateTime getJodaDateTime (Object object){
		DateTime in = new DateTime(object);
//		System.out.println(in.getYear()); //当年
//		System.out.println(in.getMonthOfYear()); //当月
//		System.out.println(in.getDayOfMonth());  //当月第几天
//		System.out.println(in.getDayOfWeek());//本周第几天
//		System.out.println(in.getDayOfYear());//本年第几天
//		System.out.println(in.getHourOfDay());//时
//		System.out.println(in.getMinuteOfHour());//分
//		System.out.println(in.getMinuteOfDay());//当天第几分钟
//		System.out.println(in.getSecondOfMinute());//秒
//		System.out.println(in.getSecondOfDay());//当天第几秒
//		System.out.println(in.getWeekOfWeekyear());//本年第几周
//		System.out.println(in.getZone());//所在时区
//		System.out.println(in.dayOfWeek().getAsText()); //当天是星期几，例如：星期五
//		System.out.println(in.yearOfEra().isLeap()); //当你是不是闰年，返回boolean值
//		System.out.println(in.dayOfMonth().getMaximumValue());//当月day里面最大的值
		return in;
	}

	public static DateTime getJodaDateTime (String dateStr, String datePattern){
		DateTimeFormatter fmt = DateTimeFormat.forPattern(datePattern);//自定义日期格式
		return DateTime.parse(dateStr, fmt);
	}

    /**
     * 返回当前年（2位）+当前天在当前年的第几天（3位）+当前小时（2位）
     * @param date
     * @return 1836517
     */
    public static String getDateHourPrefix(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DAY_OF_YEAR); // 今天是第多少天
        int hour =  c.get(Calendar.HOUR_OF_DAY);
        String dayFmt = String.format("%1$03d", day); // 返回一年中第几天，0补位操作 必须满足三位
        String hourFmt = String.format("%1$02d", hour);  //返回一天中第几个小时，0补位操作 必须满足2位
        StringBuffer prefix = new StringBuffer();
        prefix.append((year - 2000)).append(dayFmt).append(hourFmt);
        return prefix.toString();
    }

    /**
     * 返回当前年（2位）+当前日期（4位）
     * @param date
     * @return 181231
     */
    public static String getDatePrefix(Date date) {
        return DateUtil.getDate(date, DateUtil.datePattern4);
    }

    /**
     * 将Date类转换为XMLGregorianCalendar
     * @param date
     * @return
     */
    public static XMLGregorianCalendar date2XmlDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        DatatypeFactory dtf = null;
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
        }
        XMLGregorianCalendar dateType = dtf.newXMLGregorianCalendar();
        dateType.setYear(cal.get(Calendar.YEAR));
        //由于Calendar.MONTH取值范围为0~11,需要加1
        dateType.setMonth(cal.get(Calendar.MONTH)+1);
        dateType.setDay(cal.get(Calendar.DAY_OF_MONTH));
        dateType.setHour(cal.get(Calendar.HOUR_OF_DAY));
        dateType.setMinute(cal.get(Calendar.MINUTE));
        dateType.setSecond(cal.get(Calendar.SECOND));
        return dateType;
    }

    /**
     * 将XMLGregorianCalendar转换为Date
     * @param cal
     * @return
     */
    public static Date xmlDate2Date(XMLGregorianCalendar cal){
        return cal.toGregorianCalendar().getTime();
    }

    /**
     * 将Date转换为LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    /**
     * 将LocalDateTime转换为Date
     * @param localDateTime
     * @return
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        return date;
    }

    /**
     * 将LocalDateTime类转换为XMLGregorianCalendar
     * @param localDateTime
     * @return
     */
    public static XMLGregorianCalendar localDateTimeToXmlDate(LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        return date2XmlDate(date);
    }

    /**
     * 将XMLGregorianCalendar转换为LocalDateTime
     * @param cal
     * @return
     */
    public static LocalDateTime xmlDate2LocalDateTime(XMLGregorianCalendar cal){
        Date date = xmlDate2Date(cal);
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 根据传入的2017年9月20日 09:30格式的日期时间字符串，正则获取其中的数字存为字符数组中
     *      Eg：2017年9月20日 09:30  结果为：nums = {"2017","9","20","09","30"}
     *
     * @param dateStr    格式为 2017年9月20日 09:30
     * @return
     */
    public static String[] getDateStrNumByGroups(String dateStr){
        String[] nums = new String[6];
        Matcher matcher = pattern.matcher( dateStr );
        int i = 0;
        while ( matcher.find() ){
            nums[i++] = matcher.group();
        }
        return nums;
    }
}
