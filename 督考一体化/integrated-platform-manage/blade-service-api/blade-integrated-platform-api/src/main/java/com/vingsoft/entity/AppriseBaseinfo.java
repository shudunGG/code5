package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:评价基本信息实体
 * @date 2022-04-18 09:56
 */
@Data
@TableName("apprise_baseinfo")
@ApiModel(value = "apprise_baseinfo对象", description = "评价基本信息表")
public class AppriseBaseinfo extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 事项名称
	 */
	@ApiModelProperty(value = "事项名称")
	private String servName;

	/**
	 * 权重
	 */
	@ApiModelProperty(value = "权重")
	private String weight;

	/**
	 * 评分细则
	 */
	@ApiModelProperty(value = "评分细则")
	private String pfxz;

	/**
	 * 当前阶段名称
	 */
	@ApiModelProperty(value = "当前阶段名称")
	private String stage;

	/**
	 * 当前阶段id
	 */
	@ApiModelProperty(value = "当前阶段id")
	private String stageId;

	/**
	 * 评价指标分类 1年度考核 2季度考核
	 */
	@ApiModelProperty(value = "评价指标分类")
	private String evaluationType;

	/**
	 * 年度、季度评价主键id
	 */
	@ApiModelProperty(value = "年度、季度评价主键id")
	private Long evaluationId;

	/**
	 * 部门评价信息
	 */
	@TableField(exist = false)
	private List<AppriseDept> appriseDeptList;

	/**
	 * 部门名称
	 */
	@TableField(exist = false)
	private String deptName;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;
}
