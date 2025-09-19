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
package org.springblade.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DesUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.system.entity.ApplicationSystemManagement;
import org.springblade.system.service.IApplicationSystemManagementService;
import org.springblade.system.vo.ApplicationSystemManagementVO;
import org.springblade.system.wrapper.ApplicationSystemManagementWrapper;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

/**
 * 应用系统管理表 控制器
 *
 * @author BladeX
 * @since 2021-10-25
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system-management")
@Api(value = "应用系统管理表", tags = "应用系统管理表接口")
public class ApplicationSystemManagementController extends BladeController {

	private final IApplicationSystemManagementService systemManagementService;
	//加解密秘钥，不可更改，否则数据库密码解不开
	private final static String DES_KEY = "tvUp0Pzs90ClqhGO";


	/**
	 * 分页 应用系统管理表
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "传入systemManagement")
	public R<IPage<ApplicationSystemManagementVO>> list(ApplicationSystemManagement applicationSystemManagement, Query query) {
		IPage<ApplicationSystemManagement> pages = systemManagementService.page(Condition.getPage(query), Condition.getQueryWrapper(applicationSystemManagement));
		return R.data(ApplicationSystemManagementWrapper.build().pageVO(pages));
	}

	/**
	 * 新增 应用系统管理表
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "传入systemManagement")
	public R save(@Valid @RequestBody ApplicationSystemManagement applicationSystemManagement) {
		//appSecret
		String appSecret = applicationSystemManagement.getAppKey() + new Date().getTime();
		applicationSystemManagement.setAppSecret(DesUtil.encryptToBase64(appSecret, DES_KEY));
		return R.status(systemManagementService.save(applicationSystemManagement));
	}

	/**
	 * 修改 应用系统管理表
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "传入systemManagement")
	public R update(@Valid @RequestBody ApplicationSystemManagement applicationSystemManagement) {
		//检查是否修改appKey
		ApplicationSystemManagement management = systemManagementService.getById(applicationSystemManagement.getId());
		if (Func.isEmpty(management)) {
			return R.fail("当前应用系统已被删除！");
		} else if (StringUtil.equals(management.getAppKey(), applicationSystemManagement.getAppKey(), false)) {
			applicationSystemManagement.setAppSecret(DesUtil.encryptToBase64(applicationSystemManagement.getAppSecret(), DES_KEY));
			return R.status(systemManagementService.updateById(applicationSystemManagement));
		} else {
			//appSecret
			String appSecret = applicationSystemManagement.getAppKey() + new Date().getTime();
			applicationSystemManagement.setAppSecret(DesUtil.encryptToBase64(appSecret, DES_KEY));
			return R.status(systemManagementService.updateById(applicationSystemManagement));
		}

	}

	/**
	 * 更改禁用启用
	 */
	@PostMapping("/switchStatus")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "更改禁用启用", notes = "传入systemManagement")
	public R switchStatus(@Valid @RequestBody ApplicationSystemManagement applicationSystemManagement) {
		return R.status(systemManagementService.switchStatus(applicationSystemManagement));
	}

	/**
	 * 删除 应用系统管理表
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(systemManagementService.deleteLogic(Func.toLongList(ids)));
	}


}
