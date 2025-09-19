package com.vingsoft.vo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 领导关注和我的关注 视图实体类
 *
 * @Author JG🧸
 * @Create 2022/5/2 14:14
 */
@Data
@ApiModel(value = "LeaderAndMyFocusVO 对象", description = "LeaderAndMyFocusVO 对象")
public class LeaderAndMyFocusVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目名称 年度考评分类：1 政治思想建设、2 领导能力、3 党的建设、高质量发展（4 市直高质量发展、5 区县高质量发展）
	 */
	@ApiModelProperty(value = "项目名称")
	private String projectName;

	/**
	 * 考核分类
	 */
	@ApiModelProperty(value = "考核分类id")
	private String appraiseClassify;

	/**
	 * 考核分类名称
	 */
	@ApiModelProperty(value = "考核分类名称")
	private String appraiseClassifyName;

	/**
	 * 主要指标及评价要点
	 */
	@ApiModelProperty(value = "主要指标及评价要点")
	private String majorTarget;

	/**
	 * 考核对象id
	 */
	@ApiModelProperty(value = "考核对象id")
	private String appraiseObjectId;

	/**
	 * 考核对象
	 */
	@ApiModelProperty(value = "考核对象")
	private String appraiseObject;

	/**
	 * 评价单位名称
	 */
	@ApiModelProperty(value = "评价单位名称")
	private String appraiseDeptname;

	/**
	 * 评价单位id
	 */
	@ApiModelProperty(value = "评价单位id")
	private String appraiseDeptid;

	/**
	 * 指标状态：0暂存 1推进中 2已完成
	 */
	@ApiModelProperty(value = "指标状态")
	private String targetStatus;

	/**
	 * 完成时间
	 */
	@ApiModelProperty(value = "完成时间")
	private Date finishDate;

}
