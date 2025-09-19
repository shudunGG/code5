package org.springblade.plugin.data.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * function_parameter实体类
 *
 * @author
 */
@Data
@TableName("function_parameter")
@ApiModel(value = "function_parameter实体类", description = "函数参数数据表实体类")
public class FunctionParameter extends BaseEntity {
	private static final long serialVersionUID = -2713120138353772365L;
	/**
	 * 参数名称（必须以fP_x格式命名）
	 */
	@ApiModelProperty(value = "参数名称")
	private String parameterName;
	/**
	 * 参数含义
	 */
	@ApiModelProperty(value = "参数含义")
	private String parameterMeaning;
	/**
	 * 是否限制参数值1限制0不限制
	 */
	@ApiModelProperty(value = "是否限制参数值1限制0不限制")
	private Integer ifLimitValue;
	/**
	 * 选值范围，必须以英文逗号分隔
	 */
	@ApiModelProperty(value = "选值范围")
	private String selectValueRange;
	/**
	 * 所属函数的检验名称
	 */
	@ApiModelProperty(value = "所属函数的检验名称")
	private String functionName;
	/**
	 * 参数序号
	 */
	@ApiModelProperty(value = "参数序号")
	private Integer parameterSort;

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
