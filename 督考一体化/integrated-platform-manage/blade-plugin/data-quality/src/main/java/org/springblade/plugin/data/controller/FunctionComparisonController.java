package org.springblade.plugin.data.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.dto.FunctionComparisonDTO;
import org.springblade.plugin.data.service.IFunctionComparisonService;
import org.springblade.plugin.data.util.ExpressionUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * FunctionComparison的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/functionComparison")
@Api(value = "函数对照表数据", tags = "函数对照表接口")
public class FunctionComparisonController extends BladeController {

	private IFunctionComparisonService comparisonService;

	private ExpressionUtil expressionUtil;

	/**
	 * @return org.springblade.core.tool.api.R<java.util.List < org.springblade.plugin.data.dto.FunctionComparisonDTO>>
	 * @Author MaQY
	 * @Description 获取函数树形结构
	 * @Date 下午 3:59 2021/11/11 0011
	 * @Param []
	 **/
	@GetMapping("/getFunctionTree")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取函数树形结构", notes = "无参")
	public R<List<FunctionComparisonDTO>> getFunctionTree() {
		return R.data(comparisonService.getFunctionTree());
	}

	/**
	 * @return org.springblade.core.tool.api.R
	 * @Author MaQY
	 * @Description 验证表达式是否正确
	 * @Date 下午 4:41 2021/11/11 0011
	 * @Param [expression]
	 **/
	@PostMapping("/validateExpression")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "验证表达式是否正确", notes = "传入expression")
	public R validateExpression(@RequestParam String expression) {
		return expressionUtil.validate(expression);
	}
}
