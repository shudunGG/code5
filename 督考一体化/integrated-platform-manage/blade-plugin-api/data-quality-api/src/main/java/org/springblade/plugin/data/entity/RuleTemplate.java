package org.springblade.plugin.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;


/**
 * rule_template实体类
 *
 * @author
 */
@Data
@TableName("rule_template")
@ApiModel(value = "rule_template实体类", description = "规则模板表实体类")
public class RuleTemplate extends BaseEntity {
	private static final long serialVersionUID = -1874198372561891699L;
	/**
	 * 规则类型
	 */
	@ApiModelProperty(value = "规则类型")
	private String type;
	/**
	 * 规则代码
	 */
	@ApiModelProperty(value = "规则代码")
	private String code;
	/**
	 * 问题级别
	 */
	@ApiModelProperty(value = "问题级别")
	private Integer problemLevel;
	/**
	 * 权重
	 */
	@ApiModelProperty(value = "权重")
	private Integer weight;
	/**
	 * 联合不为空
	 */
	@ApiModelProperty(value = "联合不为空")
	private String unionNotNull;
	/**
	 * 检查字段
	 */
	@ApiModelProperty(value = "检查字段")
	private String checkField;
	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述")
	@TableField("`describe`")
	private String describe;
	/**
	 * 状态1启用0禁用
	 */
	@ApiModelProperty(value = "状态1启用0禁用")
	private Integer enableStatus;
	/**
	 * 取值范围（最小值）
	 */
	@ApiModelProperty(value = "取值范围（最小值）")
	private String minValue;
	/**
	 * 取值范围（最大值）
	 */
	@ApiModelProperty(value = "取值范围（最大值）")
	private String maxValue;
	/**
	 * 检查字段类型
	 */
	@ApiModelProperty(value = "检查字段类型")
	private String checkType;
	/**
	 * 检查公式
	 */
	@ApiModelProperty(value = "检查公式")
	private String checkFormula;
	/**
	 * 组合重复
	 */
	@ApiModelProperty(value = "组合重复1是0否")
	private String combineRepeat;
	/**
	 * 允许误差最大天数（默认0）
	 */
	@ApiModelProperty(value = "允许误差最大天数（默认0）")
	private Integer maxAllowableErrorDays;
	/**
	 * 规范类型
	 */
	@ApiModelProperty(value = "规范类型")
	private String specificationType;
	/**
	 * 规范子类型
	 */
	@ApiModelProperty(value = "规范子类型")
	private String specificationSubtype;
	/**
	 * 规范类型为长度时的最小长度
	 */
	@ApiModelProperty(value = "规范类型为长度时的最小长度")
	private Integer minLength;
	/**
	 * 规范类型为长度时的最大长度
	 */
	@ApiModelProperty(value = "规范类型为长度时的最大长度")
	private Integer maxLength;
	/**
	 * 规范类型为包含字符串时的字符串
	 */
	@ApiModelProperty(value = "规范类型为包含字符串时的字符串")
	private String containString;
	/**
	 * 规范类型为包含字符串时的字符串位置，prefix,suffix,any,specific
	 */
	@ApiModelProperty(value = "规范类型为包含字符串时的字符串位置，prefix,suffix,any,specific")
	private String charPosition;
	/**
	 * 当包含字符串子规范是特定位置时，表示位置
	 */
	@ApiModelProperty(value = "当包含字符串子规范是特定位置时，表示位置")
	private Integer specificPosition;
	/**
	 * 正则表达式，当规范类型是正则表达式时
	 */
	@ApiModelProperty(value = "正则表达式，当规范类型是正则表达式时")
	private String regex;
	/**
	 * 引用方式
	 */
	@ApiModelProperty(value = "引用方式1字典值2表字段")
	private String referenceMode;
	/**
	 * 当引用方式为字典的时候存放业务字典ID
	 */
	@ApiModelProperty(value = "当引用方式为字典的时候存放业务字典ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long dicBizId;
	/**
	 * 当引用方式为表字段时存放数据源ID
	 */
	@ApiModelProperty(value = "当引用方式为表字段时存放数据源ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long datasourceId;
	/**
	 * 当引用方式为表字段时存放数据源名称
	 */
	@ApiModelProperty(value = "当引用方式为表字段时存放数据源名称")
	private String datasourceName;
	/**
	 * 当引用方式是表字段的时候，存放表名
	 */
	@ApiModelProperty(value = "当引用方式是表字段的时候，存放表名")
	@JsonSerialize(using = ToStringSerializer.class)
	private String tableName;
	/**
	 * 当引用方式是表字段时存放字段名
	 */
	@ApiModelProperty(value = "当引用方式是表字段时存放字段名")
	@JsonSerialize(using = ToStringSerializer.class)
	private String fieldName;
	/**
	 * 主题表ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "主题信息表主键")
	private Long themeId;

}
