package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.ProjectFiles;
import com.vingsoft.entity.ProjectLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.service.IProjectLogService;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 项目管理日志
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/projectLog")
@Api(value = "项目管理日志", tags = "项目管理日志")
public class ProjectLogController extends BladeController {

	private final IProjectLogService projectLogService;

	private final IUserClient userClient;
	private final ISysClient sysClient;

	/**
	 * 分页查询
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理日志", notes = "")
	public R<IPage<ProjectLog>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		query.setDescs("create_time");
		IPage<ProjectLog> pages = projectLogService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectLog.class));
		return R.data(pages);
	}


	/**
	 * 动态接口
	 * @return
	 */
	@GetMapping("/detailList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "项目管理日志", notes = "")
	public R<IPage<ProjectLog>> list1(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		QueryWrapper<ProjectLog> queryWrapper = new QueryWrapper<>();
		//除四大班子之外只能看本部门的信息
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
			queryWrapper.and(wrapper -> wrapper
				.eq(StringUtils.isNotNull(user.getDeptId()),"create_dept",user.getDeptId().toString())
			);
		}
		query.setDescs("create_time");

		IPage<ProjectLog> pages = projectLogService.page(Condition.getPage(query), Condition.getQueryWrapper(entity, ProjectLog.class));

		return R.data(pages);
	}





	/**
	 * 新增
	 * @param projectLog
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody ProjectLog projectLog) {
		return R.status(projectLogService.save(projectLog));
	}

	/**
	 * 修改
	 * @param projectLog
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody ProjectLog projectLog) {
		return R.status(projectLogService.updateById(projectLog));
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
		return R.status(projectLogService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return`
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(projectLogService.removeByIds(Arrays.asList(id)));
	}

}
