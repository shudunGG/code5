/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import org.springblade.system.user.mybatis.SM4EncryptHandler;

import java.util.Date;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("blade_user")
@EqualsAndHashCode(callSuper = true)
public class User extends TenantEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户编号
	 */
	private String code;
	/**
	 * OA用户编号
	 */
	private String oaUserCode;
	/**
	 * 用户平台
	 */
	private Integer userType;
	/**
	 * 账号
	 */
	private String account;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 昵称
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String name;
	/**
	 * 真名
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String realName;
	/**
	 * 头像
	 */
	private String avatar;
	/**
	 * 邮箱
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String email;
	/**
	 * 手机
	 */
	@TableField(typeHandler = SM4EncryptHandler.class)
	private String phone;
	/**
	 * 生日
	 */
	private Date birthday;
	/**
	 * 性别
	 */
	private Integer sex;
	/**
	 * 角色id
	 */
	private String roleId;
	/**
	 * 部门id
	 */
	private String deptId;
	/**
	 * 岗位id
	 */
	private String postId;

	/**
	 * 管理部门
	 */
	private String manageDept;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 登录次数
	 */
	private Integer loginCount;

	/**
	 * 最后一次登录时间
	 */
	private Date lastLoginTime;
}
