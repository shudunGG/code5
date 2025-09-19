package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.ReportsBaseinfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.constant.PropConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.Registration;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * 公共方法-批示/留言 控制层
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
@Slf4j
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/MessageInformation")
@Api(value = "公共方法-批示/留言", tags = "批示/留言接口控制层代码")
public class MessageInformationController extends BladeController {

	private final IMessageInformationService iMessageInformationService;
	private final IAppriseFilesService iAppriseFilesService;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final IUserSearchClient iUserSearchClient;
	private final IUnifyMessageService messageService;
	@Resource
	private final IDictBizClient dictBizClient;
	private final ISupervisionLogService supervisionLogService;
	@Resource
	private final ISysClient sysClient;
	private final ISupervisionInfoService iSupervisionInfoService;
	private final IProjectSummaryService projectSummaryService;
	private final IProjectLogService projectLogService;
	private final SmsDockingService smsDockingService;
	@Resource
	private IAnnualEvaluationService annualEvaluationService;
	@Resource
	private IQuarterlyEvaluationService quarterlyEvaluationService;
	@Resource
	private ILeaderAppriseService leaderAppriseService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 详细信息
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "批示/留言详细信息", notes = "传入messageInformation")
	public R<List<MessageInformation>> detail(MessageInformation messageInformation) {
		//sql查询条件
		QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<MessageInformation>();
		queryWrapper.select(" * ");
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getBusinessId()),"business_id",messageInformation.getBusinessId());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getMonthBusinessId()),"month_business_id",messageInformation.getMonthBusinessId());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getPsOrLy()),"ps_or_ly",messageInformation.getPsOrLy());
		queryWrapper.eq(StringUtils.isNotBlank(messageInformation.getBusinessType()),"business_type",messageInformation.getBusinessType());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getChildId()),"child_id",messageInformation.getChildId());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getCreateUser()),"create_user",messageInformation.getCreateUser());
		queryWrapper.orderByDesc("create_time");
		List<MessageInformation> detail = iMessageInformationService.list(queryWrapper);

		for (MessageInformation messageInformation1 : detail) {
			if (messageInformation1.getBusinessId() != null) {
				QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
				filesQueryWrapper.eq("business_id",Long.valueOf(messageInformation1.getId()));
				//filesQueryWrapper.eq("business_table",messageInformation1.getBusinessTable());
				List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
				messageInformation1.setAppriseFilesList(appriseFilesList);
			}
		}

		return R.data(detail);
	}

	/**
	 * 详细信息
	 */
	@PostMapping("/detailApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "批示/留言详细信息-app", notes = "传入messageInformation")
	public R detailApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("批示/留言详细信息-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String businessId = jsonParams.getString("businessId");
			String monthBusinessId = jsonParams.getString("monthBusinessId");
			String psOrLy = jsonParams.getString("psOrLy");
			String businessType = jsonParams.getString("businessType");
			String childId = jsonParams.getString("childId");
			String createUser = jsonParams.getString("createUser");

			//sql查询条件
			QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<>();
			queryWrapper.select(" * ");
			queryWrapper.eq(StringUtils.isNotNull(businessId),"business_id",businessId);
			queryWrapper.eq(StringUtils.isNotNull(monthBusinessId),"month_business_id",monthBusinessId);
			queryWrapper.eq(StringUtils.isNotNull(psOrLy),"ps_or_ly",psOrLy);
			queryWrapper.eq(StringUtils.isNotBlank(businessType),"business_type",businessType);
			queryWrapper.eq(StringUtils.isNotNull(childId),"child_id",childId);
			queryWrapper.eq(StringUtils.isNotNull(createUser),"create_user",createUser);
			queryWrapper.orderByDesc("create_time");
			List<MessageInformation> detail = iMessageInformationService.list(queryWrapper);

			for (MessageInformation messageInformation1 : detail) {
				if (messageInformation1.getBusinessId() != null) {
					QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
					filesQueryWrapper.eq("business_id", messageInformation1.getId());
					List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
					messageInformation1.setAppriseFilesList(appriseFilesList);
				}
			}
			JSONArray jsonArray = objectMapper.convertValue(detail, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}

	}


	/**
	 * 详细信息分页
	 */
	@GetMapping("/listPage")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "批示/留言详细信息分页", notes = "传入messageInformation")
	public R<IPage<MessageInformation>> detail(MessageInformation messageInformation, Query query) {
		//sql查询条件
		QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<MessageInformation>();
		queryWrapper.select(" * ");
		if(StringUtils.isNotEmpty(messageInformation.getBusinessType()) && messageInformation.getBusinessType().equals("5")){ //工作月调度
			queryWrapper.eq(StringUtils.isNotNull(messageInformation.getMonthBusinessId()),"month_business_id",messageInformation.getMonthBusinessId());
		}else{
			queryWrapper.eq(StringUtils.isNotNull(messageInformation.getBusinessId()),"business_id",messageInformation.getBusinessId());
		}
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getPsOrLy()),"ps_or_ly",messageInformation.getPsOrLy());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getChildId()),"child_id",messageInformation.getChildId());
		queryWrapper.eq(StringUtils.isNotBlank(messageInformation.getBusinessType()),"business_type",messageInformation.getBusinessType());
		queryWrapper.eq(StringUtils.isNotNull(messageInformation.getCreateUser()),"create_user",messageInformation.getCreateUser());
		//查询30天内的
		queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL 30 DAY) <= create_time");
		queryWrapper.orderByDesc("create_time");

		//查询数据，封装分页参数
		IPage<MessageInformation> detail = iMessageInformationService.page(Condition.getPage(query), queryWrapper);

		for (MessageInformation messageInformation1 : detail.getRecords()) {
			if (messageInformation1.getBusinessId() != null) {
				QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
				filesQueryWrapper.eq("business_id",Long.valueOf(messageInformation1.getId()));
				List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
				messageInformation1.setAppriseFilesList(appriseFilesList);
			}
		}

		return R.data(detail);
	}

	/**
	 * 详细信息分页-app
	 */
	@PostMapping("/listPageApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "批示/留言详细信息分页", notes = "传入messageInformation")
	public R listPageApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("批示/留言详细信息分页-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));
			MessageInformation messageInformation = objectMapper.convertValue(jsonParams, MessageInformation.class);
			//sql查询条件
			QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<MessageInformation>();
			queryWrapper.select(" * ");
			if(StringUtils.isNotEmpty(messageInformation.getBusinessType()) && messageInformation.getBusinessType().equals("5")){ //工作月调度
				queryWrapper.eq(StringUtils.isNotNull(messageInformation.getMonthBusinessId()),"month_business_id",messageInformation.getMonthBusinessId());
			}else{
				queryWrapper.eq(StringUtils.isNotNull(messageInformation.getBusinessId()),"business_id",messageInformation.getBusinessId());
			}
			queryWrapper.eq(StringUtils.isNotNull(messageInformation.getPsOrLy()),"ps_or_ly",messageInformation.getPsOrLy());
			queryWrapper.eq(StringUtils.isNotNull(messageInformation.getChildId()),"child_id",messageInformation.getChildId());
			queryWrapper.eq(StringUtils.isNotBlank(messageInformation.getBusinessType()),"business_type",messageInformation.getBusinessType());
			queryWrapper.eq(StringUtils.isNotNull(messageInformation.getCreateUser()),"create_user",messageInformation.getCreateUser());
			//查询30天内的
			queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL 30 DAY) <= create_time");
			queryWrapper.orderByDesc("create_time");

			//查询数据，封装分页参数
			IPage<MessageInformation> detail = iMessageInformationService.page(Condition.getPage(query), queryWrapper);

			for (MessageInformation messageInformation1 : detail.getRecords()) {
				if (messageInformation1.getBusinessId() != null) {
					QueryWrapper<AppriseFiles> filesQueryWrapper = new QueryWrapper<>();
					filesQueryWrapper.eq("business_id",Long.valueOf(messageInformation1.getId()));
					List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(filesQueryWrapper);
					messageInformation1.setAppriseFilesList(appriseFilesList);
				}
			}
			JSONObject pageJson = objectMapper.convertValue(detail, JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 公共方法-批示/留言
	 * @param messageInformation
	 * @return
	 */
	@PostMapping("/saveMsgAndFile")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "公共方法-批示/留言", notes = "传入MessageInformation对象")
	public R save(@Valid @RequestBody MessageInformation messageInformation) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		messageInformation.setAppriseUser(userNameDecrypt);
		messageInformation.setAppriseUserId(user.getId());
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
		messageInformation.setAppriseuserDeptname(deptName);

		iMessageInformationService.save(messageInformation);

		if (messageInformation.getAppriseFilesList() != null) {
			List<AppriseFiles> appriseFilesList = messageInformation.getAppriseFilesList();
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setFileFrom("PC端");
				appriseFiles.setBusinessId(messageInformation.getId());
				appriseFiles.setUploadUserName(userNameDecrypt);
				iAppriseFilesService.save(appriseFiles);
			}
		}
		String title = "新增批示/留言";
		String businessId = String.valueOf(messageInformation.getId());
		String businessTable = "MessageInformation";
		int businessType = BusinessType.INSERT.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title, businessId, businessTable, businessType);
		}


		//TODO 各个模块发送消息
		String receiveUser = messageInformation.getAppriseUserId()+",";//接收人
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

			//发送短信开始 20230516
			//督查督办批示、留言
			if("1".equals(messageInformation.getBusinessType())){
				String mobiles = "";
					//督查督办需要发送消息的对象：牵头单位、督办人、评价人、“@”对象。（如果督办人和评价人是同一手机号，只发一次）
				SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(messageInformation.getBusinessId());
				//1、牵头单位
				String[] qtdwNames = supervisionInfo.getLeadUnitName().split(",");//事项牵头单位名称
				for (String qtdwName : qtdwNames) {
					//根据部门名称获取用户
					List<User> userListDx1 = iUserSearchClient.getDeptUsers(qtdwName).getData();
					if(userListDx1 != null && userListDx1.size() > 0){
						for(User userDx1 : userListDx1){
							mobiles += userDx1.getPhone()+",";
						}
					}
				}
				//2、督办人（一个）
				String supervisor = supervisionInfo.getSupervisorName();
				//根据部门名称获取用户
				List<User> userListDx2 = iUserSearchClient.getDeptUsers(supervisor).getData();
				if(userListDx2 != null && userListDx2.size() > 0){
					for(User userDx2 : userListDx2){
						if(!mobiles.contains(userDx2.getPhone())){  //去除重复手机号
							mobiles += userDx2.getPhone()+",";
						}
					}
				}

				//3、评价人（一个）
				String evaluator = supervisionInfo.getEvaluatorName();
				//根据部门名称获取用户
				List<User> userListDx3 = iUserSearchClient.getDeptUsers(evaluator).getData();
				if(userListDx3 != null && userListDx3.size() > 0){
					for(User userDx3 : userListDx3){
						if(!mobiles.contains(userDx3.getPhone())){  //去除重复手机号
							mobiles += userDx3.getPhone()+",";
						}
					}
				}

				//4、@对象
				String atUserId = messageInformation.getAtUserId();
				if(StringUtil.isNotBlank(atUserId)){
					String[] userIdsDx4 = messageInformation.getAtUserId().split(",");
					for (String userIdDx4 : userIdsDx4) {
						List<User> userListDx4 = iUserSearchClient.listByUser(userIdDx4).getData();
						if(!mobiles.contains(userListDx4.get(0).getPhone())) {
							mobiles += userListDx4.get(0).getPhone();
						}
					}
				}
				log.info("督察督办所有需要发短信的号码："+mobiles);
				if(StringUtil.isNotBlank(mobiles)){
					mobiles = mobiles.substring(0, mobiles.length() - 1);
					String content = "您有一条领导批示，请登录督考一体化平台进行查看。";
					smsDockingService.send(mobiles,content);
				}
			}
			//项目管理批示、留言
			if("3".equals(messageInformation.getBusinessType())){
				String mobiles = "";
				ProjectSummary projectSummary = projectSummaryService.getById(messageInformation.getBusinessId());
				//1、市直行业主管部门
				String[] szhyzgbmNames = projectSummary.getSzhyzgbmName().split(",");
				for (String szhyzgbmName : szhyzgbmNames) {
					//根据部门名称获取用户
					List<User> userListXm1 = iUserSearchClient.getDeptUsers(szhyzgbmName).getData();
					if(userListXm1 != null && userListXm1.size() > 0){
						for(User userXm1 : userListXm1){
							mobiles += userXm1.getPhone()+",";
						}
					}
				}
				//2、县级包抓领导
				String[] xjbzlds = projectSummary.getXjbzld().split(",");
				for (String xjbzld : xjbzlds) {
					List<User> userListXm2 = iUserSearchClient.listByUser(xjbzld).getData();
					if(userListXm2 != null && userListXm2.size() > 0){
						for(User userXm2 : userListXm2){
							mobiles += userXm2.getPhone()+",";
						}
					}
				}
				//3、调度单位
				String[] dwmcNames = projectSummary.getDwmcName().split(",");
				for (String dwmcName : dwmcNames) {
					//根据部门名称获取用户
					List<User> userListXm3 = iUserSearchClient.getDeptUsers(dwmcName).getData();
					if(userListXm3 != null && userListXm3.size() > 0){
						for(User userXm3 : userListXm3){
							mobiles += userXm3.getPhone()+",";
						}
					}
				}
				//4、@对象
				String atUerId = messageInformation.getAtUserId();
				if(StringUtil.isNotBlank(atUerId)){
					String[] userIdsDx4 = atUerId.split(",");
					for (String userIdDx4 : userIdsDx4) {
						List<User> userListDx4 = iUserSearchClient.listByUser(userIdDx4).getData();
						if(!mobiles.contains(userListDx4.get(0).getPhone())) {
							mobiles += userListDx4.get(0).getPhone();
						}
					}
				}
				log.info("督察督办所有需要发短信的号码："+mobiles);
				if(StringUtil.isNotBlank(mobiles)){
					mobiles = mobiles.substring(0, mobiles.length() - 1);
					String content = "您有一条领导批示，请登录督考一体化平台进行查看。";
					smsDockingService.send(mobiles,content);
				}
			}
			//发送短信结束 20230516
		}
		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id

		if("3".equals(messageInformation.getBusinessType())){//项目管理批示/留言
			ProjectSummary projectSummary = projectSummaryService.getById(messageInformation.getBusinessId());
			if(isLead){
				//市委办公室、市政府办公室、市发改委
				String deptId1 = sysClient.getDeptIdsByFuzzy("000000","市委办公室").getData();
				String deptId2 = sysClient.getDeptIdsByFuzzy("000000","市政府办公室").getData();
				String deptId3 = sysClient.getDeptIdsByFuzzy("000000","市发展改革委").getData();
				List<User> users= iUserSearchClient.listByPostAndDept(leadPostId,deptId1).getData();//获取单位下面所有领导用户
				users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId2).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId3).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId1).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId2).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId3).getData());
				//市发改委投资科
				receiveUser += PropConstant.getProjectShzhId("6207")+",";
				//市级行业主管部门
				String deptId4 = projectSummary.getSzhyzgbm();
				if(StringUtil.isNotBlank(deptId4)){
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId4).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId4).getData());
				}
				//调度单位
				String deptId5 = projectSummary.getDwmc();
				if(StringUtil.isNotBlank(deptId5)){
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId5).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId5).getData());
				}
				//市级包抓领导
				String sjbzld = projectSummary.getSjbzld();
				if(StringUtil.isNotBlank(sjbzld)){
					receiveUser += sjbzld+",";
				}
				//如果不是市级项目
				if(!"6207".equals(projectSummary.getAreaCode())){
					//县级四大班子
					String roleIdXj = sysClient.getRoleIds("000000", "县级四大班子").getData().replace(",","");
					users.addAll(userClient.getUserListByRoleId(roleId).getData());
					//县级发改局
					receiveUser += PropConstant.getProjectShzhId(projectSummary.getAreaCode())+",";
					//县发改局领导
					User u = userClient.userInfoById(Long.parseLong(PropConstant.getProjectShzhId(projectSummary.getAreaCode()))).getData();
					if(u != null && StringUtil.isNotBlank(u.getDeptId())){
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,u.getDeptId()).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId,u.getDeptId()).getData());
					}
					//县级行业主管部门
					String deptId6 = projectSummary.getXqhyzgbm();
					if(StringUtil.isNotBlank(deptId6)){
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId6).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId6).getData());
					}
				}
				for (User user1 : users) {
					if(!user.getId().equals(user1.getId())){
						receiveUser += user1.getId()+",";
					}
				}
			}
//			receiveUser += projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());
			String userIds= projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());//项目内的人员
			receiveUser += receiveUser + userIds;

			receiveUser += projectSummary.getCreateUser().toString()+",";
			String content = "【"+userNameDecrypt+"】对【"+projectSummary.getTitle()+"】进行了留言/批示";
			String appMsgType = "34";

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(projectSummary.getId());
			message.setMsgTitle("项目管理留言/批示");
			message.setMsgType(appMsgType);
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(content);
			message.setTwoLevelType(appMsgType);
			message.setCreateTime(new Date());
			message.setMsgSubitem("项目管理");
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
			messageService.sendMessageInfo(message);

			message.setId(null);
			message.setMsgType("11");
			message.setMsgPlatform("app");
			messageService.sendMessageInfo(message);

			ProjectLog projectLog = new ProjectLog();//项目日志
			projectLog.setProjId(projectSummary.getId());
			projectLog.setHandleType("项目批示");
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】对【"+projectSummary.getTitle()+"】进行了留言/批示");
			projectLogService.save(projectLog);
		}else if("1".equals(messageInformation.getBusinessType())){//督察督办批示/留言
			SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(messageInformation.getBusinessId());

			receiveUser += supervisionInfo.getCreateUser()+",";//事项下发单位
			if(isLead){//如果当前用户是四大班子领导
				String deptId = supervisionInfo.getCreateDept().toString();
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,deptId).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users= iUserSearchClient.listByPostAndDept(leadPostId,deptId).getData();//获取单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users = userClient.getUserListByDeptId(deptId).getData();//获取该单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
			}
			String[] ids1 = supervisionInfo.getLeadUnit().split(",");//事项牵头单位
			for (String id : ids1) {
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				if(isLead){//如果当前用户是四大班子领导
					users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}

					users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
				}
			}
			String[] ids2 = supervisionInfo.getDutyUnit().split(",");//事项责任单位
			for (String id : ids2) {
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				if(isLead){//如果当前用户是四大班子领导
					users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}

					users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
				}
			}

			String content = "【"+userNameDecrypt+"】对【"+supervisionInfo.getServName()+"】进行了留言/批示";
			String msgType = "41";
			String appMsgType = "42";

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(supervisionInfo.getId());
			message.setMsgTitle("督查督办留言/批示");
			message.setMsgType(msgType);
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(content);
			message.setCreateTime(new Date());

			//关于@发送消息
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
			messageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
			message.setMsgSubitem(value);
			message.setTwoLevelType(appMsgType);
			messageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(supervisionInfo.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("8");
			log.setOperationTime(new Date());
			log.setContent("【"+userNameDecrypt+"】对【"+supervisionInfo.getServName()+"】进行留言/批示");
			supervisionLogService.save(log);
		}else if("2".equals(messageInformation.getBusinessType())){//考核评价
			//发送消息
			AnnualEvaluation ae = annualEvaluationService.getById(messageInformation.getBusinessId());
			QuarterlyEvaluation qe = quarterlyEvaluationService.getById(messageInformation.getBusinessId());
			if(ae!=null){//年度评价
				String msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
				String msgIntro = "【"+userNameDecrypt+"】对年度评价指标：【"+ae.getMajorTarget()+"】进行留言/批示";
				Long deptId=ae.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = ae.getAppraiseObjectId().split(",");//考核对象单位ids
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(ids));
				ids1List.add(deptId.toString());
				String[] ids1 = (String[]) ids1List.toArray(new String[0]);
				String[] ids2 = ae.getAppraiseDeptid().split(",");

				for (String id : ids1) {
						List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						if(isLead){
							users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}
							users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}

						}
					}
					for (String id : ids2) {
						List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						if(isLead){
							users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}
							users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}
					}
				}
				UnifyMessage message = new UnifyMessage();
				message.setMsgId(Long.valueOf(messageInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("年度评价批示/留言");//消息标题
				message.setMsgType("43");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
				message.setTwoLevelType("46");//年度批示留言
				messageService.sendMessageInfo(message);
			}else if(qe!=null){//季度评价
				//发送消息
				String msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
				String msgIntro="";
				if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getMajorTarget()+"】进行留言/批示";
				}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getFirstTarget()+"】进行留言/批示";
				} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getTwoTarget()+"】进行留言/批示";
				}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getImportWork()+"】进行留言/批示";
				} else {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标进行留言/批示";
				}

				Long deptId=qe.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = qe.getCheckObjectId().split(",");//考核对象单位id
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(ids));
				ids1List.add(deptId.toString());
				String[] ids1 = (String[]) ids1List.toArray(new String[0]);
				for (String id : ids1) {
						List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						if(isLead){
							users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}
							users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
							if(users!=null){
								for(User u : users){
									receiveUser += u.getId()+",";
								}
							}
						}
				}
				String[] ids2 = qe.getAppraiseDeptid().split(",");
				for (String id : ids2) {
					List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
					if(isLead){
						users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
					}
				}

				UnifyMessage message = new UnifyMessage();
				message.setMsgId(Long.valueOf(messageInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("季度评价批示/留言");//消息标题
				message.setMsgType("44");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
				message.setTwoLevelType("47");//季度批示留言
				messageService.sendMessageInfo(message);
			}
		}else if("4".equals(messageInformation.getBusinessType())){//绩效考核-领导评价
			Long  leaderAppriseId= messageInformation.getBusinessId();
			//获取领导评价对象
			LeaderApprise la =leaderAppriseService.getById(leaderAppriseId);
			//给领导评价创建人发消息
			Long createDept = la.getCreateDept();
			List<User> createDeptUsers= iUserSearchClient.listByPostAndDept(authPostId, String.valueOf(createDept)).getData();//获取单位下面所有管理员用户
			if(createDeptUsers!=null){
				for(User u : createDeptUsers){
					receiveUser += u.getId()+",";
				}
			}
			//给评价单位发消息
			List<User> users= iUserSearchClient.listByPostAndDept(authPostId,la.getDeptId()).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
			//四大班子给下面领导发消息
			if(isLead){
				users= iUserSearchClient.listByPostAndDept(leadPostId,la.getDeptId()).getData();//获取责任单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
				users = userClient.getUserListByDeptId(la.getDeptId()).getData();//获取该责任单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users= iUserSearchClient.listByPostAndDept(leadPostId, String.valueOf(createDept)).getData();//获取创建单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
				users = userClient.getUserListByDeptId(String.valueOf(createDept)).getData();//获取创建单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
			}
			//发送消息
			String content = "【"+userNameDecrypt+"】领导对【"+la.getDeptName()+"】进行了领导评价留言/批示";
			UnifyMessage message = new UnifyMessage();
			message.setMsgId(Long.valueOf(la.getId()));//消息主键（业务主键）
			message.setMsgTitle("领导评价");//消息标题
			message.setMsgType("48");//消息类型，字典编码：web_message_type
			message.setMsgPlatform("web");//平台：web或app
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));
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
		}
		return R.success("操作成功！");
	}

	/**
	 * 公共方法-批示/留言-app
	 */
	@PostMapping("/saveMsgAndFileApp")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "公共方法-批示/留言", notes = "传入MessageInformation对象")
	public R saveMsgAndFileApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("公共方法-批示/留言-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		String encryptSign;
		JSONObject jsonParams;
		if (dataMap.get("extra") != null) {
			encryptSign = dataMap.get("sign").toString();
			jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
		}else{
			return R.fail("加密解析错误");
		}
		MessageInformation messageInformation = objectMapper.convertValue(jsonParams, MessageInformation.class);
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		messageInformation.setAppriseUser(userNameDecrypt);
		messageInformation.setAppriseUserId(user.getId());
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
		messageInformation.setAppriseuserDeptname(deptName);

		iMessageInformationService.save(messageInformation);

		if (messageInformation.getAppriseFilesList() != null) {
			List<AppriseFiles> appriseFilesList = messageInformation.getAppriseFilesList();
			//向文件信息表中保存数据
			for (AppriseFiles appriseFiles : appriseFilesList) {
				appriseFiles.setFileFrom("PC端");
				appriseFiles.setBusinessId(messageInformation.getId());
				appriseFiles.setUploadUserName(userNameDecrypt);
				iAppriseFilesService.save(appriseFiles);
			}
		}
		String title = "新增批示/留言";
		String businessId = String.valueOf(messageInformation.getId());
		String businessTable = "MessageInformation";
		int businessType = BusinessType.INSERT.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title, businessId, businessTable, businessType);
		}


		//TODO 各个模块发送消息
		String receiveUser = messageInformation.getAppriseUserId()+",";//接收人
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

			//发送短信开始 20230516
			//督查督办批示、留言
			if("1".equals(messageInformation.getBusinessType())){
				String mobiles = "";
				//督查督办需要发送消息的对象：牵头单位、督办人、评价人、“@”对象。（如果督办人和评价人是同一手机号，只发一次）
				SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(messageInformation.getBusinessId());
				//1、牵头单位
				String[] qtdwNames = supervisionInfo.getLeadUnitName().split(",");//事项牵头单位名称
				for (String qtdwName : qtdwNames) {
					//根据部门名称获取用户
					List<User> userListDx1 = iUserSearchClient.getDeptUsers(qtdwName).getData();
					if(userListDx1 != null && userListDx1.size() > 0){
						for(User userDx1 : userListDx1){
							mobiles += userDx1.getPhone()+",";
						}
					}
				}
				//2、督办人（一个）
				String supervisor = supervisionInfo.getSupervisorName();
				//根据部门名称获取用户
				List<User> userListDx2 = iUserSearchClient.getDeptUsers(supervisor).getData();
				if(userListDx2 != null && userListDx2.size() > 0){
					for(User userDx2 : userListDx2){
						if(!mobiles.contains(userDx2.getPhone())){  //去除重复手机号
							mobiles += userDx2.getPhone()+",";
						}
					}
				}

				//3、评价人（一个）
				String evaluator = supervisionInfo.getEvaluatorName();
				//根据部门名称获取用户
				List<User> userListDx3 = iUserSearchClient.getDeptUsers(evaluator).getData();
				if(userListDx3 != null && userListDx3.size() > 0){
					for(User userDx3 : userListDx3){
						if(!mobiles.contains(userDx3.getPhone())){  //去除重复手机号
							mobiles += userDx3.getPhone()+",";
						}
					}
				}

				//4、@对象
				String atUserId = messageInformation.getAtUserId();
				if(StringUtil.isNotBlank(atUserId)){
					String[] userIdsDx4 = messageInformation.getAtUserId().split(",");
					for (String userIdDx4 : userIdsDx4) {
						List<User> userListDx4 = iUserSearchClient.listByUser(userIdDx4).getData();
						if(!mobiles.contains(userListDx4.get(0).getPhone())) {
							mobiles += userListDx4.get(0).getPhone();
						}
					}
				}
				log.info("督察督办所有需要发短信的号码："+mobiles);
				if(StringUtil.isNotBlank(mobiles)){
					mobiles = mobiles.substring(0, mobiles.length() - 1);
					String content = "您有一条领导批示，请登录督考一体化平台进行查看。";
					smsDockingService.send(mobiles,content);
				}
			}
			//项目管理批示、留言
			if("3".equals(messageInformation.getBusinessType())){
				String mobiles = "";
				ProjectSummary projectSummary = projectSummaryService.getById(messageInformation.getBusinessId());
				//1、市直行业主管部门
				String[] szhyzgbmNames = projectSummary.getSzhyzgbmName().split(",");
				for (String szhyzgbmName : szhyzgbmNames) {
					//根据部门名称获取用户
					List<User> userListXm1 = iUserSearchClient.getDeptUsers(szhyzgbmName).getData();
					if(userListXm1 != null && userListXm1.size() > 0){
						for(User userXm1 : userListXm1){
							mobiles += userXm1.getPhone()+",";
						}
					}
				}
				//2、县级包抓领导
				String[] xjbzlds = projectSummary.getXjbzld().split(",");
				for (String xjbzld : xjbzlds) {
					List<User> userListXm2 = iUserSearchClient.listByUser(xjbzld).getData();
					if(userListXm2 != null && userListXm2.size() > 0){
						for(User userXm2 : userListXm2){
							mobiles += userXm2.getPhone()+",";
						}
					}
				}
				//3、调度单位
				String[] dwmcNames = projectSummary.getDwmcName().split(",");
				for (String dwmcName : dwmcNames) {
					//根据部门名称获取用户
					List<User> userListXm3 = iUserSearchClient.getDeptUsers(dwmcName).getData();
					if(userListXm3 != null && userListXm3.size() > 0){
						for(User userXm3 : userListXm3){
							mobiles += userXm3.getPhone()+",";
						}
					}
				}
				//4、@对象
				String atUerId = messageInformation.getAtUserId();
				if(StringUtil.isNotBlank(atUerId)){
					String[] userIdsDx4 = atUerId.split(",");
					for (String userIdDx4 : userIdsDx4) {
						List<User> userListDx4 = iUserSearchClient.listByUser(userIdDx4).getData();
						if(!mobiles.contains(userListDx4.get(0).getPhone())) {
							mobiles += userListDx4.get(0).getPhone();
						}
					}
				}
				log.info("督察督办所有需要发短信的号码："+mobiles);
				if(StringUtil.isNotBlank(mobiles)){
					mobiles = mobiles.substring(0, mobiles.length() - 1);
					String content = "您有一条领导批示，请登录督考一体化平台进行查看。";
					smsDockingService.send(mobiles,content);
				}
			}
			//发送短信结束 20230516
		}
		String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id

		if("3".equals(messageInformation.getBusinessType())){//项目管理批示/留言
			ProjectSummary projectSummary = projectSummaryService.getById(messageInformation.getBusinessId());
			if(isLead){
				//市委办公室、市政府办公室、市发改委
				String deptId1 = sysClient.getDeptIdsByFuzzy("000000","市委办公室").getData();
				String deptId2 = sysClient.getDeptIdsByFuzzy("000000","市政府办公室").getData();
				String deptId3 = sysClient.getDeptIdsByFuzzy("000000","市发展改革委").getData();
				List<User> users= iUserSearchClient.listByPostAndDept(leadPostId,deptId1).getData();//获取单位下面所有领导用户
				users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId2).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId3).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId1).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId2).getData());
				users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId3).getData());
				//市发改委投资科
				receiveUser += PropConstant.getProjectShzhId("6207")+",";
				//市级行业主管部门
				String deptId4 = projectSummary.getSzhyzgbm();
				if(StringUtil.isNotBlank(deptId4)){
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId4).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId4).getData());
				}
				//调度单位
				String deptId5 = projectSummary.getDwmc();
				if(StringUtil.isNotBlank(deptId5)){
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId5).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId5).getData());
				}
				//市级包抓领导
				String sjbzld = projectSummary.getSjbzld();
				if(StringUtil.isNotBlank(sjbzld)){
					receiveUser += sjbzld+",";
				}
				//如果不是市级项目
				if(!"6207".equals(projectSummary.getAreaCode())){
					//县级四大班子
					String roleIdXj = sysClient.getRoleIds("000000", "县级四大班子").getData().replace(",","");
					users.addAll(userClient.getUserListByRoleId(roleId).getData());
					//县级发改局
					receiveUser += PropConstant.getProjectShzhId(projectSummary.getAreaCode())+",";
					//县发改局领导
					String aa = PropConstant.getProjectShzhId(projectSummary.getAreaCode());
					System.out.println(aa);
					User u = userClient.userInfoById(Long.parseLong(Func.isNotEmpty(PropConstant.getProjectShzhId(projectSummary.getAreaCode()))?PropConstant.getProjectShzhId(projectSummary.getAreaCode()):"123456")).getData();
					if(u != null && StringUtil.isNotBlank(u.getDeptId())){
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,u.getDeptId()).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId,u.getDeptId()).getData());
					}
					//县级行业主管部门
					String deptId6 = projectSummary.getXqhyzgbm();
					if(StringUtil.isNotBlank(deptId6)){
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId,deptId6).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId,deptId6).getData());
					}
				}
				for (User user1 : users) {
					if(!user.getId().equals(user1.getId())){
						receiveUser += user1.getId()+",";
					}
				}
			}
			String userIds= projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());//项目内的人员
			receiveUser += receiveUser + userIds;

			receiveUser += projectSummary.getCreateUser().toString()+",";
			String content = "【"+userNameDecrypt+"】对【"+projectSummary.getTitle()+"】进行了留言/批示";
			String appMsgType = "34";

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(projectSummary.getId());
			message.setMsgTitle("项目管理留言/批示");
			message.setMsgType(appMsgType);
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(content);
			message.setTwoLevelType(appMsgType);
			message.setCreateTime(new Date());
			message.setMsgSubitem("项目管理");
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
			messageService.sendMessageInfo(message);

			message.setId(null);
			message.setMsgType("11");
			message.setMsgPlatform("app");
			messageService.sendMessageInfo(message);

			ProjectLog projectLog = new ProjectLog();//项目日志
			projectLog.setProjId(projectSummary.getId());
			projectLog.setHandleType("项目批示");
			projectLog.setHandleUser(userNameDecrypt);
			projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
			String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
			projectLog.setHandleContent("【"+handleUserDecrypt+"】对【"+projectSummary.getTitle()+"】进行了留言/批示");
			projectLogService.save(projectLog);
		}else if("1".equals(messageInformation.getBusinessType())){//督察督办批示/留言
			SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(messageInformation.getBusinessId());

			receiveUser += supervisionInfo.getCreateUser()+",";//事项下发单位
			if(isLead){//如果当前用户是四大班子领导
				String deptId = supervisionInfo.getCreateDept().toString();
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,deptId).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users= iUserSearchClient.listByPostAndDept(leadPostId,deptId).getData();//获取单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users = userClient.getUserListByDeptId(deptId).getData();//获取该单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
			}
			String[] ids1 = supervisionInfo.getLeadUnit().split(",");//事项牵头单位
			for (String id : ids1) {
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				if(isLead){//如果当前用户是四大班子领导
					users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}

					users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
				}
			}
			String[] ids2 = supervisionInfo.getDutyUnit().split(",");//事项责任单位
			for (String id : ids2) {
				List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				if(isLead){//如果当前用户是四大班子领导
					users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}

					users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
				}
			}

			String content = "【"+userNameDecrypt+"】对【"+supervisionInfo.getServName()+"】进行了留言/批示";
			String msgType = "41";
			String appMsgType = "42";

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(supervisionInfo.getId());
			message.setMsgTitle("督查督办留言/批示");
			message.setMsgType(msgType);
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(content);
			message.setCreateTime(new Date());

			//关于@发送消息
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
			messageService.sendMessageInfo(message);

			String value = dictBizClient.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo()).getData();
			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
			message.setMsgSubitem(value);
			message.setTwoLevelType(appMsgType);
			messageService.sendMessageInfo(message);

			SupervisionLog log = new SupervisionLog();
			log.setServCode(supervisionInfo.getServCode());
			log.setOperationDept(user.getDeptId());
			log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
			log.setOperationUser(user.getId().toString());
			log.setOperationUserName(userNameDecrypt);
			log.setOperationType("8");
			log.setOperationTime(new Date());
			log.setContent("【"+userNameDecrypt+"】对【"+supervisionInfo.getServName()+"】进行留言/批示");
			supervisionLogService.save(log);
		}else if("2".equals(messageInformation.getBusinessType())){//考核评价
			//发送消息
			AnnualEvaluation ae = annualEvaluationService.getById(messageInformation.getBusinessId());
			QuarterlyEvaluation qe = quarterlyEvaluationService.getById(messageInformation.getBusinessId());
			if(ae!=null){//年度评价
				String msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
				String msgIntro = "【"+userNameDecrypt+"】对年度评价指标：【"+ae.getMajorTarget()+"】进行留言/批示";
				Long deptId=ae.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = ae.getAppraiseObjectId().split(",");//考核对象单位ids
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(ids));
				ids1List.add(deptId.toString());
				String[] ids1 = (String[]) ids1List.toArray(new String[0]);
				String[] ids2 = ae.getAppraiseDeptid().split(",");

				for (String id : ids1) {
					List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
					if(isLead){
						users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}

					}
				}
				for (String id : ids2) {
					List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
					if(isLead){
						users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
					}
				}
				UnifyMessage message = new UnifyMessage();
				message.setMsgId(Long.valueOf(messageInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("年度评价批示/留言");//消息标题
				message.setMsgType("43");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
				message.setTwoLevelType("46");//年度批示留言
				messageService.sendMessageInfo(message);
			}else if(qe!=null){//季度评价
				//发送消息
				String msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
				String msgIntro="";
				if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getMajorTarget()+"】进行留言/批示";
				}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getFirstTarget()+"】进行留言/批示";
				} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getTwoTarget()+"】进行留言/批示";
				}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标：【"+qe.getImportWork()+"】进行留言/批示";
				} else {
					msgIntro = "【"+userNameDecrypt+"】对季度评价指标进行留言/批示";
				}

				Long deptId=qe.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = qe.getCheckObjectId().split(",");//考核对象单位id
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(ids));
				ids1List.add(deptId.toString());
				String[] ids1 = (String[]) ids1List.toArray(new String[0]);
				for (String id : ids1) {
					List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
					if(isLead){
						users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
					}
				}
				String[] ids2 = qe.getAppraiseDeptid().split(",");
				for (String id : ids2) {
					List<User> users= iUserSearchClient.listByPostAndDept(authPostId,id).getData();//获取单位下面所有管理员用户
					if(users!=null){
						for(User u : users){
							receiveUser += u.getId()+",";
						}
					}
					if(isLead){
						users= iUserSearchClient.listByPostAndDept(leadPostId,id).getData();//获取单位下面所有领导用户
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
						users = userClient.getUserListByDeptId(id).getData();//获取该单位所有分管领导
						if(users!=null){
							for(User u : users){
								receiveUser += u.getId()+",";
							}
						}
					}
				}

				UnifyMessage message = new UnifyMessage();
				message.setMsgId(Long.valueOf(messageInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("季度评价批示/留言");//消息标题
				message.setMsgType("44");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));//接收人去重
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_PSLY);
				message.setTwoLevelType("47");//季度批示留言
				messageService.sendMessageInfo(message);
			}
		}else if("4".equals(messageInformation.getBusinessType())){//绩效考核-领导评价
			Long  leaderAppriseId= messageInformation.getBusinessId();
			//获取领导评价对象
			LeaderApprise la =leaderAppriseService.getById(leaderAppriseId);
			//给领导评价创建人发消息
			Long createDept = la.getCreateDept();
			List<User> createDeptUsers= iUserSearchClient.listByPostAndDept(authPostId, String.valueOf(createDept)).getData();//获取单位下面所有管理员用户
			if(createDeptUsers!=null){
				for(User u : createDeptUsers){
					receiveUser += u.getId()+",";
				}
			}
			//给评价单位发消息
			List<User> users= iUserSearchClient.listByPostAndDept(authPostId,la.getDeptId()).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
			//四大班子给下面领导发消息
			if(isLead){
				users= iUserSearchClient.listByPostAndDept(leadPostId,la.getDeptId()).getData();//获取责任单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
				users = userClient.getUserListByDeptId(la.getDeptId()).getData();//获取该责任单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}

				users= iUserSearchClient.listByPostAndDept(leadPostId, String.valueOf(createDept)).getData();//获取创建单位下面所有领导用户
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
				users = userClient.getUserListByDeptId(String.valueOf(createDept)).getData();//获取创建单位所有分管领导
				if(users!=null){
					for(User u : users){
						receiveUser += u.getId()+",";
					}
				}
			}
			//发送消息
			String content = "【"+userNameDecrypt+"】领导对【"+la.getDeptName()+"】进行了领导评价留言/批示";
			UnifyMessage message = new UnifyMessage();
			message.setMsgId(Long.valueOf(la.getId()));//消息主键（业务主键）
			message.setMsgTitle("领导评价");//消息标题
			message.setMsgType("48");//消息类型，字典编码：web_message_type
			message.setMsgPlatform("web");//平台：web或app
			message.setReceiveUser(StringUtils.isNotEmpty(messageInformation.getAtUserId())?messageInformation.getAtUserId()+","+AuthUtil.getUserId():quchong(receiveUser));
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
		}
		//return R.data(VSTool.encrypt(encryptSign, "操作成功", VSTool.CHN));
		return R.success("操作成功！");
	}

	/**
	 * 撤回 公共方法-批示/留言
	 * @param id
	 * @return
	 */
	@GetMapping("/revokeMsgAndFile")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "撤回-公共方法-批示/留言", notes = "传入MessageInformation对象")
	public R revoke(Long id) {
		iMessageInformationService.removeById(id);
		// 删除文件信息表的数据
		iAppriseFilesService.remove(Wrappers.<AppriseFiles>lambdaQuery().eq(AppriseFiles::getBusinessId, id));

		String title = "撤回批示/留言";
		String businessId = String.valueOf(id);
		String businessTable = "MessageInformation";
		int businessType = BusinessType.DELETE.ordinal();
		String[] businessIds = businessId.split(",");
		if (businessIds.length > 0) {
			for (int i = 0; i < businessIds.length; i++) {
				SpringUtil.getBean(IOperLogService.class).saveLog(title, businessIds[i], businessTable, businessType);
			}
		} else {
			SpringUtil.getBean(IOperLogService.class).saveLog(title, businessId, businessTable, businessType);
		}

		return R.success("操作成功！");
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
}
