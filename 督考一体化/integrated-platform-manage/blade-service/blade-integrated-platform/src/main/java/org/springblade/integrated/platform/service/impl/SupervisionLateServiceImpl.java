package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionLateVO;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.mapper.SupervisionLateMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.entity.Dept;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/20 23:46
* @Version:        1.0
*/
@Service
@AllArgsConstructor
public class SupervisionLateServiceImpl extends BaseServiceImpl<SupervisionLateMapper, SupervisionLate> implements ISupervisionLateService {

	private final IUserClient userClient;

	private final ISysClient sysClient;

	private final IUnifyMessageService messageService;

	private final ISupervisionFilesService filesService;

	private final ISupervisionInfoService supervisionInfoService;

	private final ISupervisionPhasePlanService supervisionPhasePlanService;

	private final ISupervisionPhaseRemindService supervisionPhaseRemindService;

	private final ISupervisionPhaseReportService supervisionPhaseReportService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveOrUpdate(SupervisionLateVO vo) {
		SupervisionLate entity = Objects.requireNonNull(BeanUtil.copy(vo, SupervisionLate.class));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		Dept dept = sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData();
		SupervisionInfo info = supervisionInfoService.getById(entity.getServId());
		boolean flag = true;

		boolean isAudit = AuthUtil.getUserId().equals(entity.getCreateUser());//是否当前人员
		if (entity.getId()==null) {//新增
			entity.setLateDate(new Date());//申请时间
			entity.setLateDeptName(dept.getDeptName());
			entity.setLateDeptId(dept.getId());
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			entity.setLateUserName(userNameDecrypt);
			entity.setLateUserId(user.getId());
			entity.setReceiveName(userClient.userInfoById(info.getCreateUser()).getData().getRealName());
			entity.setReceiveId(info.getCreateUser());
			flag = this.save(entity);
			isAudit = true;
		}else{//修改
			if(isAudit){
				SupervisionFiles fileDel = new SupervisionFiles();
				fileDel.setServCode(entity.getServCode());
				fileDel.setPhaseId(entity.getId());
				fileDel.setFileFrom("延期");
				fileDel.setCreateDept(entity.getLateDeptId());
				filesService.remove(Condition.getQueryWrapper(fileDel));
			}
			flag = this.updateById(entity);
		}


		if (isAudit) {
			List<SupervisionFiles> filesList = vo.getFilesList();//申请附件
			if (filesList!=null&&filesList.size()>0) {
				for (SupervisionFiles file : filesList) {
					file.setServCode(entity.getServCode());
					file.setPhaseId(entity.getId());
					file.setFileFrom("延期");
					file.setUploadUser(user.getId().toString());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					file.setUploadUserName(userNameDecrypt);
					file.setUploadTime(new Date());
					filesService.save(file);
				}
			}
		}

		if (!(3==entity.getLateStatus()&&isAudit)) {//审核不通过并且是当前人，说明是修改操作
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			String content = "【"+userNameDecrypt+"】申请了【"+entity.getServName()+"】延期";
			String msgType = "65";
			String appMsgType = "65";
			String receiveUser = entity.getReceiveId().toString();

			if(2==entity.getLateStatus()){
				content = "【"+userNameDecrypt+"】审核通过了【"+entity.getServName()+"】延期申请";
				msgType = "66";
				appMsgType = "66";
				receiveUser = entity.getLateUserId().toString();

				long day = entity.getLateDay()*24*60*60*1000;//延期时限

				info.setWcsx(new Date(info.getWcsx().getTime()+day));

				SupervisionPhasePlan plan = new SupervisionPhasePlan();
				plan.setServCode(info.getServCode());
				plan = supervisionPhasePlanService.getOne(Condition.getQueryWrapper(plan));//事项阶段

				SupervisionPhaseRemind remind = new SupervisionPhaseRemind();
				remind.setServCode(info.getServCode());
				remind = supervisionPhaseRemindService.getOne(Condition.getQueryWrapper(remind));//事项阶段提醒

				SupervisionPhaseReport report = new SupervisionPhaseReport();//阶段汇报
				report.setPhaseId(plan.getId());
				List<SupervisionPhaseReport> list = supervisionPhaseReportService.list(Condition.getQueryWrapper(report));

				//TODO 处理申请延期后的时间
				plan.setEndTime(new Date(plan.getEndTime().getTime()+day));
				remind.setReportTime(new Date(remind.getReportTime().getTime()+day));
				for (SupervisionPhaseReport sr : list) {
					if(Objects.isNull(sr.getParentId())){
						sr.setRemindReportTime(new Date(sr.getRemindReportTime().getTime()+day));
						if(Integer.parseInt(sr.getReportStatus())>5&&sr.getRemindReportTime().after(new Date())){//延期后，计划汇报时间未超期
							sr.setReportStatus(String.valueOf(Integer.parseInt(sr.getReportStatus())-6));
						}
						supervisionPhaseReportService.updateById(sr);
					}
				}
				supervisionInfoService.updateById(info);
				supervisionPhasePlanService.updateById(plan);
				supervisionPhaseRemindService.updateById(remind);

			}else if(3==entity.getLateStatus()){
				content = "【"+userNameDecrypt+"】审核不通过【"+entity.getServName()+"】延期申请";
				msgType = "67";
				appMsgType = "67";
				receiveUser = entity.getLateUserId().toString();
			}

			UnifyMessage message = new UnifyMessage();
			message.setMsgId(entity.getServId());
			message.setMsgTitle("事项申请延期");
			message.setMsgType(msgType);
			message.setMsgStatus(0);
			message.setMsgPlatform("web");
			message.setMsgIntro(content);
			message.setCreateTime(new Date());
			message.setReceiveUser(receiveUser);
			messageService.sendMessageInfo(message);

			message.setId(null);
			message.setMsgPlatform("app");
			message.setMsgType(Constants.MSG_TYPE_APP_ONE_DB);
			message.setMsgSubitem("事情申请延期");
			message.setTwoLevelType(appMsgType);
			messageService.sendMessageInfo(message);
		}
		return flag;
	}
}
