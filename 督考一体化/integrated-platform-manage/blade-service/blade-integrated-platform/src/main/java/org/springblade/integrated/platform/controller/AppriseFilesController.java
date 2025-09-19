package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.AppriseFiles;
import com.vingsoft.entity.TaskFiles;
import com.vingsoft.vo.TaskFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 考核评价文件信息 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/21 16:10
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("AppriseFiles")
@Api(value = "考核评价文件接口", tags = "考核评价文件信息控制层代码")
public class AppriseFilesController extends BladeController {

	@Autowired
	private IAppriseFilesService iAppriseFilesService;

	/**
	 * 考核评价文件详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价文件详细信息", notes = "传入 messageInformation")
	public R<List<AppriseFiles>> detail(AppriseFiles appriseFiles) {
		//sql查询条件
		QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<AppriseFiles>();
		queryWrapper.select(" * ");
		queryWrapper.eq(appriseFiles.getBusinessId()!=null,"business_id",appriseFiles.getBusinessId());
		List<AppriseFiles> detail = iAppriseFilesService.list(queryWrapper);
		return R.data(detail);
	}

	/**
	 * 分页查询
	 * @param appriseFiles
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "项目文件分页查询", notes = "传入appriseFiles")
	public R<IPage<AppriseFiles>> list(AppriseFiles appriseFiles, Query query) {
		//sql查询条件
		QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<AppriseFiles>();
		queryWrapper.eq(appriseFiles.getBusinessId()!=null,"business_id",appriseFiles.getBusinessId());
		//查询数据，封装分页参数
		IPage<AppriseFiles> pages = iAppriseFilesService.page(Condition.getPage(query),queryWrapper);
		return R.data(pages);
	}



	/**
	 * 新增考核评价文件信息
	 * @param appriseFiles
	 * @return
	 */
	@PostMapping("/saveFileInfo")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增考核评价文件信息", notes = "传入 AppriseFiles 对象")
	public R save(@Valid @RequestBody AppriseFiles appriseFiles) throws Exception {
		iAppriseFilesService.save(appriseFiles);
		String title = "新增考核评价文件信息";
		String businessId = String.valueOf(appriseFiles.getId());
		String businessTable = "AppriseFiles";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		return R.success("操作成功！");
	}

	/**
	 * 获取汇报附件信息
	 */
	@GetMapping("/reportsFilesList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取汇报附件信息", notes = "传入汇报表主键")
	public R<List<AppriseFiles>> reportsFilesList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		String businessId = (String) entity.get("businessId");
		QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<AppriseFiles>();
		queryWrapper= Condition.getQueryWrapper(entity, AppriseFiles.class);
		queryWrapper.eq("business_id",businessId);
		List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapper);
		return R.data(appriseFilesList);
	}

}
