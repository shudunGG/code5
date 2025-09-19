package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.vingsoft.entity.ProjectSpecialist;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-18 09:38:59
 */
@Data
@ApiModel(value = "投资项目视图对象", description = "TzProjectSummaryVO对象")
public class TzProjectSummaryVO implements Serializable {
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
	 * 项目类型
	 */
	private String xmType;


	/**
	 * 项目挂牌
	 */
	private String autoState;


	/**
	 * 项目名称
	 */
	private String title;

	/**
	 * 投资主体
	 */
	private String projMain;

	/**
	 * 建设内容和规模
	 */
	private String projContent;

	/**
	 * 项目地址
	 */
	private String xmAddress;

	/**
	 * 总投资（万元）
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float totalInvestment;

	/**
	 * 年度计划投资（万元）
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float yearEconomic;

	/**
	 * 当月完成投资（万元）
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Float monthEconomic;


	/**
	 * 项目单位及责任人
	 */
	private String sgdw;
	/**
	 *  建设规模
	 */
	private String projScale;
	private String sjbzld;
	private String sjbzldName;
	private String wcqk;

	/**
	 * 1月完成投资
	 */
	private String wctz01;

	/**
	 * 1-2月完成投资
	 */
	private String wctz02;

	/**
	 * 1-3月完成投资
	 */
	private String wctz03;

	/**
	 * 1-4月完成投资
	 */
	private String wctz04;

	/**
	 * 1-5月完成投资
	 */
	private String wctz05;

	/**
	 * 1-6月完成投资
	 */
	private String wctz06;

	/**
	 * 1-7月完成投资
	 */
	private String wctz07;

	/**
	 * 1-8月完成投资
	 */
	private String wctz08;

	/**
	 * 1-9月完成投资
	 */
	private String wctz09;

	/**
	 * 1-10月完成投资
	 */
	private String wctz10;

	/**
	 * 1-11月完成投资
	 */
	private String wctz11;

	/**
	 * 1-12月完成投资
	 */
	private String wctz12;

	/**
	 * 1月计划投资
	 */
	private String jhtz01;

	/**
	 * 1-2月计划投资
	 */
	private String jhtz02;

	/**
	 * 1-3月计划投资
	 */
	private String jhtz03;

	/**
	 * 1-4月计划投资
	 */
	private String jhtz04;

	/**
	 * 1-5月计划投资
	 */
	private String jhtz05;

	/**
	 * 1-6月计划投资
	 */
	private String jhtz06;

	/**
	 * 1-7月计划投资
	 */
	private String jhtz07;

	/**
	 * 1-8月计划投资
	 */
	private String jhtz08;

	/**
	 * 1-9月计划投资
	 */
	private String jhtz09;

	/**
	 * 1-10月计划投资
	 */
	private String jhtz10;

	/**
	 * 1-11月计划投资
	 */
	private String jhtz11;

	/**
	 * 1-12月计划投资
	 */
	private String jhtz12;

	/**
	 * 产业类型
	 */
	private String cyType;

	/**
	 * 项目负责人
	 */
	private String sgfzr;

	private String totalTz;

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

	/**
	 * 完成成总投资
	 */
	private float totalEconomic;
	//计划差额
	private float jhce;
	//当月计划
	private float dyjh;

	private Date qqgzjhwcsj;
	private Date startDatePlan;
	private String qqcylx;

	private String bgFileName;

	private String bgFileUrl;

	/**
	 * 项目专员列表
	 */
	@TableField(exist = false)
	private List<ProjectSpecialist> projectSpecialistList;

	/**能否汇报*/
	@TableField(exist = false)
	private boolean canReport;
}
