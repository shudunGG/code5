package org.springblade.integrated.platform.service.impl;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionPhaseReportAllMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionPhaseReportAllServiceImpl extends BaseServiceImpl<SupervisionPhaseReportAllMapper, SupervisionPhaseReportAll> implements ISupervisionPhaseReportAllService {

	@Lazy
	@Autowired
	private ISupervisionFilesService filesService;

	@Lazy
	@Autowired
	private ISupervisionInfoService supervisionInfoService;

	@Autowired
	private IUnifyMessageService messageService;

	@Resource
	private IDictBizClient dictBizClient;

	@Resource
	private IUserClient userClient;

	@Autowired
	private ISupervisionPhasePlanService supervisionPhasePlanService;

	@Autowired
	private ISupervisionSubmitAuditService supervisionSubmitAuditService;

	@Autowired
	private ISupervisionLogService supervisionLogService;

	@Resource
	private ISysClient sysClient;

	@Autowired
	private ISupervisionPhaseReportService supervisionPhaseReportService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAll(SupervisionPhaseReportAll report) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		report.setReportUser(user.getId().toString());
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		report.setReportUserName(userNameDecrypt);
		Dept dept = sysClient.getDept(Long.parseLong(user.getDeptId())).getData();
		report.setReportDept(dept.getId().toString());
		report.setReportDeptName(dept.getDeptName());
		report.setReportTime(new Date());
		report.setRemindReportTime(new Date());
		boolean flag = this.saveOrUpdate(report);

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(report.getServCode());
		fileDel.setPhaseId(report.getPhaseId());
		fileDel.setFileFrom("6");
		fileDel.setCreateDept(Long.parseLong(report.getReportDept()));
		filesService.remove(Condition.getQueryWrapper(fileDel));

		List<SupervisionFiles> supervisionFilesList = report.getSupervisionFilesList();
		for (SupervisionFiles supervisionFiles : supervisionFilesList) {
			supervisionFiles.setServCode(report.getServCode());
			supervisionFiles.setPhaseId(report.getPhaseId());
			supervisionFiles.setFileFrom("6");
			supervisionFiles.setUploadTime(new Date());
			supervisionFiles.setUploadUserName(userNameDecrypt);
			supervisionFiles.setUploadUser(user.getId().toString());
			filesService.save(supervisionFiles);
		}

		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		String receiveUser = "";
		String content = "";
		String msgType = "";
		String appMsgType = "";
		if("2".equals(report.getReportStatus())){//直接汇总
			content = "【"+userNameDecrypt+"】汇总上报【"+info.getServName()+"】阶段汇报";
			msgType = "58";
			appMsgType = "58";
			receiveUser = info.getCreateUser().toString();//督办单位

			SupervisionPhasePlan plan = supervisionPhasePlanService.getById(report.getPhaseId());
			if(ObjectUtil.isNotEmpty(plan)&&plan.getReportStatus().equals("0")){//阶段改为上报状态
				plan.setReportStatus("1");
			}
			supervisionPhasePlanService.updateById(plan);

			SupervisionPhaseReport report1 = new SupervisionPhaseReport();
			report1.setPhaseId(report.getPhaseId());
			report1.setReportStatus("1");//已汇报
			SupervisionPhaseReport report2 = new SupervisionPhaseReport();
			report2.setReportStatus("2");//已汇总
			supervisionPhaseReportService.update(report2,Condition.getQueryWrapper(report1));

			report1.setReportStatus("7");//超期已汇报
			report2.setReportStatus("8");//超期已汇总
			supervisionPhaseReportService.update(report2,Condition.getQueryWrapper(report1));
		}else if("3".equals(report.getReportStatus())){//下发人员送审
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】汇总阶段汇报审核申请";
			msgType = "59";
			appMsgType= "59";

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
				audit.setOperationType("reportAll");
				audit.setCreateUser(AuthUtil.getUserId());
				audit.setCreateTime(new Date());
				this.supervisionSubmitAuditService.save(audit);
			}
		}else if("5".equals(report.getReportStatus())){//直接汇总
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】汇总阶段汇报";
			msgType = "61";
			appMsgType = "61";
			receiveUser += report.getUpdateUser().toString();

			SupervisionPhaseReport report1 = new SupervisionPhaseReport();
			report1.setPhaseId(report.getPhaseId());
			SupervisionPhaseReport report2 = new SupervisionPhaseReport();
			report2.setReportStatus("1");
			supervisionPhaseReportService.update(report2,Condition.getQueryWrapper(report1));
		}

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("汇总阶段汇报");
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

		SupervisionLog log = new SupervisionLog();
		log.setServCode(report.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("5");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

		return flag;
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
