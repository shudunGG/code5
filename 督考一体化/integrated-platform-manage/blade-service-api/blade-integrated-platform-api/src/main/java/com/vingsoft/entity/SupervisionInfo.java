package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * supervision_info
 * @author
 */
@Data
@TableName("supervision_info")
public class SupervisionInfo extends BaseEntity implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

    /**
     * 事项名称
     */
    private String servName;

    /**
     * 事项一级分类
     */
    private String servTypeOne;

    /**
     * 事项二级分类
     */
    private String servTypeTwo;

    /**
     * 事项三级分类
     */
    private String servTypeThree;

    /**
     * 事项四级分类
     */
    private String servTypeFour;

    /**
     * 完成时限
     */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date wcsx;

    /**
     * 上报计划
     */
    private String isPushPlan;

    /**
     * 事项状态
     */
    private String servStatus;

	/**
	 * 相关要求
	 */
	private String requirement;

	/**
	 * 责任领导
	 */
	private String dutyLeader;

	/**
	 * 牵头单位
	 */
	private String leadUnit;

	/**
	 * 责任单位
	 */
	private String dutyUnit;

	/**
	 * 督办人
	 */
	private String supervisor;

	/**
	 * 评价人
	 */
	private String evaluator;

    /**
     * 更新部门
     */
    private String updateDept;

	/**
	 * 流程状态
	 */
	private  String flowStatus;

	/**
	 * 责任领导名称
	 */
	private String dutyLeaderName;

	/**
	 * 牵头单位名称
	 */
	private String leadUnitName;

	/**
	 * 责任单位名称
	 */
	private String dutyUnitName;

	/**
	 * 督办人名称
	 */
	private String supervisorName;

	/**
	 * 评价人名称
	 */
	private String evaluatorName;

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
	private Integer status;

	/**
	 * 其他分类
	 */
	private String otherType;

	/**
	 * 下发事项ID
	 */
	private Long issue_serv_id;

	/**
	 * 市委市政府(1市委2市政府)
	 */
	private Integer swszf;

	/**
	 * 阶段信息
	 */
	@TableField(exist = false)
	private List<SupervisionPhasePlan> supervisionPhasePlanList;

	/**
	 * 事项附件
	 */
	@TableField(exist = false)
	private List<SupervisionFiles> supervisionFilesList;

	/**
	 * 部门签收情况表
	 */
	@TableField(exist = false)
	private SupervisionSign supervisionSign;

	/**
	 * 上报计划
	 */
	@TableField(exist = false)
	private SupervisionUpPlan supervisionUpPlan;

	/**
	 * 阶段汇报
	 */
	@TableField(exist = false)
	private SupervisionPhaseReport supervisionPhaseReport;

	/**
	 * 阶段信息
	 */
	@TableField(exist = false)
	private SupervisionPhasePlan supervisionPhasePlan;

	/**
	 * 关注对象
	 */
	@TableField(exist = false)
	private FollowInformation followInformation;

	/**
	 * 责任单位签收情况
	 */
	@TableField(exist = false)
	private Map<Long, Integer> dutyMap;

	/**
	 * 牵头单位签收情况
	 */
	@TableField(exist = false)
	private Map<Long, Integer> leadMap;
}
