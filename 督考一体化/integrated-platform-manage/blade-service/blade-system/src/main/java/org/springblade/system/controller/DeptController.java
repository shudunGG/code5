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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.secure.constant.AuthConstant;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.cache.DictCache;
import org.springblade.system.entity.Dept;
import org.springblade.system.entity.DictBiz;
import org.springblade.system.enums.DictEnum;
import org.springblade.system.service.IDeptService;
import org.springblade.system.service.IDictBizService;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.vo.DeptVO;
import org.springblade.system.wrapper.DeptWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 控制器
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/dept")
@Api(value = "部门", tags = "部门")
//@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
public class DeptController extends BladeController {

	private final IDeptService deptService;
	private final IDictBizService dictService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入dept")
	public R<DeptVO> detail(Dept dept) {
		Dept detail = deptService.getOne(Condition.getQueryWrapper(dept));
		return R.data(DeptWrapper.build().entityVO(detail));
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "deptName", value = "部门名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "fullName", value = "部门全称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入dept")
	public R<List<DeptVO>> list(@ApiIgnore @RequestParam Map<String, Object> dept, BladeUser bladeUser) {
		QueryWrapper<Dept> queryWrapper = Condition.getQueryWrapper(dept, Dept.class);
		List<Dept> list = deptService.list((!bladeUser.getTenantId().equals(BladeConstant.ADMIN_TENANT_ID)) ? queryWrapper.lambda().eq(Dept::getTenantId, bladeUser.getTenantId()) : queryWrapper);
		return R.data(DeptWrapper.build().listNodeVO(list));
	}

	/**
	 * 部门分组列表
	 * @return
	 */
	@GetMapping("/findDeptGroup")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R findDeptGroup(String year) {
		User user = UserCache.getUser(AuthUtil.getUserId());
		List<DictBiz> dictBizs = dictService.getDeptGroup(year);
		List<Map<String,Object>> groups = new ArrayList<>();
		if(dictBizs!=null && !dictBizs.isEmpty()) {
			for (DictBiz dict : dictBizs) {
				Map<String,Object> map = new HashMap<>();
				QueryWrapper queryWrapper = new QueryWrapper<>(new Dept()).gt("find_in_set('" + (year + "-" + dict.getDictKey()) + "', dept_group)", 0);
				if(StringUtil.isNotBlank(user.getManageDept())){
					queryWrapper.notIn("id",user.getManageDept());
				}
				queryWrapper.orderByAsc("sort");
				List list = deptService.list(queryWrapper);
				map.put("group",dict);
				map.put("depts",list);
				groups.add(map);
			}
		}
		return R.data(groups);
	}

	/**
	 * 部门分组列表
	 * @param deptGroup
	 * @return
	 */
	@GetMapping("/findListByGroup")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R<List<DeptVO>> findListByGroup(@RequestParam String deptGroup) {
 		List<Dept> list = deptService.list(new QueryWrapper<>(new Dept()).gt("find_in_set('" + deptGroup + "', dept_group)", 0));
		return R.data(DeptWrapper.build().listNodeVO(list));
	}

	/**
	 * 市委领导/市政府领导
	 * @param
	 * @return
	 */
	@GetMapping("/findListSwld")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R<List<DeptVO>> findListSwld() {
		String swldDeptId = PropConstant.getSwldDeptId();
		String szfDeptId = PropConstant.getSzfldDeptId();
		List<Dept> list = deptService.list(Wrappers.<Dept>lambdaQuery().in(Dept::getId, Func.toLongList(swldDeptId+","+szfDeptId)));
		return R.data(DeptWrapper.build().listNodeVO(list));
	}

	/**
	 * 市委领导/市政府领导
	 * @param
	 * @return
	 */
	@PostMapping("/findListSwldApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R findListSwldApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("findListSwldApp-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();

			String swldDeptId = PropConstant.getSwldDeptId();
			String szfDeptId = PropConstant.getSzfldDeptId();
			List<Dept> list = deptService.list(Wrappers.<Dept>lambdaQuery().in(Dept::getId, Func.toLongList(swldDeptId+","+szfDeptId)));
			JSONArray deptVOList = objectMapper.convertValue(DeptWrapper.build().listNodeVO(list), JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, deptVOList.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 获取管理的部门列表
	 * @return
	 */
	@GetMapping("/findManageList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R<List<Dept>> findManageList() {
		User user = UserCache.getUser(AuthUtil.getUserId());
		List<Dept> list = deptService.findManageList(user.getManageDept());
		return R.data(list);
	}

	/**
	 * 获取管理部门之外的其他部门列表
	 * @param deptGroup
	 * @return
	 */
	@GetMapping("/otherDept")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入deptGroup")
	public R<List<DeptVO>> otherDept(@RequestParam String deptGroup) {
		User user = UserCache.getUser(AuthUtil.getUserId());
		QueryWrapper queryWrapper = new QueryWrapper<>(new Dept()).eq("dept_group",deptGroup);
		if(StringUtil.isNotBlank(user.getManageDept())){
			queryWrapper.notIn("id",user.getManageDept());
		}
		List<Dept> list = deptService.list(queryWrapper);
		return R.data(DeptWrapper.build().listNodeVO(list));
	}

	/**
	 * 懒加载列表
	 */
	@GetMapping("/lazy-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "deptName", value = "部门名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "fullName", value = "部门全称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "懒加载列表", notes = "传入dept")
	public R<List<DeptVO>> lazyList(@ApiIgnore @RequestParam Map<String, Object> dept, Long parentId, BladeUser bladeUser) {
		List<DeptVO> list = deptService.lazyList(bladeUser.getTenantId(), parentId, dept);
		return R.data(DeptWrapper.build().listNodeLazyVO(list));
	}

	/**
	 * 获取部门树形结构
	 *
	 * @return
	 */
	@GetMapping("/tree")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "树形结构", notes = "树形结构")
	public R<List<DeptVO>> tree(String tenantId, BladeUser bladeUser) {
		List<DeptVO> tree = deptService.tree(Func.toStrWithEmpty(tenantId, bladeUser.getTenantId()));
		return R.data(tree);
	}

	/**
	 * 获取部门树形结构
	 * @return
	 */
	@PostMapping("/treeApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "树形结构-app", notes = "树形结构")
	public R treeApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("appTask-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String tenantId = jsonParams.getString("tenantId");

			List<DeptVO> tree = deptService.tree(Func.toStrWithEmpty(tenantId, AuthUtil.getUser().getTenantId()));
			JSONArray jsonArray = objectMapper.convertValue(tree, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 懒加载获取部门树形结构
	 */
	@GetMapping("/lazy-tree")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "懒加载树形结构", notes = "树形结构")
	public R<List<DeptVO>> lazyTree(String tenantId, Long parentId, BladeUser bladeUser) {
		List<DeptVO> tree = deptService.lazyTree(Func.toStrWithEmpty(tenantId, bladeUser.getTenantId()), parentId);
		return R.data(tree);
	}

	/**
	 * 懒加载获取部门树形结构-app
	 */
	@PostMapping("/lazy-treeApp")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "懒加载树形结构", notes = "树形结构")
	public R lazyTreeApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("lazy-treeApp-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String tenantId = jsonParams.getString("tenantId");
			Long parentId = jsonParams.getLong("parentId");

			List<DeptVO> tree = deptService.lazyTree(Func.toStrWithEmpty(tenantId, AuthUtil.getUser().getTenantId()), parentId);
			JSONArray jsonArray = objectMapper.convertValue(tree, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入dept")
	public R submit(@Valid @RequestBody Dept dept) {
		hasDouble(dept);
		if (deptService.submit(dept)) {
			CacheUtil.clear(SYS_CACHE);
			// 返回懒加载树更新节点所需字段
			Kv kv = Kv.create().set("id", String.valueOf(dept.getId())).set("tenantId", dept.getTenantId())
				.set("deptCategoryName", DictCache.getValue(DictEnum.ORG_CATEGORY, dept.getDeptCategory()));
			return R.data(kv);
		}
		return R.fail("操作失败");
	}

	/**
	 * 排除年份选择重复
	 */
	private void hasDouble(Dept dept){
		if (!Func.isEmpty(dept) && null != dept.getDeptGroup() && dept.getDeptGroup().length() > 0) {
			String deptGroup = dept.getDeptGroup();
			String[] split = deptGroup.split(",");
			Set<String> set = new HashSet<>();
			for (String s : split) {
				if (!set.add(s.split("-")[0])) {
					throw new RuntimeException(s.split("-")[0] + " 年份选择重复");
				}
			}
		}
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(deptService.removeDept(ids));
	}

	/**
	 * 下拉数据源
	 */
	@PreAuth(AuthConstant.PERMIT_ALL)
	@GetMapping("/select")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "下拉数据源", notes = "传入id集合")
	public R<List<Dept>> select(Long userId, String deptId) {
		if (Func.isNotEmpty(userId)) {
			User user = UserCache.getUser(userId);
			deptId = user.getDeptId();
		}
		List<Dept> list = deptService.list(Wrappers.<Dept>lambdaQuery().in(Dept::getId, Func.toLongList(deptId)));
		return R.data(list);
	}

	/**
	 * 获取区划id
	 */
	@GetMapping("/getAreaId")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "删除", notes = "传入id")
	public R<Long> getAreaId(@RequestParam String id) {
//		addGroup();
		Dept dept = deptService.getById(id);
		Long areaId = null;
		if(dept.getDeptName().contains("委办")&&!dept.getDeptName().contains("市")){
			areaId = dept.getParentId();
		}
		return R.data(areaId);
	}

	//新增
//	private void addDisplayYear(){
//		// 获取当前的年份
//		LocalDate today = LocalDate.now();
//		int year = today.getYear();
//		// 但是如果有部门已经是今年展示，说明已经进了操作，就不在进行操作
//		int count = deptService.count(Wrappers.<Dept>update().lambda()
//			.like(Dept::getDisplayYear, String.valueOf(year)));
//		if (count == 0) {
//			deptService.update(Wrappers.<Dept>update().lambda()
//				.setSql("display_year = CONCAT_WS(',',display_year, '" + year + "') ")
//				.like(Dept::getDisplayYear, String.valueOf(year - 1))
//				.notLike(Dept::getDisplayYear, String.valueOf(year)));
//		}
//	}
	/**
	 * 新增部门分类
	 * */
	@GetMapping("/addDeptGroup")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "新增部门分类", notes = "")
	private R addDeptGroup(){
		addGroup();
		return R.status(true);
	}

	private void addGroup() {
		// 获取当前的年份
		LocalDate today = LocalDate.now();
		int year = today.getYear();
		// 但是如果有部门已经是今年展示，说明已经进行了 操作，就不再进行操作
		int count = deptService.count(Wrappers.<Dept>query().lambda()
			.like(Dept::getDeptGroup, String.valueOf(year)));
		if (count < 1) {
			deptService.update(Wrappers.<Dept>update().lambda()
				.setSql("dept_group = concat_ws(',', dept_group, concat('" + year + "', '-',substring_index(substring_index(dept_group, ',', -1), '-', -1)))")
				.like(Dept::getDeptGroup, String.valueOf(year - 1))
				.notLike(Dept::getDeptGroup, String.valueOf(year)));
		}
	}
}
