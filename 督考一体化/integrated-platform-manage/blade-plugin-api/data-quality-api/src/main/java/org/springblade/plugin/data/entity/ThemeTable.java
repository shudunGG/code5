package org.springblade.plugin.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Objects;

/**
 * theme_table实体类
 *
 * @author
 */
@Data
@ApiModel(value = "主题信息表数据", description = "theme_table实体类")
@TableName("theme_table")
public class ThemeTable extends BaseEntity {
	private static final long serialVersionUID = 7807493482327759721L;
	/**
	 * 表名
	 */
	@ApiModelProperty(value = "表名")
	private String name;
	/**
	 * 主题表名称
	 */
	@ApiModelProperty(value = "主题表名称")
	private String title;
	/**
	 * 标题
	 */
	@ApiModelProperty(value = "标题")
	private String remarks;
	/**
	 * 业务模型管理表主键
	 */
	@ApiModelProperty(value = "业务模型管理表主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long modelId;
	/**
	 * 质检时间字段
	 */
	@ApiModelProperty(value = "质检时间字段")
	private String qualityTestingTimeField;
}
