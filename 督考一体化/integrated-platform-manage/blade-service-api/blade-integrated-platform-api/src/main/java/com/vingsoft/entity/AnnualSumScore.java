package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 季度评价得分表
 *
 * @Author JG🧸
 * @Create 2022/4/20 9:09
 */
@Data
@TableName("annual_sum_score")
@ApiModel(value = "annual_sum_score 对象", description = "年度评价得分表")
public class AnnualSumScore extends BaseEntity{

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
	 * 主要指标及评价要点
	 */
	/*@ApiModelProperty(value = "主要指标及评价要点")
	private String majorTarget;*/

	/**
	 * 年份
	 */
	@ApiModelProperty(value = "年份")
	private String annualYear;

	/**
	 * 县区高质量发展得分
	 */
	@ApiModelProperty(value = "县区高质量发展")
	private Double xqgzlfzScore = 0.0;

	/**
	 * 市直高质量发展得分
	 */
	@ApiModelProperty(value = "市直高质量发展")
	private Double szgzlfzScore = 0.0;

	/**
	 * 政治思想建设得分
	 */
	@ApiModelProperty(value = "政治思想建设")
	private Double zzsxjsScore = 0.0;

	/**
	 * 领导能力得分
	 */
	@ApiModelProperty(value = "领导能力")
	private Double ldnlScore = 0.0;

	/**
	 * 党的建设得分
	 */
	@ApiModelProperty(value = "党的建设")
	private Double ddjsScore = 0.0;

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
	 *季度评价得分
	 */
	@ApiModelProperty(value = "季度评价得分")
	private Double jdpjScore = 0.0;

	/**
	 *总分
	 */
	@ApiModelProperty(value = "总分")
	private Double annualSumScore = 0.0;


	/**
	 * 年度评价主键id
	 */
	@ApiModelProperty(value = "年度评价主键id")
	private Long annualEvaluationId;

	/**
	 * 排序
	 */
	@ApiModelProperty(value = "排序")
	private Integer annualType;

	/**
	 * 是否发布 默认0未发布 1已发布
	 */
	@ApiModelProperty(value = "是否发布")
	private Integer isSend;

	/**
	 * 平均分
	 */
	@TableField(exist = false)
	private Double avgAnnualSumScore = 0.0;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;

	/**
	 * 高质量发展得分(县区+市直)
	 */
	@TableField(exist = false)
	private Double gzlfzScore = 0.0;

}
