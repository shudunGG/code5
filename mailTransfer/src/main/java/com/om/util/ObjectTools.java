package com.om.util;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class ObjectTools {
	static Logger log=Logger.getLogger(ObjectTools.class);
	public static final String yyyy_mm_dd="yyyy-MM-dd";
	public static final String yyyy_mm_dd_hh_mm_ss="yyyy-MM-dd HH:mm:ss";

	public static String getStrFromHtml(String input) {
		if (input == null || input.trim().equals("")) {
			return "";
		}
		// 去掉所有html元素,
		String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
				"<[^>]*>", "");
		str = str.replaceAll("[(/>)<]", "");
		return str;
	}

	public static List<Map> getPageList(long total,long pageSize){
		List list = new ArrayList();
		long start = 0;
		long end = 0;
		long page = 1;
		do{
			start = (page-1)*pageSize;
			end = page*pageSize;
			Map map = new HashMap();
			map.put("start",start);
			map.put("end",end);
			list.add(map);
			page ++;
		}while(end<=total);
		return list;
	}

	// 判断字符串是否为空

	public static boolean isNull(String value) {
		boolean lb_return = true;
		if(value==null || value=="" || value.length()==0 || value.trim().length()==0 || value.equals("null") || value.equals("undefined")){
			lb_return = true;
		}else{
			lb_return = false;
		}
		return lb_return;
	}
	public static boolean isNull(Object value) {
		boolean lb_return = true;
		if(value==null || value.toString().length()==0 || value.toString().trim().length()==0 || value.toString().equals("null")){
			lb_return = true;
		}else{
			lb_return = false;
		}
		return lb_return;
	}
	public static int toInt(String strInt,int defaultValue){
		try{
			if(isNull(strInt)){
				return defaultValue;
			}
			return Integer.parseInt(strInt);
		}
		catch ( Exception e ){
			return defaultValue;
		}
	}
	public static int toInt(Object objParam,int defaultValue){
		try{
			if(isNull(objParam)){
				return defaultValue;
			}
			return Integer.parseInt(toString(objParam));
		}
		catch (Exception e){
			return defaultValue;
		}
	}
	// 转换字符串
	public static String toStringForDefault(Object value,String defaultValue) {
		try{
			String newString = String.valueOf(value);
			if(isNull(newString)){
				return defaultValue;
			}
			return newString;
		}catch (Exception e){
			e.printStackTrace();
			return defaultValue;
		}
	}
	// 转换字符串
	public static String toString(Object value) {
		String newString = null;
		try{
			newString = String.valueOf(value);
			if(newString==null || "".equals(newString) || "null".equals(newString)){
				newString=null;
			}
		}catch (Exception e){}
		return newString;
	}
	// 转换字符串
	public static String toString(Object value,String ignorStr) {//遇到ignorStr这样的字符串也视为空
		String newString = null;
		try{
			newString = String.valueOf(value);
			if(newString==null || "".equals(newString) || "null".equals(newString) || ignorStr.equals

(newString)){
				newString = null;
			}
		}catch (Exception e){}
		return newString;
	}
	//转换成浮点数
	public static float toFloat( Object value ) {
		float newFloat = 0;
		try{
			newFloat = Float.parseFloat(String.valueOf(value));
		}
		catch ( Exception e ){
			newFloat = 0;
		}
		return newFloat;
	}
	//转换成浮点数
	public static float toFloat( Object value,String ignorStr ) {////遇到ignorStr这样的字符串也视为0
		float newFloat = 0;
		try{
			String newString = String.valueOf(value).toString();
			if(!ignorStr.equals(newString)){
				newFloat = Float.parseFloat(newString);
			}
		}
		catch ( Exception e ){
			newFloat = 0;
		}
		return newFloat;
	}
	public static float toFloat(Object value,float defaultValue) {
		float newFloat = 0;
		try{
			String newString = String.valueOf(value).toString();
			newFloat = Float.parseFloat(newString);
			return newFloat;
		}
		catch ( Exception e ){
			return defaultValue;
		}
	}
	/*日期时间处理方法集合开始*/
	public static String doNow(String format) {//获取指定格式的当前时间
		Calendar c=Calendar.getInstance();
		Date date=c.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat();
		try{
			if(isNull(format)){
				sdf.applyPattern("yyyy-MM-dd");
			}else{
				sdf.applyPattern(format);
			}
			return sdf.format(date);
		}catch(Exception e){
			sdf.applyPattern("yyyy-MM-dd");
			return sdf.format(date);
		}
	}


	public static String getCurMonth() {//获取指定格式的当前时间
		Calendar c=Calendar.getInstance();
		Date date=c.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat();
		try{
			sdf.applyPattern("yyyyMM");
			return sdf.format(date);
		}catch(Exception e){
			sdf.applyPattern("yyyyMMdd");
			return sdf.format(date);
		}
	}

	public static String doNowAddYear(String format,int year) {//获取指定格式的当前时间
		Calendar c=Calendar.getInstance();
		c.add(Calendar.YEAR, year);
		Date date=c.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat();
		try{
			if(isNull(format)){
				sdf.applyPattern("yyyy-MM-dd");
			}else{
				sdf.applyPattern(format);
			}
			return sdf.format(date);
		}catch(Exception e){
			sdf.applyPattern("yyyy-MM-dd");
			return sdf.format(date);
		}
	}
	/*获取给定日期减去或者加上指定时间后的时间*/
	public static String doNowAddNum(String format,int num,String type){
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sdf=null;
		try{
			if(isNull(format)){
				sdf=new SimpleDateFormat("yyyy-MM-dd");
			}else{
				sdf=new SimpleDateFormat(format);
			}
			
			if("day".equals(type)){
				c.add(c.DAY_OF_MONTH, num);
			}else if("hour".equals(type)){
				c.add(c.HOUR_OF_DAY, num);
			}
			return sdf.format(c.getTime());
		}catch(Exception e){
			sdf=new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(c.getTime());
		}
	}
	/*获取给定日期减去或者加上指定时间后的时间*/
	public static String doDateTimeAddNum(String format,String dateTime,int num,String type){
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sdf=null;
		try{
			if(isNull(format)){
				sdf=new SimpleDateFormat("yyyy-MM-dd");
			}else{
				sdf=new SimpleDateFormat(format);
			}
			Date date=sdf.parse(dateTime);
			c.setTime(date);
			
			if("day".equals(type)){
				c.add(c.DAY_OF_MONTH, num);
			}else if("hour".equals(type)){
				c.add(c.HOUR_OF_DAY, num);
			}
			return sdf.format(c.getTime());
		}catch(Exception e){
			sdf=new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(c.getTime());
		}
	}
	public static String doFormatTimeStr(String str,String formatOld,String formatNew){//按指定格式格式化字符串
		String strValue="";
		try{
			if(isNull(str) || isNull(formatOld) || isNull(formatNew)){
				return strValue;
			}
			SimpleDateFormat sdf=new SimpleDateFormat();
			sdf.applyPattern(formatOld);
			Date dateT=sdf.parse(str);
			sdf.applyPattern(formatNew);
			strValue=sdf.format(dateT);
		}catch(Exception e){e.printStackTrace();}
		return strValue;
	}
	public static String doCertainyTime(long time,String format) {//获取指定时间的制定的格式字符串
		String str="";
		String defaultFormat="yyyy-MM-dd";
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(time);
		Date date=c.getTime();
		SimpleDateFormat sdf=new SimpleDateFormat();
		try{
			sdf.applyPattern(format);
			str=sdf.format(date);
		}catch(Exception e){
			sdf.applyPattern(defaultFormat);
			str=sdf.format(date);
		}
		return str;
		
	}
	public static String doDateToStr(Date date,String format){//将date转换成指定格式的字符串日期格式
		try{
			if((date==null) || isNull(format)){
				return null;
			}
			SimpleDateFormat sdf=new SimpleDateFormat(format);
			return sdf.format(date);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	//字符串转换成日期
	public static Date doStrToDate(String dateStr,String format){
		try{
			if(isNull(format) || isNull(dateStr)){
				return null;
			}
			SimpleDateFormat sdf=new SimpleDateFormat(format);
			return sdf.parse(dateStr);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static String doTimeAddMinite(String time,int minute){//在原定时间上加上一定分钟数，返回新的时间如:8:00加上20分钟就为8:30


		try{
			SimpleDateFormat sdf=new SimpleDateFormat();
			sdf.applyPattern("yyyy-MM-dd HH:mm");
			Calendar c=Calendar.getInstance();
			c.setTime(sdf.parse("2000-01-01 "+time));
			c.add(Calendar.MINUTE, minute);
			sdf.applyPattern("HH:mm");
			return sdf.format(c.getTime());
		}catch(Exception e){
		//	e.printStackTrace();
			return time;
		}
	}
	//日期格式验证
	public static boolean doDateValidate(String date){
		try{
			if(isNull(date)){
				return false;
			}
			
			//String dateReg="^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";
			//可以验证2008-02-29，2009-3-31，2009-5-31，1980-11-17
			String dateReg="^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0?[1-9])|(1[0-2]))-((0?[1-9])|(1\\d)|(2[0-8])))|((((0?[13578])|(1[02]))-31)|(((0?[1,3-9])|(1[0-2]))-(29|30)))))$";

			Pattern p = Pattern.compile(dateReg);         
	        Matcher m = p.matcher(date); 
	        if(m.matches()){
	        	return true;
	        }
	        return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	//根据format的格式,给dateTime加上num后的日期时间,addMode-指定num-是分钟,秒deng
	public static String doDateTimeAddNum(String format,String dateTime,String addMode,int num){//(首选方法)
		try{
			if(isNull(format) || isNull(dateTime) || isNull(addMode) || num<=0){
				return null;
			}
			SimpleDateFormat sdf=new SimpleDateFormat(format);
			Date dateTimeT=sdf.parse(dateTime);
			long mill=dateTimeT.getTime();
			if("minute".equals(addMode)){
				mill+=num*60*1000;
			}
			Calendar c=Calendar.getInstance();
			c.setTimeInMillis(mill);
			Date addDateTimeT=c.getTime();
			
			return sdf.format(addDateTimeT);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static String doCertainyTimeAddMinite(String format,String nowTime,int minite){//例如:2011-10-20 10:20 - 30分钟后的时间
		try{
			SimpleDateFormat sdf=new SimpleDateFormat(format);
			Date nowDate=sdf.parse(nowTime);
			long mill=nowDate.getTime();
			mill+=minite*60*1000;
			Calendar c=Calendar.getInstance();
			c.setTimeInMillis(mill);
			Date addMiniteDate=c.getTime();
			return sdf.format(addMiniteDate);
		}catch(Exception e){
			return null;
		}
	}
	/**======================================================================
     * 功能：计算两个时间差
     * @param timeFormat 时间格式(可以自定)
     * @param timeF 第一个时间点
     * @param timeB 第二个时间点
     * @param mode mode=hour-计算出的值小时，=minite分钟
     * @return 
     * @throws ParseException 
     */	
	public static int doTimeCha(String timeFormat,String timeF,String timeB,String mode){
		try{
			SimpleDateFormat sdf=new SimpleDateFormat(timeFormat);
			Date dateF=sdf.parse(timeF);
			Date dateB=sdf.parse(timeB);
			
			if("hour".equals(mode)){
				return (int)((dateB.getTime()-dateF.getTime())/(60L*1000L*60L));
			}else if("minite".equals(mode)){
				return (int)((dateB.getTime()-dateF.getTime())/(60L*1000L));
			}else if("other".equals(mode)){
				long value=(dateB.getTime()-dateF.getTime());
				
				return (int)value;
			}
			return 0;
		}catch(Exception e){
			return 0;
		}
	}
	//取代上方法，上方法不准确
	public static long doTimeCha_ReturnLong(String timeFormat,String timeF,String timeB,String mode){
		try{
			SimpleDateFormat sdf=new SimpleDateFormat(timeFormat);
			Date dateF=sdf.parse(timeF);
			Date dateB=sdf.parse(timeB);
			
			if("hour".equals(mode)){
				return ((dateB.getTime()-dateF.getTime())/(60L*1000L*60L));
			}else if("minite".equals(mode)){
				return ((dateB.getTime()-dateF.getTime())/(60L*1000L));
			}else if("other".equals(mode)){
				long value=(dateB.getTime()-dateF.getTime());
				
				return value;
			}
			return 0;
		}catch(Exception e){
			return 0;
		}
	}
	/**
	 * 同上，只是参数类型不一样
	 * */
	public static int doTimeCha(String timeFormat,Date timeF,Date timeB,String mode){
		try{
//			SimpleDateFormat sdf=new SimpleDateFormat(timeFormat);
//			Date dateF=sdf.parse(timeF);
//			Date dateB=sdf.parse(timeB);
			if("hour".equals(mode)){
				return (int)((timeB.getTime()-timeF.getTime())/(60L*1000L*60L));
			}else if("minite".equals(mode)){
				return (int)((timeB.getTime()-timeF.getTime())/(60L*1000L));
			}else if("other".equals(mode)){
				return (int)(timeB.getTime()-timeF.getTime());
			}
			return 0;
		}catch(Exception e){
			return 0;
		}
	}
	/*日期时间处理方法集合结束*/
	public static String doEncodingFromGb2312(String value,String encoding){
		String resultValue="";
		try{
			resultValue=new String(value.getBytes("GB2312"),encoding);
		}catch(Exception e){
		}
		return resultValue;
	}
	public static String doEncoding(String value,String oldEncoding,String newEncoding){
		try{
			if(isNull(value)){
				return null;
			}
			return new String(value.getBytes(oldEncoding),newEncoding);
		}catch(Exception e){
			return null;
		}
	}
	public static String doEncodingFromIso(String value,String encoding){
		String resultValue="";
		try{
			resultValue=new String(value.getBytes("ISO-8859-1"),encoding);
		}catch(Exception e){
		}
		return resultValue;
	}
	public static String[] doArrayBySign(String str,String sign){
		String[] attr=null;
		try{
			attr=str.split(sign);
		}catch(Exception e){
			attr=null;
		}
		return attr;
	}
	public static String doArrayBySignAndTakeValueOfIndex(String str,String sign,int index){
		try{
			String[] array=str.split(sign);
			if(array==null){
				return null;
			}
			return array[index];
		}catch(Exception e){
			return null;
		}
	}
	public static String doInterStr(String str,int lenP){//按照指定长度截取字符串
	//	String strValue="";
		try{
			int len=str.length();
			if(len<=lenP){
				return str;
			}else{
			//	strValue=str.substring(0, lenP)+"...";
				return str.substring(0, lenP);
			}
		}catch(Exception e){
			return "";
		}
		
	}
	public static int doObjectToType(String strValue,int defaultValue){//把OBJECT的类型的数据转换成指定类型
		int num=0;
		try{
		//	log.info("strValue--------------------"+strValue);
			num=Integer.parseInt(strValue);
		//	log.info("num--------------------"+num);
		}catch(Exception e){
			num=defaultValue;
		}
		return num;
	}
	public static String doStrHandle(String strP){//处理如2009-10-24 11:22:30.0截取掉最后的.0
		String strValue="";
		try{
			int pos=strP.lastIndexOf(".");
			strValue=strP.substring(0, pos);
		}catch(Exception e){}
		return strValue;
	}
	public static String doStrQuoteHandle(String strP){//处理如2009-10-24 11:22:30.0截取成2009-10-24 11:22
		String strValue="";
		try{
			int pos=strP.lastIndexOf(":");
			strValue=strP.substring(0, pos);
		}catch(Exception e){
			strValue="";
		}
		return strValue;
	}
	public static String dateStrToOtherStr(String strP){//处理如20091024151846(YYYYMMDDHH24MISS)截取成(YYYY-MM-DD)2009-10-24
		String strValue="";
		try{
			
			strValue=strP.substring(0,4)+"-"+strP.substring(4,6)+"-"+strP.substring(6,8);
		}catch(Exception e){
			strValue="";
		}
		return strValue;
	}
	public static String doStr_Handle(String strP){//处理字符串最后的_字符
		String strValue="";
		try{
			int pos=strP.lastIndexOf("_");
			if(strP.length()-1==pos){
				strValue=strP.substring(0, pos);
			}else{
				strValue=strP;
			}
		}catch(Exception e){}
		return strValue;
	}
	
	public static String doNowMonth(){
		Calendar c=Calendar.getInstance();
		return String.valueOf(c.get(Calendar.MONTH)+1);
	}
	public static String doNowDay(){
		Calendar c=Calendar.getInstance();
		return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
	}
	//检查字符串长度
	public static boolean doCheckStrLength(String str,int len){//大于len返回true，小于len返回false
		boolean flag=false;
		try{
			if(str.length()>len){
				flag=true;
			}
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}
	//检查字符串转换为字节数组的长度
	public static boolean doCheckStrToByteLength(String str,int len){//大于len返回true，小于len返回false
		boolean flag=false;
		try{
			if(str.getBytes().length>len){
				flag=true;
			}
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}
	//检查是否整数型
	public static boolean doCheckIsIntegerOr(String str){//是整数型返回true,否则返回false
		boolean flag=false;
		try{
			Integer.parseInt(str);
			flag=true;
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}
	//检查字符串是否是正整数型,例如一个字符串只能包含1234567890这里面的字符
	private static final String zhengZhengShuForm   =   "0123456789"; 
	public static boolean doCheckIsZhengZhengShuOr(String strParam){
		boolean flag=true;//默认为是正整数格式
		if(!isNull(strParam)){
			int len=strParam.length();
			for(int i = 0;i<len;i++)   { 
               if(zhengZhengShuForm.indexOf(strParam.charAt(i))==-1){ 
                  flag=false;
                  break;
                } 
			} 
		}else{
			flag=false;
		}
		return flag;
	}
	//检查字符串是否是数字型,例如一个字符串只能包含123456789.这里面的字符
	private static final String   numberTemp   =   "0123456789."; 
	public static boolean doCheckIsNumberOr(String strParam){
		boolean flag=true;//默认为是数字格式
		if(!isNull(strParam)){
			int len=strParam.length();
			for(int i = 0;i<len;i++)   { 
               if(numberTemp.indexOf(strParam.charAt(i))==-1){ 
                  flag=false;
                  break;
                } 
			} 
		}else{
			flag=false;
		}
		return flag;
	}
	public static boolean doCheckIsDateOr(String strParam){//检查字符串是否是日期型格式为2010-10-10
		boolean flag=true;//默认为是
		String defaultFormat="yyyy-MM-dd";
		try{
			if(strParam.length()>10){
				flag=false;
			}else{
				SimpleDateFormat sdf=new SimpleDateFormat(defaultFormat);
				sdf.parse(strParam);
			}
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}
	/*验证只能是输入1,2,3,4这类格式,两头不能有,*/
	public static boolean doStrFormt_1(String strParam){
		boolean flag=true;//默认格式正确
		String defaultFormat="0123456789,";
		try{
			if(isNull(strParam)){
				flag=false;
				return flag;
			}
			if(strParam.length()>10){
				flag=false;
				return flag;
			}
			if(strParam.indexOf(",")==0){
				flag=false;
				return flag;
			}
			if(strParam.indexOf(",")==strParam.length()-1){
				flag=false;
				return flag;
			}
			int len=strParam.length();
			for(int i = 0;i<len;i++)   { 
               if(defaultFormat.indexOf(strParam.charAt(i))==-1){ 
                  flag=false;
                  break;
                } 
			} 
		}catch(Exception e){
			flag=false;
		}
		return flag;
	}
	public static String doTimeMake(String hour,String minite){//将08,06转换成06:06时间格式
		try{
			if(isNull(hour) && isNull(minite)){
				return "00:00";
			}else if(isNull(hour)){
				return "00:"+minite;
			}else if(isNull(minite)){
				return hour+":00";
			}
			return hour+":"+minite;
		}catch(Exception e){
			return "00:00";
		}
	}
	public static int doDaysOfTwoDate(String startDate,String endDate){//计算两日期的间隔天数,如2010-10-2到2010-10-20
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			return (int)((sdf.parse(endDate).getTime()-sdf.parse(startDate).getTime())/(24L*60L*60L*1000L));
		}catch(Exception e){
			return 0;
		}
	}
	public static String doNewDate(String date,int num){//计算一个日期如2010-10-10,加上一个整数得到一个新的日期
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			Calendar c=Calendar.getInstance();
			c.setTime(sdf.parse(date));
			c.add(Calendar.DAY_OF_YEAR, num);
			return sdf.format((c.getTime()));
		}catch(Exception e){
			return "2000-01-01";//统一默认时间
		}
	}

	
	//专用方法-timeF,timeB-格式为yyyy-MM-dd HH:mm:ss,计算两个时间点。返回，分秒格式
	public static int[] doTimeInterval(String timeF,String timeB){
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateF=sdf.parse(timeF);
			Date dateB=sdf.parse(timeB);
			int secondTotal=(int)((dateB.getTime()-dateF.getTime())/1000L);
			int minite=secondTotal/60;
			int second=secondTotal%60;
			int[] miniteSecondArray=new int[2];
			
			miniteSecondArray[0]=minite;
			miniteSecondArray[1]=second;
			
			return miniteSecondArray;
		}catch(Exception e){
			return null;
		}
	}
	
//	public static boolean doTimeBiJiao_1(String timeF,String timeS){//比较8:00和9:00这样的时间大小
//		if(Integer.parseInt(timeF.replaceAll(":", "")) < Integer.parseInt(timeS.replaceAll(":", ""))){
//			return true;
//		}else{
//			return false;
//		}
//	}
	public static String doStringArrObj(String strP,int pos,String regx){//例如ab,cc,dd,通split转换成数组,取指定POS位置的元素
		try{
			return (strP.split(regx))[pos];
		}catch(Exception e){
			return "";
		}
	}
	public static double doHoursOfMinute(int minute){//分钟转换为小时,并且保留一位小数,会四舍五入
		try{
			return (Math.round(((double)minute/60)*10)/10.0);
		}catch(Exception e){
			return 0;
		}
	}
	public static double doHoursOfMinute(double minute){//分钟转换为小时,并且保留一位小数,会四舍五入，double类型参数
		try{
			return (Math.round(((double)minute/60)*10)/10.0);
		}catch(Exception e){
			return 0;
		}
	}
	public static int doDaysOfMonth(String month){
		try {   
			Calendar cal = new GregorianCalendar();   
		    //或者用Calendar   cal   =   Calendar.getInstance();   
		  
		    /**设置date**/  
		    SimpleDateFormat oSdf = new SimpleDateFormat ("",Locale.ENGLISH);   
		    oSdf.applyPattern("yyyy-MM-dd");   
		    
		  //      log.info(oSdf.parse(month));   
		     cal.setTime(oSdf.parse(month));   
		   
		  
		    /**或者设置月份，注意月是从0开始计数的，所以用实际的月份-1才是你要的月份**/  
		    //一月份: cal.set(   2009,   1-1,   1   );   
		  
		    /**如果要获取上个月的**/    
		    //cal.set(Calendar.DAY_OF_MONTH, 1);   
		    //日期减一,取得上月最后一天时间对象   
		     //cal.add(Calendar.DAY_OF_MONTH, -1);   
		   //输出上月最后一天日期   
		     //log.info(cal.get(Calendar.DAY_OF_MONTH));   
		    /**开始用的这个方法获取月的最大天数，总是得到是31天**/  
		    //int num = cal.getMaximum(Calendar.DAY_OF_MONTH);   
		    /**开始用的这个方法获取实际月的最大天数**/  
		    int num2 = cal.getActualMaximum(Calendar.DAY_OF_MONTH);   
		  
	//	    log.info(num2);  
		    return num2;
		 } catch (ParseException e) {   
		       return 0;
		 }   	
	}
	
	//删除磁盘上的文件
	public static void doUploadFileDelete(String filePath){
		if(isNull(filePath)){
			return;
		}
		File file=new File(filePath);
		if(file.exists()){
			file.delete();
		}
	}

	public static String doDomainTake(String reqUrl){//提取域名
		try{
			if(!isNull(reqUrl)){
				reqUrl=reqUrl.trim();
				reqUrl=reqUrl.substring(7);//去掉http://头
				int index=reqUrl.indexOf("/");
				if(index==-1){
					return reqUrl;
				}else{
					return reqUrl.substring(0,index);
				}
			}
			return "";
		}catch(Exception e){
			return "";
		}
	}
	
	public static String randomStringAndInt(int num) {
		String new_str = "";

		for (int i = 0; i < num; i++) {
			int a = (int) (100 * Math.random() + 100 * Math.random());
			while (true) {
				if ((a > 64 && a < 91) || (a > 47 && a < 58))
					break;
				else
					a = (int) (100 * Math.random() + 100 * Math.random());
			}
			new_str += (char) a;
		}

		return new_str;
	}

	public static String randomString(int num) {
		String new_str = "";

		for (int i = 0; i < num; i++) {
			int a = (int) (100 * Math.random() + 100 * Math.random());
			while (true) {
				if (a > 64 && a < 91)
					break;
				else
					a = (int) (100 * Math.random() + 100 * Math.random());
			}
			new_str += (char) a;
		}

		return new_str;
	}
	//判断一个字符串是否在一个字符数组里。是-true,否-false
	public static boolean checkStrArrayContainStr(String[] strArray,String str){
		try{
			if(strArray==null || strArray.length<=0){
				return false;
			}
			if(isNull(str)){
				return false;
			}
			boolean flag=false;
			int len=strArray.length;
			for(int i=0;i<len;i++){
				if(str.equals(strArray[i].trim())){
					flag=true;
					break;
				}
			}
			return flag;
		}catch(Exception e){
			return false;
		}
	}
	
	public static String findGradeName(int grade){
		if(grade==1){
			return "第一学年";
		}else if(grade==2){
			return "第二学年";
		}else if(grade==3){
			return "第三学年";
		}else if(grade==4){
			return "第四学年";
		}else if(grade==5){
			return "第五学年";
		}else if(grade==6){
			return "第六学年";
		}else if(grade==7){
			return "第七学年";
		}else if(grade==8){
			return "第八学年";
		}else if(grade==9){
			return "第九学年";
		}
		return null;
	}
	public static String findTermName(int term){
		if(term==1){
			return "第一学期";
		}else if(term==2){
			return "第二学期";
		}else if(term==3){
			return "第三学期";
		}else if(term==4){
			return "第四学期";
		}else if(term==5){
			return "第五学期";
		}else if(term==6){
			return "第六学期";
		}else if(term==7){
			return "第七学期";
		}else if(term==8){
			return "第八学期";
		}else if(term==9){
			return "第九学期";
		}else if(term==10){
			return "第十学期";
		}else if(term==11){
			return "第十一学期";
		}else if(term==12){
			return "第十二学期";
		}else if(term==13){
			return "第十三学期";
		}else if(term==14){
			return "第十四学期";
		}else if(term==15){
			return "第十五学期";
		}else if(term==16){
			return "第十六学期";
		}else if(term==17){
			return "第十七学期";
		}else if(term==18){
			return "第十八学期";
		}
		return null;
	}
	
	public static int findInStatusFromWords(String str){
		if("正常".equals(str)){
			return 1;
		}else if("借读".equals(str)){
			return 2;
		}else if("转入".equals(str)){
			return 3;
		}
		return 0;
	}
	public static String findEnrollChangeName(int typeId){
		if(typeId==1){
			return "休学";
		}else if(typeId==2){
			return "复学";
		}else if(typeId==3){
			return "辍学";
		}else if(typeId==4){
			return "转学(转入)";
		}else if(typeId==5){
			return "转学(转出)";
		}else if(typeId==6){
			return "借读";
		}
		return "";
	}
	/**======================================================================
     * 功能：身份证的有效验证
     * @param IDStr 身份证号
     * @return 有效：true        无效：false
     * @throws ParseException 
     */	
	public static boolean IDCardValidate(String IDStr) throws ParseException{
		String errorInfo = "";//记录错误信息
        String[] ValCodeArr = {"1","0","x","9","8","7","6","5","4","3","2"};
        String[] Wi = {"7","9","10","5","8","4","2","1","6","3","7","9","10","5","8","4","2"};
        //String[] Checker = {"1","9","8","7","6","5","4","3","2","1","1"};
        String Ai="";
        String lastChar="";
        //================ 号码的长度 15位或18位 ================
        if(IDStr.length()!=15 && IDStr.length()!=18){
        	errorInfo="号码长度应该为15位或18位。";
            log.info(errorInfo);
            return false;
        }
        //=======================(end)======================== 
        //================ 数字 除最后一位都为数字 ================
        if(IDStr.length()==18){
        	Ai=IDStr.substring(0,17);
        	lastChar=IDStr.substring(17);
        }else if(IDStr.length()==15){
        	Ai=IDStr.substring(0,6)+"19"+IDStr.substring(6,15);
        }
        if(isNumeric(Ai)==false){
        	errorInfo="15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
            log.info(errorInfo);
            return false;
        }
        //=======================(end)========================
        //================ 出生年月是否有效 ================
        String strYear=Ai.substring(6 ,10);//年份
        String strMonth=Ai.substring(10,12);//月份
        String strDay=Ai.substring(12,14);//月份
        if(isDate(strYear+"-"+strMonth+"-"+strDay)==false){
        	 errorInfo="生日无效。";
             log.info(errorInfo);
             return false;
        }
        GregorianCalendar gc=new GregorianCalendar();
        SimpleDateFormat s=new SimpleDateFormat("yyyy-MM-dd");
        if((gc.get(Calendar.YEAR)-Integer.parseInt(strYear))>150 || (gc.getTime().getTime()-s.parse

(strYear+"-"+strMonth+"-"+strDay).getTime())<0){
	        errorInfo="生日不在有效范围。";
	        log.info(errorInfo);
	        return false;
        }
        if(Integer.parseInt(strMonth)>12 || Integer.parseInt(strMonth)==0){
        	errorInfo="月份无效";
            log.info(errorInfo);
            return false;
        }
        if(Integer.parseInt(strDay)>31 || Integer.parseInt(strDay)==0){
        	errorInfo="日期无效";
            log.info(errorInfo);
            return false;
        }
        //=====================(end)=====================
        //================ 地区码时候有效 ================
        Hashtable h=GetAreaCode();
        if(h.get(Ai.substring(0,2))==null){
        	 errorInfo="地区编码错误。";
             log.info(errorInfo);
             return false;
        }
        //==============================================
        //================ 判断最后一位的值 ================
        int TotalmulAiWi=0;
        for(int i=0 ; i<17 ; i++){
        	TotalmulAiWi = TotalmulAiWi + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }
        int modValue=TotalmulAiWi % 11;
        String strVerifyCode=ValCodeArr[modValue];
  
        Ai=Ai+strVerifyCode;
   
        if(IDStr.length()==18){
        	//如果最后一位是数字,则都认为是正确的
        	if(isNumeric(lastChar)){
        		return true;
        	}
     //   	if(Ai.equals(IDStr)==false){
        	if(Ai.equalsIgnoreCase(IDStr)==false){//苏杰修改-最后一位字母可以大写也可以小写
        		errorInfo="身份证无效，最后一位字母错误";
    //            log.info(errorInfo);
                return false;
        	}
        	
        }else{
       // 	log.info("所在地区:"+h.get(Ai.substring(0,2).toString()));
      //      log.info("新身份证号:"+Ai);
            return true;
        }
        //=====================(end)=====================
 //       log.info("所在地区:"+h.get(Ai.substring(0,2).toString()));
        return true;
	}
	//验证是否是数字
	public static boolean isNumeric(String str){
		Pattern pattern=Pattern.compile("[0-9]*");
		Matcher isNum=pattern.matcher(str);
		if(isNum.matches()){
			return true;
		}else{
			return false;
		}
		 /*判断一个字符时候为数字
        if(Character.isDigit(str.charAt(0))){
        	return true;
        }else{
        	return false;
        }*/ 
	}
	/**======================================================================
     * 功能：设置地区编码
     * @return Hashtable 对象
     */
	private static Hashtable GetAreaCode(){
		Hashtable hashtable=new Hashtable();
		hashtable.put("11","北京");
        hashtable.put("12","天津");
        hashtable.put("13","河北");
        hashtable.put("14","山西");
        hashtable.put("15","内蒙古");
        hashtable.put("21","辽宁");
        hashtable.put("22","吉林");
        hashtable.put("23","黑龙江");
        hashtable.put("31","上海");
        hashtable.put("32","江苏");
        hashtable.put("33","浙江");
        hashtable.put("34","安徽");
        hashtable.put("35","福建");
        hashtable.put("36","江西");
        hashtable.put("37","山东");
        hashtable.put("41","河南");
        hashtable.put("42","湖北");
        hashtable.put("43","湖南");
        hashtable.put("44","广东");
        hashtable.put("45","广西");
        hashtable.put("46","海南");
        hashtable.put("50","重庆");
        hashtable.put("51","四川");
        hashtable.put("52","贵州");
        hashtable.put("53","云南");
        hashtable.put("54","西藏");
        hashtable.put("61","陕西");
        hashtable.put("62","甘肃");
        hashtable.put("63","青海");
        hashtable.put("64","宁夏");
        hashtable.put("65","新疆");
        hashtable.put("71","台湾");
        hashtable.put("81","香港");
        hashtable.put("82","澳门");
        hashtable.put("91","国外");
        return hashtable;
	}
	/**======================================================================
     * 功能：判断字符串是否为日期格式
     * @param strDate
     * @return
     */
	public static boolean isDate(String strDate){
		Pattern pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?(" +
				"(0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
		Matcher m=pattern.matcher(strDate);
		if(m.matches()){
			return true;
		}else{
			return false;
		}
	}
	// 格式化数字为字符串
	public static String intToString(int num, String format) {
		DecimalFormat df = new DecimalFormat(format);
		String str_tem = "";
		str_tem = df.format(num);
		return str_tem;
	}
	//去空格
	public static String trim(String str) {
		if(!isNull(str)){
			return str.trim();
		}
		return str;
	}
	public static String formatText2Html(String in){
		String str="";
		if(in==null){
			return "";
		}else{
			str=in.replaceAll("<","&lt;");
			str=str.replaceAll(">","&gt;");
			str=str.replaceAll(" ","&nbsp;");
			str=str.replaceAll("\r\n","<br/>");
			return str;
		}
	}
	public static String formatHtml2Text(String in){
		String str="";
		if(in==null){
			return "";
		}else{
			str=in.replaceAll("&lt;","<");
			str=str.replaceAll("&gt;",">");
			str=str.replaceAll("&nbsp;"," ");
			str=str.replaceAll("<br/>","\r\n");
			return str;
		}
	}
	
	public static List getListPage(int size,int page,List data){
		if(data.size() == 0 ){
			return data; 
		}
		int max = data.size();
		int min = 0;
		if(page<1){
			page =1;
		}
		min = (page-1) * size;
		max = page *size;
		if(min<0)min=0;
		if(min>(data.size()-1))min=data.size()-1;
		if(max<0)max=0;
		if(max>(data.size()))max=data.size();
		
		return data.subList(min, max);
		
	}
	
	 /** 
     * MD5 加密 
     */  
    public static String md5(String str) {  
        MessageDigest messageDigest = null;  
  
        try {  
            messageDigest = MessageDigest.getInstance("MD5");  
  
            messageDigest.reset();  
  
            messageDigest.update(str.getBytes("UTF-8"));  
        } catch (NoSuchAlgorithmException e) {  
            System.out.println("NoSuchAlgorithmException caught!");  
            System.exit(-1);  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
  
        byte[] byteArray = messageDigest.digest();  
  
        StringBuffer md5StrBuff = new StringBuffer();  
  
        for (int i = 0; i < byteArray.length; i++) {              
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
            else  
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
        }  
  
        return md5StrBuff.toString();  
    }

    public static String switchUrlParam(JSONObject obj){
    	StringBuffer sb = new StringBuffer();
    	Iterator it = obj.keySet().iterator();
    	while(it.hasNext()){
    		String key = (String)it.next();
    		String value = "";
    		if(obj.get(key)!=null){
    			value = obj.get(key).toString();
			}
    		sb.append(key).append("=").append(value).append("&");
		}
    	if(sb.length()>1){
    		sb.deleteCharAt(sb.length()-1);
		}
    	return sb.toString();
	}
    
    public static String getRandomCodeByNowTime() {  
    	long nowTime=System.currentTimeMillis();
    	int a = (int)(Math.random()*10);
    	int b = (int)(Math.random()*10);
    	String s = ""+nowTime+a+b;
    	return s;
    }

	//获取一个字符串，查找这个字符串出现的次数;
	public static int getStringCount(String str, String key) {
		int count = 0;
		int index = 0;
		int num = str.indexOf(key);
		while ((index = str.indexOf(key)) != -1) {
			count++;
			str = str.substring(str.indexOf(key) + key.length());
		}
		return count;
	}

	public static void main(String[] args){
		try {
			//测试验证SQL语句，效果不好
			String sqls = "delete from BCP_Prize where 1=1";
			MySqlStatementParser parser = new MySqlStatementParser(sqls);
			List<SQLStatement> stmtList = parser.parseStatementList();

			// 将AST通过visitor输出
			/*StringBuilder out = new StringBuilder();
			MySqlOutputVisitor visitor = new MySqlOutputVisitor(out);

			for (SQLStatement stmt : stmtList) {
				stmt.accept(visitor);
				System.out.println(out + ";");
				out.setLength(0);
			}*/

			//ObjectTools obj = new ObjectTools();
			//System.out.println(obj.getRandomCodeByNowTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	