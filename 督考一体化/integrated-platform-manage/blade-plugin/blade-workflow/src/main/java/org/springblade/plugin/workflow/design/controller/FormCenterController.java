package org.springblade.plugin.workflow.design.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.utils.TokenUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Base64Util;
import org.springblade.core.tool.utils.DesUtil;
import org.springblade.plugin.workflow.design.entity.WfForm;
import org.springblade.plugin.workflow.design.service.IWfFormService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luye
 */
@RestController
@AllArgsConstructor
@RequestMapping("/form/center")
@Api(tags = "表单中心")
public class FormCenterController {

	private final IWfFormService formService;

	@GetMapping("getForm")
	@ApiOperation("获取表单")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "formKey", value = "formKey", required = true),
		@ApiImplicitParam(name = "version", value = "版本号"),
	})
	public R getForm(@ApiIgnore WfForm form) {
		return R.data(formService.getFormByKey(form));
	}

}
