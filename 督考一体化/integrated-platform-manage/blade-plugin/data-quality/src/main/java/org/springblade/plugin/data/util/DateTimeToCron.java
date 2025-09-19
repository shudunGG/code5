package org.springblade.plugin.data.util;



/**
 * @author MaQiuyun
 * @date 2021/12/1515:33
 * @description:将字符串类型的日期、时间等转换为定时任务表达式
 */
public class DateTimeToCron {
	/**
	 * 具体的日期时间转为cron
	 *
	 * @param datetime yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String datetimeToCron(String datetime) {
		//获取具体年月日时分秒对应的数值
		String[] split = datetime.split(" ");
		String[] ymd = split[0].split("-");
		String[] hms = split[1].split(":");
		StringBuffer cron = new StringBuffer(hms[2]).append(" ").append(hms[1]).append(" ").append(hms[0]).append(" ").append(ymd[2]).append(" ").append(ymd[1]).append(" ? ").append(ymd[0]);
		return cron.toString();
	}

	/**
	 * 时间转为cron（即获取每日的某一时间点执行的Cron）
	 *
	 * @param time HH:mm:ss
	 * @return
	 */
	public static String timeToCron(String time) {
		String[] hms = time.split(":");
		StringBuffer cron = new StringBuffer(hms[2]).append(" ").append(hms[1]).append(" ").append(hms[0]).append(" * * ?");
		return cron.toString();
	}

	/**
	 * 周几和时间转为cron(即每周几的某个时间点执行的Cron)
	 *
	 * @param week 1~7
	 * @param time HH:mm:ss
	 * @return
	 */
	public static String weekTimeToCron(String week, String time) {
		String[] hms = time.split(":");
		StringBuffer cron = new StringBuffer(hms[2]).append(" ").append(hms[1]).append(" ").append(hms[0]).append(" ? * ").append(week);
		return cron.toString();
	}

	/**
	 * 日期和时间转为cron（每月的某天的某个时间执行的cron）
	 *
	 * @param day  1~31
	 * @param time HH:mm:ss
	 * @return
	 */
	public static String dayTimeToCron(String day, String time) {
		String[] hms = time.split(":");
		StringBuffer cron = new StringBuffer(hms[2]).append(" ").append(hms[1]).append(" ").append(hms[0]).append(" ").append(day).append(" * ? *");
		return cron.toString();
	}

	/**
	 * 月、日、时间转cron（每年的某个时间执行的cron）
	 *
	 * @param month 1~12
	 * @param day   1~31
	 * @param time  HH:mm:ss
	 * @return
	 */
	public static String monthDayTimeToCron(String month, String day, String time) {
		String[] hms = time.split(":");
		StringBuffer cron = new StringBuffer(hms[2]).append(" ").append(hms[1]).append(" ").append(hms[0]).append(" ").append(day).append(" ").append(month).append(" ? *");
		return cron.toString();
	}
}
