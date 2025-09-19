package com.vingsoft.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
* @Description:    牵头单位上报阶段计划表
* @Author:         WangRJ
* @CreateDate:     2022/4/18 11:41
* @Version:        1.0
*/
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "阶段计划上报", description = "阶段计划上报")
public class SupervisionUpPlan extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 事项id
	 */
	private Long servId;
	/**
	 * 事项名称
	 */
	private String servName;
	/**
	 * 上报单位
	 */
	private Long upDept;
	/**
	 * 单位联系人
	 */
	private String upUser;
	/**
	 * 联系电话
	 */
	private String upPhone;
	/**
	 * 阶段id
	 */
	private Long planId;
	/**
	 * 阶段名称
	 */
	private String planName;
	/**
	 * 汇报时间
	 */
	private Date planTime;
	/**
	 * 阶段计划内容
	 */
	private String context;
	/**
	 * 审核意见
	 */
	private String auditOpinion;

	/**
	 * 上报单位名称
	 */
	private String upDeptName;
}
