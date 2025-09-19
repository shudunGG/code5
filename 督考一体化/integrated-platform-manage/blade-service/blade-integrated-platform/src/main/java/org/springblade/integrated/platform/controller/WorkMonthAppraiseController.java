package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.entity.SupervisionScore;
import com.vingsoft.entity.WorkMonthAppraise;
import com.vingsoft.vo.WorkMonthAppraiseVO;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.service.ISupervisionScoreService;
import org.springblade.integrated.platform.service.IWorkMonthAppraiseService;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springblade.integrated.platform.wrapper.WorkMonthAppraiseWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作月调度评价-控制层
 * @Waston
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/workMonthAppraise")
public class WorkMonthAppraiseController extends BladeController {
	@Resource
	private IWorkMonthAppraiseService workMonthAppraiseService;
	@Resource
	ISupervisionScoreService supervisionScoreService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "分页", notes = "entity")
	public R<IPage<WorkMonthAppraiseVO>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<WorkMonthAppraise> pages = workMonthAppraiseService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, WorkMonthAppraise.class));
		return R.data(WorkMonthAppraiseWrapper.build().pageVO(pages));
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "传入workMonthAppraise")
	@Transactional
	public R save(@Valid @RequestBody WorkMonthAppraise workMonthAppraise) throws ParseException {

		//评价纳入督查督办评价统计-存部门得分表
		SupervisionScore supervisionScore = new SupervisionScore();
		supervisionScore.setServCode(workMonthAppraise.getServCode());
		supervisionScore.setDeptId(Long.parseLong(workMonthAppraise.getDeptCode()));
		supervisionScore.setScoreType("5"); //1督查督办评价得分;2:督查督办系统退回扣分;3:督查督办系统加分;4:督查督办系统汇报超期扣分;5:工作月调度汇报完成评分
		supervisionScore.setScore(BigDecimal.valueOf(workMonthAppraise.getAppraiseScore()));
		supervisionScore.setDetails("工作月调度汇报完成评分:"+workMonthAppraise.getAppraiseScore());
		supervisionScoreService.save(supervisionScore);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String newDate = sdf.format(new Date());
		workMonthAppraise.setCreateDate(sdf.parse(newDate));
		return R.status(workMonthAppraiseService.save(workMonthAppraise));
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "详情", notes = "传入code")
	public R<WorkMonthAppraise> detail(WorkMonthAppraise workMonthAppraise) {
		WorkMonthAppraise detail = workMonthAppraiseService.getOne(Condition.getQueryWrapper(workMonthAppraise));
		return R.data(detail);
	}

	/**
	 * 详情-app
	 */
	@PostMapping("/detailApp")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "详情", notes = "传入code")
	public R detailApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("workMonthAppraise-detailApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			WorkMonthAppraise workMonthAppraise = objectMapper.convertValue(jsonParams, WorkMonthAppraise.class);
			WorkMonthAppraise detail = workMonthAppraiseService.getOne(Condition.getQueryWrapper(workMonthAppraise));
			JSONObject pageJson = objectMapper.convertValue(detail, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, Func.isNotEmpty(pageJson)?pageJson.toJSONString():new JSONObject().toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 修改评价
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody WorkMonthAppraise workMonthAppraise) {
		return R.status(workMonthAppraiseService.updateById(workMonthAppraise));
	}

	/**
	 * 修改评价-app
	 */
	@PostMapping("/updateApp")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "修改", notes = "vo")
	public R updateApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("修改评价-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			WorkMonthAppraise workMonthAppraise = objectMapper.convertValue(jsonParams, WorkMonthAppraise.class);
			return R.data(VSTool.encrypt(encryptSign, String.valueOf(workMonthAppraiseService.updateById(workMonthAppraise)), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

}
