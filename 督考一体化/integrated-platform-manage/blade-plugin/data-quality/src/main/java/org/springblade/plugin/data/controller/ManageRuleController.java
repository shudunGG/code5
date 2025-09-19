package org.springblade.plugin.data.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.plugin.data.dto.ManageRuleDTO;
import org.springblade.plugin.data.entity.ManageRule;
import org.springframework.web.bind.annotation.*;

import org.springblade.plugin.data.service.IManageRuleService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * ManageRule的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/manageRule")
@Api(value = "规则管理表数据", tags = "规则管理表接口")
public class ManageRuleController extends BladeController {

	/**
	 * ManageRuleService服务
	 */
	private IManageRuleService manageRuleService;

	/**
	 * @return org.springblade.core.tool.api.R<com.baomidou.mybatisplus.core.metadata.IPage < org.springblade.plugin.data.entity.ManageRule>>
	 * @Author MaQY
	 * @Description 自定义分页查询
	 * @Date 下午 4:33 2021/11/3 0003
	 * @Param [rule, query]
	 **/
	@GetMapping("/selectPageList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "传入manageRule")
	public R<IPage<ManageRule>> selectPageList(@RequestParam @ApiIgnore Map<String, Object> rule, Query query) {
		return R.data(manageRuleService.selectPageList(Condition.getPage(query), JSONObject.parseObject(JSONObject.toJSONString(rule), ManageRuleDTO.class)));
	}

	/**
	 * @return org.springblade.core.tool.api.R<java.lang.String>
	 * @Author MaQY
	 * @Description 生成规则代码
	 * @Date 下午 3:06 2021/11/4 0004
	 * @Param [type, themeId]
	 **/
	@GetMapping("/generateCode")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "生成规则代码", notes = "传入规则类型和主题表ID")
	public R<String> generateCode(@RequestParam("type") String type, @RequestParam("themeId") String themeId) {
		return R.data(manageRuleService.generateCode(type, themeId));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 保存规则同时保存关联关系
	 * @Date 上午 10:48 2021/11/18 0018
	 * @Param [manageRuleDTO]
	 **/
	@PostMapping("/saveRule")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "保存规则", notes = "传入manageRuleDTO")
	public R saveRule(@RequestBody ManageRuleDTO manageRuleDTO) {
		return R.status(manageRuleService.saveRule(manageRuleDTO));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除规则
	 * @Date 上午 11:50 2021/11/18 0018
	 * @Param [ids]
	 **/
	@PostMapping("/deleteRules")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "删除规则", notes = "传入主键集合")
	public R deleteRules(@RequestParam String ids) {
		return R.status(manageRuleService.deleteRules(ids));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除一个规则
	 * @Date 上午 11:47 2021/11/19 0019
	 * @Param [id]
	 **/
	@PostMapping("/deleteRule")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "删除一个规则", notes = "传入主键")
	public R deleteRule(@RequestParam String id) {
		return R.status(manageRuleService.deleteRule(id));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 更新一个规则（逻辑检查规则会把相应关联关系删除后重新新增）
	 * @Date 下午 1:37 2021/11/19 0019
	 * @Param [manageRuleDTO]
	 **/
	@PostMapping("/updateRule")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "更新一个规则", notes = "传入manageRuleDTO")
	public R updateRule(@RequestBody ManageRuleDTO manageRuleDTO) {
		return R.status(manageRuleService.updateRule(manageRuleDTO));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 保存规则和模板
	 * @Date 上午 10:43 2021/11/22 0022
	 * @Param [manageRuleDTO]
	 **/
	@PostMapping("/saveRuleAndTemplate")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "保存规则和模板", notes = "传入manageRuleDTO")
	public R saveRuleAndTemplate(@RequestBody ManageRuleDTO manageRuleDTO) {
		return R.status(manageRuleService.saveRuleAndTemplate(manageRuleDTO));
	}

	/**
	 * 根据规则主键集合获取规则和相应的关联关系
	 *
	 * @param ids
	 * @return
	 */
	@GetMapping("/getRules")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "根据规则主键集合获取规则和相应的关联关系", notes = "传入主键集合")
	public R<List<ManageRule>> getRules(@RequestParam("ids") String ids) {
		return R.data(manageRuleService.listByIds(Func.toLongList(ids)));
	}

	/**
	 * 获取详情
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getRuleDetail")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "获取详情", notes = "传入主键")
	public R<ManageRuleDTO> getRuleDetail(@RequestParam("id") String id) {
		return R.data(manageRuleService.getRuleDetail(id));
	}
}
