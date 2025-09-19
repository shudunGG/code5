package org.springblade.plugin.data.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.RuleTemplate;
import org.springblade.plugin.data.vo.RuleTemplateVO;
import org.springframework.web.bind.annotation.*;

import org.springblade.plugin.data.service.IRuleTemplateService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * RuleTemplate的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/ruleTemplate")
@Api(value = "RuleTemplate的路由接口服务", tags = "规则模板表接口")
public class RuleTemplateController extends BladeController {

	/**
	 * RuleTemplateService服务
	 */
	private IRuleTemplateService ruleTemplateService;

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.entity.RuleTemplate>>
	 * @Author MaQY
	 * @Description 根据类型获取列表
	 * @Date 上午 11:07 2021/11/22 0022
	 * @Param [type]
	 **/
	@GetMapping("/listByType")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取列表", notes = "传入type")
	public R<List<RuleTemplate>> listByType(@RequestParam String type) {
		return R.data(ruleTemplateService.list(Wrappers.<RuleTemplate>query().lambda().eq(RuleTemplate::getType, type)));
	}

	/**
	 * @return org.springblade.core.tool.api.R<com.baomidou.mybatisplus.core.metadata.IPage < org.springblade.plugin.data.entity.RuleTemplate>>
	 * @Author MaQY
	 * @Description 分页查询
	 * @Date 上午 11:16 2021/11/22 0022
	 * @Param [rule, query]
	 **/
	@GetMapping("/pageList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页查询", notes = "传入rule")
	public R<IPage<RuleTemplate>> pageList(@RequestParam @ApiIgnore Map<String, Object> rule, Query query) {
		return R.data(ruleTemplateService.page(Condition.getPage(query), Condition.getQueryWrapper(rule, RuleTemplate.class)));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除模板
	 * @Date 上午 11:21 2021/11/22 0022
	 * @Param [ids]
	 **/
	@PostMapping("/removeRuleTemplates")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "删除模板", notes = "传入主键集合")
	public R removeRuleTemplates(@RequestParam("ids") String ids) {
		return R.data(ruleTemplateService.removeRuleTemplates(ids));
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 删除模板
	 * @Date 上午 11:21 2021/11/22 0022
	 * @Param [id]
	 **/
	@PostMapping("/removeRuleTemplate")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "删除模板", notes = "传入主键")
	public R removeRuleTemplate(@RequestParam("id") String id) {
		return R.data(ruleTemplateService.removeRuleTemplate(id));
	}

	/**
	 * @return org.springblade.core.tool.api.R<org.springblade.plugin.data.dto.ManageRuleDTO>
	 * @Author MaQY
	 * @Description 根据模板主键获取模板详情（逻辑检查类的同时获取关联关系）
	 * @Date 下午 1:59 2021/11/22 0022
	 * @Param [id]
	 **/
	@GetMapping("/getRuleTemplate")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "获取模板详情", notes = "传入模板主键")
	public R<RuleTemplateVO> getRuleTemplate(@RequestParam("id") String id) {
		return R.data(ruleTemplateService.getRuleTemplate(id));
	}
}
