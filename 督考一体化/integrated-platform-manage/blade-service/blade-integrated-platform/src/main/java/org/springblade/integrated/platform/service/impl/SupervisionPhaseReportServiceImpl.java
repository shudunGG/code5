package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionPhaseReportMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionPhaseReportServiceImpl extends BaseServiceImpl<SupervisionPhaseReportMapper, SupervisionPhaseReport> implements ISupervisionPhaseReportService {

	@Lazy
	@Autowired
	private ISupervisionFilesService filesService;

	@Resource
	private ISysClient sysClient;

	@Lazy
	@Autowired
	private ISupervisionInfoService supervisionInfoService;

	@Resource
	private IUserSearchClient iUserSearchClient;

	@Autowired
	private IUnifyMessageService messageService;

	@Resource
	private IDictBizClient dictBizClient;

	@Resource
	private IUserClient userClient;

	@Lazy
	@Autowired
	private ISupervisionPhasePlanService supervisionPhasePlanService;

	@Autowired
	private ISupervisionSubmitAuditService supervisionSubmitAuditService;

	@Lazy
	@Autowired
	private ISupervisionScoreService supervisionScoreService;

	@Autowired
	private  ISupervisionLogService supervisionLogService;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Lazy
	@Autowired
	private ISupervisionPhaseReportAllService supervisionPhaseReportAllService;

	@Autowired
	private ISupervisionPhaseReportBackService supervisionPhaseReportBackService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAll(SupervisionPhaseReport report, boolean isLeadUnit) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(report.getServCode());
		fileDel.setPhaseId(report.getPhaseId());
		fileDel.setFileFrom("5");
		fileDel.setCreateDept(Long.parseLong(report.getReportDept()));
		filesService.remove(Condition.getQueryWrapper(fileDel));

		List<SupervisionFiles> supervisionFilesList = report.getSupervisionFilesList();
		for (SupervisionFiles supervisionFiles : supervisionFilesList) {
			supervisionFiles.setServCode(report.getServCode());
			supervisionFiles.setPhaseId(report.getPhaseId());
			supervisionFiles.setFileFrom("5");
			supervisionFiles.setUploadTime(new Date());
			supervisionFiles.setUploadUserName(userNameDecrypt);
			supervisionFiles.setUploadUser(user.getId().toString());
			supervisionFiles.setId(null);
			filesService.save(supervisionFiles);
		}

		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		if(ObjectUtil.isEmpty(info.getDutyUnit())&&(report.getReportStatus().equals("1")||report.getReportStatus().equals("7"))&& ObjectUtil.isEmpty(report.getParentId())){
			if(report.getReportStatus().equals("1"))
				report.setReportStatus("2");
			if(report.getReportStatus().equals("7"))
				report.setReportStatus("8");
			SupervisionPhasePlan supervisionPhasePlan = supervisionPhasePlanService.getById(report.getPhaseId());
			if(ObjectUtil.isNotEmpty(supervisionPhasePlan)&&supervisionPhasePlan.getReportStatus().equals("0")){
				supervisionPhasePlan.setReportStatus("1");
			}
			supervisionPhasePlanService.updateById(supervisionPhasePlan);
		}

		String receiveUser = "";
		String content = "";
		String msgType = "";
		String appMsgType = "";
		if("0".equals(report.getReportStatus())&&"1".equals(report.getDownStatus())){//责任单位待汇报，已下发
			content = "【"+userNameDecrypt+"】已下发【"+info.getServName()+"】";
			receiveUser = report.getDownUserId();
			msgType = "17";
			appMsgType = "3";
		/*}else if("0".equals(report.getReportStatus())&&"0".equals(report.getDownStatus())){//责任单位待汇报，撤回下发
			content = "【"+user.getRealName()+"】已撤回【"+info.getServName()+"】下发";
			receiveUser = report.getDownUserId();
			report.setDownUserId(null);//撤回下发人员id
			msgType = "17";
			appMsgType = "3";*/
		}else if("1".equals(report.getReportStatus())||"7".equals(report.getReportStatus())){//7.超期汇报
			SupervisionPhaseReportAll supervisionPhaseReportAll = new SupervisionPhaseReportAll();
			supervisionPhaseReportAll.setPhaseId(report.getPhaseId());
			List<SupervisionPhaseReportAll> list = supervisionPhaseReportAllService.list(Condition.getQueryWrapper(supervisionPhaseReportAll));
			if(list!=null&&list.size()>0){
				if ("1".equals(report.getReportStatus())) {
					report.setReportStatus("2");
				} else {
					report.setReportStatus("8");
				}
			}
			content = "【"+userNameDecrypt+"】上报【"+info.getServName()+"】阶段汇报";
			msgType = "19";
			appMsgType = "5";
			if(ObjectUtil.isNotEmpty(report.getParentId())){
				SupervisionPhaseReport re = this.getById(report.getParentId());
				receiveUser = re.getCreateUser().toString();
			}else{
				String createUser = info.getCreateUser().toString();//督办单位
				String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位
				String dutyUnitUser = this.getUserIds(info.getDutyUnit());//责任单位

				if("0".equals(report.getDownStatus())){//未分派
					receiveUser = createUser+","+leadUnitUser;
				}else{
					receiveUser = createUser+","+leadUnitUser+dutyUnitUser;
				}
			}
		}else if("3".equals(report.getReportStatus())||"9".equals(report.getReportStatus())){//汇报送审:9.超期汇报送审
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段汇报审核申请";
			msgType = "21";
			appMsgType= "7";
			String[] userIdArr = report.getUserId().split(",");
			String batchNumber = UUID.randomUUID().toString();
			if(StatusConstant.AUDIT_SYNC_1.equals(report.getSync())){//并行
				receiveUser = report.getUserId();
			}else{
				receiveUser = userIdArr[0];
			}
			for(int i=0;i<userIdArr.length;i++){
				String userId = userIdArr[i];
				SupervisionSubmitAudit audit = new SupervisionSubmitAudit();
				audit.setBatchNumber(batchNumber);
				audit.setServId(report.getId());
				audit.setReportId(info.getId());
				audit.setTitle(report.getTitle());
				audit.setUserId(Long.parseLong(userId));
				if(StatusConstant.AUDIT_SYNC_1.equals(report.getSync())){
					audit.setStatus(0);
				}else{
					audit.setStatus(i==0?0:3);
				}
				audit.setSort(i);
				audit.setSync(Integer.parseInt(report.getSync()));
				audit.setOperationType("report");
				audit.setCreateUser(AuthUtil.getUserId());
				audit.setCreateTime(new Date());
				this.supervisionSubmitAuditService.save(audit);
			}
		}else if("5".equals(report.getReportStatus())||"11".equals(report.getReportStatus())){//11.超期汇报审核不通过
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段汇报";
			msgType = "19";
			appMsgType = "5";
			receiveUser += report.getReportUser();

			SupervisionScore entity = new SupervisionScore();
			entity.setServCode(info.getServCode());
			entity.setDeptId(report.getCreateDept());
			entity.setScoreType("2");
			entity.setDetails("退回扣分");
			SupervisionScore one = supervisionScoreService.getOne(Condition.getQueryWrapper(entity));

			if(one==null){
				//20230625 不是部门内部自己退回才扣分
				if(!(StringUtil.isNotBlank(report.getIssueDept()) && report.getIssueDept().equals(report.getCreateDept()))){
					entity.setScore(BigDecimal.valueOf(-2.00));
					supervisionScoreService.save(entity);
				}
			}

			if("1".equals(report.getIsJG())){//是否报送监管单位
				SupervisionPhaseReportBack back = Objects.requireNonNull(BeanUtil.copy(report, SupervisionPhaseReportBack.class));
				back.setJgDept(report.getJgDept());
				supervisionPhaseReportBackService.save(back);
			}
		}else if("2".equals(report.getReportStatus())||"8".equals(report.getReportStatus())){//8.超期汇报
			content = "【"+userNameDecrypt+"】上报【"+info.getServName()+"】阶段汇报";
			msgType = "19";
			appMsgType = "5";
			receiveUser = info.getCreateUser().toString();//督办单位
		}

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段汇报");
		message.setMsgType(msgType);
		message.setMsgStatus(0);
		message.setMsgPlatform("web");
		message.setMsgIntro(content);
		message.setCreateTime(new Date());
		message.setReceiveUser(quchong(receiveUser));
		messageService.sendMessageInfo(message);

		String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
		message.setMsgSubitem(value);
		message.setTwoLevelType(appMsgType);
		messageService.sendMessageInfo(message);

		//判断是牵头单位汇总还是责任单位汇报
		if (!isLeadUnit) {return this.updateById(report);}
		SupervisionPhaseReportAll reportAll = new SupervisionPhaseReportAll();
		BeanUtils.copyProperties(report,reportAll);
		return supervisionPhaseReportAllService.updateById(reportAll);
	}

	@Override
	public List<SupervisionPhaseReport> phaseOverdue() {
		QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper();
		wrapper.eq("report_status","0");
		wrapper.lt("date_format( remind_report_time, '%Y-%m-%d' )","date_format( now( ) , '%Y-%m-%d' )");
		List<SupervisionPhaseReport> list = this.list(wrapper);
		return list;
	}

	@Override
	public boolean issueByCrossDept(String serCode,String deptIds, BladeUser user) {
		User userA=null;
		//获取所有需要下发的汇报
		QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper<>();
		wrapper.eq("serv_code",serCode);
		if(ObjectUtil.isNotEmpty(user)){
			R<User> userR = userClient.userInfoById(user.getUserId());
			if(ObjectUtil.isNotEmpty(userR.getData())){
				userA=userR.getData();
			}
			wrapper.eq("report_Dept",user.getDeptId());
		}
		wrapper.eq("serv_code",serCode);
		List<SupervisionPhaseReport> list = this.list(wrapper);
		//需要跨部门下发的汇报
		List<SupervisionPhaseReport> reportIssueList=new ArrayList<>();
		List<User> magUsers=new ArrayList<>();
		for(SupervisionPhaseReport report:list){
			report.setIssueStatus("1");
			String[] deptStr = deptIds.split(",");
			for(String deptId:deptStr){
				SupervisionPhaseReport  reportIssue=new SupervisionPhaseReport();
				R<String> deptNameR = sysClient.getDeptName(Long.valueOf(deptId));
				String deptName = deptNameR.getData();
				if(ObjectUtil.isNotEmpty(deptName)){
					reportIssue.setReportDeptName(deptName);
				}
				reportIssue.setServCode(report.getServCode());
				reportIssue.setPhaseId(report.getPhaseId());
				reportIssue.setPhaseName(report.getPhaseName());
				reportIssue.setRemindReportTime(report.getRemindReportTime());
				reportIssue.setReportStatus("0");
				reportIssue.setReportDept(deptId);
				reportIssue.setParentId(report.getId().toString());
				if(ObjectUtil.isNotEmpty(user)){
					reportIssue.setIssueDept(user.getDeptId());
					reportIssue.setIssueDeptName(sysClient.getDeptName(Long.valueOf(user.getDeptId())).getData());
					reportIssue.setIssueUser(user.getUserId().toString());
					if(ObjectUtil.isNotEmpty(userA)){
						reportIssue.setIssueUserName(userA.getRealName());
					}
				}
				reportIssueList.add(reportIssue);
				//获取需要发送消息的人员
				R<List<User>> userLeader = userClient.getUserLeader(deptId, Constants.USER_POST_GLY_id);
				if (ObjectUtil.isNotEmpty(userLeader.getData())) {
					magUsers.addAll(userLeader.getData());
				}
			}

		}

		this.saveBatch(reportIssueList);
		//将所有父节点汇报下发状态更新为已跨部门下发
		this.updateBatchById(list);




		//发送消息
		String userId = "";
		for (int i = 0; i < magUsers.size(); i++) {
			if (i == magUsers.size() - 1) {
				userId += magUsers.get(i).getId().toString();
			} else {
				userId += magUsers.get(i).getId().toString() + ",";
			}
		}
		QueryWrapper<SupervisionInfo> wrapperInfo=new QueryWrapper<>();
		wrapperInfo.eq("serv_code",serCode);
		SupervisionInfo info = supervisionInfoService.getOne(wrapperInfo);
		if (ObjectUtil.isNotEmpty(userId)) {
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(info.getId());
			unifyMessage.setMsgTitle("事项跨部门下发消息");
			unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_PC_XF);
			unifyMessage.setMsgPlatform("web");
			unifyMessage.setReceiveUser(userId);
			if(ObjectUtil.isNotEmpty(userA)){
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(userA.getRealName());
				unifyMessage.setMsgIntro("【" + userNameDecrypt + "】已下发【" + info.getServName() + "】。");
			}
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);
			//sysClie
			R<String> value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo());
			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
			unifyMessage.setMsgSubitem(value.getData());
			unifyMessage.setTwoLevelType(Constants.DCDB_MAG_TYPE_APP_DB_tow_DCXF);
			unifyMessageService.sendMessageInfo(unifyMessage);
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		if(ObjectUtil.isNotEmpty(userA)){
			log.setOperationUser(userA.getId().toString());
			log.setOperationUserName(userA.getRealName());
		}
		log.setOperationType("1");
		log.setOperationTime(new Date());
		log.setContent("【" + info.getServName() + "】已下发");
		supervisionLogService.save(log);

		return true;
	}

	public String getUserIds(String deptIds){
		String receiveUser = "";

		String glyId = sysClient.getPostIdsByFuzzy("000000","管理员").getData();//获取管理员岗位id
		String[] ids = deptIds.split(",");
		for (String id : ids) {
			List<User> users= iUserSearchClient.listByPostAndDept(glyId,id).getData();//获取单位下面所有管理员用户
			if(users!=null){
				for(User u : users){
					receiveUser += u.getId()+",";
				}
			}
		}
		return receiveUser;
	}

	public String quchong(String res){
		String[] receiveUsers = res.split(",");
		List<String> receiveList = new ArrayList<>();
		String receiveUser = "";
		for (String s : receiveUsers) {
			if(!receiveList.contains(s)){
				receiveList.add(s);
			}
		}
		for (String s : receiveList) {
			receiveUser += s+",";
		}
		return receiveUser;
	}
}
