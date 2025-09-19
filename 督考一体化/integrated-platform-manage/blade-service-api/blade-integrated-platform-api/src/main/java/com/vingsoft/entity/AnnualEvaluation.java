package com.vingsoft.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:年度考评信息实体
 * @date 2022-04-08 20:11
 */
@Data
@TableName("annual_evaluation")
@ApiModel(value = "annual_evaluation对象", description = "年度考评指标表")
public class AnnualEvaluation  extends BaseEntity{

	/**
	 * 项目名称字典值
	 */
	@ApiModelProperty(value = "项目名称字典值")
	private String projectId;

	/**
	 * 项目名称
	 */
	@ApiModelProperty(value = "项目名称")
	private String projectName;

	/**
	 * 考核分类
	 */
	@ApiModelProperty(value = "考核分类id")
	private String appraiseClassify;

	/**
	 * 考核分类名称
	 */
	@ApiModelProperty(value = "考核分类名称")
	private String appraiseClassifyName;

	/**
	 * 主要指标及评价要点
	 */
	@ApiModelProperty(value = "主要指标及评价要点")
	private String majorTarget;

	/**
	 * 考核对象id
	 */
	@ApiModelProperty(value = "考核对象id")
	private String appraiseObjectId;

	/**
	 * 考核对象
	 */
	@ApiModelProperty(value = "考核对象")
	private String appraiseObject;

	/**
	 * 评价单位名称
	 */
	@ApiModelProperty(value = "评价单位名称")
	private String appraiseDeptname;

	/**
	 * 评价单位id
	 */
	@ApiModelProperty(value = "评价单位id")
	private String appraiseDeptid;

	/**
	 * 权重
	 */
	@ApiModelProperty(value = "权重")
	private String weight = "0.0";

	/**
	 * 完成时间
	 */
	@ApiModelProperty(value = "完成时间")
	private Date finishDate;


	/**
	 * 年度考评分类：政治思想建设、领导能力、党的建设、高质量发展（市直高质量发展、区县高质量发展）
	 */
	@ApiModelProperty(value = "年度考评分类")
	private String type;

	/**
	 * 年度考评状态
	 */
	@ApiModelProperty(value = "年度考评状态")
	private String checkStatus;

	/**
	 * 指标状态：0暂存 1推进中 2已完成
	 */
	@ApiModelProperty(value = "指标状态")
	private String targetStatus;

	/*每种状态下指标的数量*/
	@TableField(exist = false)
	private Integer targetStatusNum;

	/**
	 * 办理状态：0正常1催办
	 */
	@ApiModelProperty(value = "办理状态")
	private String handleStatus;

	/**
	 * 阶段信息
	 */
	@TableField(exist = false)
	private List<StageInformation> stageInformationList;

	/**
	 * 操作类型 0 新增 1下发
	 */
	@TableField(exist = false)
	private String operateType;


	/**
	 * 甘州区
	 */
	@ApiModelProperty(value = "甘州区")
	private String ganzhouqu = "0.0";

	/**
	 * 临泽县
	 */
	@ApiModelProperty(value = "临泽县")
	private String linzexian = "0.0";

	/**
	 * 高台县
	 */
	@ApiModelProperty(value = "高台县")
	private String gaotaixian = "0.0";

	/**
	 * 山丹县
	 */
	@ApiModelProperty(value = "山丹县")
	private String shandanxian = "0.0";

	/**
	 * 民乐县
	 */
	@ApiModelProperty(value = "民乐县")
	private String minlexian = "0.0";

	/**
	 * 肃南县
	 */
	@ApiModelProperty(value = "肃南县")
	private String sunanxian = "0.0";

	/**
	 * 是否评价，用于限制当前指标只能评价一次
	 */
	@ApiModelProperty(value = "是否评价")
	private Integer isAppraise;

	/**
	 * 未评价人员
	 */
	@ApiModelProperty(value = "未评价人员")
	private String notAppriseUser;

	/**
	 * 年份
	 */
	@TableField(exist = false)
	private String quarterlyYear;

	/**
	 * 责任单位id
	 */
	@TableField(exist = false)
	private String responsibleUnitId;

	/** 业务表ID */
	@TableField(exist = false)
	private Long businessId;

	/**
	 *送审标题
	 */
	@TableField(exist = false)
	private String title;

	/**
	 *送审用户id
	 */
	@TableField(exist = false)
	private String userIds;

	/**
	 *是否异步
	 */
	@TableField(exist = false)
	private String sync;

	/**
	 * 汇报id
	 */
	@TableField(exist = false)
	private String reportId;
	/**
	 * 阶段id
	 */
	@TableField(exist = false)
	private String stageId;

	/**
	 * 是否汇报
	 */
	@TableField(exist = false)
	private String isHb;

	/**
	 * 是否发布
	 */
	@TableField(exist = false)
	private String isSend;
}
