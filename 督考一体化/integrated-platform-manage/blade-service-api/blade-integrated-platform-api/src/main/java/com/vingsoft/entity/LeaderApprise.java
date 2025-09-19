package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vingsoft.vo.LeaderAppriseScoreVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-08 20:15
 */
@Data
@TableName("leader_apprise")
@ApiModel(value = "leader_apprise对象", description = "领导评价信息表")
public class LeaderApprise extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

    /**
     * 单位名称
     */
	@ApiModelProperty(value = "单位名称")
	private String deptName;

    /**
     * 单位id
     */
	@ApiModelProperty(value = "单位id")
    private String deptId;


    /**
     * 满意度:满意1，比较满意2，基本满意3，不满意4
     */
	@ApiModelProperty(value = "满意度")
    private String satisfaction;

    /**
     * 得分
     */
	@ApiModelProperty(value = "得分")
    private String score;

	/**
	 * 得分占比
	 */
	@ApiModelProperty(value = "得分占比")
	private String scorePart;

    /**
     * 打分角色名称
     */
	@ApiModelProperty(value = "打分角色名称")
    private String appriseRolename;

    /**
     * 评价内容
     */
	@ApiModelProperty(value = "评价内容")
    private String appriseContent;

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
     * 评价时间
     */
	@ApiModelProperty(value = "评价时间")
    private Date appriseDate;

	/**
	 *年度/季度指标类型：1年度 2季度
	 */
	@ApiModelProperty(value = "指标类型")
	private String evaluationType;

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
	 * 多个单位id
	 */
	@TableField(exist = false)
	private String deptIds;

	/**
	 * 所属年份
	 */
	private String appriseYear;

	/**
	 * 所属季度
	 */
	private String appriseQuarter;

	/**
	 * 是否能修改 Y ：能 ，N：不能
	 */
	@TableField(exist = false)
	private String isUpt;

	/**
	 * 分管部门排序
	 */
	@TableField(exist = false)
	private String sort;

	/**
	 * 领导评价平均分
	 */
	@TableField(exist = false)
	private String avgScore;


	/**
	 * 是否发布 默认0未发布 1已发布
	 */
	@ApiModelProperty(value = "是否发布")
	private String isSend;

}
