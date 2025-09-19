package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.crypto.sm4.SM4Crypto;
import com.vingsoft.entity.*;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionPhaseReportChiMapper;
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
public class SupervisionPhaseReportChiServiceImpl extends BaseServiceImpl<SupervisionPhaseReportChiMapper, SupervisionPhaseReportChi> implements ISupervisionPhaseReportChiService {

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
	private ISupervisionSubmitAuditService supervisionSubmitAuditService;

	@Autowired
	private ISupervisionLogService supervisionLogService;

	@Resource
	private ISysClient sysClient;

	@Autowired
	private ISupervisionPhaseReportService supervisionPhaseReportService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAll(SupervisionPhaseReportChi report) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		Dept dept = sysClient.getDept(Long.parseLong(user.getDeptId())).getData();
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));
		String receiveUser = "";
		String content = "";
		String msgType = "";
		String appMsgType = "";

		// TODO 新增分派
		if(StringUtils.isNotEmpty(report.getDownUsers())){
			String[] ids = report.getDownUsers().split(",");
			boolean flag = false;
			for (String id : ids) {
				User u = userClient.userInfoById(Long.parseLong(id)).getData();//接收人
				report.setReportStatus("0");
				report.setReportUser(id);
				report.setReportUserName(u.getRealName());
				Dept d = sysClient.getDept(Long.parseLong(u.getDeptId())).getData();;//接收人单位
				report.setReportDept(d.getId().toString());
				report.setReportDeptName(d.getDeptName());
				report.setReportStatus("0");
				report.setDownUserId(u.getId());//接收人
				report.setDownUserName(u.getRealName());
				report.setDownStatus("1");

				report.setIssueUser(user.getId().toString());//发送人
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				report.setIssueUserName(userNameDecrypt);
				report.setIssueDept(dept.getId().toString());//发送人单位
				report.setIssueDeptName(dept.getDeptName());
				report.setIssueStatus("1");

				//20240411145628处理脱敏字段开始
				SM4Crypto sm4 = SM4Crypto.getInstance();
				if(StringUtils.isNotEmpty(report.getLinkedName()) && sm4.checkDataIsEncrypt(report.getLinkedName())){
					report.setLinkedName(sm4.decrypt(report.getLinkedName()));
				}
				if(StringUtils.isNotEmpty(report.getLinkedPhone()) && sm4.checkDataIsEncrypt(report.getLinkedPhone())){
					report.setLinkedPhone(sm4.decrypt(report.getLinkedPhone()));
				}
				//20240411145628处理脱敏字段结束

				report.setId(null);
				flag = this.save(report);
			}

			if(flag){
				//20240411144434处理脱敏字段开始
				SM4Crypto sm4 = SM4Crypto.getInstance();
				String realName = user.getRealName();
				if(StringUtils.isNotEmpty(realName) && sm4.checkDataIsEncrypt(realName)){
					realName = sm4.decrypt(realName);
				}
				//20240411144434处理脱敏字段结束
				content = "【"+realName+"】分派了【"+info.getServName()+"】的阶段汇报任务";
				msgType = "72";
				appMsgType = "72";
				receiveUser = report.getDownUsers();//接收人
				sendMessage(report, user, info, receiveUser, content, msgType, appMsgType);//发送消息

				SupervisionPhaseReport byId = supervisionPhaseReportService.getById(report.getParentId());
				byId.setDownStatus("1");
				byId.setDownUserId(report.getDownUsers());
				supervisionPhaseReportService.updateById(byId);
			}
			return flag;
		}



		// TODO 分派汇报
		report.setReportTime(new Date());
		report.setRemindReportTime(new Date());
		boolean flag = this.saveOrUpdate(report);

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(report.getServCode());
		fileDel.setPhaseId(report.getPhaseId());
		fileDel.setFileFrom("chi");
		fileDel.setCreateDept(Long.parseLong(report.getReportDept()));
		filesService.remove(Condition.getQueryWrapper(fileDel));

		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		List<SupervisionFiles> supervisionFilesList = report.getSupervisionFilesList();
		for (SupervisionFiles supervisionFiles : supervisionFilesList) {
			supervisionFiles.setServCode(report.getServCode());
			supervisionFiles.setPhaseId(report.getPhaseId());
			supervisionFiles.setFileFrom("chi");
			supervisionFiles.setUploadTime(new Date());
			supervisionFiles.setUploadUserName(userNameDecrypt);
			supervisionFiles.setUploadUser(user.getId().toString());
			filesService.save(supervisionFiles);
		}

		if("1".equals(report.getReportStatus())){//直接汇报
			content = "【"+userNameDecrypt+"】上报【"+info.getServName()+"】阶段分派汇报";
			msgType = "68";
			appMsgType = "68";
			receiveUser = report.getIssueUser();//分派人
		}else if("3".equals(report.getReportStatus())){//下发人员送审
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段汇报审核申请";
			msgType = "69";
			appMsgType= "69";

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
				audit.setOperationType("reportChi");
				audit.setCreateUser(AuthUtil.getUserId());
				audit.setCreateTime(new Date());
				this.supervisionSubmitAuditService.save(audit);
			}
		}else if("5".equals(report.getReportStatus())){//直接汇总
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段分派汇报";
			msgType = "71";
			appMsgType = "71";
			receiveUser += report.getReportUser();//接收人
		}
		sendMessage(report, user, info, receiveUser, content, msgType, appMsgType);//发送消息
		return flag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteAll(SupervisionPhaseReportChi report) {
		boolean flag;
		try {
			//撤销汇报表已下发状态
			SupervisionPhaseReport byId = supervisionPhaseReportService.getById(report.getParentId());
			byId.setDownStatus("0");
			byId.setDownUserId(null);
			supervisionPhaseReportService.updateById(byId);
			//删除分派表数据
			this.remove(Wrappers.<SupervisionPhaseReportChi>update().lambda().eq(SupervisionPhaseReportChi::getParentId,report.getParentId()));
			//删除附件数据
			SupervisionFiles fileDel = new SupervisionFiles();
			fileDel.setServCode(report.getServCode());
			fileDel.setPhaseId(report.getPhaseId());
			fileDel.setFileFrom("chi");
			fileDel.setCreateDept(Long.parseLong(report.getReportDept()));
			filesService.remove(Condition.getQueryWrapper(fileDel));
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean delete(SupervisionPhaseReportChi report) {
		boolean flag;
		try {
			//删除分派表数据
			this.removeById(report.getId());

			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			SupervisionInfo info = new SupervisionInfo();
			info.setServCode(report.getServCode());
			info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

			String receiveUser = report.getReportUser();
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			String content = "【"+userNameDecrypt+"】撤销了【"+info.getServName()+"】阶段分派汇报";
			String msgType = "71";
			String appMsgType = "71";

			List<SupervisionPhaseReportChi> list = this.list(Wrappers.<SupervisionPhaseReportChi>update().lambda().eq(SupervisionPhaseReportChi::getParentId, report.getParentId()));
			if(!(list!=null&&list.size()>0)){
				//撤销汇报表已下发状态
				SupervisionPhaseReport byId = supervisionPhaseReportService.getById(report.getParentId());
				byId.setDownStatus("0");
				byId.setDownUserId(null);
				supervisionPhaseReportService.updateById(byId);
			}

			sendMessage(report, user, info, receiveUser, content, msgType, appMsgType);//发送消息
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	private void sendMessage(SupervisionPhaseReportChi report, User user, SupervisionInfo info, String receiveUser, String content, String msgType, String appMsgType) {
		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段分派汇报");
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
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("5");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);
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
