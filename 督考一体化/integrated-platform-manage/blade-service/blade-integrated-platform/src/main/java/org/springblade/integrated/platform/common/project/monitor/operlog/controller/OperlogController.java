package org.springblade.integrated.platform.common.project.monitor.operlog.controller;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.MessageInformation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志记录
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("Operlog")
@Api(value = "操作日志记录", tags = "操作日志记录")
public class OperlogController extends BladeController
{
    @Autowired
    private  IOperLogService operLogService;
	private final IUserClient userClient;
	private final ISysClient sysClient;

	/**
	 * 考核评价工作动态
	 */
	@GetMapping("/detailList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "考核评价工作动态", notes = "传入operlog")
	public R<Map<Integer, Map<String, Object>>> detailList(OperLog operlog,String pageSize) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		if (pageSize == null || pageSize=="") {
			pageSize = "15";
		}
		QueryWrapper<OperLog> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id,business_id,business_table,title,request_method,oper_name,dept_name,oper_url,oper_param,oper_time");
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
		queryWrapper.apply("business_type!=3");
		queryWrapper.orderByDesc("create_time");
		queryWrapper.last("limit "+ pageSize);

		List<OperLog> operLogs = operLogService.list(queryWrapper);
		Map<Integer, Map<String, Object>> map = new HashMap();

		for (int i = 0; i < operLogs.size(); i++) {
			Map<String, Object> map1 =new HashMap<>();
			SM4Crypto sm4 = SM4Crypto.getInstance();
			String oper = "【" +sm4.decrypt(operLogs.get(i).getOperName()) + "】" +operLogs.get(i).getTitle();//
			//操作业务ID
			map1.put("business_id",operLogs.get(i).getBusinessId());
			//操作业务表名
			map1.put("business_table",operLogs.get(i).getBusinessTable());
			//操作人
			map1.put("operUser",operLogs.get(i).getOperName());
			//操作
			map1.put("opertion",oper);
			//请求方法 get post
			map1.put("requestMethod",operLogs.get(i).getRequestMethod());
			//请求路径
			map1.put("operUrl",operLogs.get(i).getOperUrl());
			//请求参数
			map1.put("operParam",operLogs.get(i).getOperParam());
			//操作时间
			map1.put("operTime",operLogs.get(i).getOperTime());
			map.put(i,map1);
		}
		return R.data(map);
	}


	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "日志记录详细信息", notes = "传入operlog")
	public R<List<OperLog>> detail(OperLog operlog) {

		/*String title1 = "查看日志记录详细信息";
		String businessId = String.valueOf(operlog.getBusinessId());
		String businessTable = "指标id";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);*/

		List<OperLog> detail = operLogService.selectOperLogList(operlog);
		return R.data(detail);
	}


}
