package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

/**
 * @className: SingleCheckAppraise
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 11:10 星期二
 * @Version 1.0
 **/
@Data
@TableName("single_check_appraise")
@ApiModel(value = "single_check_appraise对象", description = "单项考核排名评价表")
public class SingleCheckAppraise extends BaseEntity implements Serializable {

	@ApiModelProperty(value = "主键")
	private Long id;

	@ApiModelProperty(value = "单项考核排名指标表id")
	private Long targetId;

	@ApiModelProperty(value = "指标名称")
	private String targetName;

	@ApiModelProperty(value = "评价单位id")
	private String appraiseDeptId;

	@ApiModelProperty(value = "评价单位名称")
	private String appraiseDeptName;

	@ApiModelProperty(value = "评价对象id")
	private String appraiseObjectId;

	@ApiModelProperty(value = "评价对象名称")
	private String appraiseObjectName;

	@ApiModelProperty(value = "分数")
	private String score;

	@ApiModelProperty(value = "年度")
	private String year;

	@ApiModelProperty(value = "季度")
	private String quarter;

	@ApiModelProperty(value = "指标区域类型 1市直部门 2县区")
	private String areaType;

	@ApiModelProperty(value = "是否发布 1是 0否")
	private Integer isSend;
}
