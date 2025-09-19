package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.QuarterlyEvaluation;
import com.vingsoft.entity.SingleCheckAppraise;
import com.vingsoft.entity.SingleCheckRank;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.service.ISingleCheckAppraiseService;
import org.springblade.integrated.platform.service.ISingleCheckRankService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: SingleCheckRankController
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 15:04 星期二
 * @Version 1.0
 **/
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/singleCheckRank")
@Api(value = "单项考核排名指标", tags = "单项考核排名指标控制层代码")
public class SingleCheckRankController extends BladeController {

	@Resource
	private ISingleCheckRankService iSingleCheckRankService;

	@Resource
	private ISingleCheckAppraiseService iSingleCheckAppraiseService;

	/**
	 * 单项考核排名指标新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@RequestBody SingleCheckRank singleCheckRank) throws ParseException {
		//保存
		boolean isOk = iSingleCheckRankService.save(singleCheckRank);

		//新增数据 保存日志
		String title = "新增单项考核排名指标数据";
		String businessId = String.valueOf(singleCheckRank.getId());
		String businessTable = "SingleCheckRank";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.status(isOk);
	}

	/**
	 * 单项考核排名指标修改接口
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody SingleCheckRank singleCheckRank) throws ParseException {
		//修改
		boolean isOk = iSingleCheckRankService.saveOrUpdate(singleCheckRank);

		//修改数据 保存日志
		String title = "修改单项考核排名指标数据";
		String businessId = String.valueOf(singleCheckRank.getId());
		String businessTable = "SingleCheckRank";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.status(isOk);
	}

	/**
	 * 详情
	 * @param id
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "详情", notes = "vo")
	public R<SingleCheckRank> details(@Valid @RequestParam Long id) {

		//查看详情 保存日志
		String title = "查看单项考核排名指标详情";
		String businessId = String.valueOf(id);
		String businessTable = "SingleCheckRank";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		return R.data(iSingleCheckRankService.getById(id));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "分页", notes = "")
	public R<IPage<SingleCheckRank>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		IPage<SingleCheckRank> pages = iSingleCheckRankService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, SingleCheckRank.class));
		List<SingleCheckRank> singleCheckRankList = pages.getRecords();
		for(int i=0; i<singleCheckRankList.size(); i++){
			QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("target_id",singleCheckRankList.get(i).getId());
			queryWrapper.orderByDesc("create_time");
			List<SingleCheckAppraise> singleCheckAppraiseList = iSingleCheckAppraiseService.list(queryWrapper);
			String appraiseObjectNames = singleCheckAppraiseList.stream().map(SingleCheckAppraise::getAppraiseObjectName)
				.collect(Collectors.joining(","));
			singleCheckRankList.get(i).setAppraiseObjectNames(appraiseObjectNames);
		}
		return R.data(pages);
	}

	/**
	 * 指标数据集合，给下拉查询用
	 * @return
	 */
	@GetMapping("/targetList")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "指标数据集合", notes = "")
	public R<List<SingleCheckRank>> targetList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String year = entity.get("year").toString();
		String areaType = entity.get("areaType").toString();
		String quarter = entity.get("quarter").toString();
		QueryWrapper<SingleCheckRank> queryWrapper = new QueryWrapper<>();
		queryWrapper.select(" distinct target_name");
		queryWrapper.eq("year",year);
		queryWrapper.eq("quarter",quarter);
		queryWrapper.eq("area_type",areaType);
		queryWrapper.eq("is_deleted",0);
		queryWrapper.orderByDesc("create_time");
		List<SingleCheckRank> singleCheckRankList = iSingleCheckRankService.list(queryWrapper);
		return R.data(singleCheckRankList);
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam String id) {
		return R.status(iSingleCheckRankService.removeById(id));
	}

	/**
	 * 考核指标发布
	 */
	@PostMapping("/send")
	@ApiOperationSupport(order = 7)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "保存", notes = "vo")
	public R send(@RequestBody SingleCheckRank singleCheckRank){
		boolean isOk = false;
		String msg = "";
		QueryWrapper<SingleCheckAppraise> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("target_id",singleCheckRank.getId());
		List<SingleCheckAppraise> singleCheckAppraiseList = iSingleCheckAppraiseService.list(queryWrapper);
		if(singleCheckAppraiseList != null && singleCheckAppraiseList.size() > 0){
			for(int i=0; i<singleCheckAppraiseList.size(); i++){
				singleCheckAppraiseList.get(i).setIsSend(1);
				isOk = iSingleCheckAppraiseService.saveOrUpdate(singleCheckAppraiseList.get(i));
				if(!isOk){
					msg = "部分指标评分发布失败";
					break;
				}
			}
		}else{
			isOk = false;
			msg = "当前指标未评价，不允许发布";
		}

		Map<String,Object> resultMap = new HashMap();
		resultMap.put("isOk",isOk);
		resultMap.put("msg",msg);

		return R.data(resultMap);
	}
}
