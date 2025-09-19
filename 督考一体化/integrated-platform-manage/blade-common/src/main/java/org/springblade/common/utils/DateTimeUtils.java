package org.springblade.common.utils;

import com.alibaba.nacos.common.utils.DateFormatUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimeUtils {

	// private static Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);

	private static List<String> dateFormatPattern = new ArrayList<String>();

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

	static {
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss.S z");
		dateFormatPattern.add("yyyy-MM-dd G HH:mm:ss.S z");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss.S 'UTC'");
		dateFormatPattern.add("yyyy-MM-dd G HH:mm:ss.S 'UTC'");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss.S z");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss.S a");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ssz");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss z");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ss 'UTC'");
		dateFormatPattern.add("yyyy-MM-dd'T'HH:mm:ss.SX");
		dateFormatPattern.add("yyyy-MM-dd'T'HH:mm:ssX");
		dateFormatPattern.add("yyyy-MM-dd'T'HH:mmX");
		dateFormatPattern.add("yyyy-MM-dd HH:mm:ssa");
		dateFormatPattern.add("yyyy/MM/dd");
		dateFormatPattern.add("yyyy/M/d");
		dateFormatPattern.add("yyyy-MM-dd");
		dateFormatPattern.add("yyyy-M-d");
		dateFormatPattern.add("yyyy/M/d");
		dateFormatPattern.add("yyyy年M月d日");
		dateFormatPattern.add("yyyy年MM月dd日");
		dateFormatPattern.add("yyyy-MM-dd'T'HH:mm:ss.SSS+0800");
	}

	// 格式：中文星期
	private final static String[] FORMAT_WEEK_CHINESE_SIMPLE = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
	// 格式：中文星期
	private final static String[] FORMAT_WEEK_CHINESE = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
	// 格式：英文格式简写
	private final static String[] FORMAT_WEEK_ENGLISH_SIMPLE = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	// 格式：英文全称
	private final static String[] FORMAT_WEEK_ENGLISH = {"Sun", " Mon", " Tue", " Wed", " Thu", " Fri", " Sat"};

	/**
	 * 获取今日年份
	 *
	 * @return yyyy
	 */
	public static String getCurrentYear() {
		return DateFormatUtils.format(new Date(), "yyyy");
	}

	/**
	 * 获取今日月份
	 *
	 * @return MM
	 */
	public static String getCurrentMonth() {
		return DateFormatUtils.format(new Date(), "MM");
	}

	/**
	 * 获取今日日期
	 *
	 * @return dd
	 */
	public static String getTodayDay() {
		return DateFormatUtils.format(new Date(), "dd");
	}


	/**
	 * 返回年月
	 *
	 * @return yyyyMM
	 */
	public static String getTodayChar6() {
		return DateFormatUtils.format(new Date(), "yyyyMM");
	}

	/**
	 * 返回年月日
	 *
	 * @return yyyyMMdd
	 */
	public static String getTodayChar8() {
		return DateFormatUtils.format(new Date(), "yyyyMMdd");
	}

	/**
	 * 返回 年月日小时分
	 *
	 * @return yyyyMMddHHmm
	 */
	public static String getTodayChar12() {
		return DateFormatUtils.format(new Date(), "yyyyMMddHHmm");
	}

	/**
	 * 返回 年月日小时分秒
	 *
	 * @return yyyyMMddHHmmss
	 */
	public static String getTodayChar14() {
		return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
	}

	/**
	 * 返回 年月日小时分秒 毫秒
	 *
	 * @return yyyyMMddHHmmssS
	 */
	public static String getTodayChar17() {
		String dateString = DateFormatUtils.format(new Date(), "yyyyMMddHHmmssS");
		int length = dateString.length();

		if (length < 17) {
			String endStr = dateString.substring(14, length);
			int len = endStr.length();
			for (int i = 0; i < 3 - len; i++) {
				endStr = "0" + endStr;
			}
			dateString = dateString.substring(0, 14) + endStr;
		}
		return dateString;
	}

	/**
	 * 返回本地系统当前时间戳
	 *
	 * @return
	 */
	public static long getSysCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 日期格式转换
	 *
	 * @param timeMillis
	 * @param format
	 * @return
	 */
	public static String convertTimeFormat(Date timeMillis, String format) {
		return DateFormatUtils.format(timeMillis, format);
	}

	/**
	 * 返回当前系统时间
	 *
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String getSystemTime() {
		Calendar theCa = Calendar.getInstance();
		theCa.setTime(new Date());
		return DateFormatUtils.format(theCa.getTime(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 获取今天星期几
	 *
	 * @param type 默认值1:中文（周一）；2:中文（星期一）；3:英文缩写（Mon）;4:英文全称（Monday）
	 * @return
	 */
	public static String getWeekName(int type) {
		String strResult = " ";
		try {
			Calendar calendar = Calendar.getInstance();
			int intWeekNum = calendar.get(Calendar.DAY_OF_WEEK);
			intWeekNum = intWeekNum - 1;
			if (type == 1) {
				strResult = FORMAT_WEEK_CHINESE_SIMPLE[intWeekNum];
			} else if (type == 2) {
				strResult = FORMAT_WEEK_CHINESE[intWeekNum];
			} else if (type == 3) {
				strResult = FORMAT_WEEK_ENGLISH_SIMPLE[intWeekNum];
			} else if (type == 4) {
				strResult = FORMAT_WEEK_ENGLISH[intWeekNum];
			} else {
				strResult = FORMAT_WEEK_CHINESE_SIMPLE[intWeekNum];
			}
		} catch (Exception ex) {
			strResult = " ";
		}
		return strResult;
	}

	/**
	 * 获取当前月天数
	 *
	 * @return
	 */
	public static int getCurrentMonthDays() {
		Calendar a = Calendar.getInstance();
		a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
		return a.get(Calendar.DATE);
	}

	/**
	 * 判断当前时间是否在两个时间之间(区间都不包含)
	 *
	 * @param startTime 开始时间 （格式yyyyMMdd24HHmmss）20160229160354
	 * @param endTime   结束时间（格式yyyyMMdd24HHmmss）20160229160354
	 * @return
	 * @Auth NZF 2016-02-29
	 */
	public static boolean isBetweenTwoTimes(String startTime, String endTime) {
		//当前时间
		long nowTime = Long.parseLong(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
			return false;
		}
		if (Long.parseLong(startTime) < nowTime && nowTime < Long.parseLong(endTime)) {
			return true;
		}
		return false;
	}
	/**
	 * 17位格式判断当前时间是否在两个时间之间(区间都不包含)
	 *
	 * @param startTime 开始时间 （格式yyyyMMddHHmmssS）20160724111924116
	 * @param endTime   结束时间（格式yyyyMMdd24HHmmss）20160724111924116
	 * @return
	 * @Auth NZF 2016-02-29
	 */
	public static boolean isBetweenTwoTimes17(String startTime, String endTime) {
		//当前时间
		long nowTime = Long.parseLong(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssS"));
		if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
			return false;
		}
		if (Long.parseLong(startTime) < nowTime && nowTime < Long.parseLong(endTime)) {
			return true;
		}
		return false;
	}

	/*
	 * 设置有效时间至次日00：05：00
	 */
	public static long getTodayExpireTime() {
		Calendar calendar = Calendar.getInstance();
		String date = DateTimeUtils.getTodayChar8();
		calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6, 8)) + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 05);
		calendar.set(Calendar.SECOND, 00);

		Calendar ccalendar = Calendar.getInstance();// 当前时间
		ccalendar.setTime(new Date());
		return calendar.getTimeInMillis() - ccalendar.getTimeInMillis();
	}

	/**
	 * 失效时间至月底
	 *
	 * @return
	 */
	public static long getMonthExpireTime() {
		Calendar calendar = Calendar.getInstance();
		String date = DateTimeUtils.getTodayChar8();
		calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
		calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);

		Calendar ccalendar = Calendar.getInstance();// 当前时间
		ccalendar.setTime(new Date());
		return calendar.getTimeInMillis() - ccalendar.getTimeInMillis();
	}

	/**
	 * 永久有效(方法中实现使用1年有效时长)
	 *
	 * @return
	 */
	public static long getOneYearExpireTime() {
		Calendar calendar = Calendar.getInstance();
		String date = DateTimeUtils.getTodayChar8();
		calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)) + 1);
		calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Calendar ccalendar = Calendar.getInstance();// 当前时间
		ccalendar.setTime(new Date());
		return calendar.getTimeInMillis() - ccalendar.getTimeInMillis();
	}

	public static Date formatDate(String date){
		Date result = null;
		fp : for (String formatPattern : dateFormatPattern) {
			try {
				simpleDateFormat.applyPattern(formatPattern);
				result = simpleDateFormat.parse(date);
				if (result != null) {
					break fp;
				}
			} catch (ParseException e) {
				// e.printStackTrace();
				//logger.info(date + " format fail");
			}
		}
		return result;
	}

	/**
	 * 判断date是在当前时间的正负minute分钟内
	 * @param date
	 * @param minute
	 * @return
	 */
	public static boolean isBetween(Date date,Integer minute){
		Calendar beforeTime = Calendar.getInstance();
		beforeTime.add(Calendar.MINUTE,-minute);// 5分钟之前的时间
		Date beforeDate = beforeTime.getTime();
		beforeTime.add(Calendar.MINUTE,+minute+minute);// 5分钟之后的时间
		Date afterDate = beforeTime.getTime();
		return beforeDate.before(date) && afterDate.after(date);
	}
}

