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
import com.vingsoft.vo.ScoreAddVO;
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
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.mp.support.Query;

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
import org.springblade.integrated.platform.excel.ScoreAddExcel;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springblade.integrated.platform.service.IScoreAddService;
import org.springblade.integrated.platform.service.ISupervisionSubmitAuditService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.integrated.platform.wrapper.ScoreAddWrapper;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 考核评价-加减分纪实-加分项 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("ScoreAdd")
@Api(value = "考核评价-加减分纪实-加分项", tags = "加分项控制层代码")
public class ScoreAddController extends BladeController {

	@Resource
	private IScoreAddService scoreAddService;
	private final IAppriseFilesService iAppriseFilesService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Resource
	private IUnifyMessageService unifyMessageService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final ISysClient sysClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "加分项详情", notes = "传入score_add")
	public R<ScoreAdd> detail(ScoreAdd score_add) {
		//sql查询条件
		QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
		queryWrapper.select(" * ");
		queryWrapper.eq(score_add.getId() != null,"id",score_add.getId());
		ScoreAdd detail = scoreAddService.getOne(queryWrapper);

		String title1 = "查看了考核评价-加分项详情";
		String businessId = String.valueOf(score_add.getId());
		String businessTable = "ScoreAdd";
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
	@ApiOperation(value = "加分项详情-app", notes = "传入score_add")
	public R detailApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("加分项详情-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			ScoreAdd score_add = objectMapper.convertValue(jsonParams, ScoreAdd.class);
			//sql查询条件
			QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
			queryWrapper.select(" * ");
			queryWrapper.eq(score_add.getId() != null,"id",score_add.getId());
			ScoreAdd detail = scoreAddService.getOne(queryWrapper);

			String title1 = "查看了考核评价-加分项详情";
			String businessId = String.valueOf(score_add.getId());
			String businessTable = "ScoreAdd";
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
	 * @param score_add
	 * @param query
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-加减分纪实-加分项分页查询", notes = "传入score_add")
	public R<IPage<ScoreAddVO>> list(ScoreAdd score_add, Query query) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
		queryWrapper.select(" * ");
		if (score_add!=null && score_add.getSearchYear() != null && !score_add.getSearchYear().isEmpty() && score_add.getStartTime() == null && score_add.getEndTime() == null) {
			queryWrapper.apply("year(create_time) = {0}", score_add.getSearchYear());
		}
		//加分项查询条件
		queryWrapper.eq(StringUtils.isNotNull(score_add.getId()),"id",score_add.getId());
		//deptName
		queryWrapper.eq(StringUtils.isNotNull(score_add.getDeptName()) && !score_add.getDeptName().isEmpty(),"dept_name",score_add.getDeptName());
		//checkWay
		queryWrapper.eq(StringUtils.isNotNull(score_add.getCheckWay()),"check_way",score_add.getCheckWay());
		//winLevel
		queryWrapper.eq(StringUtils.isNotNull(score_add.getWinLevel()),"win_level",score_add.getCheckWay());
		//startTime
		queryWrapper.ge(StringUtils.isNotNull(score_add.getStartTime()),"create_time",score_add.getStartTime());
		//endTime
		queryWrapper.le(StringUtils.isNotNull(score_add.getEndTime()),"create_time",score_add.getEndTime());
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
		IPage<ScoreAdd> pages = scoreAddService.page(Condition.getPage(query), queryWrapper);
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
				.eq(SupervisionSubmitAudit::getServId,pages.getRecords().get(i).getId());
				//.eq(SupervisionSubmitAudit::getStatus,0)
			SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getOne(lambdaQueryWrapper);
			if (supervisionSubmitAudit != null) {
				pages.getRecords().get(i).setSupSubAuditId(supervisionSubmitAudit.getId().toString());
				if (supervisionSubmitAudit.getMsg() != null) {
					pages.getRecords().get(i).setAppraiseOpinion(supervisionSubmitAudit.getMsg());
				}
			}

			QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
			filesQueryWrapper.select(" * ");
			filesQueryWrapper.eq(addId != null,"business_id",addId);
			List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
			pages.getRecords().get(i).setAppriseFilesList(list);
		}

		return R.data(ScoreAddWrapper.build().pageVO(pages));
	}

	/**
	 * 分页查询
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "考核评价-加减分纪实-加分项分页查询", notes = "传入score_add")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("考核评价-加减分纪实-加分项分页查询-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			ScoreAdd score_add = objectMapper.convertValue(jsonParams, ScoreAdd.class);
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
			queryWrapper.select(" * ");
			if (score_add!=null && score_add.getSearchYear() != null && !score_add.getSearchYear().isEmpty() && score_add.getStartTime() == null && score_add.getEndTime() == null) {
				queryWrapper.apply("year(create_time) = {0}", score_add.getSearchYear());
			}
			//加分项查询条件
			queryWrapper.eq(StringUtils.isNotNull(score_add.getId()),"id",score_add.getId());
			//deptName
			queryWrapper.eq(StringUtils.isNotNull(score_add.getDeptName()),"dept_name",score_add.getDeptName());
			//checkWay
			queryWrapper.eq(StringUtils.isNotNull(score_add.getCheckWay()),"check_way",score_add.getCheckWay());
			//winLevel
			queryWrapper.eq(StringUtils.isNotNull(score_add.getWinLevel()),"win_level",score_add.getCheckWay());
			//startTime
			queryWrapper.ge(StringUtils.isNotNull(score_add.getStartTime()),"create_time",score_add.getStartTime());
			//endTime
			queryWrapper.le(StringUtils.isNotNull(score_add.getEndTime()),"create_time",score_add.getEndTime());
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
			IPage<ScoreAdd> pages = scoreAddService.page(Condition.getPage(query), queryWrapper);
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
					.eq(SupervisionSubmitAudit::getServId,pages.getRecords().get(i).getId());
				SupervisionSubmitAudit supervisionSubmitAudit = supervisionSubmitAuditService.getOne(lambdaQueryWrapper);
				if (supervisionSubmitAudit != null) {
					pages.getRecords().get(i).setSupSubAuditId(supervisionSubmitAudit.getId().toString());
					if (supervisionSubmitAudit.getMsg() != null) {
						pages.getRecords().get(i).setAppraiseOpinion(supervisionSubmitAudit.getMsg());
					}
				}

				QueryWrapper<AppriseFiles> filesQueryWrapper =new QueryWrapper<>();
				filesQueryWrapper.select(" * ");
				filesQueryWrapper.eq(addId != null,"business_id",addId);
				List<AppriseFiles> list = iAppriseFilesService.list(filesQueryWrapper);
				pages.getRecords().get(i).setAppriseFilesList(list);
			}
			JSONObject pageJson = objectMapper.convertValue(ScoreAddWrapper.build().pageVO(pages), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 新增
	 * @param score_add
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "考核评价-加减分纪实-加分项新增", notes = "传入score_add对象")
	public R save(@Valid @RequestBody ScoreAdd score_add) {
		/*try {*/
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			scoreAddService.save(score_add);

			String title1 = "新增考核评价-加减分纪实-加分项";
			String businessId = String.valueOf(score_add.getId());
			String businessTable = "ScoreAdd";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			List<AppriseFiles> appriseFilesList = score_add.getAppriseFilesList();
			if (appriseFilesList != null) {
				//向文件信息表中保存数据
				for (AppriseFiles appriseFiles : appriseFilesList) {
					appriseFiles.setBusinessId(score_add.getId());
					iAppriseFilesService.save(appriseFiles);
				}
			}
			String swbgsid = sysClient.getDeptIds("000000","市委办公室").getData();
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(score_add.getId()),score_add.getTitle(),swbgsid,score_add.getSync(), StatusConstant.OPERATION_TYPE_ADDSCORE);
			//接收人
			String receiveUser = user.getId()+","+ swbgsid;
			//发送消息
			ScoreAdd add = scoreAddService.getById(score_add.getId());
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】提交了加分项申请："+add.getWinProject();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(score_add.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("绩效考核加分项-送审");//消息标题
			unifyMessage.setMsgType("56");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem("绩效考核加分项");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("56");//加分项
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title = "绩效考核加分项-送审";
			String businessId1 = String.valueOf(score_add.getId());
			String businessTable1 = "ScoreAdd";
			int businessType1 = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId1,businessTable1,businessType1);

		return R.success("操作成功！");
	}

	/**
	 * 新增
	 * @return
	 */
	@PostMapping("/saveApp")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "考核评价-加减分纪实-加分项新增", notes = "传入score_add对象")
	public R saveApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("考核评价-加减分纪实-加分项新增-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			ScoreAdd score_add = objectMapper.convertValue(jsonParams, ScoreAdd.class);
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			scoreAddService.save(score_add);

			String title1 = "新增考核评价-加减分纪实-加分项";
			String businessId = String.valueOf(score_add.getId());
			String businessTable = "ScoreAdd";
			int businessType = BusinessType.INSERT.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			List<AppriseFiles> appriseFilesList = score_add.getAppriseFilesList();
			if (appriseFilesList != null) {
				//向文件信息表中保存数据
				for (AppriseFiles appriseFiles : appriseFilesList) {
					appriseFiles.setBusinessId(score_add.getId());
					iAppriseFilesService.save(appriseFiles);
				}
			}
			String swbgsid = sysClient.getDeptIds("000000","市委办公室").getData();
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(score_add.getId()),score_add.getTitle(),swbgsid,score_add.getSync(), StatusConstant.OPERATION_TYPE_ADDSCORE);
			//接收人
			String receiveUser = user.getId()+","+ swbgsid;
			//发送消息
			ScoreAdd add = scoreAddService.getById(score_add.getId());
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】提交了加分项申请："+add.getWinProject();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(score_add.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("绩效考核加分项-送审");//消息标题
			unifyMessage.setMsgType("56");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem("绩效考核加分项");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("56");//加分项
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title = "绩效考核加分项-送审";
			String businessId1 = String.valueOf(score_add.getId());
			String businessTable1 = "ScoreAdd";
			int businessType1 = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId1,businessTable1,businessType1);
			return R.data(VSTool.encrypt(encryptSign, "操作成功", VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 编辑
	 * @param score_add
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "考核评价-加减分纪实-加分项修改", notes = "传入score_add")
	public R update(@Valid @RequestBody ScoreAdd score_add) {
		boolean isok = scoreAddService.updateById(score_add);

		LambdaQueryWrapper<AppriseFiles> lambdaQueryWrapper = Wrappers.<AppriseFiles>query().lambda()
				.eq(AppriseFiles::getBusinessId,score_add.getId());
		iAppriseFilesService.remove(lambdaQueryWrapper);

		List<AppriseFiles> appriseFilesList = score_add.getAppriseFilesList();
		if (appriseFilesList != null) {
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setBusinessId(score_add.getId());
				iAppriseFilesService.saveOrUpdate(appriseFiles);
			}
		}
		String title1 = "修改考核评价-加减分纪实-加分项";
		String businessId = String.valueOf(score_add.getId());
		String businessTable = "ScoreAdd";
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
	@ApiOperation(value = "考核评价-加减分纪实-加分项删除", notes = "传入加分表ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam("ids") String ids) {
		boolean isok = scoreAddService.removeByIds(Func.toLongList(ids));
		String title1 = "修改考核评价-加减分纪实-加分项";
		String businessId = String.valueOf(ids);
		String businessTable = "ScoreAdd";
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
	 * @param score_add
	 * @param response
	 */
	@GetMapping("export-scoreAdd")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "考核评价-加减分纪实-加分项导出", notes = "传入scoreAdd")
	public void exportUser(ScoreAdd score_add, HttpServletResponse response) {
		//sql查询条件
		QueryWrapper<ScoreAdd> queryWrapper = new QueryWrapper<ScoreAdd>();
		queryWrapper.select(" * ");
		//sql查询条件
		queryWrapper.eq(score_add.getId() != null,"id",score_add.getId());
		List<ScoreAddExcel> list = scoreAddService.exportScoreAdd(queryWrapper);
		for (int i = 0; i < list.size(); i++) {
			//指标分类
			if (list.get(i).getCheckWay().equals("1")) {
				list.get(i).setCheckWay("季度指标");
			} else if (list.get(i).getCheckWay().equals("2")) {
				list.get(i).setCheckWay("年度指标");
			}

			//获奖级别
			if (list.get(i).getWinLevel().equals("1")) {
				list.get(i).setWinLevel("省级");
			} else if (list.get(i).getWinLevel().equals("2")) {
				list.get(i).setWinLevel("州级");
			} else if (list.get(i).getWinLevel().equals("3")) {
				list.get(i).setWinLevel("市县级");
			}

		}
		ExcelUtil.export(response, "获奖得分-" + DateUtil.time(), "获奖得分", list, ScoreAddExcel.class);
	}


	/**
	 * 加分项-送审
	 * @param scoreAdd		指标id主键,送审标题,用户主键多个逗号隔开,同步还是异步 1同步；0异步
	 * @return
	 */
	@PostMapping("/submitAuditAddScore")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "加分项-送审", notes = "加分项-送审")
	public R submitAuditAddScore(@RequestBody ScoreAdd scoreAdd){
		try {
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String swbgsid = sysClient.getDeptIds("000000","市委办公室").getData();
			LambdaQueryWrapper<SupervisionSubmitAudit> lambdaQueryWrapper = Wrappers.<SupervisionSubmitAudit>query().lambda()
				.eq(SupervisionSubmitAudit::getServId,scoreAdd.getId());
			List<SupervisionSubmitAudit> supervisionSubmitAudits = this.supervisionSubmitAuditService.list(lambdaQueryWrapper);
			//送审
			if (supervisionSubmitAudits.size() == 1) {
				SupervisionSubmitAudit supervisionSubmitAudit = new SupervisionSubmitAudit();
				supervisionSubmitAudit.setId(supervisionSubmitAudits.get(0).getId());
				supervisionSubmitAudit.setStatus(0);
				supervisionSubmitAudit.setUserId(Long.valueOf(swbgsid));
				supervisionSubmitAudit.setTitle(scoreAdd.getTitle());
				supervisionSubmitAudit.setSync(Integer.valueOf(scoreAdd.getSync()));
				supervisionSubmitAudit.setDeptName(deptName);
				this.supervisionSubmitAuditService.updateById(supervisionSubmitAudit);

				ScoreAdd scoreAdd1 = new ScoreAdd();
				scoreAdd1.setId(scoreAdd.getId());
				scoreAdd1.setIsok("0");
				scoreAddService.updateById(scoreAdd1);
			} else if (supervisionSubmitAudits.size() == 0) {
				this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(scoreAdd.getId()),scoreAdd.getTitle(),swbgsid,scoreAdd.getSync(), StatusConstant.OPERATION_TYPE_ADDSCORE);
			} else {
				for (int i = 0; i < supervisionSubmitAudits.size(); i++) {
					if (i == 0) {
						SupervisionSubmitAudit supervisionSubmitAudit = new SupervisionSubmitAudit();
						supervisionSubmitAudit.setId(supervisionSubmitAudits.get(0).getId());
						supervisionSubmitAudit.setStatus(0);
						supervisionSubmitAudit.setUserId(Long.valueOf(swbgsid));
						supervisionSubmitAudit.setTitle(scoreAdd.getTitle());
						supervisionSubmitAudit.setSync(Integer.valueOf(scoreAdd.getSync()));
						supervisionSubmitAudit.setDeptName(deptName);
						this.supervisionSubmitAuditService.updateById(supervisionSubmitAudit);

						ScoreAdd scoreAdd1 = new ScoreAdd();
						scoreAdd1.setId(scoreAdd.getId());
						scoreAdd1.setIsok("0");
						scoreAddService.updateById(scoreAdd1);
					} else {
						this.supervisionSubmitAuditService.removeById(supervisionSubmitAudits.get(0).getId());
					}
				}
			}
			//接收人
			String receiveUser = user.getId() + "," + swbgsid;
			//发送消息
			ScoreAdd add = scoreAddService.getById(scoreAdd.getId());
			String msgIntro = "【"+deptName+"】提交了加分项申请："+add.getWinProject();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(scoreAdd.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("绩效考核加分项-送审");//消息标题
			unifyMessage.setMsgType("56");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(receiveUser);
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem("绩效考核加分项");//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("56");//年度下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);

			String title1 = "绩效考核加分项-送审";
			String businessId = String.valueOf(scoreAdd.getId());
			String businessTable = "ScoreAdd";
			int businessType = BusinessType.UPDATE.ordinal();
			SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

			return R.status(true);
		} catch (Exception e){
			return R.fail(e.getMessage());
		}
	}



}
