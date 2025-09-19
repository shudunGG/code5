package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectSummary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.IProjectFilesService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 项目管理附件
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectFiles")
@Api(value = "项目管理附件", tags = "项目管理附件")
public class ProjectFilesController extends BladeController {

	private final IProjectFilesService projectFilesService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理附件", notes = "")
	public R<IPage<ProjectFiles>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<ProjectFiles> pages = projectFilesService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectFiles.class));
		return R.data(pages);
	}

	/**
	 * 新增
	 * @param projectFiles
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectFiles projectFiles) {
		return R.status(projectFilesService.save(projectFiles));
	}

	/**
	 * 修改
	 * @param projectFiles
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectFiles projectFiles) {
		return R.status(projectFilesService.updateById(projectFiles));
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String id) {
		return R.status(projectFilesService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return`
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(projectFilesService.removeByIds(Arrays.asList(id)));
	}

	/**
	 * 项目文件列表
	 * @param id
	 * @return
	 */
	@GetMapping("/batchFileList")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目文件列表", notes = "")
	public R batchFileScreen(@ApiIgnore @RequestParam String id){
		return R.data(projectFilesService.getProjectFilesListByProjId(id));
	}

	/**
	 * 项目文件列表-app
	 * @return
	 */
	@PostMapping("/batchFileListApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "项目文件列表", notes = "")
	public R batchFileListApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("项目文件列表-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String id = jsonParams.getString("id");
			List<ProjectFiles> projectFilesList = projectFilesService.getProjectFilesListByProjId(id);
			JSONArray jsonArray = objectMapper.convertValue(projectFilesList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

}
