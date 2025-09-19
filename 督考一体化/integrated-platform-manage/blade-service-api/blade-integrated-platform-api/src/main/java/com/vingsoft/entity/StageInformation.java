package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description: 阶段信息实体
 * @date 2022-04-09 15:38
 */
@Data
@TableName("stage_information")
@ApiModel(value = "stage_information对象", description = "阶段信息表")
public class StageInformation extends BaseEntity {

	/**
	 * 阶段名称
	 */
	@ApiModelProperty(value = "阶段名称")
	private String stage;

	/**
	 * 阶段要求
	 */
	@ApiModelProperty(value = "阶段要求")
	private String stageRequirement;

	/**
	 * 开始时间
	 */
	@ApiModelProperty(value = "开始时间")
	private Date startDate;

	/**
	 * 截止时间
	 */
	@ApiModelProperty(value = "截止时间")
	private Date endDate;

	/**
	 * 评价指标分类
	 */
	@ApiModelProperty(value = "评价指标分类")
	private String evaluationType;

	/**
	 * 年度、季度评价主键id
	 */
	@ApiModelProperty(value = "年度、季度评价主键id")
	private Long evaluationId;


	/**
	 * 周期
	 */
	@ApiModelProperty(value = "周期")
	private String cycle;

	/**
	 * 阶段汇报提醒信息
	 */
	@TableField(exist = false)
	private List<ReportTime> reportTimeList;

	/**
	 * 是否是当前阶段 Y/N
	 */
	@TableField(exist = false)
	private String  iscurrent;

}
