package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;
import java.util.List;

/**
 * @author zrj
 * @version 1.0
 * @description:
 * @date 2023-01-06 20:15
 */
@Data
@TableName("quarterly_assessment")
@ApiModel(value = "quarterly_assessment对象", description = "考核体系表")
public class QuarterlyAssessment extends BaseEntity{




	/**
	 * 考核分组字典值(字典编码：kh_group)
	 */
	@ApiModelProperty(value = "考核分类")
	private String checkClassify;

	/**
	 * 考核分组名称(字典编码：kh_group)
	 */
	@ApiModelProperty(value = "考核分类名称")
	private String checkClassifyName;

	/**
	 * 季度指标分类字典值（字典代码：jdpj-type）1,2,3
	 */
	@ApiModelProperty(value = "季度指标分类")
	private String jdzbType;

	/**
	 * 季度指标分类字典名称（字典代码：jdpj-type）
	 */
	@ApiModelProperty(value = "季度指标名称")
	private String jdzbName;

	/**
	 * 所属季度
	 */
	@ApiModelProperty(value = "所属季度")
	private String toQuarter;

	/**
	 * 评价要点
	 */
	@ApiModelProperty(value = "评价要点")
	private String majorTarget;
	/**
	 * 考核对象
	 */
	@ApiModelProperty(value = "考核对象")
	private String checkObject;

	/**
	 * 考核对象id
	 */
	@ApiModelProperty(value = "考核对象id")
	private String checkObjectId;

	/**
	 * 评价单位
	 */
	@ApiModelProperty(value = "评价单位")
	private String appraiseDeptname;
	/**
	 * 评价单位id
	 */
	@ApiModelProperty(value = "评价单位id")
	private String appraiseDeptid;
	/**
	 * 评价权重
	 */
	@ApiModelProperty(value = "评价权重")
	private String weight = "0.0";
	/**
	 * 完成时间
	 */
	@ApiModelProperty(value = "完成时间")
	private Date finishDate;

	/*季度考评状态*/
	@ApiModelProperty(value = "季度考评状态")
	private String checkStatus;

	/*指标状态：0暂存 1推进中 2已完成*/
	@ApiModelProperty(value = "指标状态")
	private String targetStatus;

	/*每种状态下指标的数量*/
	@TableField(exist = false)
	private Integer targetStatusNum;

	/**
	 * 办理状态：0正常1催办*/
	@ApiModelProperty(value = "办理状态")
	private String handleStatus;

	/**
	 * 指标*/
	@ApiModelProperty(value = "指标")
	private String target;



	/**
	 * 重点工作*/
	@ApiModelProperty(value = "重点工作")
	private String importWork;


	/**
	 * 评分细则*/
	@ApiModelProperty(value = "评分细则")
	private String scoringRubric;

	/**
	 * 是否评价，默认0，1是已评价 用于限制当前指标只能评价一次
	 */
	@ApiModelProperty(value = "是否评价")
	private Integer isAppraise;

	/**
	 * 未评价人员
	 */
	@ApiModelProperty(value = "未评价人员")
	private String notAppriseUser;

	/**
	 * 阶段信息
	 */
	@TableField(exist = false)
	private List<StageInformation> stageInformationList;

	/**
	 * 年份
	 */
	@TableField(exist = false)
	private String quarterlyYear;

	/**
	 * 操作类型 0 新增 1下发
	 */
	@TableField(exist = false)
	private String operateType;


	/**
	 * 责任单位id
	 */
	@TableField(exist = false)
	private String responsibleUnitId;

	/**
	 * 1领导关注，2我的关注
	 */
	@TableField(exist = false)
	private String quarterlyType;
	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;

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
	 * 季度
	 */
	@TableField(exist = false)
	private String quarterStr = "1";

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

	/**
	 * 文件路径*/
	@ApiModelProperty(value = "文件路径")
	private String fileUrl;


	/**
	 * 文件名*/
	@ApiModelProperty(value = "文件名")
	private String fileName;





}
