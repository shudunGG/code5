package com.om.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtil {
    public static SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat sdfM = new SimpleDateFormat("yyyyMM");
    public static SimpleDateFormat sdfD = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat sdfD2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat sdfD3 = new SimpleDateFormat("M.d");
    public static SimpleDateFormat sdfS = new SimpleDateFormat("yyyyMMddHHmmss");
    public static SimpleDateFormat dfT =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA);
    public static DateFormat dfdt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int calcAge(String idNumber){
        int age = 0;
        try {
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            String birthDateStr = idNumber.substring(6, 14);
            // 将出生日期字符串转换为Date类型
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date birthDate = dateFormat.parse(birthDateStr);
            // 计算年龄
            calendar.setTime(birthDate);
            int birthYear = calendar.get(Calendar.YEAR);
            age = currentYear - birthYear;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return age;
    }
    /**
     * 得到当前时间的月份，格式是202205
     * @return
     */
    public static String getCurMonth(){
        Calendar cal = Calendar.getInstance();
        return sdfM.format(cal.getTime());
    }

    public static String getCurYear(){
        Calendar cal = Calendar.getInstance();
        return sdfY.format(cal.getTime());
    }

    public static Date getNowTime(){
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    public static Date getTime(Long time){
        Date d = new Date();
        d.setTime(time);
        return d;
    }

    public static String getNowTimeStr(DateFormat pattern){
        Calendar cal = Calendar.getInstance();
        String s = pattern.format(cal.getTime());
        return s;
    }

    public static Date getDateByStr(String s,DateFormat pattern)throws ParseException {
        return pattern.parse(s);
    }

    public static String getTimeStrByLong(long modifyTimeLong) {
        return dfdt.format(new Date(modifyTimeLong));
    }

    public static String switchTimeZone(String sep) throws ParseException {
        sep = sep.replaceAll("\"","");
        Date myDate = dfT.parse (sep.replace("Z","+0000"));
        String format = dfdt.format(myDate);
        return format;
    }

    /**
     * 得到当前时间的天数，格式是20220503
     * @return
     */
    public static String getCurDay(){
        Calendar cal = Calendar.getInstance();
        return sdfD.format(cal.getTime());
    }

    /**
     * 得到今天的字符串格式，格式是2022-05-03
     * @return
     */
    public static String getTodayStr(){
        Calendar cal = Calendar.getInstance();
        return sdfD2.format(cal.getTime());
    }

    /**
     * 得到明天的字符串格式，格式是2022-05-03
     * @return
     */
    public static String getTomorrowStr(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,1);
        return sdfD2.format(cal.getTime());
    }

    /**
     * 得到今天的字符串格式，格式是5.3
     * @return
     */
    public static List<String> getLast30DayStr(){
        List<String> list = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,-30);
        for(int i=0;i<30;i++){
            cal.add(Calendar.DAY_OF_MONTH,1);
            String d = sdfD3.format(cal.getTime());
            //d = d.replaceAll("0","");
            list.add(d);
        }
        return list;
    }

    /**
     * 生成一个以当前毫秒数做为ID的随机数，并去掉前4位，前4位在天的维度是不变的，通过是放在上下文为天的路径中
     * @return
     */
    public static String getCurDayRandomId(){
        String s = System.currentTimeMillis()+"";
        s = s.substring(4,s.length())+(int)(Math.random()*100);

        return s;
    }


    public static void main(String[] args)  {

        try {
            List<String> list = DateUtil.getLast30DayStr();
            for(String s:list){
                System.out.println(s);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
