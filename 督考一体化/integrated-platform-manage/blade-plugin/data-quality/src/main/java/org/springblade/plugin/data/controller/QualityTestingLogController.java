package org.springblade.plugin.data.controller;


import com.alibaba.fastjson.JSON;
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
import org.springblade.plugin.data.entity.QualityTestingLog;
import org.springblade.plugin.data.service.IQualityTestingLogService;
import org.springblade.plugin.data.vo.QualityTestingLogVO;
import org.springblade.plugin.data.wrapper.QualityTestingLogWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * QualityTestingLog的路由接口服务
 *
 * @author
 */
@RestController
@AllArgsConstructor
@RequestMapping("/qualityTestingLog")
@Api(value = "QualityTestingLog的路由接口服务", tags = "质检日志接口")
public class QualityTestingLogController extends BladeController {

	/**
	 * QualityTestingLogService服务
	 */
	private IQualityTestingLogService qualityTestingLogService;

	/**
	 * 分页查询
	 *
	 * @param qualityTestingLog
	 * @param query
	 * @return
	 */
	@GetMapping("/pageList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "传入qualityTestingLog")
	public R<IPage<QualityTestingLogVO>> pageList(@RequestParam Map<String, Object> qualityTestingLog, Query query) {
		QualityTestingLog qualityTestingLog1 = JSONObject.toJavaObject((JSON) JSONObject.toJSON(qualityTestingLog), QualityTestingLog.class);
		IPage<QualityTestingLog> page = qualityTestingLogService.page(Condition.getPage(query), Condition.getQueryWrapper(qualityTestingLog1));
		return R.data(QualityTestingLogWrapper.build().pageVO(page));
	}
}
