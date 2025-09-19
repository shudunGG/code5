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
 * @description: 指标申请信息
 * @date 2022-04-21 11:57
 */
@Data
@TableName("apply_information")
@ApiModel(value = "apply_information对象", description = "指标申请信息表")
public class ApplyInformation extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *年度/季度指标类型：1年度 2季度
	 */
	@ApiModelProperty(value = "指标类型")
	private String evaluationType;

	/**
	 *年度/季度指标id
	 */
	@ApiModelProperty(value = "指标id")
	private Long evaluationId;

	/**
	 *申请标题
	 */
	@ApiModelProperty(value = "申请标题")
	private String title;

	/**
	 *申请类型
	 */
	@ApiModelProperty(value = "申请类型")
	private String applyType;

	/**
	 *接收人id,多个逗号隔开
	 */
	@ApiModelProperty(value = "接收人id")
	private String receiveId;

	/**
	 *接收人名称，多个逗号隔开
	 */
	@ApiModelProperty(value = "接收人名称")
	private String receiveName;


	/**
	 *申请内容
	 */
	@ApiModelProperty(value = "申请内容")
	private String applyContent;

	/**
	 *备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	/**
	 * 附件信息
	 */
	@TableField(exist = false)
	private List<AppriseFiles> appriseFilesList;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;

}
