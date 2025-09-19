package com.om.bo.base;

public class Const {
	/**
	 * 当前系统的表的前缀,同时也是jsp页面所在的目录前缀
	 */
	public final static String RESP_CODE = "code";  //统一的响应请求的key
	public final static String RESP_MSG = "msg";   //msg的key
	public final static String RESP_DATA = "data";   //msg的key
	public final static String RESP_LIST = "dataList";   //
	public final static String TOTAL = "total";
	public final static String ENTITY_LIST = "entity_list";
	public final static int SuccCode = 1;
	public final static String SUCC="SUCC";
	public final static String FAIL="FAIL";
	public final static int ErrCode = -1;
	public final static String RESP_EXCEPTION = "exception";
	/**
	 * 将登录后操作员的信息保存到Session中,queryMapper
	 */
	public final static String curOper="session_operator";


	//============后面的可能没用





	public final static String RESP_SEQ = "seq";   //msg的key

	public final static String XASIS="xAxis";
	public final static String LEGEND="legend";

	public final static String TASKID="taskId";



	public final static String AFFECT_ROW = "affectRow";   //返回数据列表的key返回数据列表的key

	public final static String TOTAL_ROW = "total"; //前台分页所需要的字段信息，统一定义

	public static final String MY_SYS_PRE="mt";
	public static final String MY_SYS_COMPANY="一体化配置";//厂家或厂家+大系统平台
	public static final String MY_SYS_NAME="一体化配置";//当前系统

	public static final String BUSI_LOGGER_KEY = "BUSI_LOGGER_KEY" ; //日志Key
	public static final String LOGGER_GLOBAL_ID = "LOGGER_GLOBAL_ID" ; //全局日志编号

	public static final String QUERY_FLAG = "query";
	public static final String SAVE_FLAG = "save";
	public static final String UPDATE_FLAG = "update";
	public static final String DELETE_FLAG = "delete";

	
	/**
	 * 通过外部登录后,将操作员信息保存到session中,queryMapper
	 */
	public final static String linkOper="session_operator_link";

	public final static String AppId="wx7fa5f1d433360284";
	public final static String AppSec="acd28ec97d1c504ca9d8a25515fa4c92";
	public final static String AppToken="iloveehomenet";
	public final static String AppShhid="1225894002";
	public final static String AppShKey="JGCL18ZJH7GDJR17Y1NSZT9B6SUXE6QE";// 商户号对应的密钥



	public final static int SuccCodeWithResult = 2;
	public final static int SaveExistData = 3;
	public final static int NoValid = 4;//规则校验不通过

	public final static int inValidCode = -2;
	public final static int NoDataCode = 100;
	public final static String NoData="没有查到数据";
	public final static String inValidMsg="校验不通过";
	public final static String ExistData="新增时已存在相同数据";
	public final static String SessionTimeout="会话超时";
	public final static String NoUser="用户不存在";
	public final static String UserExist="用户已存在";
	public final static String ErrPwd="密码不正确,请重新输入";
	public final static String ErrSystem="系统错误,请联系管理员";
	public final static String ErrSendSms="发送短信验证码失败,请稍侯重试";
	public final static String ErrCheckSms="短信验证码校验失败,请重新输入";
	public final static String ErrOrderOverTime="订单已超时不能取消";

	public final static String ErrWxNoOpenId="获取微信用户信息出错";
	public final static String ErrWxNoPayId="获取支付prepayId出错";
	public final static String ErrWxShopId="微信商城商品编号不存在";
	public final static String ErrWxCartEmpty="您的购物车为空";

	public final static String ExistCompany="公司已经存在";
	public final static String COPYRIGHT="Copyright © 2013-2017 Lanzhou Jiawen Electronic Technology Co. Ltd.All rights reserved.";


	public final static int ErrCodeWithResult = -2;


	public final static String SQL_ACTION_NULL = "查询的invokePage参数为空";
	public final static String SQL_SAVE_INFO = "保存成功";
	public final static String SQL_UPDATE_INFO = "修改成功";
	public final static String SQL_DELETE_INFO = "删除成功";




	public static class Jhrs_check{
        public final static int complete_ok=1;
        public final static int complete_fail=-1;
        public final static int unComplete=-2;
    }

	public static class DataRepairType{
        public final static int backup =1;
        public final static int repair =2;
        public final static int rollback =3;
    };

}
