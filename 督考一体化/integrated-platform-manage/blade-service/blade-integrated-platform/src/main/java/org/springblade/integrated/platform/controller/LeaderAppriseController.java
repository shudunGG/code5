package org.springblade.integrated.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.UnifyMessage;
import com.vingsoft.vo.LeaderAppriseScoreAppVO;
import com.vingsoft.vo.LeaderAppriseScoreVO;
import com.vingsoft.vo.LeaderAppriseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.LeaderAppriseConstant;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;
import org.springblade.integrated.platform.service.ILeaderAppriseService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.integrated.platform.wrapper.LeaderAppriseWrapper;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考核评价-领导评价 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/LeaderApprise")
@Api(value = "考核评价-领导评价", tags = "领导评价控制层代码")
public class LeaderAppriseController extends BladeController {

	@Resource
	private ILeaderAppriseService leaderAppriseService;
	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserSearchClient iUserSearchClient;
	private final IUnifyMessageService messageService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private IUserSearchClient userSearchClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "领导评价详情", notes = "传入leaderApprise")
	public R<LeaderApprise> detail(LeaderApprise leaderApprise) {
		//sql查询条件
		QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
		queryWrapper.select(" * ");
		queryWrapper.eq(leaderApprise.getId()!=null,"id",leaderApprise.getId());
		LeaderApprise detail = leaderAppriseService.getOne(queryWrapper);

		String title = "查看了考核评价-领导评价详情";
		String businessId = String.valueOf(leaderApprise.getId());
		String businessTable = "LeaderApprise";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		return R.data(detail);
	}

	/**
	 * 详细信息
	 */
	@PostMapping("/detailApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "领导评价详情-app", notes = "传入leaderApprise")
	public R detailApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("领导评价详情-app-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			LeaderApprise leaderApprise = objectMapper.convertValue(jsonParams, LeaderApprise.class);
			//sql查询条件
			QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
			queryWrapper.select(" * ");
			queryWrapper.eq(leaderApprise.getId()!=null,"id",leaderApprise.getId());
			LeaderApprise detail = leaderAppriseService.getOne(queryWrapper);

			String title = "查看了考核评价-领导评价详情";
			String businessId = String.valueOf(leaderApprise.getId());
			String businessTable = "LeaderApprise";
			int businessType = BusinessType.LOOK.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
			JSONObject detailResult = objectMapper.convertValue(detail, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, detailResult.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}

	}



	/**
	 * 分页查询
	 * @param leaderApprise
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-领导评价分页查询", notes = "传入leaderApprise")
	public R<IPage<LeaderAppriseVO>> list(LeaderApprise leaderApprise, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		//领导评价查询条件
		QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
		queryWrapper.select(" * ");
		queryWrapper.eq(StringUtils.isNotNull(leaderApprise.getId()),"id",leaderApprise.getId());
		//deptName
		queryWrapper.eq(StringUtils.isNotNull(leaderApprise.getDeptId()),"dept_id",leaderApprise.getDeptId());
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
			queryWrapper.and(wrapper -> wrapper
				.eq(StringUtils.isNotNull(user.getId()),"apprise_leader_id",user.getId().toString())
				.or().eq(StringUtils.isNotNull(user.getDeptId()),"dept_id",user.getDeptId())
			);
		}
		//satisfaction
		queryWrapper.eq(StringUtils.isNotNull(leaderApprise.getSatisfaction()),"satisfaction",leaderApprise.getSatisfaction());
		//startTime
		queryWrapper.ge(StringUtils.isNotNull(leaderApprise.getStartTime()),"create_time",leaderApprise.getStartTime());
		//endTime
		queryWrapper.le(StringUtils.isNotNull(leaderApprise.getEndTime()),"create_time",leaderApprise.getEndTime());
		//evaluationType
		queryWrapper.le(StringUtils.isNotNull(leaderApprise.getEvaluationType()),"evaluation_type",leaderApprise.getEvaluationType());
		queryWrapper.orderByDesc("create_time");
		//查询数据，封装分页参数
		IPage<LeaderApprise> pages = leaderAppriseService.page(Condition.getPage(query), queryWrapper);

		return R.data(LeaderAppriseWrapper.build().pageVO(pages));
	}

/*
	*/
/**
	 * 新增
	 * @param leaderApprise
	 * @return
	 *//*

	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "考核评价-领导评价新增", notes = "传入leaderApprise对象")
	public R save(@Valid @RequestBody LeaderApprise leaderApprise) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		R<Dept> RDeptName = sysClient.getDept(Long.valueOf(user.getDeptId()));
		//获取当前帐号所有角色的名称
		R<List<String>> roleNames = sysClient.getRoleNames(user.getRoleId());
		Dept deptInfo = RDeptName.getData();
		if (deptInfo != null) {
			//获取考核分组
			String deptGroup = deptInfo.getDeptGroup();
			if (StrUtil.isNotBlank(deptGroup)) {
				//TODO 第一类 1:县区
				if (deptGroup.equals("1")) {
					for (String roleName : roleNames.getData()) {
						if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_SWSZFZYLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.2;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_RDZXSWZZFSJLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQCW)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.25;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQFSZ)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.1;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQRDZXFYJCYQTLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQSZBMLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}
					}
				}//TODO 第二类 2:市直综合部门，3:市直经济部门，4:市直社会发展部门，5:市直其他部门
				else if (deptGroup.equals("2") || deptGroup.equals("3") || deptGroup.equals("4") || deptGroup.equals("5")) {
					for (String roleName : roleNames.getData()) {
						if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_SWSZFZYLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.2;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_RDZXSWZZFSJLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72CW)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.25;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72FSZ)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.1;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_XQZYLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72RDZXFYJCYQTLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}
					}
				}//TODO 第三类 6:市直学校科研院所，7:市属其他事业单位，8:市属国有企业
				else if (deptGroup.equals("6") || deptGroup.equals("7") || deptGroup.equals("8")) {
					for (String roleName : roleNames.getData()) {
						if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_SWSZFZYLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.2;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_RDZXSWZZFSJLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20CW)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.30;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20FSZ)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.15;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20RDZXFYJCYQTLD)) {
							double score  = Double.parseDouble(leaderApprise.getScore()) * 0.20;
							score  = Double.parseDouble(String.format("%.2f", score));
							leaderApprise.setScore(String.valueOf(score));
						}
					}
				}
			} else {
				return R.fail("当前部门未设置考核分组!");
			}
		} else {
			return R.fail("获取部门信息失败！");
		}
		//发送消息
		boolean isok = leaderAppriseService.save(leaderApprise);
		String title = "新增了考核评价-领导评价";
		String businessId = String.valueOf(leaderApprise.getId());
		String businessTable = "LeaderApprise";
		int businessType = BusinessType.INSERT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);

		String receiveUser="";
		boolean isLead = false;
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isLead = true;
				break;
			}
		}

		if(isLead){//说明当前用户是市级四大班子
			List<User> users = userClient.getUserListByRoleId(roleId).getData();
			for (User user1 : users) {
				if(!user.getId().equals(user1.getId())){
					receiveUser += user1.getId()+",";
				}
			}
		}
		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
		List<User> users= iUserSearchClient.listByPostAndDept(authPostId,leaderApprise.getDeptId()).getData();//获取单位下面所有管理员用户
		if(users!=null){
			for(User u : users){
				receiveUser += u.getId()+",";
			}
		}
		if(isLead){
			users= iUserSearchClient.listByPostAndDept(leadPostId,leaderApprise.getDeptId()).getData();//获取单位下面所有领导用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
			users = userClient.getUserListByDeptId(leaderApprise.getDeptId()).getData();//获取该单位所有分管领导
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
		}
		//发送消息
		String content = "【"+user.getRealName()+"】领导对【"+leaderApprise.getDeptName()+"】进行了领导评价";
		UnifyMessage message = new UnifyMessage();
		message.setMsgId(Long.valueOf(leaderApprise.getId()));//消息主键（业务主键）
		message.setMsgTitle("领导评价");//消息标题
		message.setMsgType("48");//消息类型，字典编码：web_message_type
		message.setMsgPlatform("web");//平台：web或app
		message.setReceiveUser(quchong(receiveUser));
		message.setMsgIntro(content);//消息简介
		message.setMsgSubitem("领导评价");//消息分项
		message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
		message.setCreateTime(new Date());
		messageService.sendMessageInfo(message);

		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
		message.setTwoLevelType("50");//领导评价
		messageService.sendMessageInfo(message);

		return R.status(isok);
	}
*/

	/**
	 * 编辑
	 * @param leaderApprise
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "考核评价-领导评价修改", notes = "传入leaderApprise")
	public R update(@Valid @RequestBody LeaderApprise leaderApprise) {
		boolean isok = leaderAppriseService.updateById(leaderApprise);
		String title = "修改了考核评价-领导评价";
		String businessId = String.valueOf(leaderApprise.getId());
		String businessTable = "LeaderApprise";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		return R.status(isok);
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "考核评价-领导评价删除", notes = "传入加分表ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam("ids") String ids) {
		boolean isok = leaderAppriseService.removeByIds(Func.toLongList(ids));
		String title = "删除了考核评价-领导评价";
		String businessId = ids;
		String businessTable = "LeaderApprise";
		int businessType = BusinessType.DELETE.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title, businessId, businessTable, businessType);
		}		return R.status(isok);
	}

	/**
     * 导出
	 * @param leaderApprise
     * @param response
	 */
	@GetMapping("export-leaderApprise")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "考核评价-领导评价导出", notes = "传入leaderApprise")
	public void exportUser(LeaderApprise leaderApprise, HttpServletResponse response) {
		//sql查询条件
		QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
		queryWrapper.select(" * ");
		queryWrapper.eq(leaderApprise.getId()!=null,"id",leaderApprise.getId());
		List<LeaderAppriseExcel> list = leaderAppriseService.exportLeaderApprise(queryWrapper);
		ExcelUtil.export(response, "市级分管领导评价-" + DateUtil.time(), "市级分管领导评价", list, LeaderAppriseExcel.class);

		String title = "导出了考核评价-领导评价";
		String businessId = String.valueOf(leaderApprise.getId());
		String businessTable = "LeaderApprise";
		int businessType = BusinessType.EXPORT.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
	}


	public String quchong(String res){
		String[] receiveUsers = res.split(",");
		List<String> receiveList = new ArrayList<>();
		String receiveUser = "";
		for (String s : receiveUsers) {
			if(!receiveList.contains(s)&&!s.equals(AuthUtil.getUserId().toString())){
				receiveList.add(s);
			}
		}
		for (String s : receiveList) {
			receiveUser += s+",";
		}
		return receiveUser;
	}

	/**
	 * 领导评价新页面查询方法
	 * @param entity
	 * @return
	 */
	@GetMapping("/appriseList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-领导评价查询", notes = "")
	public R appriseList(@ApiIgnore @RequestParam Map<String, Object> entity) {

		String evaluationType= (String) entity.get("evaluationType");
		String deptGroup= (String) entity.get("dictKey");
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		List<LeaderAppriseScoreVO> list= new ArrayList<LeaderAppriseScoreVO>();
		Calendar calendar = Calendar.getInstance();

		//int year = calendar.get(Calendar.YEAR);
		if(!StringUtils.isNotBlank((String)entity.get("stageYear"))){
			return  R.fail("传入的年份为空");
		}

		 int year = Integer.valueOf((String)entity.get("stageYear"));
		// 传入年份
		String appriseYear = (String) entity.get("appriseYear");
		if (appriseYear == null || appriseYear.length() < 1) {
			appriseYear = String.valueOf(year);
		}
		List<String> quarterListStr = new ArrayList<>();
		if (year < 2024) {
			quarterListStr.add("1");
			quarterListStr.add("2");
			quarterListStr.add("3");
			quarterListStr.add("4");
		} else if (year == 2024) {
			quarterListStr.add("1");
			quarterListStr.add("2");
			quarterListStr.add("6");
		} else {
			quarterListStr.add("5");
			quarterListStr.add("6");
		}
		String manageDept = "";
		/*//如果是市委书记（卢小亨）的账号，分管部门就显示6个县区
		String swsjRoleId =PropConstant.getSwsjRoleId();
		//如果是市长（赵立香）的账号，分管部门就增加6个县区
		String szRoleId =PropConstant.getSzRoleId();
		if (swsjRoleId.equals(String.valueOf(user.getId()))) {
			//获取分组后的单位id
			R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", "1");
			List<Dept> deptList = deptByGroup.getData();
			for (int i = 0; i < deptList.size(); i++) {
				manageDept += deptList.get(i).getId()+",";
			}
		}else if (szRoleId.equals(String.valueOf(user.getId()))) {
			//获取当前用户的分管部门
			manageDept = user.getManageDept()+",";
			//获取分组后的单位id
			R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", "1");
			List<Dept> deptList = deptByGroup.getData();
			for (int i = 0; i < deptList.size(); i++) {
				manageDept += deptList.get(i).getId()+",";
			}
		} else {
			//获取当前用户的分管部门
			manageDept = user.getManageDept();
		}*/
		//根据考核分组字典值获取分组单位id
		R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", deptGroup, appriseYear);
		List<Dept> deptList = deptByGroup.getData();
		for (int i = 0; i < deptList.size(); i++) {
			manageDept += deptList.get(i).getId()+",";
		}
		String[] manageDeptStr = manageDept.split(",");
		List<LeaderApprise> LeaderAppriseList= new ArrayList<LeaderApprise>();
		if(StringUtils.isNotBlank(manageDept)){
			//获取当前年份
			for(String id : manageDeptStr){
				LeaderAppriseScoreVO leaderAppriseScoreVO = new LeaderAppriseScoreVO();
				//查询部门的领导评价数据
				QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
				queryWrapper.select(" * ");
				queryWrapper.eq("dept_id",id);
				queryWrapper.eq("evaluation_type",evaluationType);
				queryWrapper.eq("apprise_leader_id",user.getId());
				queryWrapper.eq("apprise_year",year);
				Dept dept = SysCache.getDept(Long.valueOf(id));
				if (dept != null) {
					String deptName = dept.getDeptName();
					String sort = dept.getSort()+"";
					leaderAppriseScoreVO.setSort(sort);
					leaderAppriseScoreVO.setDeptName(deptName);
					leaderAppriseScoreVO.setDeptId(id);
					if("1".equals(evaluationType)){//年度
						LeaderAppriseList= leaderAppriseService.list(queryWrapper);
						if(LeaderAppriseList.size() > 0) {
							for (LeaderApprise la : LeaderAppriseList) {
								//如果创建时间超过24小时，不允许修改
								long cha = System.currentTimeMillis() - la.getCreateTime().getTime();
								double result = cha * 1.0 / (1000 * 60 * 60);
								if (result > 24) {
									la.setIsUpt("N");
								} else {
									la.setIsUpt("Y");
								}
							}
							for (LeaderApprise la : LeaderAppriseList) {
								if (la.getId() != null) {
									double score  = Double.parseDouble(la.getScore()) / Double.parseDouble(la.getScorePart());
									score  = Double.parseDouble(String.format("%.2f", score));
									la.setScore(String.valueOf(score));
								}
							}
							leaderAppriseScoreVO.setLeaderAppriseList(LeaderAppriseList);
						}else{
							List<LeaderApprise> leaderAppriseListInfo= new ArrayList<LeaderApprise>();
							LeaderApprise leaderAppriseInfo = new LeaderApprise();
							leaderAppriseInfo.setAppriseYear(String.valueOf(year));
							leaderAppriseListInfo.add(leaderAppriseInfo);
							leaderAppriseScoreVO.setLeaderAppriseList(leaderAppriseListInfo);
						}
					}
					else{//季度
						queryWrapper.isNotNull("apprise_quarter");
						LeaderAppriseList= leaderAppriseService.list(queryWrapper);
						if(LeaderAppriseList.size()>0) {
							String quarterStr="";
							for (LeaderApprise la : LeaderAppriseList) {
								quarterStr+=la.getAppriseQuarter();
								//如果创建时间超过24小时，不允许修改
								long cha = System.currentTimeMillis() - la.getCreateTime().getTime();
								double result = cha * 1.0 / (1000 * 60 * 60);
								if (result > 24) {
									la.setIsUpt("N");
								} else {
									la.setIsUpt("Y");
								}
							}

							for (int i = 0; i < quarterListStr.size(); i++) {
								if (quarterStr.indexOf(quarterListStr.get(i)) == -1) {
									LeaderApprise leaderApprise = new LeaderApprise();
									leaderApprise.setAppriseQuarter(quarterListStr.get(i));
									leaderApprise.setAppriseYear(String.valueOf(year));
									LeaderAppriseList.add(leaderApprise);
								}
							}
							//排序（升序）
							List<LeaderApprise> listSort = LeaderAppriseList.stream().sorted(Comparator.comparing(LeaderApprise::getAppriseQuarter)).collect(Collectors.toList());
							for (LeaderApprise la : listSort) {
								if (la.getId() != null) {
									if (year == 2022) {
										la.setScorePart("0.5");
									}
									if (la.getScorePart() == null || la.getScorePart().length() < 1 || Double.parseDouble(la.getScorePart()) == 0.0) {
										return R.fail("评分权重不存在或为0");
									}
									double score  = Double.parseDouble(la.getScore()) / Double.parseDouble(la.getScorePart());
									score  = Double.parseDouble(String.format("%.2f", score));
									la.setScore(String.valueOf(score));
								}
							}
							leaderAppriseScoreVO.setLeaderAppriseList(listSort);
						}else{
							List<LeaderApprise> leaderAppriseListInfo= new ArrayList<LeaderApprise>();
							/*LeaderApprise lap1 = new LeaderApprise();
							lap1.setAppriseQuarter("1");
							lap1.setAppriseYear(String.valueOf(year));

							LeaderApprise lap2 = new LeaderApprise();
							lap2.setAppriseQuarter("2");
							lap2.setAppriseYear(String.valueOf(year));

							LeaderApprise lap3 = new LeaderApprise();
							lap3.setAppriseQuarter("3");
							lap3.setAppriseYear(String.valueOf(year));

							LeaderApprise lap4 = new LeaderApprise();
							lap4.setAppriseQuarter("4");
							lap4.setAppriseYear(String.valueOf(year));

							LeaderApprise lap5 = new LeaderApprise();
							lap3.setAppriseQuarter("5");
							lap3.setAppriseYear(String.valueOf(year));

							LeaderApprise lap6 = new LeaderApprise();
							lap4.setAppriseQuarter("6");
							lap4.setAppriseYear(String.valueOf(year));*/
							for (int i = 0; i < quarterListStr.size(); i++) {
								LeaderApprise leaderApprise = new LeaderApprise();
								leaderApprise.setAppriseQuarter(quarterListStr.get(i));
								leaderApprise.setAppriseYear(String.valueOf(year));
								leaderAppriseListInfo.add(leaderApprise);
							}
/*
							leaderAppriseListInfo.add(lap1);
							leaderAppriseListInfo.add(lap2);
							leaderAppriseListInfo.add(lap3);
							leaderAppriseListInfo.add(lap4);
							leaderAppriseListInfo.add(lap5);
							leaderAppriseListInfo.add(lap6);*/
							leaderAppriseScoreVO.setLeaderAppriseList(leaderAppriseListInfo);
						}
					}
					list.add(leaderAppriseScoreVO);
				}
			}
		}else{
			return  R.fail("请先维护分管部门");
		}
		//部门升序
		if (list.size() > 0) {
			for (LeaderAppriseScoreVO las : list) {
				if (las != null) {
					if (StringUtils.isNotNull(las.getSort())) {
						las.setSort(las.getSort().toString());
					} else {
						las.setSort("100");
					}
				} else {
					las.setSort("100");
				}
			}
		}
		//按照部门的sort排序
		list.sort(new Comparator<LeaderAppriseScoreVO>() {
			@Override
			public int compare(LeaderAppriseScoreVO o1, LeaderAppriseScoreVO o2) {
				Integer sort1 = Integer.parseInt(o1.getSort());
				Integer sort2 = Integer.parseInt(o2.getSort());
				return sort1.compareTo(sort2);
			}
		});
		return R.data(list);
	}


	/**
	 *
	 * 1 第一季度 2 第二季度 3 第三季度 4 第四季度 5 上半年 6 下半年
	 * @param date
	 * @return
	 */
	public static int getSeason(java.util.Date date) {
		int season = 0;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		if (year < 2024) {
			switch (month) {
				case Calendar.JANUARY:
				case Calendar.FEBRUARY:
				case Calendar.MARCH:
					season = 1;
					break;
				case Calendar.APRIL:
				case Calendar.MAY:
				case Calendar.JUNE:
					season = 2;
					break;
				case Calendar.JULY:
				case Calendar.AUGUST:
				case Calendar.SEPTEMBER:
					season = 3;
					break;
				case Calendar.OCTOBER:
				case Calendar.NOVEMBER:
				case Calendar.DECEMBER:
					season = 4;
					break;
				default:
					break;
			}
		} else if (year == 2024) {
			switch (month) {
				case Calendar.JANUARY:
				case Calendar.FEBRUARY:
				case Calendar.MARCH:
					season = 1;
					break;
				case Calendar.APRIL:
				case Calendar.MAY:
				case Calendar.JUNE:
					season = 2;
					break;
				case Calendar.JULY:
				case Calendar.AUGUST:
				case Calendar.SEPTEMBER:
				case Calendar.OCTOBER:
				case Calendar.NOVEMBER:
				case Calendar.DECEMBER:
					season = 6;
					break;
				default:
					break;
			}
		} else {
			season = month <= Calendar.JUNE ? 5 : 6;
		}

		return season;
	}


	/**
	 * 新增or修改
	 * @param leaderAppriseScoreVO
	 * @return
	 */
	@PostMapping("/saveOrUpdate")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "考核评价-领导评价新增", notes = "传入leaderAppriseScoreVO对象")
	@Transactional(rollbackFor = Exception.class)
	public R saveOrUpdate(@Valid @RequestBody LeaderAppriseScoreVO leaderAppriseScoreVO) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		R<Dept> RDeptName = sysClient.getDept(Long.valueOf(user.getDeptId()));
		//获取当前帐号所有角色的名称
		R<List<String>> roleNames = sysClient.getRoleNames(user.getRoleId());
		Dept deptInfo = RDeptName.getData();
		boolean isok=false;
		String messageName="";
		String title = "";
	//	List<LeaderApprise>  leaderAppriseList = leaderAppriseScoreVO.getLeaderAppriseList();

		List list = new ArrayList();
		//如果只传了年份
		if(leaderAppriseScoreVO.getLeaderAppriseList() != null && !leaderAppriseScoreVO.getLeaderAppriseList().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList());
		}
		//一个季度一个list，全部遍历一遍
		if(leaderAppriseScoreVO.getLeaderAppriseList1() != null && !leaderAppriseScoreVO.getLeaderAppriseList1().isEmpty()){
		list.add(leaderAppriseScoreVO.getLeaderAppriseList1());
		}
		if(leaderAppriseScoreVO.getLeaderAppriseList2() != null && !leaderAppriseScoreVO.getLeaderAppriseList2().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList2());
		}
		if(leaderAppriseScoreVO.getLeaderAppriseList3() != null && !leaderAppriseScoreVO.getLeaderAppriseList3().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList3());
		}
		if(leaderAppriseScoreVO.getLeaderAppriseList4() != null && !leaderAppriseScoreVO.getLeaderAppriseList4().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList4());
		}
		if(leaderAppriseScoreVO.getLeaderAppriseList5() != null && !leaderAppriseScoreVO.getLeaderAppriseList5().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList5());
		}
		if(leaderAppriseScoreVO.getLeaderAppriseList6() != null && !leaderAppriseScoreVO.getLeaderAppriseList6().isEmpty()){
			list.add(leaderAppriseScoreVO.getLeaderAppriseList6());
		}



		for(int i = 0;i < list.size(); i ++) {
			List<LeaderApprise>	leaderAppriseList = (List<LeaderApprise>) list.get(i);

			if (leaderAppriseList.size() == 0) {
				return R.fail("数据不能为空！");
			}

			for (LeaderApprise leaderApprise : leaderAppriseList) {
				String appriseId = leaderApprise.getId() + "";
				String evaluationType = leaderApprise.getEvaluationType();


				if ("1".equals(evaluationType)) {
					messageName = "年度";
				} else {
					messageName = "季度";
				}

				if (appriseId != null && !"".equals(appriseId) && !"null".equals(appriseId) && !"-1".equals(appriseId)) {//修改
					title = "修改了考核评价-领导评价";
					LeaderApprise laOld = leaderAppriseService.getById(appriseId);
					laOld.setSatisfaction(leaderApprise.getSatisfaction());
					laOld.setStatus(1);
					//TODO 根据不同的类型和角色，打不同的分数
					if (deptInfo != null) {
						//获取考核分组
						String deptGroup = leaderAppriseScoreVO.getDictKey();
						if (StrUtil.isNotBlank(deptGroup)) {
							//TODO 第一类 1:县区
							if (deptGroup.equals("1")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_SWSZFZYLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_RDZXSWZZFSJLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.35;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_RDZXSWZZFSJLD);
//										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScorePart(String.valueOf(0.35));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQCW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQCW);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQFSZ)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQFSZ);
//										laOld.setScorePart(String.valueOf(0.1));
										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQRDZXFYJCYQTLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQRDZXFYJCYQTLD);
//										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScorePart(String.valueOf(0.1));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQSZBMLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQSZBMLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									}
								}
							}
							//TODO 第二类 2:市直综合部门，3:市直经济部门，4:市直社会发展部门，5:市直其他部门, 9:党群综合部门, 10:政府工作部门
//							else if (deptGroup.equals("2") || deptGroup.equals("3") || deptGroup.equals("4") || deptGroup.equals("5")) {
							else if (isExsit(deptGroup,"2", "3", "4", "5", "9", "10")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_SWSZFZYLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_RDZXSWZZFSJLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.35;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_RDZXSWZZFSJLD);
//										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScorePart(String.valueOf(0.35));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72CW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72CW);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72FSZ)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72FSZ);
//										laOld.setScorePart(String.valueOf(0.1));
										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_XQZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_XQZYLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72RDZXFYJCYQTLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72RDZXFYJCYQTLD);
//										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScorePart(String.valueOf(0.1));
										laOld.setScore(String.valueOf(score));
									}
								}
							}
							//TODO 第三类 6:市直学校科研院所，7:市属其他事业单位，8:市属国有企业
							else if (deptGroup.equals("6") || deptGroup.equals("7") || deptGroup.equals("8")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_SWSZFZYLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_RDZXSWZZFSJLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.45;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_RDZXSWZZFSJLD);
//										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScorePart(String.valueOf(0.45));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20CW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.3;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20CW);
										laOld.setScorePart(String.valueOf(0.3));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20FSZ)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20FSZ);
										laOld.setScorePart(String.valueOf(0.15));
										laOld.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20RDZXFYJCYQTLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										laOld.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20RDZXFYJCYQTLD);
										laOld.setScorePart(String.valueOf(0.2));
										laOld.setScore(String.valueOf(score));
									}
								}
							}else {
								return R.fail("当前部门未设置考核分组!");
							}
						} else {
							return R.fail("当前部门未设置考核分组!");
						}
					} else {
						return R.fail("获取部门信息失败！");
					}
					isok = leaderAppriseService.updateById(laOld);
				} else {//新增
					title = "新增了考核评价-领导评价";
					leaderApprise.setScore(leaderApprise.getScore());
					leaderApprise.setDeptName(SysCache.getDeptName(Long.valueOf(leaderApprise.getDeptId())));
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					leaderApprise.setAppriseLeader(userNameDecrypt);
					leaderApprise.setAppriseLeaderId(String.valueOf(user.getId()));
					leaderApprise.setAppriseDate(new Date());
					leaderApprise.setId(null);
					leaderApprise.setStatus(1);

					//TODO 根据不同的类型和角色，打不同的分数
					if (deptInfo != null) {
						//获取考核分组
						String deptGroup = leaderAppriseScoreVO.getDictKey();
						if (StrUtil.isNotBlank(deptGroup)) {
							//TODO 第一类 1:县区
							if (deptGroup.equals("1")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_SWSZFZYLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_RDZXSWZZFSJLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.35;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_RDZXSWZZFSJLD);
//										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScorePart(String.valueOf(0.35));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQCW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQCW);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQFSZ)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQFSZ);
//										leaderApprise.setScorePart(String.valueOf(0.1));
										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQRDZXFYJCYQTLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQRDZXFYJCYQTLD);
//										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScorePart(String.valueOf(0.1));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.XQ_APPRISE_XQSZBMLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.XQ_APPRISE_XQSZBMLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									}
								}
							}
							//TODO 第二类 2:市直综合部门，3:市直经济部门，4:市直社会发展部门，5:市直其他部门, 9:党群综合部门, 10:政府工作部门
//							else if (deptGroup.equals("2") || deptGroup.equals("3") || deptGroup.equals("4") || deptGroup.equals("5")) {
							else if (isExsit(deptGroup,"2", "3", "4", "5", "9", "10")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_SWSZFZYLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_RDZXSWZZFSJLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.35;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_RDZXSWZZFSJLD);
//										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScorePart(String.valueOf(0.35));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72CW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72CW);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72FSZ)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72FSZ);
//										leaderApprise.setScorePart(String.valueOf(0.1));
										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_XQZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_XQZYLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.SZ_APPRISE_72RDZXFYJCYQTLD)) {
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.1;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.SZ_APPRISE_72RDZXFYJCYQTLD);
//										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScorePart(String.valueOf(0.1));
										leaderApprise.setScore(String.valueOf(score));
									}
								}
							}
							//TODO 第三类 6:市直学校科研院所，7:市属其他事业单位，8:市属国有企业
							else if (deptGroup.equals("6") || deptGroup.equals("7") || deptGroup.equals("8")) {
								for (String roleName : roleNames.getData()) {
									if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_SWSZFZYLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_SWSZFZYLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_RDZXSWZZFSJLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.45;
//										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_RDZXSWZZFSJLD);
//										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScorePart(String.valueOf(0.45));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20CW)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.3;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20CW);
										leaderApprise.setScorePart(String.valueOf(0.3));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20FSZ)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.15;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20FSZ);
										leaderApprise.setScorePart(String.valueOf(0.15));
										leaderApprise.setScore(String.valueOf(score));
									} else if (roleName.equals(LeaderAppriseConstant.QTSZ_APPRISE_20RDZXFYJCYQTLD)) {
										double score = Double.parseDouble(leaderApprise.getScore()) * 0.2;
										score = Double.parseDouble(String.format("%.2f", score));
										leaderApprise.setAppriseRolename(LeaderAppriseConstant.QTSZ_APPRISE_20RDZXFYJCYQTLD);
										leaderApprise.setScorePart(String.valueOf(0.2));
										leaderApprise.setScore(String.valueOf(score));
									}
								}
							} else {
								return R.fail("当前部门未设置考核分组!");
							}
						} else {
							return R.fail("当前部门未设置考核分组!");
						}
					} else {
						return R.fail("获取部门信息失败！");
					}
					isok = leaderAppriseService.save(leaderApprise);
				}
				String businessId = String.valueOf(leaderApprise.getId());
				String businessTable = "LeaderApprise";
				int businessType = BusinessType.INSERT.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title, businessId, businessTable, businessType);

				String receiveUser = "";
				boolean isLead = false;
				String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",", "");
				String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
				for (String id : roleIds) {
					if (id.equals(roleId)) {
						isLead = true;
						break;
					}
				}
				if (isLead) {//说明当前用户是市级四大班子
					List<User> users = userClient.getUserListByRoleId(roleId).getData();
					for (User user1 : users) {
						if (!user.getId().equals(user1.getId())) {
							receiveUser += user1.getId() + ",";
						}
					}
				}
				String authPostId = sysClient.getPostIdsByFuzzy("000000", "管理员").getData();//获取管理员岗位id
				String leadPostId = sysClient.getPostIdsByFuzzy("000000", "部门领导").getData();//获取领导岗位id
			/*List<User> users= iUserSearchClient.listByPostAndDept(authPostId,leaderApprise.getDeptId()).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
			if(isLead){
				users= iUserSearchClient.listByPostAndDept(leadPostId,leaderApprise.getDeptId()).getData();//获取单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users = userClient.getUserListByDeptId(leaderApprise.getDeptId()).getData();//获取该单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
			}*/
				//发送消息
				if (StringUtils.isNotBlank(receiveUser)) {
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					String content = "【" + userNameDecrypt + "】领导对【" + leaderApprise.getDeptName() + "】进行了领导评价";
					UnifyMessage message = new UnifyMessage();
					message.setMsgId(Long.valueOf(leaderApprise.getId()));//消息主键（业务主键）
					message.setMsgTitle("领导评价");//消息标题
					message.setMsgType("48");//消息类型，字典编码：web_message_type
					message.setMsgPlatform("web");//平台：web或app
					message.setReceiveUser(quchong(receiveUser));
					message.setMsgIntro(content);//消息简介
					message.setMsgSubitem(messageName + "领导评价");//消息分项
					message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					message.setCreateTime(new Date());
					messageService.sendMessageInfo(message);

					message.setId(null);
					message.setMsgPlatform("app");
					message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
					message.setTwoLevelType("50");//领导评价
					messageService.sendMessageInfo(message);
				}
			}
		}


		return R.status(isok);
	}

	/** 匹配部门是否存在指定数字下*/
	private boolean isExsit(String deptGroup, String ...group) {
		for (String s : group) {
			if (deptGroup.equals(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 领导评价新页面查询方法（APP使用）
	 * @param entity
	 * @return
	 */
	@GetMapping("/appriseForAppList")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-领导评价APP查询", notes = "")
	public R<List<LeaderAppriseScoreAppVO>> appriseForAppList(@ApiIgnore @RequestParam Map<String, Object> entity) {
		// 传入年份
		String appriseYear = (String) entity.get("appriseYear");

		String evaluationType= (String) entity.get("evaluationType");
		if(StringUtils.isEmpty(evaluationType)){
			return R.fail("指标分类参数不能为空");
		}
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		List<LeaderAppriseScoreAppVO> dataList = new ArrayList<LeaderAppriseScoreAppVO>();

		Calendar calendar = Calendar.getInstance();



		//int year = calendar.get(Calendar.YEAR);
		if(!StringUtils.isNotBlank((String)entity.get("stageYear"))){
			return  R.fail("传入的年份为空");
		}

		int year = Integer.valueOf((String)entity.get("stageYear"));

		String manageDept = "";
		//如果是卢小亨(市委书记)的账号，分管部门就显示6个县区
		String swsjRoleId =PropConstant.getSwsjRoleId();
		//如果是市长（赵立香）的账号，分管部门就增加6个县区
		String szRoleId =PropConstant.getSzRoleId();
		if (swsjRoleId.equals(String.valueOf(user.getId()))) {
			//获取分组后的单位id
			R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", "1", appriseYear);
			List<Dept> deptList = deptByGroup.getData();
			for (int i = 0; i < deptList.size(); i++) {
				manageDept += deptList.get(i).getId()+",";
			}
		}else if (szRoleId.equals(String.valueOf(user.getId()))) {
			//获取当前用户的分管部门
			manageDept = user.getManageDept()+",";
			//获取分组后的单位id
			R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", "1", appriseYear);
			List<Dept> deptList = deptByGroup.getData();
			for (int i = 0; i < deptList.size(); i++) {
				manageDept += deptList.get(i).getId()+",";
			}
		} else {
			//获取当前用户的分管部门
			manageDept = user.getManageDept();
		}
		/*//根据考核分组字典值获取分组单位id
		String deptGroup= (String) entity.get("dictKey");
		R<List<Dept>> deptByGroup = sysClient.getDeptByGroup("000000", deptGroup);
		List<Dept> deptList = deptByGroup.getData();
		for (int i = 0; i < deptList.size(); i++) {
			manageDept += deptList.get(i).getId()+",";
		}
		String[] manageDeptStr = manageDept.split(",");
		List<LeaderApprise> LeaderAppriseList= new ArrayList<LeaderApprise>();
*/


		String[] manageDeptStr = manageDept.split(",");
		if(StringUtils.isNotBlank(manageDept)) {
			if("1".equals(evaluationType)){
				//获取当前年份
				List<LeaderApprise> list= new ArrayList<LeaderApprise>();
				for(String id : manageDeptStr){
					//根据部门id查询部门
					Dept dept = SysCache.getDept(Long.valueOf(id));
					String deptName = dept.getDeptName();
					String sort = dept.getSort()+"";
					//查询部门的领导评价数据
					QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
					queryWrapper.select(" * ");
					queryWrapper.eq("dept_id",id);
					queryWrapper.eq("evaluation_type",evaluationType);
					queryWrapper.eq("apprise_leader_id",user.getId());
					queryWrapper.eq("apprise_year",year);
					LeaderApprise leaderAppriseOne= leaderAppriseService.getOne(queryWrapper);
					if(leaderAppriseOne!=null) {
						//如果创建时间超过24小时，不允许修改
						long cha = System.currentTimeMillis() - leaderAppriseOne.getCreateTime().getTime();
						double result = cha * 1.0 / (1000 * 60 * 60);
						if (result > 24) {
							leaderAppriseOne.setIsUpt("N");
						} else {
							leaderAppriseOne.setIsUpt("Y");
						}
						leaderAppriseOne.setSort(sort);
						list.add(leaderAppriseOne);
					}else{
						LeaderApprise leaderApprise = new LeaderApprise();
						leaderApprise.setAppriseYear(String.valueOf(year));
						leaderApprise.setDeptId(id);
						leaderApprise.setDeptName(deptName);
						leaderApprise.setSort(sort);
						list.add(leaderApprise);
					}
				}
				listForSort(list);//排序
				LeaderAppriseScoreAppVO ls = new LeaderAppriseScoreAppVO();
				ls.setAppriseYear(String.valueOf(year));
				ls.setLeaderAppriseList(list);
				dataList.add(ls);
			}else{
				//查询四个季度的数据
				for(int i=1;i<5;i++){
					List<LeaderApprise> list= new ArrayList<LeaderApprise>();
					for(String id : manageDeptStr){
						Dept dept = SysCache.getDept(Long.valueOf(id));
						String deptName = dept.getDeptName();
						String sort = dept.getSort()+"";
						//查询部门的领导评价数据
						QueryWrapper<LeaderApprise> queryWrapper = new QueryWrapper<LeaderApprise>();
						queryWrapper.select(" * ");
						queryWrapper.eq("dept_id",id);
						queryWrapper.eq("evaluation_type",evaluationType);
						queryWrapper.eq("apprise_leader_id",user.getId());
						queryWrapper.eq("apprise_year",year);
						queryWrapper.eq("apprise_quarter",i);
						LeaderApprise leaderApprise1 = leaderAppriseService.getOne(queryWrapper);
						if(leaderApprise1!=null) {
							//如果创建时间超过24小时，不允许修改
							long cha = System.currentTimeMillis() - leaderApprise1.getCreateTime().getTime();
							double result = cha * 1.0 / (1000 * 60 * 60);
							if (result > 24) {
								leaderApprise1.setIsUpt("N");
							} else {
								leaderApprise1.setIsUpt("Y");
							}
							leaderApprise1.setSort(sort);
							list.add(leaderApprise1);
						}else{
							LeaderApprise leaderApprise = new LeaderApprise();
							leaderApprise.setAppriseQuarter(String.valueOf(i));
							leaderApprise.setAppriseYear(String.valueOf(year));
							leaderApprise.setDeptId(id);
							leaderApprise.setDeptName(deptName);
							leaderApprise.setSort(sort);
							list.add(leaderApprise);
						}
					}
					listForSort(list);//排序
					LeaderAppriseScoreAppVO ls = new LeaderAppriseScoreAppVO();
					ls.setAppriseYear(String.valueOf(year));
					ls.setAppriseQuarter(String.valueOf(i));
					ls.setLeaderAppriseList(list.stream().sorted(Comparator.comparing(LeaderApprise::getAppriseQuarter)).collect(Collectors.toList()));
					dataList.add(ls);
				}
			}
		}else{
			return  R.fail("请先维护分管部门");
		}
		return R.data(dataList);
	}

	/**
	 * 按部门排序字段进行升序
	 * @param list
	 */
	public  void listForSort(List<LeaderApprise> list){
		if (list.size() > 0) {
			for (LeaderApprise lap : list) {
				if (lap != null) {
					if (StringUtils.isNotNull(lap.getSort())) {
						lap.setSort(lap.getSort().toString());
					} else {
						lap.setSort("100");
					}
				} else {
					lap.setSort("100");
				}
			}
		}
		//按照部门的sort排序
		list.sort(new Comparator<LeaderApprise>() {
			@Override
			public int compare(LeaderApprise o1, LeaderApprise o2) {
				Integer sort1 = Integer.parseInt(o1.getSort());
				Integer sort2 = Integer.parseInt(o2.getSort());
				return sort1.compareTo(sort2);
			}
		});
	}
}
