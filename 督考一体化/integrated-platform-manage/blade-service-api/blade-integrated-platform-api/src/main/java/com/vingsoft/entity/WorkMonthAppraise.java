package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

/**
 * work_month_appraise
 * @Waston
 */
@Data
@TableName("work_month_appraise")
@EqualsAndHashCode(callSuper = true)
public class WorkMonthAppraise extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;

	/**
	 * 评价年月
	 */
	private String month;

	/**
	 * 事项编码
	 */
	private String servCode;

	/**
	 * 评价部门
	 */
	private String deptCode;

	/**
	 * 评价分数 优秀100分，良好90分，较好80分，一般70分
	 */
	private Integer appraiseScore;

	/**
	 * 创建时间
	 */
	private Date createDate;

	/**
	 * 说明
	 */
	private String appraiseExplain;

	/**
	 * work_month表主键
	 */
	private Long workMonthId;
}
