package com.vingsoft.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.vingsoft.entity.ProjectPhasePlan;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

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
@ApiModel(value = "中央和省级预算内项目视图对象", description = "ZysjProjectSummaryVO对象")
public class ZysjProjectSummaryVO implements Serializable {
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
	 * 项目挂牌
	 */
	private String autoState;
	/**
	 * 产业类型
	 */
	private String cyType;
	/**
	 * 建设规模
	 */
	private String projScale;
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
	 * 项目单位
	 */
	private String sgdw;
	/**
	 * 项目负责人
	 */
	private String sgfzr;

	/**
	 * 项目负责人名称
	 */
	private String sgfzrName;


	/**
	 * 完成情况
	 */
	private String wcqk;

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
