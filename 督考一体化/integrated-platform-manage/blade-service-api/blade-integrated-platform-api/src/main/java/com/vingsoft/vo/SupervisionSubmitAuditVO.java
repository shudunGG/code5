package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.vingsoft.entity.SupervisionSubmitAudit;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 立项送审
 * @date 2022/4/18 17:11
 */
@Data
public class SupervisionSubmitAuditVO extends SupervisionSubmitAudit {
	/**
	 * 事项编号
	 */
	private String servCode;

	/**
	 * 事项名称
	 */
	private String servName;

	/**
	 * 事项一级分类
	 */
	private String servTypeOne;

	/**
	 * 事项二级分类
	 */
	private String servTypeTwo;

	/**
	 * 事项三级分类
	 */
	private String servTypeThree;

	/**
	 * 事项四级分类
	 */
	private String servTypeFour;

	/**
	 * 审批人
	 */
	private String approvalUserName;
}
