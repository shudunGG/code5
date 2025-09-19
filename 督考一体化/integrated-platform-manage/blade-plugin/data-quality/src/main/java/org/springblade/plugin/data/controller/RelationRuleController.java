package org.springblade.plugin.data.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.RelationRule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springblade.plugin.data.service.IRelationRuleService;

import java.util.List;

/**
 * RelationRule的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/relationRule")
@Api(value = "规则管理关联关系表", tags = "规则管理关联关系表接口")
public class RelationRuleController extends BladeController {

	/**
	 * RelationRuleService服务
	 */
	private IRelationRuleService relationRuleService;

	@GetMapping("/getRelationRuleByRuleId")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取列表", notes = "传入ruleID")
	public R<List<RelationRule>> getRelationRuleByRuleId(@RequestParam String ruleID) {
		return R.data(relationRuleService.list(Wrappers.<RelationRule>query().lambda().eq(RelationRule::getRuleId, ruleID)));
	}

}
