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
package org.springblade.system.user.feign;


import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.entity.UserInfo;
import org.springblade.system.user.entity.UserOauth;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * User Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_USER_NAME
)
public interface IUserClient {

	String API_PREFIX = "/client";
	String USER_INFO = API_PREFIX + "/user-info";
	String USER_INFO_BY_TYPE = API_PREFIX + "/user-info-by-type";
	String USER_INFO_BY_ID = API_PREFIX + "/user-info-by-id";
	String USER_INFO_BY_ACCOUNT = API_PREFIX + "/user-info-by-account";
	String USER_AUTH_INFO = API_PREFIX + "/user-auth-info";
	String SAVE_USER = API_PREFIX + "/save-user";
	String REMOVE_USER = API_PREFIX + "/remove-user";
	String USER_INFO_BY_POST = API_PREFIX + "/user-info-by-post";
	String USER_IDS = API_PREFIX + "/user-ids";
	String USER_LIST_ROLE_ID = API_PREFIX + "/user-list-role-id";
	String USER_LIST_DEPT_ID = API_PREFIX + "/user-list-dept-id";
	String USER_LOGIN_COUNT = API_PREFIX + "/user-login-count";
	String USER_LIST_BY_NAMES = API_PREFIX + "/user-list-by-names";

	/**
	 * 更新用户登录次数
	 * @param account
	 * @return
	 */
	@GetMapping(USER_LOGIN_COUNT)
	R<Boolean> updateUserLoginCount(@RequestParam("account") String account);

	/**
	 * 获取用户信息
	 *
	 * @param userId 用户id
	 * @return
	 */
	@GetMapping(USER_INFO_BY_ID)
	R<User> userInfoById(@RequestParam("userId") Long userId);


	/**
	 * 根据账号获取用户信息
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 * @return
	 */
	@GetMapping(USER_INFO_BY_ACCOUNT)
	R<User> userByAccount(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return
	 */
	@GetMapping(USER_INFO)
	R<UserInfo> userInfo(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @param userType 用户平台
	 * @return
	 */
	@GetMapping(USER_INFO_BY_TYPE)
	R<UserInfo> userInfo(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account, @RequestParam("userType") String userType);

	/**
	 * 获取第三方平台信息
	 *
	 * @param userOauth 第三方授权用户信息
	 * @return UserInfo
	 */
	@PostMapping(USER_AUTH_INFO)
	R<UserInfo> userAuthInfo(@RequestBody UserOauth userOauth);

	/**
	 * 新建用户
	 *
	 * @param user 用户实体
	 * @return
	 */
	@PostMapping(SAVE_USER)
	R<Boolean> saveUser(@RequestBody User user);

	/**
	 * 删除用户
	 *
	 * @param tenantIds 租户id集合
	 * @return
	 */
	@PostMapping(REMOVE_USER)
	R<Boolean> removeUser(@RequestParam("tenantIds") String tenantIds);

	/**
	 * 获取用户领导
	 *
	 * @param deptId 部门id
	 * @param postId 岗位id
	 * @return
	 */
	@PostMapping(USER_INFO_BY_POST)
	R<List<User>> getUserLeader(@RequestParam("deptId") String deptId, @RequestParam("postId") String postId);

	/**
	 * 获取用户ids
	 *
	 * @param userNames 用户名
	 * @return 用户ids
	 */
	@GetMapping(USER_IDS)
	R<String> getUserIds(@RequestParam("userNames") String userNames);

	/**
	 * 获取四大班子用户
	 *
	 * @param roleId 角色id
	 * @return 用户list
	 */
	@GetMapping(USER_LIST_ROLE_ID)
	R<List<User>> getUserListByRoleId(@RequestParam("roleId") String roleId);

	/**
	 * 获取单位分管领导
	 *
	 * @param deptId 单位id
	 * @return 用户list
	 */
	@GetMapping(USER_LIST_DEPT_ID)
	R<List<User>> getUserListByDeptId(@RequestParam("deptId") String deptId);

	@GetMapping(USER_LIST_BY_NAMES)
	R<List<String>> getPhones(@RequestParam("names") List<String> names);
}
