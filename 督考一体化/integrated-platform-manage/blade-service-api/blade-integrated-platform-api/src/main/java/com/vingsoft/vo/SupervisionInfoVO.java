package com.vingsoft.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vingsoft.entity.FollowInformation;
import com.vingsoft.entity.SupervisionInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-13 16:35:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "事项基本信息", description = "事项基本信息")
public class SupervisionInfoVO extends SupervisionInfo {


	private String signStatus;

	private String deptType;

	private Long signId;

	private Long planId;

	private String servType;

	private String upPlanStatus;

	private String planUpStatus;

	private String reporId;

	private Long reportStatus;

	private String followUser;

	private Long auditId;

	private String operationType;

	private String auditUserId;

	private int isEvaluate;

	private String reportDownUserName;

	private  String reportDownUserId;

	private  String reportDownStatus;

	private  String auditHB;

	private  Long auditJH;

	private  Long upPlanOtherNum;

	private  Long upPlanId;

	private  Long lateId;

	private int lateStatus;

	private int lateNum;

	private int planNum;
	/**
	 * 跨部门下发状态
	 */
	private String issueStatus;

	/**
	 * 跨部门下发汇报父ID
	 */
	private String parentId;

	/**
	 * 是否存在部门超期未汇报
	 */
	private String isOverdue;

}
