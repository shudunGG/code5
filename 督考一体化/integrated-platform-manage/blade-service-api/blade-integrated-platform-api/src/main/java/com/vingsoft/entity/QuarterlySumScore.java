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
 * 季度评价得分表
 *
 * @Author JG🧸
 * @Create 2022/4/19 22:59
 */
@Data
@TableName("quarterly_sum_score")
@ApiModel(value = "quarterly_sum_score 对象", description = "季度评价得分表")
public class QuarterlySumScore extends BaseEntity{

	/**
	 * 考核分组字典值(字典编码：kh_group)
	 */
	@ApiModelProperty(value = "考核分组id")
	private String checkClassify;

	/**
	 * 考核分组字典名称(字典编码：kh_group)
	 */
	@ApiModelProperty(value = "考核分类")
	private String checkClassifyName;

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
	 * 责任单位名称
	 */
	@ApiModelProperty(value = "责任单位")
	private String responsibleUnitName;

	/**
	 * 责任单位id
	 */
	@ApiModelProperty(value = "责任单位id")
	private String responsibleUnitId;

	/**
	 * 事项名称,又叫【二级指标】，【评价要点】，【主要指标及评价要点】
	 */
	@ApiModelProperty(value = "事项名称")
	private String servName;

	/**
	 * 阶段
	 */
	@ApiModelProperty(value = "阶段")
	private String stage;

	/**
	 * 阶段id
	 */
	@ApiModelProperty(value = "阶段id")
	private String stageId;

	/**
	 * 阶段的年份
	 */
	@ApiModelProperty(value = "阶段的年份")
	private String stageYear;

	/**
	 *党建工作得分
	 */
	@ApiModelProperty(value = "党建工作得分")
	private Double djgzScore = 0.0;

	/**
	 *工作实绩得分
	 */
	@ApiModelProperty(value = "工作实绩得分")
	private Double gzsjScore = 0.0;

	/**
	 *党风廉政得分
	 */
	@ApiModelProperty(value = "党风廉政得分")
	private Double dflzScore = 0.0;

	/**
	 *三抓三促得分
	 */
	@ApiModelProperty(value = "三抓三促得分")
	private Double s3z3cScore = 0.0;

	/**
	 *加分
	 */
	@ApiModelProperty(value = "加分")
	private Double addScore = 0.0;

	/**
	 *减分
	 */
	@ApiModelProperty(value = "减分")
	private Double minusScore = 0.0;

	/**
	 * 领导评价得分
	 */
	@ApiModelProperty(value = "领导评价得分")
	private Double leaderScore = 0.0;

	/**
	 * 督查督办得分
	 */
	@ApiModelProperty(value = "督查督办得分")
	private Double dcdbScore = 0.0;

	/**
	 *总分
	 */
	@ApiModelProperty(value = "总分")
	private Double quarterlySumScore = 0.0;


	/**
	 * 季度评价主键id
	 */
	@ApiModelProperty(value = "季度评价主键id")
	private Long quarterlyEvaluationId;

	/**
	 * 排序
	 */
	@ApiModelProperty(value = "排序")
	private Integer quarterlyType;

	/**
	 * 是否发布 默认0未发布 1已发布
	 */
	@ApiModelProperty(value = "是否发布")
	private Integer isSend;

	/**
	 * 平均分
	 */
	@TableField(exist = false)
	private Double avgQuarterlySumScore = 0.0;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
