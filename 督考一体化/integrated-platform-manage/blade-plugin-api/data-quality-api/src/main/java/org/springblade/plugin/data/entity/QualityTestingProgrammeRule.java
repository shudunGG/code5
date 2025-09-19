package org.springblade.plugin.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * @author MaQiuyun
 * @date 2021/12/16 14:46
 * @description: 质检方案和规则关联表
 */
@Data
@TableName("quality_testing_programme_rule")
@ApiModel(value = "quality_testing_programme_rule对象", description = "质检方案和规则关联表")
public class QualityTestingProgrammeRule extends BaseEntity {
	/**
	 * 质检方案表ID
	 */
	@ApiModelProperty(value = "质检方案表ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long qualityTestingProgrammeId;
	/**
	 * 规则管理表ID
	 */
	@ApiModelProperty(value = "规则管理表ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long manageRuleId;

	/**
	 * 去除父类属性
	 */
	@TableField(exist = false)
	private Long updateUser;
	@TableField(exist = false)
	private Date updateTime;
	@TableField(exist = false)
	private Integer status;
	@TableField(exist = false)
	private Integer isDeleted;
}
