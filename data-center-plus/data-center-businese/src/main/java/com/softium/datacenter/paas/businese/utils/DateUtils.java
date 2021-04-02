package com.softium.datacenter.paas.web.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期工具类
 *
 * @author abysen
 * @date 2019-09-09
 */
public class DateUtils {

	public final static String FORMAT_DATE_PATTERN = "yyyy-MM-dd";

	public final static String FORMAT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public final static String FORMAT_DATETIME_PATTERN_FULL = "yyyy-MM-dd HH:mm:ss.SSS";
	public final static String FORMAT_DATETIME_PATTERN_FULL_1 = "yyyy-MM-dd HH:mm:ss.S";

	public final static String FORMAT_TIME_PATTERN = "HH:mm:ss";

	public final static String FORMAT_YYYYMMDD = "yyyyMMdd";

	public final static String FORMAT_YYYYMM = "yyyyMM";

	public final static String FORMAT_YYYY_MM = "yyyy-MM";

	public final static String FORMAT_YYY_MM_DD = "yyyy/MM/dd";
	public final static String FORMAT_YYY_MM_DD_PATTERN = "yyyy/MM/dd HH:mm:ss";
	public final static String FORMAT_YYY_MM_DD_PATTERN_FULL  = "yyyy/MM/dd HH:mm:ss.SSS";

	public final static String FORMT_TIME_ZONE = "HH:mm";

	public final static String FORMAT_TIME_ZONE_SECOND = "yyyy-MM-dd'T'HH:mm:ss";
	public final static String FORMAT_TIME_ZONE_MINUTS = "yyyy-MM-dd'T'HH:mm";
	public final static String FORMAT_TIME_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


	public final static String FORMAT_DD = "dd";
	public final static Map<String,String> formatDate = new Hashtable<>();
	static {
		//弱匹配日期，日期是否正确，交由JAVA代码判断
		formatDate.clear();
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}$",FORMAT_DATE_PATTERN);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$",FORMAT_DATETIME_PATTERN);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{0,3}$",FORMAT_DATETIME_PATTERN);
		formatDate.put("^[1-9]\\d{3}/\\d{1,2}/\\d{1,2}",FORMAT_YYY_MM_DD);
		formatDate.put("^[1-9]\\d{3}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$",FORMAT_YYY_MM_DD_PATTERN);
		formatDate.put("^[1-9]\\d{3}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}.\\d{0,3}$",FORMAT_YYY_MM_DD_PATTERN);
		formatDate.put("^[1-9]\\d{7}$",FORMAT_YYYYMMDD);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\+\\d{1,2}:\\d{1,2}$",FORMT_TIME_ZONE);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}$",FORMAT_TIME_ZONE_SECOND);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}$",FORMAT_TIME_ZONE_MINUTS);
		formatDate.put("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}Z$",FORMAT_TIME_UTC);

	}




	public static String dateToStr(Date date) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(date);
		}
		return "";
	}

	public static String dateToStrs(Date date) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(date);
		}
		return "";
	}

	public static String dateToStrYearMonth(Date date) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			return sdf.format(date);
		}
		return "";
	}

	public static String dateToStrYM(Date date) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			return sdf.format(date);
		}
		return "";
	}

	public static Date format(Long timeStamp) {
		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();*/
	}

	public static Date format(Long timeStamp,String formatStr) {
		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat(formatStr);
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();*/
	}

	public static String formatYearMonth(Long timeStamp) {
		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public static String formatDateToStr(Long timeStamp) {
		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATETIME_PATTERN);
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}


	public static Date format(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 下周一日期
	 *
	 * @return
	 */
	public static Date nextWeekMonday() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDayStart());
		if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			// 设置本周一
			calendar.set(Calendar.DAY_OF_WEEK, 2);
		} else {
			// 设置本周一
			calendar.set(Calendar.DAY_OF_WEEK, 2);
			// 下周一 (在本周一的基础上+1个星期)
			calendar.add(Calendar.WEEK_OF_MONTH, 1);
		}

		return calendar.getTime();
	}

	/**
	 * 本周日
	 *
	 * @return
	 */
	public static String currentWeekSunday(String date) {
		Calendar calendar = Calendar.getInstance();
		//calendar.setTime(format(date));
		// 周日
		if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			calendar.set(Calendar.DAY_OF_WEEK, 1);
			calendar.add(Calendar.WEEK_OF_MONTH, 1);
		}
		return dateToStr(calendar.getTime());
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 *
	 * @return
	 */
	public static String getDate() {
		return getDateTime(new Date());
	}

	/**
	 * 数据库需要存储的时间对象
	 *
	 * @return
	 */
	public static Timestamp getTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * 带格式的日期转化<br>
	 *
	 * @param formatPattern 转换的字符串格式
	 *                      <br>
	 * @param d             日期
	 *                      <br>
	 * @return
	 */
	public static String toString(String formatPattern, Date d) {
		return new SimpleDateFormat(formatPattern).format(d);
	}

	/**
	 * 当前日期 <br>
	 *
	 * @return yyyy-MM-dd
	 */
	public static String getCurrentDate() {
		return getDate(new Date());
	}

	/**
	 * 日期对象转字符串
	 *
	 * @param d
	 * @return yyyy-MM-dd
	 */
	public static String getDate(Date d) {
		return new SimpleDateFormat(FORMAT_DATE_PATTERN).format(d);
	}

	/**
	 * 日期时间 <br>
	 *
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String getDateTime(Date d) {
		if (d != null) {
			return new SimpleDateFormat(FORMAT_DATETIME_PATTERN).format(d);
		} else {
			return null;
		}
	}

	/**
	 * 当前日期时间 <br>
	 *
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String getCurrentDateTime() {
		return getDateTime(new Date());
	}

	/**
	 * 时间<br>
	 *
	 * @return HH:mm:ss
	 */
	public static String getTime(Date d) {
		return new SimpleDateFormat(FORMAT_TIME_PATTERN).format(d);
	}

	/**
	 * 当前时间的毫秒数
	 *
	 * @return
	 */
	public static Long lastTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 当前时间的秒数
	 *
	 * @return
	 */
	public static Long currentTimeSeconds() {
		return lastTimeMillis() / 1000;
	}

	/**
	 * 当前时间<br>
	 *
	 * @return HH:mm:ss
	 */
	public static String getCurrentTime() {
		return getTime(new Date());
	}

	/**
	 * <b>获取当前时间</b><br>
	 * y 年 M 月 d 日 H 24小时制 h 12小时制 m 分 s 秒
	 *
	 * @param format 日期格式
	 * @return String
	 */
	public static String getCurrentDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}

	/**
	 * 获取制定日期的格式化字符串
	 *
	 * @param date   Date 日期
	 * @param format String 格式
	 * @return String
	 */
	public static String getFormatedDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 判断哪个日期在前 日过日期一在日期二之前，返回true,否则返回false
	 *
	 * @param date1 日期一
	 * @param date2 日期二
	 * @return boolean
	 */
	public static boolean isBefore(Date date1, Date date2) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);

		if (c1.before(c2)) {
			return true;
		}

		return false;
	}

	/**
	 * 将字符串转换成日期
	 *
	 * @param date
	 *            String 日期字符串
	 * @return Date
	 * @throws ParseException
	 */
	public static Date parseDateFromString(String date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date resultDate = null;
		try {
			resultDate = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return resultDate;
	}

	/**
	 * 获取指定日期当月的最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date lastDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获取指定日期当月的第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date firstDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date indexDayOfMonth(Date date,Integer days){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}

	/**
	 * 获取指定日期上月的最后第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date lastDayOfMonthNext(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获取指定日期上月的第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date firstDayOfMonthNext(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}



	/**
	 * 以当前日期为基准，偏移N个月后的，某一天
	 * @param date			基准日期
	 * @param offsetMonth   偏移月份，正数为向后，负数为向前
	 * @param day			偏移运算后，日为那天
	 * @return
	 */
	public static Date dayOfMonthOffect(Date date,int offsetMonth,int day){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, offsetMonth);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}
	/**
	 * 是否是闰年
	 *
	 * @param year 年份
	 * @return boolean
	 */
	public static boolean isLeapYear(int year) {
		GregorianCalendar calendar = new GregorianCalendar();
		return calendar.isLeapYear(year);
	}

	/**
	 * 获取指定日期之前或者之后多少天的日期
	 *
	 * @param day    指定的时间
	 * @param offset 日期偏移量，正数表示延后，负数表示天前
	 * @return Date
	 */
	public static Date getDateByOffset(Date day, int offset) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		c.add(Calendar.DAY_OF_MONTH, offset);
		return c.getTime();
	}

	/**
	 * 获取一天开始时间 如 2014-12-12 00:00:00
	 *
	 * @return
	 */
	public static Date getDayStart() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一天结束时间 如 2014-12-12 23:59:59
	 *
	 * @return
	 */
	public static Date getDayEnd() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}

	/**
	 * 获取一天开始时间 如 2014-12-12 00:00:00
	 *
	 * @return
	 */
	public static Date getDayStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取一天结束时间 如 2014-12-12 23:59:59
	 *
	 * @return
	 */
	public static Date getDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}


	/**
	 * 时间分段 比如：2014-12-12 10:00:00 ～ 2014-12-12 14:00:00 分成两段就是 2014-12-12
	 * 10：00：00 ～ 2014-12-12 12：00：00 和2014-12-12 12：00：00 ～ 2014-12-12 14：00：00
	 *
	 * @param start  起始日期
	 * @param end    结束日期
	 * @param pieces 分成几段
	 */
	public static Date[] getDatePieces(Date start, Date end, int pieces) {

		Long sl = start.getTime();
		Long el = end.getTime();

		Long diff = el - sl;

		Long segment = diff / pieces;

		Date[] dateArray = new Date[pieces + 1];

		for (int i = 1; i <= pieces + 1; i++) {
			dateArray[i - 1] = new Date(sl + (i - 1) * segment);
		}

		// 校正最后结束日期的误差，可能会出现偏差，比如14:00:00 ,会变成13:59:59之类的
		dateArray[pieces] = end;

		return dateArray;
	}

	/**
	 * 获取某个日期的当月第一天
	 *
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * 下个月的今天
	 *
	 * @return
	 */
	public static String getDayOfNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
	}

	/**
	 * 获取某个日期的当月最后一天
	 *
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return cal.getTime();
	}

	/**
	 * 获取某个日期的当月第一天
	 *
	 * @return
	 */
	public static Date getFirstDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * 获取某个日期的当月最后一天
	 *
	 * @return
	 */
	public static Date getLastDayOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return cal.getTime();
	}

	/**
	 * 在指定日期上+多少
	 *
	 * @param date          指定日期
	 * @param increnetValue 增长值
	 * @param increnetType  增长类型
	 * @return
	 */
	public static String dateToAdd(Date date, int increnetValue, int increnetType) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(increnetType, increnetValue);
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATETIME_PATTERN);
		return sdf.format(calendar.getTime());
	}

	/**
	 * yyyy-MM-dd
	 *
	 * @param date
	 * @param increnetValue
	 * @param increnetType
	 * @return
	 */
	public static String dateToAdd(String date, int increnetValue, int increnetType) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_PATTERN);
		Date d;
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			d = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(increnetType, increnetValue);

		return sdf.format(calendar.getTime());
	}

	/**
	 * 指定日期格式上增长指定的数据
	 *
	 * @param date          日期
	 * @param increnetValue 增长值
	 * @param increnetType  增长类型
	 * @param formate       格式
	 * @return
	 */
	public static String dateToAdd(String date, int increnetValue, int increnetType, String formate) {
		SimpleDateFormat sdf = new SimpleDateFormat(formate);
		Date d;
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			d = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		calendar.add(increnetType, increnetValue);

		return sdf.format(calendar.getTime());
	}

	public static Date dateAdd(Date date,int increnetValue,int increnetType){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(increnetType, increnetValue);
		return calendar.getTime();
	}

	/**
	 * 指定日期格式上增长指定的数据
	 *
	 * @param date          日期
	 * @param increnetValue 增长值
	 * @param increnetType  增长类型
	 * @param formate       格式
	 * @return
	 */
	public static String dateToAdd(Date date, int increnetValue, int increnetType, String formate) {
		SimpleDateFormat sdf = new SimpleDateFormat(formate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(increnetType, increnetValue);
		return sdf.format(calendar.getTime());
	}

	public static String timeStamp2Date(String seconds) {
		if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
			return "";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_PATTERN);
		return sdf.format(new Date(Long.parseLong(seconds)));
	}


	/**
	 * 2个日期的间隔天数
	 *
	 * @param startDay
	 * @param endDay
	 * @return
	 */
	public static int daysBetween(Date startDay, Date endDay) {
		try {
			long time1 = startDay.getTime();
			long time2 = endDay.getTime();
			long between_days = (time2 - time1) / (1000 * 3600 * 24);

			return Integer.parseInt(String.valueOf(between_days));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}



	/**
	 * 是否在当前月日期内
	 *
	 * @param date
	 * @return
	 */
	public static boolean isCurrentMonth(String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(format(date, FORMAT_DATE_PATTERN));
		String d = getFormatedDate(calendar.getTime(), "yyyy-MM");
		String cm = getFormatedDate(new Date(), "yyyy-MM");
		return cm.equals(d);
	}

	/**
	 * 是否在当前月日期内
	 *
	 * @param date
	 * @param currentMonth yyyy-MM
	 * @return
	 */
	public static boolean isCurrentMonth(String date, String currentMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(format(date, FORMAT_DATE_PATTERN));
		String d = getFormatedDate(calendar.getTime(), "yyyy-MM");
		return currentMonth.equals(d);
	}


	public static String getLastMonth(String yearMonth) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		Date date = format(yearMonth, FORMAT_YYYY_MM);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); // 设置为当前时间
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
		date = calendar.getTime();
		String accDate = format.format(date);
		return accDate;
	}
	public static String getNextMonth(String yearMonth) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		Date date = format(yearMonth, FORMAT_YYYY_MM);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); // 设置为当前时间
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1); // 设置为上一个月
		date = calendar.getTime();
		String accDate = format.format(date);
		return accDate;
	}

	public static String getNextMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); // 设置为当前时间
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
		date = calendar.getTime();
		String accDate = format.format(date);
		return accDate;
	}


	public static String getYearMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); // 设置为当前时间
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)); // 设置为上一个月
		date = calendar.getTime();
		String accDate = format.format(date);
		return accDate;
	}

	public static Date praseTimeStamp(String timeStamp) {

		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static Date praseTimeStamp1(String timeStamp) {

		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = format.format(ts);
		Date date = null;
		try {
			date = format.parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}


	public static String praseTimeStampYYMM(String timeStamp) {

		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		String d = format.format(ts);
		return d;
	}

	public static String praseTimeStampPattern(String timeStamp,String pattern) {
		//时间戳转化为Sting或Date
		Long ts = Long.valueOf(timeStamp).longValue();
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		String d = format.format(ts);
		return d;
	}


	/**
	 * 时间戳转换成：yyyy-MM-dd
	 *
	 * @param timestamp
	 * @return
	 */
	public static String timestampToYYYYMMDD(long timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String d = format.format(timestamp);
		return d;
	}


	public static Long dateTotimestamp(Date timestamp) {
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time=dateToStrs(timestamp);
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public static Long yearMonthTotimestamp(String timestamp) {
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM");
		String time=timestamp;
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public static boolean isSameDate(Date date1, Date date2) {
		try {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(date1);

			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(date2);

			boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
					.get(Calendar.YEAR);
			boolean isSameMonth = isSameYear
					&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
			boolean isSameDate = isSameMonth
					&& cal1.get(Calendar.DAY_OF_MONTH) == cal2
					.get(Calendar.DAY_OF_MONTH);

			return isSameDate;
		} catch (Exception e) {

		}
		return false;


	}

	public boolean isBeforeMonth(String beforeMonth, String currentMonth) {
		Date bm = format(beforeMonth, "yyyy-MM");
		Date cm = format(currentMonth, "yyyy-MM");
		Calendar cmCal = Calendar.getInstance();
		Calendar bmCal = Calendar.getInstance();
		cmCal.setTime(cm);
		cmCal.add(Calendar.MONTH, -1);
		cm = cmCal.getTime();
		bmCal.setTime(bm);
		bmCal.getTime();
		boolean isSameYear = cmCal.get(Calendar.YEAR) == bmCal
				.get(Calendar.YEAR);
		boolean isSameMonth = isSameYear
				&& cmCal.get(Calendar.MONTH) == bmCal.get(Calendar.MONTH);
         /*if(DateUtil.isSameDate(bm, cm)){
              return true;
          }*/
		return isSameMonth;
	}

	public static boolean isNumeric(String str) {
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");

	}


	/**
	 * 当前日期加减天
	 * @param
	 * @return
	 */
	public static String getTimeDay() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		//取今天日期,如果日期类型为String类型,可以使用df.parse()方法,转换为Date类型
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();//new一个Calendar类,把Date放进去
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);//实现日期加一操作,也就是明天
		//控制台打印的日期为明天日期,2019-06-11
		// System.out.println("明天的日期为:" + df.format(calendar.getTime()));
		//此时的日期为明天的日期,要实现昨天,日期应该减二
		calendar.add(Calendar.DATE, -2);
		// System.out.println("昨天的日期为:" + df.format(calendar.getTime()));


		return df.format(calendar.getTime());
	}

	/**
	 * 根据传入时间戳，返回日期为当天，时间为：23:59:59
	 *
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	public static String getEndDate4Search(long timestamp) {

			String dateStr = timestampToYYYYMMDD(timestamp);
			Date endDate = parseDateFromString(dateStr);
			Calendar c = Calendar.getInstance();
			c.setTime(endDate);
			c.add(Calendar.DAY_OF_YEAR, +1);
			c.add(Calendar.SECOND, -1);
			endDate = c.getTime();
			String startDataStr = getDateTime(endDate);
			return startDataStr;
	}


	public static Date getEndDate(long timestamp) {

		String dateStr = timestampToYYYYMMDD(timestamp);
		Date endDate = parseDateFromString(dateStr);
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.add(Calendar.DAY_OF_YEAR, +1);
		c.add(Calendar.SECOND, -1);
		endDate = c.getTime();
		return endDate;
	}



	public static Date getUpDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1); //得到前一天
		Date date = calendar.getTime();
		return date;
	}


	public static Date getYeasterDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1); //得到前一天
		return calendar.getTime();
	}


    //判断是否当月第一天
	public static boolean isNow() {
		//当前时间
		Date now = new Date();

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		//获取今天的日期
		String nowDay = sf.format(now);

		Calendar calendar1=Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		Date date = calendar1.getTime();

		//对比的时间
		String day = sf.format(date);

		return day.equals(nowDay);
	}

	public static String yearStart() {
		//当前时间
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar1=Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		Date date = calendar1.getTime();

		//对比的时间
		String day = sf.format(date);

		return day;
	}
	public static String getSpecifiedDayBefore(String specifiedDay){

		Calendar c = Calendar.getInstance();
		Date date=null;
		try {
			date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day=c.get(Calendar.DATE);
		c.set(Calendar.DATE,day-1);

		String dayBefore=new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		return dayBefore;
	}

	/**
	 * 获取去年的今天
	 * @param date
	 * @return
	 */
	public static Date getLastYear(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, -1);
		Date endDate = c.getTime();
		return endDate;
	}



	/**
	 * 判断日期格式是否正确
	 *
	 * @param value
	 * @return
	 */
	public static boolean isValidDate(String value) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);//指定从所传字符串的首位开始解析
		try {
			sdf.setLenient(false);
			Date date = sdf.parse(value, pos);
			if (date == null) {
				return false;
			} else {
				if (pos.getIndex() > sdf.format(date).length()) {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Date formatDateBlur(String timStr){
		for (Map.Entry<String, String> entry : formatDate.entrySet()) {
			String regMatch = entry.getKey();
			String dateFormat = entry.getValue();

			Pattern r = Pattern.compile(regMatch);
			Matcher m = r.matcher(timStr);
			if (m.matches()){
				try{
					//带时区的单独处理
					if (FORMT_TIME_ZONE.equals(dateFormat)){
						Pattern datePattern = Pattern.compile("^[1-9]\\d{3}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}");
						Matcher mDatePattern = datePattern.matcher(timStr);
						String dateTimeStr = "";
						if (mDatePattern.find()){
							dateTimeStr = mDatePattern.group().replace("T"," ");
						}


						Pattern timeZone =  Pattern.compile("\\+\\d{2}:\\d{2}$");
						Matcher mTimeZonePattern = timeZone.matcher(timStr);
						String timeZoneStr = "";
						if (mTimeZonePattern.find()){
							timeZoneStr = "GTM"+mTimeZonePattern.group();
						}
						SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATETIME_PATTERN);
						sdf.setTimeZone(TimeZone.getTimeZone(timeZoneStr));
						return sdf.parse(dateTimeStr);
					}
					else if (FORMAT_TIME_UTC.equals(dateFormat)){
						SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_UTC);
						sdf.setTimeZone(TimeZone.getTimeZone("GTM"+8));
						return sdf.parse(timStr);
					}
					else{
						return new SimpleDateFormat(dateFormat).parse(timStr);
					}
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String dateToStr(LocalDate localDate,String pattern){
		if(StringUtils.isBlank(pattern)){
			pattern=FORMAT_DATE_PATTERN;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		return localDate.format(dtf);
	}

	public static String dateToStr(LocalDateTime localDateTime,String pattern){
		if(StringUtils.isBlank(pattern)){
			pattern=FORMAT_DATE_PATTERN;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		return localDateTime.format(dtf);
	}

	public static LocalDateTime strToLocalDateTime(String dateTime,String pattern){
		if(StringUtils.isBlank(pattern)){
			pattern=FORMAT_DATETIME_PATTERN;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		return LocalDateTime.parse(dateTime,dtf);
	}

	public static LocalDateTime timestampToLocalDateTime(Long timestamp){
		Instant instant = Instant.ofEpochMilli(timestamp);
		ZoneId zone = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(instant,zone);
	}

	public static LocalDate strToLocalDate(String dateTime,String pattern){
		if(StringUtils.isBlank(pattern)){
			pattern=FORMAT_DATETIME_PATTERN;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
		return LocalDate.parse(dateTime,dtf);
	}


	public static long getTimestampOfDateTime(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		Instant instant = localDateTime.atZone(zone).toInstant();
		return instant.toEpochMilli();
	}
/**判断某日期，是否在指定日期范围内
 *@param startTime 开始时间
 * @param  endTime 结束时间
 * @param compareTime  所需校验时间 */
public static boolean compareStrDate(String startTime,String endTime,String compareTime){
	boolean timeStatus=false;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	try {
		long objstartTime=sdf.parse(startTime).getTime();
		long objendTime=sdf.parse(endTime).getTime();
		long objCompareTime=sdf.parse(compareTime).getTime();
		if(objCompareTime>=objstartTime&&objCompareTime<=objendTime){
			timeStatus=true;
		}
	}catch (ParseException e){
		e.getMessage();
	}
	return timeStatus;
}
		public static void main(String[] args) throws Exception {

		/*
		 * Date d=new Date(); System.out.println(getDateByOffset(d,-1));
		 */
		// System.out.println(getFormatedDate(getFirstDayOfMonth(new Date()),
		// "yyyy-MM-dd"));
		// System.out.println(getFormatedDate(getLastDayOfMonth(new Date()),
		// "yyyy-MM-dd"));
		// System.out.println(getFormatedDate(getFirstDayOfMonth(2013, 2),
		// "yyyy-MM-dd"));
		// System.out.println(getFormatedDate(getLastDayOfMonth(2000, 2),
		// "yyyy-MM-dd"));
		// System.out.println(getFormatedDate(getLastDayOfMonth(2001, 2),
		// "yyyy-MM-dd"));
		// System.out.println(daysBetween("2016-7-29", "2016-7-22"));
		//

		//System.out.println(getLastMonth(getCurrentDate()));
//		System.out.println(getLastMonth(getCurrentDate()));
//		System.out.println(getNextMonth());
//		System.out.println(	timeStamp2Date("2019-09-24T11:42:11.212Z"));
//		System.out.println(	timeStamp2Date("2019-09-24T11:42:11.212Z"));
//		System.out.println(timeStamp2Date("2019-12-16T09:03:31+08:00"));


		//System.out.println(format("2019-09-01",FORMAT_YYYY_MM).equals(format("2019-09",FORMAT_YYYY_MM)));
		//System.out.println((format("2019-10-09")));
		//System.out.println((format("2019-10-09T02:38:56.920Z")));
//		System.out.println(dateToStr(format("2019-10-09T02:38:56.920Z")));


		//System.out.println("---------"+praseTimeStamp("1569398944268"));
		//System.out.println("000"+isNumeric("123好"));
		//Date date = new Date();
		//System.out.println(timestampToYYYYMMDD(date.getTime()));

//		String endDate = getEndDate4Search(System.currentTimeMillis());
//		System.out.println("endDate:" + dateToStrYM(format("2019-09",FORMAT_YYYY_MM)));
//		System.out.println(getNextMonth("2019-12"));

		//System.out.println(DateUtils.getFormatedDate(getDateByOffset(new Date(),-1),DateUtils.FORMAT_DATETIME_PATTERN));
		//System.out.println(DateUtils.getFormatedDate(getDateByOffset(new Date(),1),DateUtils.FORMAT_DATETIME_PATTERN));
		System.out.println(DateUtils.getLastMonth(getCurrentDate()));

		System.out.println(DateUtils.dateToStr(DateUtils.formatDateBlur("2019-11-21")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-11-22 10:10:12")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-11-23 10:10:12.000")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-11-23 11:10:12.0")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-11-23 12:10:12.00")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-11-23 2:1:2.00")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019/11/24")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019/11/25 10:20:12")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019/11/28 10:20:12.000")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("20191126")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-12-17T09:03:31+08:00")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-12-19T09:03:31")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-12-20T09:03")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2017-11-27T03:16:03.944Z")));
		System.out.println(DateUtils.dateToStrs(DateUtils.formatDateBlur("2019-12-11T18:40:07")));
	}
}
