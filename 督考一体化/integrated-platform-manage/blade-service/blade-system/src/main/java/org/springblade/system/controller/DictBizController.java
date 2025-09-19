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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.entity.DictBiz;
import org.springblade.system.service.IDictBizService;
import org.springblade.system.vo.DictBizVO;
import org.springblade.system.wrapper.DictBizWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.DICT_CACHE;

/**
 * 控制器
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/dict-biz")
@Api(value = "业务字典", tags = "业务字典")
public class DictBizController extends BladeController {

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
	@ApiOperation(value = "详情", notes = "传入dict")
	public R<DictBizVO> detail(DictBiz dict) {
		DictBiz detail = dictService.getOne(Condition.getQueryWrapper(dict));
		return R.data(DictBizWrapper.build().entityVO(detail));
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入dict")
	public R<List<DictBizVO>> list(@ApiIgnore @RequestParam Map<String, Object> dict) {
		List<DictBiz> list = dictService.list(Condition.getQueryWrapper(dict, DictBiz.class).lambda().orderByAsc(DictBiz::getSort));
		return R.data(DictBizWrapper.build().listNodeVO(list));
	}

	/**
	 * 顶级列表
	 */
	@GetMapping("/parent-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "列表", notes = "传入dict")
	public R<IPage<DictBizVO>> parentList(@ApiIgnore @RequestParam Map<String, Object> dict, Query query) {
		return R.data(dictService.parentList(dict, query));
	}

	/**
	 * 子列表
	 */
	@GetMapping("/child-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "parentId", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "列表", notes = "传入dict")
	public R<List<DictBizVO>> childList(@ApiIgnore @RequestParam Map<String, Object> dict, @RequestParam(required = false, defaultValue = "-1") Long parentId) {
		return R.data(dictService.childList(dict, parentId));
	}

	/**
	 * 获取字典树形结构
	 */
	@GetMapping("/tree")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "树形结构", notes = "树形结构")
	public R<List<DictBizVO>> tree() {
		List<DictBizVO> tree = dictService.tree();
		return R.data(tree);
	}

	/**
	 * 获取字典树形结构
	 */
	@GetMapping("/parent-tree")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "树形结构", notes = "树形结构")
	public R<List<DictBizVO>> parentTree() {
		List<DictBizVO> tree = dictService.parentTree();
		return R.data(tree);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入dict")
	public R submit(@Valid @RequestBody DictBiz dict) {
		CacheUtil.clear(DICT_CACHE);
		if (Func.isEmpty(dict.getId()) && dict.getCode().equals("dept_group")) {
			addYear(dict);
		}else{
			updateYear(dict);
		}
		return R.status(dictService.submit(dict));
	}
	/**
	 * 新增部门分类时候，添加当前年份
	 */
	private void addYear(DictBiz dict){
		// 获取当前的年份
		LocalDate today = LocalDate.now();
		int year = today.getYear();
		dictService.update(Wrappers.<DictBiz>update().lambda()
			.setSql("dict_value = CONCAT_WS(',',dict_value, '" + dict.getDictKey() + "') ")
			.setSql("remark = concat(dict_key, '年目录：', dict_value)")
			.eq(DictBiz::getCode, "display_year")
			.eq(DictBiz::getDictKey,String.valueOf(year))
			.eq(DictBiz::getIsSealed, 0));
	}
	private void updateYear(DictBiz dict){
		DictBiz byId = dictService.getById(dict.getId());
		if (!byId.getDictKey().equals(dict.getDictKey())) {
			//如果字典值key和数据库中不一样，就更改对应年份的字典值的值
			List<DictBiz> years = dictService.getList("display_year");
			for (DictBiz year : years) {
				String dictValue = year.getDictValue();
				String[] split = dictValue.split(",");
				for (int i = 0; i < split.length; i++) {
					if (split[i].equals(byId.getDictKey())) {
						split[i] = dict.getDictKey();
					}
				}
				String join = String.join(",", split);
				int i = year.getRemark().indexOf(year.getDictValue());
				String substring = year.getRemark().substring(0, i);
				year.setDictValue(join);
				year.setRemark(substring + join);
			}
			dictService.updateBatchById(years);
		}

	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(DICT_CACHE);
		removeGroupYear(ids);
		return R.status(dictService.removeDict(ids));
	}
	/*
	* 删除时候，如果是部门分类删除，则先删除年份中的该部门
	* */
	private void removeGroupYear(String ids) {
		if (null != ids && ids != "") {
			List<DictBiz> dictBizList = dictService.list(Wrappers.<DictBiz>query().lambda()
				.in(DictBiz::getId, Func.toLongList(ids)));
			dictBizList.forEach(dictBiz -> {
				if (dictBiz.getCode().equals("dept_group")) {
					List<DictBiz> displayYears = dictService.list(Wrappers.<DictBiz>query().lambda()
						.eq(DictBiz::getCode, "display_year")
						.like(DictBiz::getDictValue, dictBiz.getDictKey()));
					if (Func.isNotEmpty(displayYears)) {
						for (DictBiz year : displayYears) {
							String[] groups = year.getDictValue().split(",");
							List<String> strings = new ArrayList<>();
							for (String n : groups) {
								if (!n.equals(dictBiz.getDictKey())) {
									strings.add(n);
								}
							}
							String value = String.join(",", strings);
							year.setDictValue(value);
							year.setRemark(year.getDictKey() + "年目录：" + value);
							dictService.submit(year);
						}
					}

				}
			});

		}
	}

	/**
	 * 获取字典
	 */
	@GetMapping("/dictionary")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "获取字典", notes = "获取字典")
	public R<List<DictBiz>> dictionary(String code) {
		addDisplayYear();
		List<DictBiz> tree = dictService.getList(code);
		return R.data(tree);
	}

	/**
	 * 获取部门分组 按年份显示
	 * @param year 传年份   2021
	 * */
	@GetMapping("/getDeptGroup")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "对应年份的部门分组", notes = "对应年份的部门分组")
	public R<List<DictBiz>> getDeptGroup(String year){
		return R.data(dictService.getDeptGroup(year));
	}

	/**
	 * 获取部门分组 按年份显示
	 * */
	@PostMapping("/getDeptGroupApp")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "对应年份的部门分组-app", notes = "对应年份的部门分组-app")
	public R getDeptGroupApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("对应年份的部门分组-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String year = jsonParams.getString("year");
			JSONArray jsonArray = objectMapper.convertValue(dictService.getDeptGroup(year), JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 获取字典树
	 */
	@GetMapping("/dictionary-tree")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "获取字典树", notes = "获取字典树")
	public R<List<DictBizVO>> dictionaryTree(String code) {

		List<DictBiz> tree = dictService.getList(code);
		return R.data(DictBizWrapper.build().listNodeVO(tree));
	}

	/**
	 * 自动添加年份和部门的分组
	 * */
	private void addDisplayYear() {
		// 获取当前的年份
		LocalDate today = LocalDate.now();
		int year = today.getYear();
		// 如果有今年的年份,就不添加
		int count = dictService.count(Wrappers.<DictBiz>query().lambda()
			.eq(DictBiz::getCode, "display_year")
			.eq(DictBiz::getIsSealed,0)
			.eq(DictBiz::getDictKey, String.valueOf(year)));
		if (count == 0) {
			synchronized(this) {
				// 同步代码块
				int i = dictService.count(Wrappers.<DictBiz>query().lambda()
					.eq(DictBiz::getCode, "display_year")
					.eq(DictBiz::getIsSealed, 0)
					.eq(DictBiz::getDictKey, String.valueOf(year)));
				if (i == 0) {
					DictBiz dictBiz = dictService.getOne(Wrappers.<DictBiz>query().lambda()
						.eq(DictBiz::getCode, "display_year")
						.eq(DictBiz::getIsSealed, 0)
						.gt(DictBiz::getParentId, 0) //上级id大于0
						.eq(DictBiz::getDictKey, String.valueOf(year - 1)));
					if (Func.isNotEmpty(dictBiz)) {
						dictBiz.setId(null);
						dictBiz.setDictKey(String.valueOf(year));
						dictBiz.setSort(dictBiz.getSort() + 1);
						dictBiz.setRemark(year + "年目录：" + dictBiz.getDictValue());
						dictService.submit(dictBiz);
					}
				}
			}
		}
	}

}
