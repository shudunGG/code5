package com.vingsoft.vo;

import com.vingsoft.entity.UnifyMessage;
import lombok.Data;

import java.util.Date;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/24 21:49
 */
@Data
public class SupervisionFollowVO {

	private String followUser;

	private Long followUserId;

	private Long servId;

	private String servCode;

	private String servName;

	private Date createTime;

	/**
	 * 事项一级分类
	 */
	private String servTypeOne;

	/**
	 * 事项二级分类
	 */
	private String servTypeTwo;



}
