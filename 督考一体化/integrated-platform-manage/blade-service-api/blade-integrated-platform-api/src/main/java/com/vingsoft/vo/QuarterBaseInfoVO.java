package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 考核评价-首页季度分页列表 视图实体类
 *
 * @Author JG🧸
 * @Create 2022/4/20 21:01
 */
@Data
@ApiModel(value = "QuarterBaseInfoVO 对象", description = "QuarterBaseInfoVO 对象")
public class QuarterBaseInfoVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 季度指标分类字典值（字典代码：jdpj-type）1,2,3
	 */
	@TableField(exist = false)
	private String jdzbType;

	/**
	 * 季度指标分类字典名称（字典代码：jdpj-type）
	 */
	@TableField(exist = false)
	private String jdzbName;
	/**
	 * 季度评价主键id
	 */
	@TableField(exist = false)
	private Long quarterlyEvaluationId;
	/**
	 * 考核分组字典值(字典编码：kh_group)
	 */
	@TableField(exist = false)
	private String checkClassify;

	/**
	 * 考核分组字典名称(字典编码：kh_group)
	 */
	@TableField(exist = false)
	private String checkClassifyName;
	/**
	 * 评价单位id
	 */
	@TableField(exist = false)
	private String appraiseDeptid;
	/**
	 * 评价单位
	 */
	@TableField(exist = false)
	private String appraiseDeptname;
	/**
	 * 责任单位名称
	 */
	@TableField(exist = false)
	private String responsibleUnitName;
	/**
	 * 责任单位id
	 */
	@TableField(exist = false)
	private String responsibleUnitId;
	/**
	 * 阶段
	 */
	@TableField(exist = false)
	private String stage;

	/**
	 * 阶段的年份
	 */
	@TableField(exist = false)
	private String stageYear;
	/**
	 *总分
	 */
	@TableField(exist = false)
	private Double quarterlySumScore = 0.0;
	/**
	 * 创建人id
	 */
	@TableField(exist = false)
	private Long createUser;
	/**
	 * 创建单位id
	 */
	@TableField(exist = false)
	private Long createDept;
	/**
	 * 创建时间
	 */
	@TableField(exist = false)
	private Date createTime;

	/**
	 * 一级指标
	 */
	@TableField(exist = false)
	private String firstTarget;

	/**
	 * 二级指标
	 */
	@TableField(exist = false)
	private String twoTarget;

	/**
	 * 评价要点
	 */
	@TableField(exist = false)
	private String majorTarget;

	/**
	 * 评分细则
	 */
	@TableField(exist = false)
	private String scoringRubric;

	/**
	 * 权重
	 */
	@TableField(exist = false)
	private String weight;

	/**
	 * 评分说明
	 */
	@TableField(exist = false)
	private String scoringDescription;

	/**
	 *百分制分数
	 */
	@TableField(exist = false)
	private Double score = 0.0;

	/**
	 * 考评总分
	 */
	@TableField(exist = false)
	private Map<String,Object> totalScore;

	/**
	 * 考评总分
	 */
	@TableField(exist = false)
	private Double A3 = 0.0;

	/**
	 * 是否发布
	 */
	@TableField(exist = false)
	private String isSend;

	/**
	 * 季度中文
	 */
	@TableField(exist = false)
	private String quarter;

	/**
	 * 工作实绩得分（评价打的分数）
	 */
	@TableField(exist = false)
	private Double gzsjScore=0.0;
}
