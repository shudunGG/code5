package com.om.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static SimpleDateFormat sdfM = new SimpleDateFormat("yyyyMM");
    public static SimpleDateFormat sdfD = new SimpleDateFormat("yyyyMMdd");
    public static SimpleDateFormat sdfD2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat sdfS = new SimpleDateFormat("yyyyMMddHHmmss");
    public static SimpleDateFormat dfT =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINA);
    public static DateFormat dfdt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 得到当前时间的月份，格式是202205
     * @return
     */
    public static String getCurMonth(){
        Calendar cal = Calendar.getInstance();
        return sdfM.format(cal.getTime());
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
     * 根据参数today先确定今天，然后再得到指定偏移的天数
     * @return
     */
    public static String getDay(String today,int days)throws ParseException {
        Date d = null;
        if(today.indexOf("-")>-1){
            d = sdfD2.parse(today);
        }else{
            d = sdfD.parse(today);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH,days);
        if(today.indexOf("-")>-1){
            return sdfD2.format(cal.getTime());
        }else{
            return sdfD.format(cal.getTime());
        }

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
     * 生成一个以当前毫秒数做为ID的随机数，并去掉前4位，前4位在天的维度是不变的，通过是放在上下文为天的路径中
     * @return
     */
    public static String getCurDayRandomId(){
        String s = System.currentTimeMillis()+"";
        s = s.substring(4,s.length())+(int)(Math.random()*100);

        return s;
    }


    public static void main(String[] args)  {
        String s1 = "20220322110126";
        String s2 = "20220321110126";
        try {
            String s3 = ""+sdfS.parse(s1).getTime();
            System.out.println(s3);
            System.out.println(s3.substring(4,s3.length()));
            /**
             * 1647918086000
             * 1647831686000
             */
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
