package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.vo.SupervisionSignVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.wrapper.SupervisionSignWrapper;
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
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 督察督办附件控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionFiles")
@Api(value = "督察督办附件", tags = "督察督办附件")
public class SupervisionFilesController extends BladeController {

	private final ISupervisionFilesService supervisionFilesService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 分页查询
	 * @param start
	 * @param limit
	 * @return
	 */
	@GetMapping("/list/{start}/{limit}")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "督察督办评价List", notes = "")
	public R<PageInfo> list(@ApiIgnore @PathVariable Integer start, @PathVariable Integer limit, @RequestParam String columnCode ) {
		QueryWrapper<SupervisionFiles> ew =new QueryWrapper<>();
		PageHelper.startPage(start,limit).setOrderBy("update_time desc");
		Map<String,Object> param=new HashMap<>();
		param.put("columnCode",columnCode);
		List<SupervisionFiles> list = supervisionFilesService.list(ew);
		PageInfo pageInfo = new PageInfo(list);
		return R.data(pageInfo);
	}

	/**
	 * 签收列表
	 * @param servCode
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "附件", notes = "附件列表")
	public R<List<SupervisionFiles>> list(@RequestParam("servCode") String servCode){
		List<SupervisionFiles> files = this.supervisionFilesService.list(new QueryWrapper<>(new SupervisionFiles()).eq("serv_code",servCode).orderByDesc("create_time"));
		return R.data(files);
	}

	/**
	 * 附件列表
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "附件", notes = "附件列表")
	public R listApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("附件列表-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String servCode = jsonParams.getString("servCode");
			List<SupervisionFiles> files = this.supervisionFilesService.list(new QueryWrapper<>(new SupervisionFiles()).eq("serv_code",servCode).orderByDesc("create_time"));
			JSONArray jsonArray = objectMapper.convertValue(files, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param SupervisionFiles
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody SupervisionFiles SupervisionFiles) {
		return R.status(supervisionFilesService.save(SupervisionFiles));
	}

	/**
	 * 修改
	 * @param SupervisionFiles
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SupervisionFiles SupervisionFiles) {
		return R.status(supervisionFilesService.updateById(SupervisionFiles));
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
		return R.status(supervisionFilesService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(supervisionFilesService.removeByIds(Arrays.asList(id)));
	}

}
