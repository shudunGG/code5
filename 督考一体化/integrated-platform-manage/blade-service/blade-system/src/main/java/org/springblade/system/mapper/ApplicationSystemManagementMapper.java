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
package org.springblade.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springblade.system.entity.ApplicationSystemManagement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


/**
 * 应用系统管理表 Mapper 接口
 *
 * @author BladeX
 * @since 2021-10-25
 */
public interface ApplicationSystemManagementMapper extends BaseMapper<ApplicationSystemManagement> {
	/**
	 * 变更禁用启用状态
	 **/
	@Update("update applcation_system_management set enable_status = #{management.enableStatus} where id = #{management.id}")
	int switchStatus(@Param("management") ApplicationSystemManagement applicationSystemManagement);
}
