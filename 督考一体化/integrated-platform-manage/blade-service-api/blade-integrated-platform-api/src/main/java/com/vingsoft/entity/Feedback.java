package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 意见反馈
 * @date 2022/4/29 14:50
 */
@Data
@TableName("feedback")
public class Feedback extends BaseEntity implements Serializable {

	/**
	 * 反馈部门
	 */
	private Long dept;

	/**
	 * 反馈内容
	 */
	private String content;

	/**
	 * 相关附件
	 */
	private String files;
}
