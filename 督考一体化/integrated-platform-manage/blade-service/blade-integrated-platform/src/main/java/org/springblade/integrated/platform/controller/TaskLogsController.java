package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
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
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.service.ITaskLogsService;
import org.springblade.integrated.platform.wrapper.TaskFilesWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 任务文件 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/18 13:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("TaskLogs")
@Api(value = "任务日志", tags = "任务日志接口控制层代码")
public class TaskLogsController extends BladeController {

	@Autowired
	private ITaskLogsService iTaskLogsService;

	/**
	 * 任务日志
	 * @param taskFiles
	 * @return
	 */
	/*@PostMapping("/saveTaskFiles")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增任务日志", notes = "传入file文件")
	public R save(@Valid @RequestBody List<TaskFiles> taskFiles) throws Exception {

		return R.success("操作成功！");
	}*/

	/**
	 * 分页查询
	 * @param taskFiles
	 * @param query
	 * @return
	 */
	/*@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "任务日志分页查询", notes = "传入taskFiles")
	*//*public R<IPage<TaskFilesVO>> list(TaskFiles taskFiles, Query query) {
		//sql查询条件
		QueryWrapper<TaskFiles> queryWrapper = new QueryWrapper<TaskFiles>();
		queryWrapper.select(" * ");
		queryWrapper.eq(taskFiles.getId()!=null,"id",taskFiles.getId());
		//查询数据，封装分页参数
		IPage<TaskFiles> pages = iTaskFilesService.page(Condition.getPage(query),queryWrapper);
		return R.data(TaskFilesWrapper.build().pageVO(pages));
	}*/




}
