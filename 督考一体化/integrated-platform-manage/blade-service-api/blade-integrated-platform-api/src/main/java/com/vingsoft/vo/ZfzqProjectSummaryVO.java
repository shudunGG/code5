package com.vingsoft.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-18 09:38:59
 */
@Data
@ApiModel(value = "政府专项债券项目视图对象", description = "ZfzqProjectSummaryVO对象")
public class ZfzqProjectSummaryVO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 项目id
	 */
	private Long id;
	/**
     * 项目状态
     */
    private String porjStatus;
	/**
	 * 调度状态
	 */
	private String reportStatus;
	/**
	 * 项目名称
	 */
	private String title;
	/**
	 * 项目负责人
	 */
	private String sgfzr;
	/**
	 * 隶属关系
	 */
	private String subordination;

	/**
	 * 建设性质
	 */
	private String xmType;

	/**
	 * 专项名称
	 */
	private String zxmc;
	/**
	 * 下达资金文号
	 */
	private String xdzjwh;

	/**
	 * 下达资金日期
	 */
	private Date xdzjTime;
	/**
	 * 投资计划(合计)
	 */
	private String tzjhhj;

	/**
	 * 投资计划(中央)
	 */
	private String zytzjh;

	/**
	 * 项目负责人名称
	 */
	private String sgfzrName;

	/**
	 * 投资计划(省级)
	 */
	private String sjtzjh;

	/**
	 * 投资计划(地方)
	 */
	private String dftzjh;

	/**
	 * 审核id
	 */
	private Long auditId;

	/**
	 * 汇报id
	 */
	private Long reportId;

	private Long createUser;
	private String areaCode;

	private String sfgx;//管辖标识
	private String sfdd;//调度标识

	private String bgFileName;

	private String bgFileUrl;
}
