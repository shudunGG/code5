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
package org.springblade.plugin.data.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.entity.Datasource;

import java.util.List;

/**
 * 数据源配置表 服务类
 *
 * @author Chill
 */
public interface IDatasourceService extends BaseService<Datasource> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.Datasource>
	 * @Author MaQY
	 * @Description 获取业务模型管理所需的下拉列表
	 * @Date 上午 11:43 2021/10/27 0027
	 * @Param []
	 **/
	List<Datasource> businessModelList();
}
