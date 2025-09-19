package org.springblade.integrated.platform.constant;

import io.swagger.models.auth.In;

/**
 * @author mrtang
 * @version 1.0
 * @description: 状态常量
 * @date 2022/4/24 09:09
 */
public interface StatusConstant {

	/**
	 * 流程状态：督办立项
	 */
	public static final String FLOW_STATUS_0 = "0";
	/**
	 * 流程状态：未签收
	 */
	public static final String FLOW_STATUS_1 = "1";
	/**
	 * 流程状态：计划中
	 */
	public static final String FLOW_STATUS_2 = "2";
	/**
	 * 流程状态：推进中
	 */
	public static final String FLOW_STATUS_3 = "3";
	/**
	 * 流程状态：已完成
	 */
	public static final String FLOW_STATUS_4 = "4";
	/**
	 * 流程状态：立项送审
	 */
	public static final String FLOW_STATUS_5 = "5";


	/**
	 * 事项状态：正常推进
	 */
	public static final String SERV_STATUS_1 = "1";
	/**
	 * 事项状态：推进滞后/已超期
	 */
	public static final String SERV_STATUS_2 = "2";
	/**
	 * 事项状态：即将超期
	 */
	public static final String SERV_STATUS_3 = "3";
	/**
	 * 事项状态：已完成
	 */
	public static final String SERV_STATUS_4 = "4";
	/**
	 * 事项状态：超期汇报/超期未汇报
	 */
	public static final String SERV_STATUS_5 = "5";
	/**
	 * 事项状态：被退回
	 */
	public static final String SERV_STATUS_6 = "7";


	/**
	 * 督查汇报状态：未汇报
	 */
	public static final String DC_REPORT_STATUS_0 = "0";
	/**
	 * 督查汇报状态：责任单位汇报
	 */
	public static final String DC_REPORT_STATUS_1 = "1";
	/**
	 * 督查汇报状态：牵头单位汇总
	 */
	public static final String DC_REPORT_STATUS_2 = "2";
	/**
	 * 督查汇报状态：送审中
	 */
	public static final String DC_REPORT_STATUS_3 = "3";
	/**
	 * 督查汇报状态：送审不通过
	 */
	public static final String DC_REPORT_STATUS_4 = "4";
	/**
	 * 督查汇报状态：督查督办退回
	 */
	public static final String DC_REPORT_STATUS_5 = "5";
	/**
	 * 督查汇报状态：超期未汇报
	 */
	public static final String DC_REPORT_STATUS_6 = "6";
	/**
	 * 督查汇报状态：超期已汇报
	 */
	public static final String DC_REPORT_STATUS_7 = "7";

	/**
	 * 上报计划状态：未上报
	 */
	public static final String UP_PLAN_STATUS_0 = "0";
	/**
	 * 上报计划状态：已上报
	 */
	public static final String UP_PLAN_STATUS_1 = "1";
	/**
	 * 上报计划状态：已通过
	 */
	public static final String UP_PLAN_STATUS_2 = "2";
	/**
	 * 上报计划状态：已驳回
	 */
	public static final String UP_PLAN_STATUS_3 = "3";
	/**
	 * 上报计划状态：送审中
	 */
	public static final String UP_PLAN_STATUS_4 = "4";
	/**
	 * 上报计划状态：送审驳回
	 */
	public static final String UP_PLAN_STATUS_5 = "5";

	/**
	 * 审核状态：待审核
	 */
	public static final String AUDIT_STATUS_0 = "0";
	/**
	 * 审核状态：通过
	 */
	public static final String AUDIT_STATUS_1 = "1";
	/**
	 * 审核状态：不通过
	 */
	public static final String AUDIT_STATUS_2 = "2";
	/**
	 * 审核状态：冻结
	 */
	public static final String AUDIT_STATUS_3 = "3";

	/**
	 * 审核：异步
	 */
	public static final Integer AUDIT_SYNC_0 = 0;
	/**
	 * 审核：同步
	 */
	public static final String AUDIT_SYNC_1 = "1";

	/**
	 * 送审业务类型 info——督察督办
	 */
	public static final String OPERATION_TYPE_INFO = "info";
	/**
	 * 送审业务类型 plan——上报计划
	 */
	public static final String OPERATION_TYPE_PLAN = "plan";
	/**
	 * 送审业务类型 report——阶段汇报
	 */
	public static final String OPERATION_TYPE_REPORT = "report";
	/**
	 * 送审业务类型 reportChi——下发阶段汇报
	 */
	public static final String OPERATION_TYPE_REPORT_CHI = "reportChi";
	/**
	 * 送审业务类型 reportAll——阶段汇总
	 */
	public static final String OPERATION_TYPE_REPORT_ALL = "reportAll";
	/**
	 * 送审业务类型 reportAlt——下发阶段汇总
	 */
	public static final String OPERATION_TYPE_REPORT_ALT = "reportAlt";


	/**
	 * 送审业务类型 quarterapprisehb——考核评价汇报申请---季度 汇报表id
	 */
	public static final String OPERATION_TYPE_QUARTERAPPRISEHB = "quarterapprisehb";
	/**
	 * 送审业务类型 quarterappriseScore——考核评价改分申请---季度
	 */
	public static final String OPERATION_TYPE_QUARTERAPPRISESCORE = "quarterappriseScore";
	/**
	 * 送审业务类型 quarterappriseXf——考核评价下发---季度
	 */
	public static final String OPERATION_TYPE_QUARTERAPPRISEXF = "quarterappriseXf";

	/**
	 * 送审业务类型 annualapprisehb——考核评价汇报申请---年度 汇报表id
	 */
	public static final String OPERATION_TYPE_ANNUALAPPRISEHB = "annualapprisehb";
	/**
	 * 送审业务类型 annualappriseScore——考核评价改分申请---年度
	 */
	public static final String OPERATION_TYPE_ANNUALAPPRISESCORE = "annualappriseScore";
	/**
	 * 送审业务类型 annualappriseXf——考核评价下发---年度
	 */
	public static final String OPERATION_TYPE_ANNUALAPPRISEXF = "annualappriseXf";

	/**
	 * 送审业务类型 addscore——考核评价加分项
	 */
	public static final String OPERATION_TYPE_ADDSCORE = "addscore";
	/**
	 * 送审业务类型 minusscore——考核评价减分项
	 */
	public static final String OPERATION_TYPE_MINUSSCORE = "minusscore";


	/**
	 * 送审业务类型 ware——项目入库
	 */
	public static final String OPERATION_TYPE_WARE = "ware";

	/**
	 * 单位类型：lead 牵头单位
	 */
	public static final String DEPT_TYPE_LEAD = "lead";
	/**
	 * 单位类型：duty 责任单位
	 */
	public static final String DEPT_TYPE_DUTY = "duty";

	/**
	 * 统一消息状态：未读
	 */
	public static final Integer UNIFY_MESSAGE_0 = 0;
	/**
	 * 统一消息状态：已读
	 */
	public static final Integer UNIFY_MESSAGE_1 = 1;



	/**
	 * 考核评价汇报状态：送审通过
	 */
	public static final String KH_REPORT_STATUS_3 = "3";
	/**
	 * 考核评价汇报状态：送审不通过
	 */
	public static final String KH_REPORT_STATUS_4 = "4";

	/**
	 * web平台消息类别：考核评价-汇报送审
	 */
	public static final String WEB_MSG_TYPE_20 = "20";


	/**
	 * 消息状态：0未读
	 */
	public static final Integer MSG_STATUS_0 = 0;
	/**
	 * 消息状态：1已读
	 */
	public static final Integer MSG_STATUS_1 = 1;

	/**
	 * 平台：app
	 */
	public static final String MSG_PLATFORM_APP = "app";
	/**
	 * 平台：web
	 */
	public static final String MSG_PLATFORM_WEB = "web";

	/**
	 * web平台消息类别：督察督办-下发送审
	 */
	public static final String WEB_MSG_TYPE_15 = "15";
	/**
	 * web平台消息类别：督察督办-汇报送审
	 */
	public static final String WEB_MSG_TYPE_21 = "21";
	/**
	 * web平台消息类别：督察督办-上报送审
	 */
	public static final String WEB_MSG_TYPE_22 = "22";
	/**
	 * web平台消息类别：督察督办-下发
	 */
	public static final String WEB_MSG_TYPE_63 = "63";
	/**
	 * web平台消息类别：督察督办-计划上报
	 */
	public static final String WEB_MSG_TYPE_24 = "24";

	/**
	 * app平台消息类别：督察督办
	 */
	public static final String APP_MSG_TYPE_9 = "9";
	/**
	 * app平台消息类别：计划
	 */
	public static final String APP_MSG_TYPE_2 = "2";
	/**
	 * app平台消息类别：汇报
	 */
	public static final String APP_MSG_TYPE_1 = "1";

	/**
	 * app平台二级消息类别：督办-下发
	 */
	public static final String APP_TWO_MSG_TYPE_1 = "1";
	/**
	 * app平台二级消息类别：督办-下发送审
	 */
	public static final String APP_TWO_MSG_TYPE_2 = "2";
	/**
	 * app平台二级消息类别：督查-计划上报
	 */
	public static final String APP_TWO_MSG_TYPE_10 = "10";
	/**
	 * app平台二级消息类别：督办-上报送审
	 */
	public static final String APP_TWO_MSG_TYPE_8 = "8";
	/**
	 * app平台二级消息类别：督办-汇报送审
	 */
	public static final String APP_TWO_MSG_TYPE_7 = "7";

	/**
	 * 督查督办附件来源：立项送审-审核
	 */
	public static final String FILE_FROM_3 = "3";

}
