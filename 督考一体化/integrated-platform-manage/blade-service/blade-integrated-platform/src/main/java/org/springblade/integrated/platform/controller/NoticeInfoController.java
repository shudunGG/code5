package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.NoticeInfo;
import com.vingsoft.vo.NoticeInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.service.INoticeInfoService;
import org.springblade.integrated.platform.wrapper.NoticeInfoWrapper;
import org.springblade.system.user.entity.User;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 11:07
 *  @Description: 通知公告控制器
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/noticeInfo")
@Api(value = "通知公告", tags = "通知公告")
public class NoticeInfoController extends BladeController {

	private final INoticeInfoService noticeInfoService;

	/**
	 * 分页查询
	 * @param query
	 * @param entity
	 * @return
	 */
	@GetMapping("/listPage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "通知公告List", notes = "")
	public R listPage(@ApiIgnore Query query, @RequestParam Map<String, Object> entity ) {
		QueryWrapper<NoticeInfo> ew = Condition.getQueryWrapper(entity, NoticeInfo.class);
		ew.orderByDesc("create_Time","release_Time");
		IPage<NoticeInfo> page = this.noticeInfoService.page(Condition.getPage(query),ew);
		return R.data(NoticeInfoWrapper.build().pageVO(page));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "通知公告List", notes = "")
	public R list(@ApiIgnore  @RequestParam Map<String, Object> entity ) {
		QueryWrapper<NoticeInfo> queryWrapper = Condition.getQueryWrapper(entity, NoticeInfo.class);
		List<NoticeInfo> records = noticeInfoService.list(queryWrapper);
		return R.data(NoticeInfoWrapper.build().listVO(records));
	}

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<NoticeInfoVO> details(@Valid @RequestParam Long id) {
		return R.data(NoticeInfoWrapper.build().entityVO(noticeInfoService.getById(id)));
	}

	/**
	 * 新增
	 * @param noticeInfo
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody NoticeInfoVO noticeInfo , BladeUser user) {
		if(noticeInfo.getReleaseStatus().equals("1")&&ObjectUtil.isNotEmpty(user)){
			noticeInfo.setReleaseUser(user.getUserId());
			noticeInfo.setReleaseUserName(user.getRoleName());
			noticeInfo.setReleaseDept(Long.valueOf(user.getDeptId()));
			noticeInfo.setReleaseTime(DateUtils.getNowDate());
		}
		if(noticeInfo.getFilesUrlJson()!=null && noticeInfo.getFilesUrlJson().size()>0){
			noticeInfo.setFilesUrl(noticeInfo.getFilesUrlJson().toJSONString());
		}else{
			noticeInfo.setFilesUrl("");
		}
		if(noticeInfo.getTextFileUrlJson()!=null && noticeInfo.getTextFileUrlJson().size()>0){
			noticeInfo.setTextFileUrl(noticeInfo.getTextFileUrlJson().toJSONString());
		}else{
			noticeInfo.setTextFileUrl("");
		}
		return R.status(noticeInfoService.save(noticeInfo));
	}

	/**
	 * 修改
	 * @param noticeInfo
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody NoticeInfoVO noticeInfo,BladeUser user) {
		if(noticeInfo.getReleaseStatus().equals("1")&&ObjectUtil.isNotEmpty(user)){
			noticeInfo.setReleaseUser(user.getUserId());
			noticeInfo.setReleaseUserName(user.getRoleName());
			noticeInfo.setReleaseDept(Long.valueOf(user.getDeptId()));
		}
		noticeInfo.setReleaseTime(DateUtils.getNowDate());
		if(noticeInfo.getFilesUrlJson()!=null && noticeInfo.getFilesUrlJson().size()>0){
			noticeInfo.setFilesUrl(noticeInfo.getFilesUrlJson().toJSONString());
		}else{
			noticeInfo.setFilesUrl("");
		}
		if(noticeInfo.getTextFileUrlJson()!=null && noticeInfo.getTextFileUrlJson().size()>0){
			noticeInfo.setTextFileUrl(noticeInfo.getTextFileUrlJson().toJSONString());
		}else{
			noticeInfo.setTextFileUrl("");
		}
		return R.status(noticeInfoService.updateById(noticeInfo));
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入id")
	public R remove(@ApiParam(value = "主键", required = true) @RequestParam Long id) {
		NoticeInfo noticeInfo=new NoticeInfo();
		noticeInfo.setId(id);
		noticeInfo.setIsDeleted(1);
		return R.status(noticeInfoService.removeById(id));
	}
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/batchRemove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "批量删除", notes = "传入ids")
	public R batchRemove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		String id[] =ids.split(",");
		return R.status(noticeInfoService.removeByIds(Arrays.asList(id)));
	}

}
