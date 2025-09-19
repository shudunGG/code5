package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springblade.integrated.platform.service.IReportsService;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TangYanXing
 * @version 1.0
 * @description: 汇报信息控制层
 * @date 2022-04-17 16:19
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/reports")
@Api(value = "汇报信息", tags = "汇报信息控制层代码")
public class ReportsController extends BladeController {

	private final IReportsService iReportsService;
	@Autowired
	private IAppriseFilesService iAppriseFilesService;

	private final IUserClient userClient;
	private final ISysClient sysClient;
	/**
	 * 汇报保存接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@Valid @RequestBody Reports rp) throws ParseException {
		boolean isok  = iReportsService.saveReports(rp);

		String title1 = "新增汇报信息";
		String businessId = String.valueOf(rp.getId());
		String businessTable = "Reports";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 汇报记录
	 * @return
	 */
	@GetMapping("/recordList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "汇报记录列表查询", notes = "")
	public R<IPage<Reports>> recordList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		String stageInformationId = (String) entity.get("stageInformationId");
		//sql查询条件
		QueryWrapper<Reports> queryWrapper = new QueryWrapper<Reports>();
		queryWrapper= Condition.getQueryWrapper(entity, Reports.class);
		if(StringUtils.isNotBlank(stageInformationId)){
			queryWrapper.eq("stage_information_id",entity.get("stageInformationId"));
		}
		queryWrapper.eq("evaluation_id",entity.get("evaluationId"));
		queryWrapper.eq("evaluation_type",entity.get("evaluationType"));
		//appriseLeader
		/*boolean isok = true;
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
			queryWrapper.and(wrapper -> wrapper
				.eq(StringUtils.isNotNull(user.getId()),"create_user",user.getId().toString())
				.or().eq(StringUtils.isNotNull(user.getDeptId()),"create_dept",user.getDeptId())
				.or().eq(StringUtils.isNotNull(user.getDeptId()),"dept_id",user.getDeptId())
			);
		}*/



		IPage<Reports> pages = iReportsService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}


	/**
	 * 详情
	 * @param
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "汇报信息详情", notes = "vo")
	public R<Reports> details(@ApiIgnore @RequestParam String id) {
		Reports rp = iReportsService.getById(id);//获取汇报信息
		AppriseFiles files = new AppriseFiles();//获取汇报附件信息
		files.setBusinessId(Long.parseLong(id));
		files.setBusinessTable("Reports");
		List<AppriseFiles> list = iAppriseFilesService.list(Condition.getQueryWrapper(files));
		rp.setAppriseFilesList(list);
		return R.data(rp);
	}

	/**
	 * 汇报信息修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody Reports rp) throws ParseException {
		boolean isok  = iReportsService.uptReports(rp);

		LambdaQueryWrapper<AppriseFiles> lambdaQueryWrapper = Wrappers.<AppriseFiles>query().lambda()
			.eq(AppriseFiles::getBusinessId,rp.getId());
		iAppriseFilesService.remove(lambdaQueryWrapper);
		List<AppriseFiles> appriseFilesList = rp.getAppriseFilesList();
		if (appriseFilesList != null) {
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setBusinessId(rp.getId());
				iAppriseFilesService.saveOrUpdate(appriseFiles);
			}
		}

		String title1 = "修改汇报信息";
		String businessId = String.valueOf(rp.getId());
		String businessTable = "Reports";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

}
