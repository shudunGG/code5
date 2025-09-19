package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author mrtang
 * @version 1.0
 * @description: 事项部门得分信息表
 * @date 2022/4/16 17:55
 */
@Data
@TableName("supervision_score")
public class SupervisionScore extends BaseEntity implements Serializable {

	/**
	 * 事项编号
	 */
	private String servCode;
	/**
	 * 部门ID
	 */
	private Long deptId;
	/**
	 * 得分类型
	 */
	private String scoreType;

	/**
	 * 得分
	 */
	private BigDecimal score;

	/**
	 * 得分明细
	 */
	private String details;

	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;

	@TableField(exist = false)
	private String superYear;

}
