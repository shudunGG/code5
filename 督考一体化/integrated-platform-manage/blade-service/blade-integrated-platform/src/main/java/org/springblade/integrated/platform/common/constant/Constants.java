package org.springblade.integrated.platform.common.constant;

import java.math.BigDecimal;

/**
 * 通用常量信息
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
public class Constants
{
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 系统用户授权缓存
     */
    public static final String SYS_AUTH_CACHE = "sys-authCache";

    /**
     * 参数管理 cache name
     */
    public static final String SYS_CONFIG_CACHE = "sys-config";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache name
     */
    public static final String SYS_DICT_CACHE = "sys-dict";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     */
    public static final String[] JOB_WHITELIST_STR = { "org.springblade.integrated.platform" };

    /**
     * 定时任务违规的字符
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.springblade.integrated.platform.common.utils.file" };


	public static final String USER_POST_GLY = "gly";

	//岗位
	/**
	 * 管理员
	 */
	public static final String USER_POST_GLY_id ="1516056792837869570" ;

	/**
	 * 部门领导
	 */
	public static final String USER_POST_DEPT_LD_id ="1123598817738675202" ;

	/**
	 * 市级领导
	 */
	public static final String USER_POST_SJ_LD_id ="1123598817738675201" ;

	//督查督办PC消息类型
	public static final String DCDB_MAG_TYPE_PC_XF ="15" ;

	/**
	 * 下发送审
	 */
	public static final String DCDB_MAG_TYPE_PC_XFSS ="15" ;

	//督查督办APP消息类型一级分类
	//督办消息
	public static final String DCDB_MAG_TYPE_APP_DB ="9" ;


	//督查督办APP消息类型二级分类
	//督办消息 督查下发
	public static final String DCDB_MAG_TYPE_APP_DB_tow_DCXF ="1" ;

	/**
	 * APP消息分类一级分类
	 */
	public static final String MSG_TYPE_APP_ONE_HB="1";//汇报

	public static final String MSG_TYPE_APP_ONE_JH="2";//计划

	public static final String MSG_TYPE_APP_ONE_QS="3";//签收

	public static final String MSG_TYPE_APP_ONE_PSLY="4";//批示留言

	public static final String MSG_TYPE_APP_ONE_XTWJ="5";//协同文件

	public static final String MSG_TYPE_APP_ONE_XRW="6";//新任务

	public static final String MSG_TYPE_APP_ONE_QT="7";//其它

	public static final String MSG_TYPE_APP_ONE_CQ="8";//超期

	public static final String MSG_TYPE_APP_ONE_DB="9";//督办

	public static final String MSG_TYPE_APP_ONE_BJ="10";//办结

	public static final String MSG_TYPE_APP_ONE_XMTZ="11";//项目通知


	//督办消息 督查下发
	public static final String DCDB_MAG_TYPE_APP_DB_tow_DCXFSS ="2" ;

	//督查督办评价类型
	/**
	 * 责任领导评价
	 */
	public static final String DCDB_EVALUATE_TYPE_LEADER ="1" ;
	/**
	 * 督查督办评价
	 */
	public static final String DCDB_EVALUATE_TYPE_DCDB ="2" ;
	/**
	 * 牵头单位评价
	 */
	public static final String DCDB_EVALUATE_TYPE_LEADUNIT ="3" ;

	//督查督办得分类型
	//评价得分
	public static final String DCDB_SCORE_TYPE_PJ ="1" ;

	//加分
	public static final String DCDB_SCORE_TYPE_JIAF ="2" ;

	//减分
	public static final String DCDB_SCORE_TYPE_JIANF ="3" ;

	//事项单位类型
	//牵头单位
	public static final String DCDB_SERV_DEPT_TYPE_LEAD ="lead" ;

	//责任单位
	public static final String DCDB_SERV_DEPT_TYPE_DUTY ="duty" ;

	//部门评价得分系数
	/**
	 * 存在责任领导评价牵头单位得分领导评价系数
	 */
	public static final BigDecimal LEADER_SCORE_COEFFICIENT =BigDecimal.valueOf(0.5);

	/**
	 * 存在责任领导评价牵头单位得分督查督办系数
	 */
	public static final BigDecimal DCDB_SCORE_COEFFICIENT =BigDecimal.valueOf(0.5);

	/**
	 * 存在责任领导评价责任单位得分领导系数
	 */
	public static final BigDecimal LEADUNIT_LEADER_SCORE_COEFFICIENT =BigDecimal.valueOf(0.5);

	/**
	 * 存在责任领导评价责任单位得分督查督办系数
	 */
	public static final BigDecimal LEADUNIT_DCDB_SCORE_COEFFICIENT =BigDecimal.valueOf(0.25);

	/**
	 * 存在责任领导评价责任单位得分牵头单位系数
	 */
	public static final BigDecimal LEADUNIT_LEAD_UNIT_SCORE_COEFFICIENT =BigDecimal.valueOf(0.25);

	/**
	 * 无责任领导评价牵头单位得分督查督办系数
	 */
	public static final BigDecimal NOLEADER_LEAD_UNIT_DCDB_SCORE_COEFFICIENT =BigDecimal.valueOf(1);

	/**
	 * 无责任领导评价责任单位得分督查督办系数
	 */
	public static final BigDecimal NOLEADER_DUTY_DCDB_SCORE_COEFFICIENT =BigDecimal.valueOf(0.5);

	/**
	 * 无责任领导评价责任单位得分牵头单位系数
	 */
	public static final BigDecimal NOLEADUNIT_DUTY_LEAD_UNIT_SCORE_COEFFICIENT =BigDecimal.valueOf(0.5);

	/**
	 * 督查督办查询所有数据角色
	 */
	public static final String DCDB_SELECT_ROLE ="市级四大班子,督查督办查询角色";

	/**
	 * 超期1天数扣分
	 */
	public static final BigDecimal OVERDUE_SCORE_ONE =BigDecimal.valueOf(-1);

	/**
	 * 超期3天数扣分
	 */
	public static final BigDecimal OVERDUE_SCORE_THREE=BigDecimal.valueOf(-5);

	/**
	 * 超期5天数扣分
	 */
	public static final BigDecimal OVERDUE_SCORE_FIVE=BigDecimal.valueOf(-50);

	/**
	 * 超期5天数扣分
	 */
	public static final BigDecimal OVERDUE_SCORE_EIGHT=BigDecimal.valueOf(-100);
}
