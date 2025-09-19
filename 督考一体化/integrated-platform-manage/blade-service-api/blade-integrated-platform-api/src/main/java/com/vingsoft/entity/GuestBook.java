package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

/**
 * @author mrtang
 * @version 1.0
 * @description: 意见反馈——留言簿
 * @date 2022/4/29 19:35
 */
@Data
@TableName("guest_book")
public class GuestBook extends BaseEntity implements Serializable {

	/**
	 * 意见表主键
	 */
	private Long feedbackId;

	/**
	 * 留言内容
	 */
	private String content;

	/**
	 * 相关附件
	 */
	private String files;
}
