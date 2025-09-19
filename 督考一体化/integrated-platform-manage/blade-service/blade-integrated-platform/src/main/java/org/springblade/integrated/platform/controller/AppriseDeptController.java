package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.IAppriseDeptService;
import org.springblade.integrated.platform.service.IQuarterlyEvaluationService;
import org.springblade.integrated.platform.service.IQuarterlySumScoreService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-18 15:33
 */

@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/appriseDept")
public class AppriseDeptController {
	private final IAppriseDeptService IAppriseDeptService;

	@Resource
	private IUserClient userClient;
	private final ISysClient sysClient;

	private final IQuarterlyEvaluationService iQuarterlyEvaluationService;
	private final IQuarterlySumScoreService iQuarterlySumScoreService;


	/**
	 * 单位评价记录
	 * @return
	 */
	@GetMapping("/appriseDeptList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "单位评价记录列表查询", notes = "")
	public R<IPage<AppriseDept>> recordList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String isLookRole = "打分详情";
		String userName = user.getName();



		String roleNames = SysCache.getRoleNames(user.getRoleId()).toString();
		//sql查询条件
		String responsibleUnitId = (String) entity.get("responsibleUnitId");
		String evaluationId = (String) entity.get("evaluationId");
		String evaluationType = (String) entity.get("evaluationType");

		//所属年份季度
		String quarter =	iQuarterlyEvaluationService.getById(evaluationId).getToQuarter();
		Date shijian = iQuarterlyEvaluationService.getById(evaluationId).getCreateTime();
		String year= new SimpleDateFormat("yyyy").format(shijian);
		//获取考核分组
		String checkClassify =	iQuarterlyEvaluationService.getById(evaluationId).getCheckClassify();
		String userDeptId = user.getDeptId();








		QueryWrapper<AppriseDept> queryWrapper = new QueryWrapper<AppriseDept>();
		if(StringUtils.isNotBlank(responsibleUnitId)){
			queryWrapper.eq("responsible_unit_id",responsibleUnitId);
		}
		if(StringUtils.isNotBlank(evaluationId)){
			queryWrapper.eq("evaluation_id",evaluationId);
		}
		if(StringUtils.isNotBlank(evaluationType)){
			queryWrapper.eq("evaluation_type",evaluationType);
		}
		queryWrapper.eq("type","1");





		//appriseLeader
		boolean isok = true;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isok = false;
				break;
			}
		}
		//如果用户是四大班子领导，那就不执行评价人的查询条件，直接查看所有的的评价信息
		if (isok) {
			if (roleNames.contains(isLookRole)) {
				queryWrapper.and(wrapper -> wrapper
					.eq(StringUtils.isNotNull(user.getDeptId()), "responsible_unit_id", user.getDeptId())
					.or()
					.eq(StringUtils.isNotNull(user.getDeptId()), "create_dept", user.getDeptId())
				);
			} else {
				queryWrapper.and(wrapper -> wrapper
					.ne(StringUtils.isNotNull(user.getDeptId()),"responsible_unit_id",user.getDeptId())
					//.or()
					.eq(StringUtils.isNotNull(user.getDeptId()),"create_dept",user.getDeptId())
				);
			}
		}
		IPage<AppriseDept> pages = IAppriseDeptService.page(Condition.getPage(query), queryWrapper);


		//打分人
		String appriseDept1="";
		if(pages.getRecords() !=null && pages.getRecords().size()>0){
		appriseDept1 =pages.getRecords().get(0).getCreateDeptName();
		}else{
		appriseDept1="1";
		}




		//本人是评价人，直接看到分数
		if(!Objects.equals(appriseDept1, userName)){
			QueryWrapper<QuarterlySumScore> queryWrapper1 = new QueryWrapper<QuarterlySumScore>();
			queryWrapper1.eq("check_classify",checkClassify);
			queryWrapper1.eq("stage_year",year);
			queryWrapper1.eq("stage",quarter);

			if (!roleNames.contains(isLookRole)) {
				queryWrapper1.eq("is_send","1");
			}
			List<QuarterlySumScore> detail = iQuarterlySumScoreService.list(queryWrapper1);
			if(detail ==null || detail.size()==0){
				return  R.data(new Page<>());
			}
		 }



		return R.data(pages);
	}

	/**
	 * 详情
	 * @param
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "评价信息详情", notes = "")
	public R<AppriseDept> details(@ApiIgnore @RequestParam String id) {
		AppriseDept ad = IAppriseDeptService.getById(id);//获取单位评价信息
		return R.data(ad);
	}

}
