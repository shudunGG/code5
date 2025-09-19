package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.LeaderApprise;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:领导评价
 * @date 2022-05-14 18:22
 */
@Data
@ApiModel(value = "LeaderAppriseScoreVO对象", description = "LeaderAppriseScoreVO对象")
public class LeaderAppriseScoreVO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 部门id
	 */
	private String deptId;

	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 部门排序
	 */
	private String sort;

	/**
	 * 考核分组字典值
	 */
	private String dictKey;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList;


	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList1;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList2;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList3;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList4;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList5;

	@TableField(exist = false)
	List<LeaderApprise> leaderAppriseList6;

}
