package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-25 09:36
 */

@Data
@ApiModel(value = "关注信息对象", description = "关注信息对象")
public class SupervisionEvaluateVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 单位id
	 */
	private String deptId;

	/**
	 * 单位名称
	 */
	private String deptName;

	/**
	 * 承办总数
	 */
	private String cbnum;

	/**
	 * 已完成
	 */
	private String ywcnum;

	/**
	 * 正常推进
	 */
	private String zctjnum;

	/**
	 * 已超期
	 */
	private String ycqnum;

	/**
	 * 分数
	 */
	private Double score;

	/**
	 * 排名
	 */
	private Integer ranking;

	/**
	 * 部门排序
	 */
	private String sort;

}
