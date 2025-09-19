package com.vingsoft.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-15 14:59:13
 */
@Data
@ApiModel(value = "部门得分VO", description = "部门得分VO")
public class SupervisionDeptScoretVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String servCode;

	private String servName;

    private Long deptId;

    private  String deptName;

    private String score;

	private BigDecimal sort;

    private String details;

}
