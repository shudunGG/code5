package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * supervision_evaluate
 *
 * @author
 */
@Data
@TableName("supervision_evaluate")
public class SupervisionEvaluate extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 6068337054997335504L;

	/**
	 * 评价分类
	 */
	private String appraiseClassify;

	/**
	 * 评价分类名称
	 */
	private String appraiseClassifyName;

	/**
	 * 事项编号
	 */
	private String servCode;

	/**
	 * 评价人id
	 */
	private String evaluateUser;

	/**
	 * 评价人名称
	 */
	private String evaluateUserName;

	/**
	 * 被评价单位id
	 */
	private String evaluatedDept;

	/**
	 * 被评价单位名称
	 */
	private String evaluatedDeptName;

	/**
	 * 评价结果
	 */
	private String result;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 得分
	 */
	private Double score;

	/**
	 * 评价类型
	 */
	private String evaluateType;

	/**
	 * 评价单位
	 */
	private String appraiseDeptname;

	/**
	 * 评价单位ID
	 */
	private Long appraiseDeptid;

	/**
	 * 评价时间
	 */
	private Date evaluateTime;

	@TableField(exist = false)
	private Integer status;

	//年份
	@TableField(exist = false)
	private String SupervisionYear;

	//排序
	@TableField(exist = false)
	private Integer sCount = 0;

	/**
	 * 是否超时
	 */
	@TableField(exist = false)
	private Long isOverTime;

}
