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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.system.entity.TopMenu;
import org.springblade.system.vo.SubSystemVO;

import java.util.List;

/**
 * 顶部菜单表 Mapper 接口
 *
 * @author BladeX
 */
public interface TopMenuMapper extends BaseMapper<TopMenu> {

	/**
	 * 获取子系统列表
	 * @return
	 */
	List<SubSystemVO> getSubsystemList();

	/**
	 * 根据菜单名称获取顶部菜单对象
	 * @param menuName
	 * @return
	 */
	TopMenu findTopMenuByMenuName(String menuName);
}
