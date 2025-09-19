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
package org.springblade.system.user.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.excel.UserExcel;
import org.springblade.system.user.vo.UserTreeVO;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author Chill
 */
public interface UserMapper extends BaseMapper<User> {


	/**
	 * 更新登录次数
	 * @param account
	 * @return
	 */
	int updateUserLoginCount(@Param("account") String account);

	/**
	 * 自定义分页
	 *
	 * @param page
	 * @param user
	 * @param deptIdList
	 * @param tenantId
	 * @return
	 */
	List<User> selectUserPage(IPage<User> page, @Param("user") User user, @Param("deptIdList") List<Long> deptIdList, @Param("tenantId") String tenantId);

	/**
	 * 获取用户
	 *
	 * @param tenantId
	 * @param account
	 * @return
	 */
	User getUser(String tenantId, String account);

	/**
	 * 获取导出用户数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<UserExcel> exportUser(@Param("ew") Wrapper<User> queryWrapper);
	/**
	 * 根据用户所在部门获取树形结构
	 *
	 * @return
	 */
	@Select("select id,parent_id,id `account`,dept_name `name`,NULL `user_id`,'true' `hasChildren`\n" +
		"FROM blade_dept where is_deleted=0 and id in (select DISTINCT dept_id from blade_user where is_deleted=0 " +
		"union all select DISTINCT parent_id `dept_id` from blade_dept where is_deleted=0)\n" +
		"union ALL\n" +
		"select id `id`,dept_id `parent_id`,account,real_name `name`,id `user_id`,'false' `hasChildren`\n" +
		"FROM blade_user where is_deleted=0 ")
	List<UserTreeVO> getUserTree();

	/**
	 * 获取用户
	 *
	 * @param names
	 * @return
	 */
	List<String> getPhones(@Param("names") List<String> names);

}
