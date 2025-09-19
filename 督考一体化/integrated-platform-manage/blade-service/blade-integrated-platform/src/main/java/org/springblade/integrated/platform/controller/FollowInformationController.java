package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.FollowInformationVO;
import com.vingsoft.vo.FollowProjectInformationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.PropConstant;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.FollerInformationWrapper;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;


/**
 *
 *  @author: Adam
 *  @Date: 2022-4-9 18:39:00
 *  @Description: 关注服务
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/followInformation")
@Api(value = "关注服务", tags = "关注服务")
public class FollowInformationController extends BladeController {

	private final IFollowInformationService followInformationService;
	private final IProjectSummaryService projectSummaryService;
	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserClient userClient;
	private final ISupervisionInfoService iSupervisionInfoService;
	private final IQuarterlyEvaluationService iQuarterlyEvaluationService;
	private final IAnnualEvaluationService iAnnualEvaluationService;
	@Resource
	private final IUserSearchClient iUserSearchClient;
	private final IUnifyMessageService messageService;
	@Resource
	private final IDictBizClient dictBizClient;
	private final ISupervisionLogService supervisionLogService;
	private final IProjectLogService projectLogService;
	@Resource
	private IAnnualEvaluationService annualEvaluationService;
	@Resource
	private IQuarterlyEvaluationService quarterlyEvaluationService;

	/**
	 * 我的关注
	 * @return
	 */
	@GetMapping("/mylist")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "关注服务", notes = "")
	public R<IPage<FollowInformationVO>> mylist(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		query.setDescs("follow_date");
		String businessType = (String) entity.get("businessType");
		Object followUserId_equal = entity.get("followUserId_equal");
		if(businessType == null){
			return R.fail("缺少参数businessType");
		}
		if(followUserId_equal == null){
			return R.fail("缺少参数followUserId_equal");
		}
		List<String> list = new ArrayList<>();
		if(businessType.equals("2") || businessType.equals("4")){//考核评价
			list.add("2");
			list.add("4");
		}else{
			list.add(businessType);
		}

		LambdaQueryWrapper<FollowInformation> wrapper = Wrappers.<FollowInformation>query().lambda()
			.in(FollowInformation::getBusinessType,list).eq(FollowInformation::getFollowUserId,followUserId_equal);
		IPage<FollowInformationVO> pages = FollerInformationWrapper.build().pageVO(followInformationService.page(Condition.getPage(query), wrapper));
		List<FollowInformationVO> records = pages.getRecords();

		for (int i = 0; i < records.size(); i++) {
			FollowInformationVO vo = records.get(i);
			if("1".equals(vo.getBusinessType())){//如果是督查督办
				SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(vo.getBusinessId());
				if(supervisionInfo!=null) {
					vo.setServName(supervisionInfo.getServName());
					vo.setServTypeThree(supervisionInfo.getServTypeThree());
				}
			}

			if("2".equals(vo.getBusinessType()) || "4".equals(vo.getBusinessType())){//如果是考核评价
				QuarterlyEvaluation quarterlyEvaluation = iQuarterlyEvaluationService.getById(vo.getBusinessId());
				AnnualEvaluation annualEvaluation = iAnnualEvaluationService.getById(vo.getBusinessId());
				if(quarterlyEvaluation!=null) {
					vo.setCheckClassifyName(quarterlyEvaluation.getCheckClassifyName());
					vo.setMajorTarget(quarterlyEvaluation.getMajorTarget());
				}
				if(annualEvaluation!=null) {
					vo.setCheckClassifyName(annualEvaluation.getAppraiseClassifyName());
					vo.setMajorTarget(annualEvaluation.getMajorTarget());
				}
			}

			if("3".equals(vo.getBusinessType())){//如果是项目管理
				ProjectSummary projectSummary = projectSummaryService.getById(vo.getBusinessId());
				if(projectSummary!=null) {
					vo.setProjectName(projectSummary.getTitle());
					vo.setProjectType(projectSummary.getXmType());
				}
			}



		}
		return R.data(pages);
	}

	/**
	 * 领导关注项目
	 * @return
	 */
	@GetMapping("/leaderProjectList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "领导关注项目列表", notes = "")
	public R<IPage<FollowInformationVO>> leaderProjectList(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Object userid = entity.get("followUserId_equal");
		Object businessType = entity.get("businessType");
		if(userid == null){
			return R.fail("缺少参数followUserId_equal");
		}
		User user = userClient.userInfoById(Long.parseLong(userid.toString())).getData();
		boolean isLead = false;//是不是市级四大班子
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
		if(user !=null && user.getPostId() !=null) {
			entity.remove("followUserId_equal");
			String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
			String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
			for (String id : roleIds) {
				if (id.equals(roleId)) {
					isLead = true;
					break;
				}
			}

//			List<String> deptIds = new ArrayList<>();
//			deptIds.add(PropConstant.getSwldDeptId());//市委领导(部门id)
//			deptIds.add(PropConstant.getSzfldDeptId());//市政务领导(部门id)

			List<String> list = new ArrayList<>();
			if(businessType.equals("2") || businessType.equals("4")){//考核评价
				list.add("2");
				list.add("4");
			}else{
				list.add((String) businessType);
			}
			List<Long> userIds = new ArrayList<>();
			List<User> users = userClient.getUserListByRoleId(roleId).getData();//添加四大班子用户
			for (User user1 : users) {
				if(user.getId().longValue()!=user1.getId().longValue()){
					userIds.add(user1.getId());
				}
			}
			//如果账号不是领导账号，需添加领导关注
			if (!isLead) {
				if (StringUtil.isBlank(user.getDeptId())) {
					return R.fail("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(user.getDeptId(), leadPostId).getData();//(部门领导postid，该id在数据库中与市委领导部门id一致)
				if(!leaders.isEmpty()){
					for (User leader : leaders) {
						if(!AuthUtil.getUserId().equals(leader.getId())){
							userIds.add(leader.getId());
						}
					}
				}
			}
			LambdaQueryWrapper<FollowInformation> wrapper = Wrappers.<FollowInformation>query().lambda()
				.in(FollowInformation::getBusinessType,list).in(FollowInformation::getFollowUserId, userIds);
			wrapper.orderByDesc(FollowInformation::getFollowDate);
			IPage<FollowInformationVO> pages = FollerInformationWrapper.build().pageVO(followInformationService.page(Condition.getPage(query), wrapper));
			List<FollowInformationVO> records = pages.getRecords();
			for (int i = 0; i < records.size(); i++) {
				FollowInformationVO vo = records.get(i);
				if("1".equals(vo.getBusinessType())){//如果是督查督办
					SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(vo.getBusinessId());
					if(supervisionInfo!=null) {
						vo.setServName(supervisionInfo.getServName());
						vo.setServTypeThree(supervisionInfo.getServTypeThree());
					}
				}

				if("2".equals(vo.getBusinessType()) || "4".equals(vo.getBusinessType())){//如果是考核评价
					QuarterlyEvaluation quarterlyEvaluation = iQuarterlyEvaluationService.getById(vo.getBusinessId());
					AnnualEvaluation annualEvaluation = iAnnualEvaluationService.getById(vo.getBusinessId());
					if(quarterlyEvaluation!=null) {
						vo.setCheckClassifyName(quarterlyEvaluation.getCheckClassifyName());
						vo.setMajorTarget(quarterlyEvaluation.getMajorTarget());
					}
					if(annualEvaluation!=null) {
						vo.setCheckClassifyName(annualEvaluation.getAppraiseClassifyName());
						vo.setMajorTarget(annualEvaluation.getMajorTarget());
					}
				}

				if("3".equals(vo.getBusinessType())){
					ProjectSummary projectSummary = projectSummaryService.getById(vo.getBusinessId());
					if(projectSummary!=null){
						vo.setProjectName(projectSummary.getTitle());
						vo.setProjectType(projectSummary.getXmType());
					}
				}
			}
			return R.data(pages);
		}else{
			return R.fail("找不到该用户");
		}
	}

	/**
	 * 新增
	 * @param followInformation
	 * @return
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "新增", notes = "vo")
	public R save(@Valid @RequestBody FollowInformation followInformation) {
		int count = followInformationService.count(Wrappers.<FollowInformation>query().lambda().eq(FollowInformation::getFollowUserId,followInformation.getFollowUserId()).eq(FollowInformation::getBusinessId,followInformation.getBusinessId()));
		if(count>0){
			return R.fail("该用户已关注");
		}
		followInformation.setFollowDate(new Date());
		if(followInformation.getFollowUserId() == null){
			return R.fail("缺少参数followUserId");
		}
		User user = userClient.userInfoById(Long.parseLong(followInformation.getFollowUserId().toString())).getData();
		if(user.getDeptId() == null){
			return R.fail("该用户找不到单位");
		}else{
			followInformation.setFollowDeptId(Long.parseLong(user.getDeptId()));
		}
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		followInformation.setFollowUser(userNameDecrypt);
		boolean result = followInformationService.save(followInformation);

		if(result){
			String receiveUser = "";//接收人
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
				List<User> users = userClient.getUserListByRoleId(roleId).getData();//添加四大班子用户
				for (User user1 : users) {
					if(!user.getId().equals(user1.getId())){
						receiveUser += user1.getId()+",";
					}
				}
			}
			String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
			String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id

			//项目管理
			if("3".equals(followInformation.getBusinessType())){
				ProjectSummary projectSummary = projectSummaryService.getById(followInformation.getBusinessId());
				ProjectLog projectLog = new ProjectLog();//项目日志
				projectLog.setProjId(projectSummary.getId());
				projectLog.setHandleType("项目关注");
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】关注了【"+projectSummary.getTitle()+"】");
				projectLogService.save(projectLog);

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
						String roles = sysClient.getRoleIds("000000", "县级四大班子").getData();
						if(StringUtil.isNotBlank(roles)) {
							String roleIdXj =roles.replace(",", "");
							users.addAll(userClient.getUserListByRoleId(roleIdXj).getData());
						}
						//县级发改局
						receiveUser += PropConstant.getProjectShzhId(projectSummary.getAreaCode())+",";
						//县发改局领导
						User u = userClient.userInfoById(Long.parseLong(PropConstant.getProjectShzhId(projectSummary.getAreaCode()))).getData();
						if(u!=null){
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
//				receiveUser += projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());
				String userIds= projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());//项目内的人员
				receiveUser += receiveUser + userIds;
				receiveUser += projectSummary.getCreateUser().toString();

//				UnifyMessage message = new UnifyMessage();
//				message.setMsgId(projectSummary.getId());
//				message.setMsgTitle("项目管理关注");
//				message.setMsgType("47");
//				message.setMsgStatus(0);
//				message.setTwoLevelType("44");
//				message.setMsgPlatform("web");
//				message.setMsgSubitem("项目管理");
//				message.setMsgIntro(projectLog.getHandleContent());
//				message.setCreateTime(new Date());
//				message.setReceiveUser(quchong(receiveUser));//接收人去重
//				messageService.sendMessageInfo(message);
//
//				message.setId(null);
//				message.setMsgPlatform("app");
//				message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
//				messageService.sendMessageInfo(message);

			}else if("1".equals(followInformation.getBusinessType())){//督察督办关注
				SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(followInformation.getBusinessId());
				receiveUser = supervisionInfo.getCreateUser()+",";
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
				String content = "【"+userNameDecrypt+"】关注了【"+supervisionInfo.getServName()+"】";
				String msgType = "40";
				String appMsgType = "41";
				String[] ids1 = supervisionInfo.getLeadUnit().split(",");
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
				if (StringUtils.isNotBlank(supervisionInfo.getDutyUnit())) {
					String[] ids2 = supervisionInfo.getDutyUnit().split(",");
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
				}

//				UnifyMessage message = new UnifyMessage();
//				message.setMsgId(supervisionInfo.getId());
//				message.setMsgTitle("督查督办关注");
//				message.setMsgType(msgType);
//				message.setMsgStatus(0);
//				message.setMsgPlatform("web");
//				message.setMsgIntro(content);
//				message.setCreateTime(new Date());
//				message.setReceiveUser(quchong(receiveUser));//接收人去重
//				messageService.sendMessageInfo(message);
//
//				String value = dictBizClient.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo()).getData();
//				message.setId(null);
//				message.setMsgPlatform("app");
//				message.setMsgType(Constants.MSG_TYPE_APP_ONE_DB);
//				message.setMsgSubitem(value);
//				message.setTwoLevelType(appMsgType);
//				messageService.sendMessageInfo(message);

//				SupervisionLog log = new SupervisionLog();
//				log.setServCode(supervisionInfo.getServCode());
//				log.setOperationDept(user.getDeptId());
//				log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
//				log.setOperationUser(user.getId().toString());
//				log.setOperationUserName(user.getRealName());
//				log.setOperationType("10");
//				log.setOperationTime(new Date());
//				log.setContent("【"+user.getRealName()+"】关注了【"+supervisionInfo.getServName()+"】");
//				supervisionLogService.save(log);
			}else if("2".equals(followInformation.getBusinessType())){//年度评价
				AnnualEvaluation ae = annualEvaluationService.getById(followInformation.getBusinessId());
				String msgSubmit = "";
				if (ae == null) {
					msgSubmit = "";
				} else {
					msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
				}
				String msgIntro = "【"+userNameDecrypt+"】关注了年度评价指标：【"+ae.getMajorTarget()+"】";
				Long deptId=ae.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = ae.getAppraiseObjectId().split(",");//考核对象单位ids
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

				String[] ids2 = ae.getAppraiseDeptid().split(",");//评价单位ids
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

//				UnifyMessage message = new UnifyMessage();
//				message.setMsgId(Long.valueOf(followInformation.getBusinessId()));//消息主键（业务主键）
//				message.setMsgTitle("年度评价领导关注");//消息标题
//				message.setMsgType("45");//消息类型，字典编码：web_message_type
//				message.setMsgPlatform("web");//平台：web或app
//				message.setReceiveUser(quchong(receiveUser));//接收人去重
//				message.setMsgIntro(msgIntro);//消息简介
//				message.setMsgSubitem(msgSubmit);//消息分项
//				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
//				message.setCreateTime(new Date());
//				messageService.sendMessageInfo(message);
//
//				message.setId(null);
//				message.setMsgPlatform("app");
//				message.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
//				message.setTwoLevelType("48");//领导关注年度评价
//				messageService.sendMessageInfo(message);
			}else if("4".equals(followInformation.getBusinessType())){//季度评价
				//发送消息
				QuarterlyEvaluation qe = quarterlyEvaluationService.getById(followInformation.getBusinessId());
				String msgSubmit = "";
				if (qe == null) {
					msgSubmit = "";
				} else {
					msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
				}
				String msgIntro="";
				if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】关注了季度评价指标：【"+qe.getMajorTarget()+"】";
				}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】关注了季度评价指标：【"+qe.getFirstTarget()+"】";
				} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】关注了季度评价指标：【"+qe.getTwoTarget()+"】";
				}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
					msgIntro = "【"+userNameDecrypt+"】关注了季度评价指标：【"+qe.getImportWork()+"】";
				} else {
					msgIntro = "【"+userNameDecrypt+"】关注了季度评价指标";
				}
				Long deptId=qe.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] ids = qe.getCheckObjectId().split(",");//考核对象单位ids
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
//				UnifyMessage message = new UnifyMessage();
//				message.setMsgId(Long.valueOf(followInformation.getBusinessId()));//消息主键（业务主键）
//				message.setMsgTitle("季度评价领导关注");//消息标题
//				message.setMsgType("46");//消息类型，字典编码：web_message_type
//				message.setMsgPlatform("web");//平台：web或app
//				message.setReceiveUser(quchong(receiveUser));//接收人去重
//				message.setMsgIntro(msgIntro);//消息简介
//				message.setMsgSubitem(msgSubmit);//消息分项
//				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
//				message.setCreateTime(new Date());
//				messageService.sendMessageInfo(message);
//
//				message.setId(null);
//				message.setMsgPlatform("app");
//				message.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
//				message.setTwoLevelType("49");//领导关注季度评价
//				messageService.sendMessageInfo(message);
			}
			return R.data(followInformation.getId());
		}else{
			return R.status(false);
		}
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
	 * 修改
	 * @param followInformation
	 * @return
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "修改", notes = "vo")
	public R update(@Valid @RequestBody FollowInformation followInformation) {
		return R.status(followInformationService.updateById(followInformation));
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
		return R.status(followInformationService.removeById(id));
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
		String idList[] =ids.split(",");

		List<FollowInformation> followList = followInformationService.listByIds(Arrays.asList(idList));
		for (FollowInformation followInformation : followList) {
			User user = userClient.userInfoById(Long.parseLong(followInformation.getFollowUserId() .toString())).getData();

			String receiveUser = "";//接收人
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
				List<User> users = userClient.getUserListByRoleId(roleId).getData();//添加四大班子用户
				for (User user1 : users) {
					if(!user.getId().equals(user1.getId())){
						receiveUser += user1.getId()+",";
					}
				}
			}
			String authPostId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
			String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
			if("1".equals(followInformation.getBusinessType())){//督察督办关注
				SupervisionInfo supervisionInfo = iSupervisionInfoService.getById(followInformation.getBusinessId());
				receiveUser = supervisionInfo.getCreateUser()+",";
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
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				String content = "【"+userNameDecrypt+"】取消了关注【"+supervisionInfo.getServName()+"】";
				String msgType = "40";
				String appMsgType = "41";
				String[] ids1 = supervisionInfo.getLeadUnit().split(",");
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
				if (StringUtils.isNotBlank(supervisionInfo.getDutyUnit())) {
					String[] ids2 = supervisionInfo.getDutyUnit().split(",");
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
				}

				UnifyMessage message = new UnifyMessage();
				message.setMsgId(supervisionInfo.getId());
				message.setMsgTitle("督查督办取消关注");
				message.setMsgType(msgType);
				message.setMsgStatus(0);
				message.setMsgPlatform("web");
				message.setMsgIntro(content);
				message.setCreateTime(new Date());
				message.setReceiveUser(receiveUser);
				messageService.sendMessageInfo(message);

				String value = dictBizClient.getValue(supervisionInfo.getServTypeOne(), supervisionInfo.getServTypeTwo()).getData();
				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
				message.setMsgSubitem(value);
				message.setTwoLevelType(appMsgType);
				messageService.sendMessageInfo(message);

				SupervisionLog log = new SupervisionLog();
				log.setServCode(supervisionInfo.getServCode());
				log.setOperationDept(user.getDeptId());
				log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
				log.setOperationUser(user.getId().toString());
				log.setOperationUserName(userNameDecrypt);
				log.setOperationType("7");
				log.setOperationTime(new Date());
				log.setContent("【"+userNameDecrypt+"】取消了关注【"+supervisionInfo.getServName()+"】");
				supervisionLogService.save(log);
			}else if("3".equals(followInformation.getBusinessType())) {//项目管理关注
				ProjectSummary projectSummary = projectSummaryService.getById(followInformation.getBusinessId());
				ProjectLog projectLog = new ProjectLog();//项目日志
				projectLog.setProjId(projectSummary.getId());
				projectLog.setHandleType("项目关注");
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				projectLog.setHandleUser(userNameDecrypt);
				projectLog.setHandleDept(sysClient.getDept(Long.parseLong(user.getDeptId())).getData().getDeptName());
				String handleUserDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(projectLog.getHandleUser());
				projectLog.setHandleContent("【"+handleUserDecrypt+"】取消了对【"+projectSummary.getTitle()+"】关注");
				projectLogService.save(projectLog);

				if(isLead) {
					//市委办公室、市政府办公室、市发改委
					String deptId1 = sysClient.getDeptIdsByFuzzy("000000", "市委办公室").getData();
					String deptId2 = sysClient.getDeptIdsByFuzzy("000000", "市政府办公室").getData();
					String deptId3 = sysClient.getDeptIdsByFuzzy("000000", "市发展改革委").getData();
					List<User> users = iUserSearchClient.listByPostAndDept(leadPostId, deptId1).getData();//获取单位下面所有领导用户
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId2).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId3).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId1).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId2).getData());
					users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId3).getData());
					//市发改委投资科
					receiveUser += PropConstant.getProjectShzhId("6207") + ",";
					//市级行业主管部门
					String deptId4 = projectSummary.getSzhyzgbm();
					if (StringUtil.isNotBlank(deptId4)) {
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId4).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId4).getData());
					}
					//调度单位
					String deptId5 = projectSummary.getDwmc();
					if (StringUtil.isNotBlank(deptId5)) {
						users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId5).getData());
						users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId5).getData());
					}
					//市级包抓领导
					String sjbzld = projectSummary.getSjbzld();
					if (StringUtil.isNotBlank(sjbzld)) {
						receiveUser += sjbzld + ",";
					}
					//如果不是市级项目
					if (!"6207".equals(projectSummary.getAreaCode())) {
						//县级四大班子
						String roles = sysClient.getRoleIds("000000", "县级四大班子").getData();
						if (StringUtil.isNotBlank(roles)) {
							String roleIdXj = roles.replace(",", "");
							users.addAll(userClient.getUserListByRoleId(roleIdXj).getData());
						}
						//县级发改局
						receiveUser += PropConstant.getProjectShzhId(projectSummary.getAreaCode()) + ",";
						//县发改局领导
						User u = userClient.userInfoById(Long.parseLong(PropConstant.getProjectShzhId(projectSummary.getAreaCode()))).getData();
						if (u != null) {
							users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, u.getDeptId()).getData());
							users.addAll(iUserSearchClient.listByPostAndDept(authPostId, u.getDeptId()).getData());
						}
						//县级行业主管部门
						String deptId6 = projectSummary.getXqhyzgbm();
						if (StringUtil.isNotBlank(deptId6)) {
							users.addAll(iUserSearchClient.listByPostAndDept(leadPostId, deptId6).getData());
							users.addAll(iUserSearchClient.listByPostAndDept(authPostId, deptId6).getData());
						}
					}
					for (User user1 : users) {
						if (!user.getId().equals(user1.getId())) {
							receiveUser += user1.getId() + ",";
						}
					}
				}

//				receiveUser += projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());
				String userIds= projectSummaryService.getUserIdListByProjId(projectSummary.getId(),AuthUtil.getUserId());//项目内的人员
				receiveUser += receiveUser + userIds;

				receiveUser += projectSummary.getCreateUser().toString();

				UnifyMessage message = new UnifyMessage();
				message.setMsgId(projectSummary.getId());
				message.setMsgTitle("项目管理取消关注");
				message.setMsgType("47");
				message.setMsgStatus(0);
				message.setTwoLevelType("44");
				message.setMsgPlatform("web");
				message.setMsgSubitem("项目管理");
				message.setMsgIntro(projectLog.getHandleContent());
				message.setCreateTime(new Date());
				message.setReceiveUser(quchong(receiveUser));//接收人去重
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
				messageService.sendMessageInfo(message);
			}else if("2".equals(followInformation.getBusinessType())){//年度评价
				AnnualEvaluation ae = annualEvaluationService.getById(followInformation.getBusinessId());
				String msgSubmit = "";
				if (ae == null) {
					msgSubmit = "";
				} else {
					msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
				}
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				String msgIntro = "【"+userNameDecrypt+"】取消了关注年度评价指标：【"+ae.getMajorTarget()+"】";
				Long deptId=ae.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] idss = ae.getAppraiseObjectId().split(",");//考核对象单位ids
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(idss));
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

				String[] ids2 = ae.getAppraiseDeptid().split(",");//评价单位ids
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
				message.setMsgId(Long.valueOf(followInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("年度评价取消关注");//消息标题
				message.setMsgType("45");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(quchong(receiveUser));//接收人去重
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
				message.setTwoLevelType("48");//领导关注年度评价
				messageService.sendMessageInfo(message);
			}else if("4".equals(followInformation.getBusinessType())){//季度评价
				//发送消息
				QuarterlyEvaluation qe = quarterlyEvaluationService.getById(followInformation.getBusinessId());
				String msgSubmit = "";
				if (qe == null) {
					msgSubmit = "";
				} else {
					msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
				}
				String msgIntro="";
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】取消了关注季度评价指标：【"+qe.getMajorTarget()+"】";
				}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】取消了关注季度评价指标：【"+qe.getFirstTarget()+"】";
				} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
					msgIntro = "【"+userNameDecrypt+"】取消了关注季度评价指标：【"+qe.getTwoTarget()+"】";
				}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
					msgIntro = "【"+userNameDecrypt+"】取消了关注季度评价指标：【"+qe.getImportWork()+"】";
				} else {
					msgIntro = "【"+userNameDecrypt+"】取消了关注季度评价指标";
				}
				Long deptId=qe.getCreateDept();//指标创建单位下面的相关人员也要发送
				String[] idss = qe.getCheckObjectId().split(",");//考核对象单位ids
				ArrayList<String> ids1List = new ArrayList<String>(Arrays.asList(idss));
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
				message.setMsgId(Long.valueOf(followInformation.getBusinessId()));//消息主键（业务主键）
				message.setMsgTitle("季度评价取消关注");//消息标题
				message.setMsgType("46");//消息类型，字典编码：web_message_type
				message.setMsgPlatform("web");//平台：web或app
				message.setReceiveUser(quchong(receiveUser));//接收人去重
				message.setMsgIntro(msgIntro);//消息简介
				message.setMsgSubitem(msgSubmit);//消息分项
				message.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				message.setCreateTime(new Date());
				messageService.sendMessageInfo(message);

				message.setId(null);
				message.setMsgPlatform("app");
				message.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
				message.setTwoLevelType("49");//领导关注季度评价
				messageService.sendMessageInfo(message);
			}
		}
		return R.status(followInformationService.removeByIds(Arrays.asList(idList)));
	}

	/**
	 * 删除
	 * @param businessId
	 * @return
	 */
	@GetMapping("/exsit")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "判断是否存在", notes = "传入businessId")
	public R exsit(@ApiParam(value = "业务主键", required = true) @RequestParam String businessId,@ApiParam(value = "关注人id", required = true) @RequestParam String followUserId) {
		List<FollowInformation> followInformation = followInformationService.list(Wrappers.<FollowInformation>query().lambda().eq(FollowInformation::getBusinessId, businessId).eq(FollowInformation::getFollowUserId,followUserId));
		if(followInformation.size()==1){
			return R.data(followInformation.get(0).getId());
		}else if(followInformation.size()>1){
			String ids = followInformation.get(0).getId()+"";
			for (int i = 1; i < followInformation.size(); i++) {
				ids+=",";
				ids+=followInformation.get(i).getId();
			}
			return R.data(ids);
		}else{
			return R.data(false);
		}
	}

	/**
	 * 我的关注（项目类）
	 * @return
	 */
	@GetMapping("/myProjFlollow")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "我的关注（项目类）", notes = "传入参数")
	public R myProjFlollow(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		if(entity.get("followUserId")==null) {
			entity.put("followUserId", AuthUtil.getUserId());
		}
		IPage<FollowProjectInformationVO> pages = followInformationService.getProjectFollow(Condition.getPage(query), entity);
		return R.data(pages);
	}

	/**
	 * 领导关注（项目类）
	 * @return
	 */
	@GetMapping("/leaderProjFlollow")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "领导关注（项目类）", notes = "")
	public R leaderProjFlollow(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		Object userid = entity.get("followUserId");
		User user = userClient.userInfoById(Long.parseLong(userid.toString())).getData();
		boolean isLead = false;//是不是市级四大班子
		String leadPostId = sysClient.getPostIdsByFuzzy("000000","部门领导").getData();//获取领导岗位id
		String roleId = sysClient.getRoleIds("000000", "市级四大班子").getData().replace(",","");
		String[] roleIds = user.getRoleId().split(",");//判断该用户是不是市级四大班子领导
		for (String id : roleIds) {
			if (id.equals(roleId)) {
				isLead = true;
				break;
			}
		}

		if(user !=null && user.getPostId() !=null) {
			entity.remove("followUserId");
//			List<String> deptIds = new ArrayList<>();
//			deptIds.add(PropConstant.getSwldDeptId());//市委领导(部门id)
//			deptIds.add(PropConstant.getSzfldDeptId());//市政务领导(部门id)
//			entity.put("deptIds",deptIds);

			List<Long> userIds = new ArrayList<>();
			List<User> users = userClient.getUserListByRoleId(roleId).getData();//添加四大班子用户关注
			for (User user1 : users) {
//				if(user.getId().longValue()!=user1.getId().longValue()){
					userIds.add(user1.getId());
//				}
			}
			//如果账号不是领导账号，需添加领导关注
			if (!isLead) {
				if (StringUtil.isBlank(user.getDeptId())) {
					return R.fail("用户找不到部门");
				}
				List<User> leaders = userClient.getUserLeader(user.getDeptId(), leadPostId).getData();//(部门领导postid，该id在数据库中与市委领导部门id一致)
				if(!leaders.isEmpty()){
					for (User leader : leaders) {
						if(!AuthUtil.getUserId().equals(leader.getId())){
							userIds.add(leader.getId());
						}
					}
				}
			}
			entity.put("leaderIds",userIds);
			IPage<FollowProjectInformationVO> pages = followInformationService.getProjectFollow(Condition.getPage(query), entity);
			return R.data(pages);
		}else{
			return R.fail("找不到该用户");
		}

	}
}
