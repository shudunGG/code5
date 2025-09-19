package org.springblade.plugin.data.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * business_model实体类
 *
 * @author
 */
@Data
@TableName("business_model")
@ApiModel(value = "BusinessModel对象", description = "业务模型管理表")
public class BusinessModel extends BaseEntity {
	/**
	 * 模型名称
	 */
	@ApiModelProperty(value = "模型名称")
	private String modelName;
	/**
	 * 模型代码
	 */
	@ApiModelProperty(value = "模型代码")
	private String modelCode;
	/**
	 * 统计类型
	 */
	@ApiModelProperty(value = "统计类型")
	private String statisticalType;
	/**
	 * 数据源
	 */
	@ApiModelProperty(value = "数据源")
	private String datasourceId;
	/**
	 * 数据表
	 */
	@ApiModelProperty(value = "数据表")
	private String dataTables;
	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述")
	private String remark;
	/**
	 * 关联表数
	 */
	@ApiModelProperty(value = "关联表数")
	private Integer associatedNumber;
}
