package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

/**
 * @className: SingleCheckRank
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 10:57 星期二
 * @Version 1.0
 **/
@Data
@TableName("single_check_rank")
@ApiModel(value = "single_check_rank对象", description = "单项考核排名指标表")
public class SingleCheckRank extends BaseEntity implements Serializable {

	@ApiModelProperty(value = "主键")
	private Long id;

	@ApiModelProperty(value = "指标名称")
	private String targetName;

	@ApiModelProperty(value = "评价单位id集合")
	private String appraiseDeptIds;

	@ApiModelProperty(value = "评价单位名称集合")
	private String appraiseDeptNames;

	@ApiModelProperty(value = "年度")
	private String year;

	@ApiModelProperty(value = "季度")
	private String quarter;

	@ApiModelProperty(value = "指标区域类型 1市直部门 2县区")
	private String areaType;

	@TableField(exist = false)
	@ApiModelProperty(value = "已有的评价对象，逗号分隔")
	private String appraiseObjectNames;
}
