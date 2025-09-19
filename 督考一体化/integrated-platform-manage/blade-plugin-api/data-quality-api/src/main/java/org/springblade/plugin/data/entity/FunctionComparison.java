package org.springblade.plugin.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * function_comparison实体类
 *
 * @author
 */
@Data
@TableName("function_comparison")
@ApiModel(value = "function_comparison实体类", description = "函数对照表数据实体类")
public class FunctionComparison extends BaseEntity {
	private static final long serialVersionUID = -7759206334352896773L;
	/**
	 * 表达式（带有参数的表达式，参数名称必须以fP_x格式命名）
	 */
	@ApiModelProperty(value = "表达式")
	private String expression;
	/**
	 * 检验名称（添加唯一索引）
	 */
	@ApiModelProperty(value = "检验名称")
	private String inspectionName;
	/**
	 * 函数类别
	 */
	@ApiModelProperty(value = "函数类别")
	private String functionCategory;
	/**
	 * mysql数据库对应的函数表达式
	 */
	@ApiModelProperty(value = "mysql数据库对应的函数表达式")
	private String mysqlFunction;
	/**
	 * oracle数据库对应的函数表达式
	 */
	@ApiModelProperty(value = "oracle数据库对应的函数表达式")
	private String oracleFunction;
	/**
	 * 函数含义
	 */
	@ApiModelProperty(value = "函数含义")
	private String functionMeaning;
	/**
	 * 用法示例
	 */
	@ApiModelProperty(value = "用法示例")
	private String usageExample;
	/**
	 * 参数数量
	 */
	@ApiModelProperty(value = "参数数量")
	private Integer parameterCount;

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
