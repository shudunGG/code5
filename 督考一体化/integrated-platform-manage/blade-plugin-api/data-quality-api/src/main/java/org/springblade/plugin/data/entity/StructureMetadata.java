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
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 3:46 2021/10/27 0027
 * @ Description：表结构
 */
@Data
@ApiModel(value = "主题信息表表结构元数据", description = "structure_metadata实体类")
@TableName("structure_metadata")
public class StructureMetadata extends BaseEntity {
	private static final long serialVersionUID = -2823326190500621185L;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 字段名
	 * @Date 上午 9:36 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "字段名")
	private String name;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 类型
	 * @Date 上午 9:36 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "类型")
	private String type;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 字段长度
	 * @Date 上午 9:36 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "字段长度")
	private Integer dataSize;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 小数位数
	 * @Date 上午 9:35 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "小数位数")
	private Integer digits;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 1可为空0不可为空
	 * @Date 上午 9:35 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "是否可以为空1可为空0不可为空")
	private Integer nullable;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 默认值
	 * @Date 上午 9:34 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "默认值")
	private String defaultValue;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 1唯一0不唯一
	 * @Date 上午 9:33 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "是否唯一1唯一0不唯一")
	private Integer ifUnique;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 注释
	 * @Date 上午 9:33 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "注释")
	private String remark;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 中文名称 （还是注释）
	 * @Date 上午 9:34 2021/10/28 0028
	 * @Param
	 **/
	@ApiModelProperty(value = "中文名称")
	private String chName;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 主题信息表主键
	 * @Date 上午 9:26 2021/10/29 0029
	 * @Param
	 **/
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "主题信息表主键")
	private Long themeId;


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
