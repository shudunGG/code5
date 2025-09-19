package org.springblade.integrated.platform.service.impl;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionUpPlanVO;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionUpPlanMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/4/18 11:57
* @Version:        1.0
*/
@Service
@AllArgsConstructor
public class SupervisionUpPlanServiceImpl extends BaseServiceImpl<SupervisionUpPlanMapper, SupervisionUpPlan> implements ISupervisionUpPlanService {

	private final ISupervisionFilesService filesService;

	private final ISupervisionInfoService supervisionInfoService;

	@Resource
	private ISysClient sysClient;

	private final ISupervisionLogService supervisionLogService;

	@Resource
	private IUserClient userClient;

	@Autowired
	private IUnifyMessageService messageService;

	@Resource
	private IDictBizClient dictBizClient;

	@Autowired
	private ISupervisionSubmitAuditService supervisionSubmitAuditService;

	@Resource
	private IUserSearchClient iUserSearchClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveAll(SupervisionUpPlanVO vo) {
		SupervisionUpPlan entity = Objects.requireNonNull(BeanUtil.copy(vo,SupervisionUpPlan.class));
		entity.setUpDeptName(sysClient.getDeptName(entity.getUpDept()).getData());
		boolean save = this.save(entity);

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		SupervisionInfo info = supervisionInfoService.getById(vo.getServId());

		List<SupervisionFiles> filesList = vo.getSupervisionFilesList();
		for (SupervisionFiles file : filesList) {
			file.setServCode(info.getServCode());
			file.setPhaseId(entity.getId());
			file.setFileFrom("2");
			file.setCreateDept(entity.getUpDept());
			file.setUploadUser(user.getId().toString());
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			file.setUploadUserName(userNameDecrypt);
			file.setUploadTime(new Date());
		}
		filesService.saveBatch(filesList);

		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		if(entity.getStatus()==1){
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段计划";
			msgType = "24";
			appType = "10";
			receiveUser = info.getCreateUser().toString();//督办单位
		}else if(entity.getStatus()==4){
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段计划审核申请";
			msgType = "22";
			appType = "8";
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String[] userIdArr = vo.getUserId().split(",");
			String batchNumber = UUID.randomUUID().toString();
			if(StatusConstant.AUDIT_SYNC_1.equals(vo.getSync())){//并行
				receiveUser = vo.getUserId();
			}else{
				receiveUser = userIdArr[0];
			}
			for(int i=0;i<userIdArr.length;i++){
				String userId = userIdArr[i];
				SupervisionSubmitAudit audit = new SupervisionSubmitAudit();
				audit.setBatchNumber(batchNumber);
				audit.setServId(entity.getId());
				audit.setReportId(info.getId());
				audit.setTitle(vo.getTitle());
				audit.setUserId(Long.parseLong(userId));
				audit.setDeptName(deptName);
				audit.setCreateUser(user.getId());
				audit.setCreateTime(new Date());
				if(StatusConstant.AUDIT_SYNC_1.equals(vo.getSync())){
					audit.setStatus(0);
				}else{
					audit.setStatus(i==0?0:3);
				}
				audit.setSort(i);
				audit.setSync(Integer.parseInt(vo.getSync()));
				audit.setOperationType("plan");
				audit.setCreateUser(AuthUtil.getUserId());
				audit.setCreateTime(new Date());
				supervisionSubmitAuditService.save(audit);
			}
		}
		if(StringUtils.isNotEmpty(receiveUser)){
			sendMessage(user, info, content, msgType, appType, receiveUser);
		}
		return save;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAll(SupervisionUpPlanVO vo) {
		SupervisionUpPlan entity = Objects.requireNonNull(BeanUtil.copy(vo,SupervisionUpPlan.class));

		SupervisionInfo info = supervisionInfoService.getById(vo.getServId());
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(info.getServCode());
		fileDel.setPhaseId(entity.getId());
		fileDel.setFileFrom("2");
		fileDel.setCreateDept(entity.getUpDept());
		filesService.remove(Condition.getQueryWrapper(fileDel));

		List<SupervisionFiles> filesList = vo.getSupervisionFilesList();
		for (SupervisionFiles file : filesList) {
			file.setServCode(info.getServCode());
			file.setPhaseId(entity.getId());
			file.setFileFrom("2");
			file.setCreateDept(entity.getUpDept());
			file.setUploadUser(user.getId().toString());
			file.setUploadUserName(userNameDecrypt);
			file.setUploadTime(new Date());
		}
		filesService.saveBatch(filesList);

		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		if(entity.getStatus()==1){
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段计划";
			msgType = "24";
			appType = "10";
			receiveUser = info.getCreateUser().toString();//督办单位
		}else if(entity.getStatus()==2){
			content = "【"+userNameDecrypt+"】审核通过【"+info.getServName()+"】阶段计划";
			msgType = "26";
			appType = "39";
			String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位
			String dutyUnitUser = this.getUserIds(info.getDutyUnit());//责任单位
			receiveUser = leadUnitUser+dutyUnitUser;

		}else if(entity.getStatus()==3){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段计划";
			msgType = "25";
			appType = "11";
			receiveUser = entity.getCreateUser().toString();
		}else if(entity.getStatus()==4){
			content = "【"+userNameDecrypt+"】已提交【"+info.getServName()+"】阶段计划审核申请";
			msgType = "22";
			appType = "8";
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String[] userIdArr = vo.getUserId().split(",");
			String batchNumber = UUID.randomUUID().toString();
			if(StatusConstant.AUDIT_SYNC_1.equals(vo.getSync())){//并行
				receiveUser = vo.getUserId();
			}else{
				receiveUser = userIdArr[0];
			}
			for(int i=0;i<userIdArr.length;i++){
				String userId = userIdArr[i];
				SupervisionSubmitAudit audit = new SupervisionSubmitAudit();
				audit.setBatchNumber(batchNumber);
				audit.setServId(entity.getId());
				audit.setReportId(info.getId());
				audit.setTitle(vo.getTitle());
				audit.setUserId(Long.parseLong(userId));
				audit.setDeptName(deptName);
				audit.setCreateUser(user.getId());
				audit.setCreateTime(new Date());
				if(StatusConstant.AUDIT_SYNC_1.equals(vo.getSync())){
					audit.setStatus(0);
				}else{
					audit.setStatus(i==0?0:3);
				}
				audit.setSort(i);
				audit.setSync(Integer.parseInt(vo.getSync()));
				audit.setOperationType("plan");
				audit.setCreateUser(AuthUtil.getUserId());
				audit.setCreateTime(new Date());
				supervisionSubmitAuditService.save(audit);
			}
		}else if(entity.getStatus()==5){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段计划";
			msgType = "39";
			appType = "40";
			receiveUser = entity.getCreateUser().toString();
		}

		if(StringUtils.isNotEmpty(receiveUser)){
			sendMessage(user, info, content, msgType, appType, receiveUser);
		}
		return this.updateById(entity);
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

	private void sendMessage(User user, SupervisionInfo info, String content, String msgType, String appType, String receiveUser) {
		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段计划上报");
		message.setMsgType(msgType);
		message.setMsgStatus(0);
		message.setMsgPlatform("web");
		message.setMsgIntro(content);
		message.setCreateTime(new Date());
		message.setReceiveUser(receiveUser);
		messageService.sendMessageInfo(message);

		String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
		message.setMsgSubitem(value);
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);
	}
}
