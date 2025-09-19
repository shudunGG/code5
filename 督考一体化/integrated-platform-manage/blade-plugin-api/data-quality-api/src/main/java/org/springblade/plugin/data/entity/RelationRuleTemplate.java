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
 * relation_rule_template实体类
 *
 * @author
 */
@Data
@TableName("relation_rule_template")
@ApiModel(value = "relation_rule_template实体类", description = "规则模板关联关系表实体类")
public class RelationRuleTemplate extends BaseEntity {
	private static final long serialVersionUID = -5209062452984743037L;
	/**
	 * 连接表表名
	 */
	@ApiModelProperty(value = "连接表表名")
	private String joinTableName;
	/**
	 * 连接表ID
	 */
	@ApiModelProperty(value = "连接表ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long joinTableId;
	/**
	 * 连接方式
	 */
	@ApiModelProperty(value = "连接方式")
	private String joinType;
	/**
	 * 连接字段（当前连接表字段）
	 */
	@ApiModelProperty(value = "连接字段")
	private String themeJoinField;
	/**
	 * 连接字段（检查页面主题表字段）
	 */
	@ApiModelProperty(value = "连接字段")
	private String checkJoinField;
	/**
	 * 连接字段名称（检查页面主题表字段）
	 */
	@ApiModelProperty(value = "连接字段名称")
	private String themeJoinFieldName;
	/**
	 * 规则管理表ID
	 */
	@ApiModelProperty(value = "规则管理表ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long ruleId;

	@TableField(exist = false)
	private Long createUser;
	@TableField(exist = false)
	private Date createTime;
	@TableField(exist = false)
	private Long updateUser;
	@TableField(exist = false)
	private Date updateTime;
	@TableField(exist = false)
	private Long createDept;
	@TableField(exist = false)
	private Integer status;
	@TableField(exist = false)
	private Integer isDeleted;
}
