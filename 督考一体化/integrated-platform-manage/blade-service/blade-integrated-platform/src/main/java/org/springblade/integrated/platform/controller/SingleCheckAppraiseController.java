package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.QuarterlySumScore;
import com.vingsoft.entity.SingleCheckAppraise;
import com.vingsoft.entity.SingleCheckRank;
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
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.service.ISingleCheckAppraiseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @className: SingleCheckAppraiseController
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 15:04 星期二
 * @Version 1.0
 **/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/singleCheckAppraise")
@Api(value = "单项考核排名评价", tags = "单项考核排名评价控制层代码")
public class SingleCheckAppraiseController extends BladeController {

	@Resource
	private ISingleCheckAppraiseService iSingleCheckAppraiseService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 单项考核排名评价新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@RequestBody SingleCheckAppraise singleCheckAppraise) throws ParseException {
		//保存
		boolean isOk = iSingleCheckAppraiseService.save(singleCheckAppraise);

		//新增数据 保存日志
		String title = "新增单项考核排名评价数据";
		String businessId = String.valueOf(singleCheckAppraise.getId());
		String businessTable = "SingleCheckAppraise";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.status(isOk);
	}

	/**
	 * 单项考核排名评价 批量新增接口
	 */
	@PostMapping("/batchSave")
	@ApiOperationSupport(order = 2)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "批量保存", notes = "vo集合")
	public R batchSave(@RequestBody List<SingleCheckAppraise> singleCheckAppraiseList) throws ParseException {

		boolean isOk = false;
		if(singleCheckAppraiseList != null && singleCheckAppraiseList.size() > 0){
			for(int i=0; i<singleCheckAppraiseList.size(); i++){
				//保存
				SingleCheckAppraise singleCheckAppraise = singleCheckAppraiseList.get(i);
				QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq(singleCheckAppraise.getTargetId() != null && !Objects.equals(singleCheckAppraise.getTargetId(), ""),"target_id",singleCheckAppraise.getTargetId());
				queryWrapper.eq(singleCheckAppraise.getAppraiseObjectId() != null && !Objects.equals(singleCheckAppraise.getAppraiseObjectId(), ""),"appraise_object_id",singleCheckAppraise.getAppraiseObjectId());
				List<SingleCheckAppraise> alreadyList = iSingleCheckAppraiseService.list(queryWrapper);
				if(alreadyList != null && alreadyList.size() > 0){
					//已经存在，不能重复保存
					isOk = true;
				}else{
					isOk = iSingleCheckAppraiseService.save(singleCheckAppraise);

					//新增数据 保存日志
					String title = "新增单项考核排名评价数据";
					String businessId = String.valueOf(singleCheckAppraise.getId());
					String businessTable = "SingleCheckAppraise";
					int businessType = BusinessType.INSERT.ordinal();
					SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
				}
			}
		}
		return R.status(isOk);
	}

	/**
	 * 单项考核排名评价修改接口
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SingleCheckAppraise singleCheckAppraise) throws ParseException {
		//修改
		boolean isOk = iSingleCheckAppraiseService.saveOrUpdate(singleCheckAppraise);

		//修改数据 保存日志
		String title = "修改单项考核排名评价数据";
		String businessId = String.valueOf(singleCheckAppraise.getId());
		String businessTable = "SingleCheckAppraise";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.status(isOk);
	}

	/**
	 * 详情
	 * @param id
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SingleCheckAppraise> details(@Valid @RequestParam Long id) {

		//查看详情 保存日志
		String title = "查看单项考核排名评价详情";
		String businessId = String.valueOf(id);
		String businessTable = "SingleCheckAppraise";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.data(iSingleCheckAppraiseService.getById(id));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "分页", notes = "")
	public R<IPage<SingleCheckAppraise>> page(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<SingleCheckAppraise> pages = iSingleCheckAppraiseService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SingleCheckAppraise.class));
		return R.data(pages);
	}

	/**
	 * 首页单项考核排名统计
	 */
	@GetMapping("/indexStatistics")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "单项考核排名-统计", notes = "传入SingleCheckAppraise")
	public R<List<SingleCheckAppraise>> detail(SingleCheckAppraise singleCheckAppraise){
		R<List<SingleCheckAppraise>> sList = iSingleCheckAppraiseService.getIndexStatistics(singleCheckAppraise);
		return sList;
	}

	/**
	 * 首页单项考核排名统计-app
	 */
	@PostMapping("/indexStatisticsApp")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "单项考核排名-统计", notes = "传入SingleCheckAppraise")
	public R indexStatisticsApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("单项考核排名-统计-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			SingleCheckAppraise singleCheckAppraise = objectMapper.convertValue(jsonParams, SingleCheckAppraise.class);
			R<List<SingleCheckAppraise>> sList = iSingleCheckAppraiseService.getIndexStatistics(singleCheckAppraise);
			JSONArray jsonArray = objectMapper.convertValue(sList.getData(), JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}

	}

	/**
	 * 根据指标表id查询评价表数据集合
	 * @param id
	 */
	@GetMapping("/getByTargetId")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "根据指标表id查询评价表数据集合", notes = "")
	public R<List<SingleCheckAppraise>> getByTargetId(@Valid @RequestParam Long id) {

		QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(id != null && !Objects.equals(id, ""),"target_id",id);
		queryWrapper.orderByDesc("create_time");
		List<SingleCheckAppraise> singleCheckAppraiseList = iSingleCheckAppraiseService.list(queryWrapper);

		return R.data(singleCheckAppraiseList);
	}

	/**
	 * 不分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "分页", notes = "")
	public R<List<SingleCheckAppraise>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("is_deleted",0);
		queryWrapper.orderByDesc("create_time");
		List<SingleCheckAppraise> singleCheckAppraiseList = iSingleCheckAppraiseService.list(queryWrapper);
		return R.data(singleCheckAppraiseList);
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
		return R.status(iSingleCheckAppraiseService.removeById(id));
	}
}
