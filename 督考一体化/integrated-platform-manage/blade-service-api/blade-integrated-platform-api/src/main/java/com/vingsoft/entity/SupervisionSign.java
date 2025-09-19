package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mrtang
 * @version 1.0
 * @description: 事项签收办结
 * @date 2022/4/16 17:55
 */
@Data
@TableName("supervision_sign")
public class SupervisionSign extends BaseEntity implements Serializable {

	/**
	 * 签收单位
	 */
	private Long signDept;
	/**
	 * 签收状态，默认0未签收
	 */
	private Integer signStatus;
	/**
	 * 签收人
	 */
	private Long signUser;

	/**
	 * 签收时间
	 */
	private Date signTime;

	/**
	 * 办结单位
	 */
	private Long overDept;
	/**
	 * 办结状态，默认0未办结
	 */
	private Integer overStatus;
	/**
	 * 办结人
	 */
	private Long overUser;

	/**
	 * 办结时间
	 */
	private Date overTime;

	/**
	 * 单位类型：lead 牵头单位 duty 责任单位
	 */
	private String deptType;

	/**
	 * 事项id，外键
	 */
	private Long servId;

	/**
	 * 事项id，外键
	 */
	@TableField(exist = false)
	private Date wcsx;
}
