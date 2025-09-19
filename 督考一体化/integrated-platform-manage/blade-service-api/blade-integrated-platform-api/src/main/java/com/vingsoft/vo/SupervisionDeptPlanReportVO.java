package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionPhasePlan;
import com.vingsoft.entity.SupervisionPhaseReport;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-13 16:35:23
 */
@Data
@ApiModel(value = "事项部门阶段汇报信息", description = "事项部门阶段汇报信息")
public class SupervisionDeptPlanReportVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	private String deptId;

	private String deptName;

	private String deptType;

	private String servCode;

	private List<SupervisionPhasePlan> supervisionPhasePlanList=new ArrayList<>();

	private SupervisionEvaluate supervisionEvaluate;



}
