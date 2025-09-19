/**
 * 考核评价-加减分纪实-减分项 对应的减分表
 *
 * @Author JG🧸
 * @Create 2022/4/8 11:25
 */
package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("score_minus")
@ApiModel(value = "score_minus对象", description = "考核评价-加减分纪实-减分项功能对应的减分表")
public class ScoreMinus extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 部门名称
	 */
	@ApiModelProperty(value = "部门名称")
	private String deptName;
	/**
	 * 部门id
	 */
	@ApiModelProperty(value = "部门id")
	private String deptId;

	/**
	 * 评价领导
	 */
	@ApiModelProperty(value = "评价领导")
	private String appriseLeader;

	/**
	 * 评价领导id
	 */
	@ApiModelProperty(value = "评价领导id")
	private String appriseLeaderId;

	/**
	 * 考核方式
	 */
	@ApiModelProperty(value = "考核方式")
	private String checkWay;

	/**
	 * 扣分项目
	 */
	@ApiModelProperty(value = "扣分项目")
	private String minusProject;
	/**
	 * 扣分依据
	 */
	@ApiModelProperty(value = "扣分依据")
	private String minusLaw;
	/**
	 * 分值
	 */
	@ApiModelProperty(value = "分值")
	private String score;

	/**
	 * 是否通过 0未审核  1通过 2不通过 3暂无权限
	 */
	@ApiModelProperty(value = "分值")
	private String isok;

	/**
	 * 考核评价文件信息
	 */
	@TableField(exist = false)
	List<AppriseFiles> appriseFilesList;

	/**
	 * 业务ID
	 */
	@TableField(exist = false)
	private String businessId;


	/**
	 * 开始时间
	 */
	@TableField(exist = false)
	private Date startTime;

	/**
	 * 截止时间
	 */
	@TableField(exist = false)
	private Date endTime;


	/**
	 *送审标题
	 */
	@TableField(exist = false)
	private String title;

	/**
	 *送审用户id
	 */
	@TableField(exist = false)
	private String userIds;

	/**
	 *是否异步
	 */
	@TableField(exist = false)
	private String sync;

	/**
	 * 审核表id
	 */
	@TableField(exist = false)
	private String supSubAuditId;

	/**
	 * 审核意见
	 */
	@TableField(exist = false)
	private String appraiseOpinion;

	/**
	 * 是否发布 默认0未发布，1已发布
	 */
	@ApiModelProperty(value = "是否发布 ")
	private Integer isSend;

	/**
	 * 查询年份
	 */
	@TableField(exist = false)
	private String searchYear;

}
