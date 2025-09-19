package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import com.vingsoft.vo.ScoreMinusVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.ScoreMinusExcel;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springblade.integrated.platform.service.IScoreMinusService;
import org.springblade.integrated.platform.service.ISupervisionSubmitAuditService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.integrated.platform.wrapper.ScoreAddWrapper;
import org.springblade.integrated.platform.wrapper.ScoreMinusWrapper;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 考核评价-加减分纪实-减分项 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/9 12:00
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/ScoreMinus")
@Api(value = "考核评价-加减分纪实-减分项", tags = "减分项控制层代码")
public class ScoreMinusController extends BladeController {

	@Resource
	private IScoreMinusService scoreMinusService;
	private final IAppriseFilesService iAppriseFilesService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Resource
	private IUnifyMessageService unifyMessageService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserSearchClient iUserSearchClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "减分项详情", notes = "传入scoreMinus")
	public R<ScoreMinus> detail(ScoreMinus scoreMinus) {
		//sql查询条件
		QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
		queryWrapper.select(" * ");
		queryWrapper.eq(scoreMinus.getId()!=null,"id",scoreMinus.getId());
		ScoreMinus detail = scoreMinusService.getOne(queryWrapper);

		String title1 = "查看了考核评价-减分项详情";
		String businessId = String.valueOf(scoreMinus.getId());
		String businessTable = "ScoreMinus";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		//关联文件表
		QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
		filesQueryWrapper.select(" * ");
		filesQueryWrapper.eq(detail.getId() != null,"business_id",detail.getId());
		List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
		detail.setAppriseFilesList(list);

		return R.data(detail);
	}

	/**
	 * 详细信息-app
	 */
	@PostMapping("/detailApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "减分项详情-app", notes = "传入scoreMinus")
	public R detailApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("减分项详情-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			ScoreMinus scoreMinus  = objectMapper.convertValue(jsonParams, ScoreMinus.class);
			//sql查询条件
			QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
			queryWrapper.select(" * ");
			queryWrapper.eq(scoreMinus.getId()!=null,"id",scoreMinus.getId());
			ScoreMinus detail = scoreMinusService.getOne(queryWrapper);

			String title1 = "查看了考核评价-减分项详情";
			String businessId = String.valueOf(scoreMinus.getId());
			String businessTable = "ScoreMinus";
			int businessType = BusinessType.LOOK.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			//关联文件表
			if(Func.isNotEmpty(detail)){
				QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
				filesQueryWrapper.select(" * ");
				filesQueryWrapper.eq(detail.getId() != null,"business_id",detail.getId());
				List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
				detail.setAppriseFilesList(list);
			}

			JSONObject pageJson = objectMapper.convertValue(detail, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, Func.isNotEmpty(pageJson)?pageJson.toJSONString():new JSONObject().toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}



	/**
	 * 分页查询
	 * @param scoreMinus
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-加减分纪实-减分项分页查询", notes = "传入scoreMinus")
	public R<IPage<ScoreMinusVO>> list(ScoreMinus scoreMinus, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		//减分项查询条件
		QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
		queryWrapper.select(" * ");
		if (scoreMinus!=null && scoreMinus.getSearchYear() != null && !scoreMinus.getSearchYear().isEmpty() && scoreMinus.getStartTime() == null && scoreMinus.getEndTime() == null) {
			queryWrapper.apply("year(create_time) = {0}", scoreMinus.getSearchYear());
		}
		queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getId()),"id",scoreMinus.getId());
		//deptName
		queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getDeptName()) && !scoreMinus.getDeptName().isEmpty(),"dept_name",scoreMinus.getDeptName());
		//checkWay
		queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getCheckWay()),"check_way",scoreMinus.getCheckWay());
		//startTime
		queryWrapper.ge(StringUtils.isNotNull(scoreMinus.getStartTime()),"create_time",scoreMinus.getStartTime());
		//endTime
		queryWrapper.le(StringUtils.isNotNull(scoreMinus.getEndTime()),"create_time",scoreMinus.getEndTime());
		queryWrapper.orderByDesc("create_time");
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
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
		if ("市委办公室".equals(deptName)) {
			isok = false;
		}
		//如果用户是四大班子领导，那就不执行评价人的查询条件，直接查看所有的的评价信息
		if (isok) {
			queryWrapper.and(wrapper -> wrapper
				.eq(StringUtils.isNotNull(user.getId()),"apprise_leader_id",user.getId().toString())
				.or().eq(StringUtils.isNotNull(user.getDeptId()),"dept_id",user.getDeptId())
				.or().eq(StringUtils.isNotNull(user.getDeptId()),"create_dept",user.getDeptId())
			);
		}

		//查询数据，封装分页参数
		IPage<ScoreMinus> pages = scoreMinusService.page(Condition.getPage(query), queryWrapper);
		//关联文件表
		for (int i = 0; i < pages.getRecords().size(); i++) {
			Long addId = pages.getRecords().get(i).getId();
			//如果未通过，则退回修改
			if (!"2".equals(pages.getRecords().get(i).getIsok()) || !"0".equals(pages.getRecords().get(i).getIsok())) {
				if (!"市委办公室".equals(deptName)) {
					//3的状态表示暂无权限
					pages.getRecords().get(i).setIsok("3");
				}
			}
			LambdaQueryWrapper<SupervisionSubmitAudit> lambdaQueryWrapper = Wrappers.<SupervisionSubmitAudit>query().lambda()
				.eq(SupervisionSubmitAudit::getServId,pages.getRecords().get(i).getId())
				.eq(SupervisionSubmitAudit::getStatus,0);
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getOne(lambdaQueryWrapper);
			if (supervisionSubmitAudit != null) {
				pages.getRecords().get(i).setSupSubAuditId(supervisionSubmitAudit.getId().toString());
				pages.getRecords().get(i).setAppraiseOpinion(supervisionSubmitAudit.getMsg());
			}
			QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
			filesQueryWrapper.select(" * ");
			filesQueryWrapper.eq(addId != null,"business_id",addId);
			List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
			pages.getRecords().get(i).setAppriseFilesList(list);
		}
		return R.data(ScoreMinusWrapper.build().pageVO(pages));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-加减分纪实-减分项分页查询", notes = "传入scoreMinus")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("考核评价-加减分纪实-减分项分页查询-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			ScoreMinus scoreMinus = objectMapper.convertValue(jsonParams, ScoreMinus.class);
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			//减分项查询条件
			QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
			queryWrapper.select(" * ");
			if (scoreMinus!=null && scoreMinus.getSearchYear() != null && !scoreMinus.getSearchYear().isEmpty() && scoreMinus.getStartTime() == null && scoreMinus.getEndTime() == null) {
				queryWrapper.apply("year(create_time) = {0}", scoreMinus.getSearchYear());
			}
			queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getId()),"id",scoreMinus.getId());
			//deptName
			queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getDeptName()),"dept_name",scoreMinus.getDeptName());
			//checkWay
			queryWrapper.eq(StringUtils.isNotNull(scoreMinus.getCheckWay()),"check_way",scoreMinus.getCheckWay());
			//startTime
			queryWrapper.ge(StringUtils.isNotNull(scoreMinus.getStartTime()),"create_time",scoreMinus.getStartTime());
			//endTime
			queryWrapper.le(StringUtils.isNotNull(scoreMinus.getEndTime()),"create_time",scoreMinus.getEndTime());
			queryWrapper.orderByDesc("create_time");
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
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			if ("市委办公室".equals(deptName)) {
				isok = false;
			}
			//如果用户是四大班子领导，那就不执行评价人的查询条件，直接查看所有的的评价信息
			if (isok) {
				queryWrapper.and(wrapper -> wrapper
					.eq(StringUtils.isNotNull(user.getId()),"apprise_leader_id",user.getId().toString())
					.or().eq(StringUtils.isNotNull(user.getDeptId()),"dept_id",user.getDeptId())
					.or().eq(StringUtils.isNotNull(user.getDeptId()),"create_dept",user.getDeptId())
				);
			}

			//查询数据，封装分页参数
			IPage<ScoreMinus> pages = scoreMinusService.page(Condition.getPage(query), queryWrapper);
			//关联文件表
			for (int i = 0; i < pages.getRecords().size(); i++) {
				Long addId = pages.getRecords().get(i).getId();
				//如果未通过，则退回修改
				if (!"2".equals(pages.getRecords().get(i).getIsok()) || !"0".equals(pages.getRecords().get(i).getIsok())) {
					if (!"市委办公室".equals(deptName)) {
						//3的状态表示暂无权限
						pages.getRecords().get(i).setIsok("3");
					}
				}
				LambdaQueryWrapper<SupervisionSubmitAudit> lambdaQueryWrapper = Wrappers.<SupervisionSubmitAudit>query().lambda()
					.eq(SupervisionSubmitAudit::getServId,pages.getRecords().get(i).getId())
					.eq(SupervisionSubmitAudit::getStatus,0);
				SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getOne(lambdaQueryWrapper);
				if (supervisionSubmitAudit != null) {
					pages.getRecords().get(i).setSupSubAuditId(supervisionSubmitAudit.getId().toString());
					pages.getRecords().get(i).setAppraiseOpinion(supervisionSubmitAudit.getMsg());
				}
				QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
				filesQueryWrapper.select(" * ");
				filesQueryWrapper.eq(addId != null,"business_id",addId);
				List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
				pages.getRecords().get(i).setAppriseFilesList(list);
			}
			JSONObject pageJson = objectMapper.convertValue(ScoreMinusWrapper.build().pageVO(pages), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param scoreMinus
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "考核评价-加减分纪实-减分项新增", notes = "传入scoreMinus对象")
	public R save(@Valid @RequestBody ScoreMinus scoreMinus) {
		try {
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			scoreMinusService.save(scoreMinus);

			String title1 = "新增考核评价-加减分纪实-减分项";
			String businessId = String.valueOf(scoreMinus.getId());
			String businessTable = "ScoreMinus";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			List<AppriseFiles> appriseFilesList = scoreMinus.getAppriseFilesList();
			if (appriseFilesList != null) {
				//向文件信息表中保存数据
				for (AppriseFiles appriseFiles : appriseFilesList) {
					appriseFiles.setBusinessId(scoreMinus.getId());
					iAppriseFilesService.save(appriseFiles);
				}
			}

			R<String> rgly = sysClient.getPostIdsByFuzzy("000000", "管理员");//获取管理员岗位id
			String glyId = rgly.getData();
			R<List<User>> ruser = iUserSearchClient.listByPostAndDept(glyId, user.getDeptId());//获取单位下面所有管理员用户
			String userid = "";
			if (ruser.getData().size()>0) {
				for (int i = 0; i < ruser.getData().size(); i++) {
					userid+=ruser.getData().get(i).getId()+",";
				}
			}
			//接收人
			String receiveUser = scoreMinus.getUserIds()+","+userid;
			//发送消息
			ScoreMinus scoreMinus1 = scoreMinusService.getById(scoreMinus.getId());
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】提交了减分项申请："+scoreMinus1.getMinusProject();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(scoreMinus.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("新增绩效考核减分项");//消息标题
			unifyMessage.setMsgType("57");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem("绩效考核减分项");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("57");//减分项
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title = "新增绩效考核减分项";
			String businessId1 = String.valueOf(scoreMinus.getId());
			String businessTable1 = "ScoreMinus";
			int businessType1 = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId1,businessTable1,businessType1);

		} catch (Exception exception) {
			return R.fail("操作失败："+exception.toString());
		}
		return R.success("操作成功！");
	}

	/**
	 * 编辑
	 * @param scoreMinus
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "考核评价-加减分纪实-减分项修改", notes = "传入scoreMinus")
	public R update(@Valid @RequestBody ScoreMinus scoreMinus) {
		boolean isok = scoreMinusService.updateById(scoreMinus);

		LambdaQueryWrapper<AppriseFiles> lambdaQueryWrapper = Wrappers.<AppriseFiles>query().lambda()
			.eq(AppriseFiles::getBusinessId,scoreMinus.getId());
		iAppriseFilesService.remove(lambdaQueryWrapper);
		List<AppriseFiles> appriseFilesList = scoreMinus.getAppriseFilesList();
		if (appriseFilesList != null) {
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setBusinessId(scoreMinus.getId());
				iAppriseFilesService.saveOrUpdate(appriseFiles);
			}
		}

		String title1 = "修改考核评价-加减分纪实-减分项";
		String businessId = String.valueOf(scoreMinus.getId());
		String businessTable = "ScoreMinus";
		int businessType = BusinessType.UPDATE.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

		return R.status(isok);
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "考核评价-加减分纪实-减分项删除", notes = "传入加分表ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam("ids") String ids) {

		boolean isok = scoreMinusService.removeByIds(Func.toLongList(ids));
		String title1 = "考核评价-加减分纪实-减分项删除";
		String businessId = String.valueOf(ids);
		String businessTable = "ScoreMinus";
		int businessType = BusinessType.DELETE.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title1, businessId, businessTable, businessType);
		}
		return R.status(isok);
	}

	/**
     * 导出
	 * @param scoreMinus
     * @param response
	 */
	@GetMapping("export-scoreMinus")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "考核评价-加减分纪实-减分项导出", notes = "传入scoreMinus")
	public void exportUser(ScoreMinus scoreMinus, HttpServletResponse response) {
		//sql查询条件
		QueryWrapper<ScoreMinus> queryWrapper = new QueryWrapper<ScoreMinus>();
		queryWrapper.select(" * ");
		queryWrapper.eq(scoreMinus.getId()!=null,"id",scoreMinus.getId());
		List<ScoreMinusExcel> list = scoreMinusService.exportScoreMinus(queryWrapper);
		for (int i = 0; i < list.size(); i++) {
			//指标分类
			if (list.get(i).getCheckWay().equals("1")) {
				list.get(i).setCheckWay("季度指标");
			} else if (list.get(i).getCheckWay().equals("2")) {
				list.get(i).setCheckWay("年度指标");
			}
		}

		ExcelUtil.export(response, "惩处扣分-" + DateUtil.time(), "惩处扣分", list, ScoreMinusExcel.class);
	}




	/**
	 * 减分项-送审
	 * @param scoreMinus		指标id主键,送审标题,用户主键多个逗号隔开,同步还是异步 1同步；0异步
	 * @return
	 */
	/*@PostMapping("/submitAuditMinusScore")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "减分项-送审", notes = "减分项-送审")
	public R submitAuditMinusScore(@RequestBody ScoreMinus scoreMinus){
		try {
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String swbgsid = sysClient.getDeptIds("000000","市委办公室").getData();
			LambdaQueryWrapper<SupervisionSubmitAudit> lambdaQueryWrapper = Wrappers.<SupervisionSubmitAudit>query().lambda()
				.eq(SupervisionSubmitAudit::getServId,scoreMinus.getId());
			List<SupervisionSubmitAudit> supervisionSubmitAudits = this.supervisionSubmitAuditService.list(lambdaQueryWrapper);
			//送审
			if (supervisionSubmitAudits.size() == 1) {
				SupervisionSubmitAudit supervisionSubmitAudit = new SupervisionSubmitAudit();
				supervisionSubmitAudit.setId(supervisionSubmitAudits.get(0).getId());
				supervisionSubmitAudit.setStatus(0);
				supervisionSubmitAudit.setUserId(Long.valueOf(swbgsid));
				supervisionSubmitAudit.setTitle(scoreMinus.getTitle());
				supervisionSubmitAudit.setSync(Integer.valueOf(scoreMinus.getSync()));
				supervisionSubmitAudit.setDeptName(deptName);
				this.supervisionSubmitAuditService.updateById(supervisionSubmitAudit);

				ScoreMinus scoreMinus1 = new ScoreMinus();
				scoreMinus1.setId(scoreMinus.getId());
				scoreMinus1.setIsok("0");
				scoreMinusService.updateById(scoreMinus1);
			} else if (supervisionSubmitAudits.size() == 0) {
				this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(scoreMinus.getId()), scoreMinus.getTitle(), swbgsid, scoreMinus.getSync(), StatusConstant.OPERATION_TYPE_ADDSCORE);
			} else {
				for (int i = 0; i < supervisionSubmitAudits.size(); i++) {
					if (i == 0) {
						SupervisionSubmitAudit supervisionSubmitAudit = new SupervisionSubmitAudit();
						supervisionSubmitAudit.setId(supervisionSubmitAudits.get(0).getId());
						supervisionSubmitAudit.setStatus(0);
						supervisionSubmitAudit.setUserId(Long.valueOf(swbgsid));
						supervisionSubmitAudit.setTitle(scoreMinus.getTitle());
						supervisionSubmitAudit.setSync(Integer.valueOf(scoreMinus.getSync()));
						supervisionSubmitAudit.setDeptName(deptName);
						this.supervisionSubmitAuditService.updateById(supervisionSubmitAudit);

						ScoreMinus scoreMinus1 = new ScoreMinus();
						scoreMinus1.setId(scoreMinus.getId());
						scoreMinus1.setIsok("0");
						scoreMinusService.updateById(scoreMinus1);
					} else {
						this.supervisionSubmitAuditService.removeById(supervisionSubmitAudits.get(0).getId());
					}
				}
			}

			//发送消息
			ScoreMinus minus = scoreMinusService.getById(scoreMinus.getId());
			String msgIntro = "【"+deptName+"】提交了加减分项申请："+minus.getMinusProject();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(scoreMinus.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("绩效考核减分项-送审");//消息标题
			unifyMessage.setMsgType("28");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(scoreMinus.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem("绩效考核减分项");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("27");//年度下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title1 = "绩效考核减分项-送审";
			String businessId = String.valueOf(scoreMinus.getId());
			String businessTable = "ScoreMinus";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}*/



}
