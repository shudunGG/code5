package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:单位评价实体
 * @date 2022-04-08 20:13
 */
@Data
@TableName("apprise_dept")
@ApiModel(value = "apprise_dept对象", description = "单位评价信息表")
public class AppriseDept extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
     * 责任单位
     */
	@ApiModelProperty(value = "责任单位")
    private String responsibleUnitName;

	/**
	 * 责任单位id
	 */
	@ApiModelProperty(value = "责任单位id")
	private Long responsibleUnitId;

    /**
     * 评分
     */
	@ApiModelProperty(value = "评分")
    private String score;

    /**
     * 评分说明
     */
	@ApiModelProperty(value = "评分说明")
    private String scoringDescription;

	/**
	 * 评价基本信息表主键id
	 */
	@ApiModelProperty(value = "评价基本信息表主键id")
	private Long appriseBaseinfoId;

	/**
	 * 评价指标分类 1年度 2季度
	 */
	@ApiModelProperty(value = "评价指标分类")
	private String evaluationType;

	/**
	 * 年度、季度评价主键id
	 */
	@ApiModelProperty(value = "年度、季度评价主键id")
	private Long evaluationId;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;

	/**
	 * 评价人姓名
	 */
	@ApiModelProperty(value = "评价人姓名")
	private String createUserName;

	/**
	 * 评价单位名称
	 */
	@ApiModelProperty(value = "评价单位名称")
	private String createDeptName;

	/**
	 * 单位评分类型：1 百分制得分 2 计算后得分
	 */
	@ApiModelProperty(value = "单位评分类型")
	private String type;

	/**
	 * 区县的权重
	 */
	@TableField(exist = false)
	private String qxWeight;
}
