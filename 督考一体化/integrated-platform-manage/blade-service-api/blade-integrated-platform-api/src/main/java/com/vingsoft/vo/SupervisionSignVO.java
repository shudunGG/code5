package com.vingsoft.vo;

import com.vingsoft.entity.SupervisionSign;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/18 10:39
 */
@Data
public class SupervisionSignVO extends SupervisionSign {
	/**
	 * 签收单位
	 */
	private String signDeptName;
	/**
	 * 签收人
	 */
	private String signUserName;
	/**
	 * 办结单位
	 */
	private String overDeptName;
	/**
	 * 办结人
	 */
	private String overUserName;
	/**
	 * 事项编号
	 */
	private String servCode;

	/**
	 * 事项名称
	 */
	private String servName;
}
