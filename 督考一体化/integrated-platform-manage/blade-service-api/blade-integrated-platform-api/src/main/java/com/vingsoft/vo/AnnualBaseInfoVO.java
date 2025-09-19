package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 考核评价-首页年度分页列表 视图实体类
 *
 * @Author JG🧸
 * @Create 2022/4/20 21:01
 */
@Data
@ApiModel(value = "AppriseBaseInfoVO 对象", description = "AppriseBaseInfoVO 对象")
public class AnnualBaseInfoVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 年度考评分类：政治思想建设、领导能力、党的建设、高质量发展（市直高质量发展、区县高质量发展）
	 */
	@TableField(exist = false)
	private String type;
	/**
	 * 项目名称字典值
	 */
	@TableField(exist = false)
	private String projectId;
	/**
	 * 项目名称 年度考评分类：1 政治思想建设、2 领导能力、3 党的建设、高质量发展（4 市直高质量发展、5 区县高质量发展）
	 */
	@TableField(exist = false)
	private String projectName;
	/**
	 * 年度评价主键id
	 */
	@TableField(exist = false)
	private Long annualEvaluationId;
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
	 * 年份
	 */
	@TableField(exist = false)
	private String annualYear;
	/**
	 * 事项名称,又叫【二级指标】，【评价要点】，【主要指标及评价要点】
	 */
	@TableField(exist = false)
	private String servName;
	/**
	 *总分
	 */
	@TableField(exist = false)
	private Double annualSumScore = 0.0;
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
	 * 评价要点
	 */
	@TableField(exist = false)
	private String majorTarget;

	/**
	 * 权重
	 */
	@TableField(exist = false)
	private String weight;
}
