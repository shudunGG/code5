package org.springblade.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 角色子系统
 * @date 2022/5/1 10:47
 */
@Data
@TableName("blade_role_subsystem")
public class RoleSubSystem implements Serializable {
	@JsonSerialize(
		using = ToStringSerializer.class
	)
	@ApiModelProperty("主键")
	@TableId(
		value = "id",
		type = IdType.ASSIGN_ID
	)
	private Long id;
	@JsonSerialize(
		using = ToStringSerializer.class
	)
	@ApiModelProperty("子系统id")
	private Long subsystemId;
	@JsonSerialize(
		using = ToStringSerializer.class
	)
	@ApiModelProperty("角色id")
	private Long roleId;
}
