package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-19 22:14
 */
@Data
@TableName("reports_baseinfo")
@ApiModel(value = "reports_baseinfo对象", description = "汇报基本信息")
public class ReportsBaseinfo extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 阶段信息表主键
	 */
	@ApiModelProperty(value = "阶段信息表主键")
	private Long stageId;

	/**
	 * 阶段名称
	 */
	@ApiModelProperty(value = "阶段名称")
	private String stage;


	/**
	 * 指标表主键
	 */
	@ApiModelProperty(value = "指标表主键")
	private Long evaluationId;

	/**
	 * 指标类型：1年度 2季度
	 */
	@ApiModelProperty(value = "指标类型")
	private String evaluationType;


	/**
	 * 责任单位id
	 */
	@ApiModelProperty(value = "责任单位id")
	private Long deptId;

	/**
	 * 责任单位名称
	 */
	@ApiModelProperty(value = "责任单位名称")
	private String deptName;

	/**
	 * 汇报状态
	 */
	@ApiModelProperty(value = "汇报状态")
	private String reportStatus;


	//督查督办子ID
	@TableField(exist = false)
	private Long childId;

	/**
	 * 业务表名
	 */
	@TableField(exist = false)
	private String businessTable;
	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
