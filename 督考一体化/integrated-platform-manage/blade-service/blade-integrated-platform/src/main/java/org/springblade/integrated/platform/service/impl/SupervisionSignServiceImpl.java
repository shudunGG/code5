package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionSignMapper;
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
import java.util.Map;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/16 18:02
 */
@Slf4j
@Service
public class SupervisionSignServiceImpl extends BaseServiceImpl<SupervisionSignMapper, SupervisionSign> implements ISupervisionSignService {

	@Autowired
	private ISupervisionPhaseReportService supervisionPhaseReportService;

	@Lazy
	@Autowired
	private ISupervisionPhasePlanService supervisionPhasePlanService;

	@Lazy
	@Autowired
	private ISupervisionInfoService supervisionInfoService;

	@Autowired
	private ISupervisionPhaseRemindService supervisionPhaseRemindService;

	@Resource
	private ISysClient sysClient;

	@Resource
	private IUserClient userClient;

	@Resource
	private IDictBizClient dictBizClient;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Override
	public R saveSignInfo(SupervisionInfo supervisionInfo){

		try {
			String leadUnit = supervisionInfo.getLeadUnit();
			String dutyUnit = supervisionInfo.getDutyUnit();

			List<User> magUsers=new ArrayList<>();
			//处理签收单位记录
			String[] leadUnits = leadUnit.split(",");
			for (String lead : leadUnits) {
				this.saveSignInfo(StatusConstant.DEPT_TYPE_LEAD,lead,supervisionInfo.getId()+"");
				//发遂消息  岗位:"管理员ID"1516056792837869570
				R<List<User>> userLeader= userClient.getUserLeader(lead, Constants.USER_POST_GLY_id);
				if(ObjectUtil.isNotEmpty(userLeader.getData())){
					magUsers.addAll(userLeader.getData());
				}
			}
			if (StringUtils.isNotEmpty(dutyUnit)) {
				String[] dutyUnits = dutyUnit.split(",");
				for (String duty : dutyUnits) {
					this.saveSignInfo(StatusConstant.DEPT_TYPE_DUTY,duty,supervisionInfo.getId()+"");
					//获取需要发送消息的人员
					R<List<User>> userLeader= userClient.getUserLeader(duty, Constants.USER_POST_GLY_id);
					if(ObjectUtil.isNotEmpty(userLeader.getData())){
						magUsers.addAll(userLeader.getData());
					}
				}
			}
			String userId="";
			for(int i=0;i<magUsers.size();i++){
				if(i==magUsers.size()-1){
					userId+=magUsers.get(i).getId().toString();
				}else{
					userId+=magUsers.get(i).getId().toString()+",";
				}
			}
			return R.status(true);
		} catch (Exception e){
			log.error(e.getMessage());
			return R.fail(e.getMessage());
		}
	}

	@Override
	public R saveSignInfo(String deptType, String deptId, String servId) {

		SupervisionSign supervisionSign = new SupervisionSign();
		supervisionSign.setDeptType(deptType);
		supervisionSign.setSignDept(Long.parseLong(deptId));
		supervisionSign.setSignStatus(0);
		supervisionSign.setOverDept(Long.parseLong(deptId));
		supervisionSign.setOverStatus(0);
		supervisionSign.setServId(Long.parseLong(servId));

		return R.status(this.save(supervisionSign));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveReportInfo(String deptType, Long deptId, String servId) {
		List<SupervisionPhasePlan> supervisionPhasePlans = supervisionPhasePlanService.listByservCode(supervisionInfoService.getById(Long.parseLong(servId)).getServCode());
		boolean flag = false;
		if("duty".equals(deptType)){
			for (SupervisionPhasePlan phasePlan : supervisionPhasePlans) {
				SupervisionPhaseReport report = new SupervisionPhaseReport();
				report.setServCode(phasePlan.getServCode());
				report.setPhaseId(phasePlan.getId());
				report.setPhaseName(phasePlan.getPhaseName());
				Dept dept = sysClient.getDept(deptId).getData();
				report.setReportDept(dept.getId().toString());
				report.setReportDeptName(dept.getDeptName());
				report.setDownStatus("0");
				SupervisionPhaseRemind phaseRemind = new SupervisionPhaseRemind();
				phaseRemind.setPhaseCode(phasePlan.getPhaseCode());
				SupervisionPhaseRemind one = supervisionPhaseRemindService.getOne(Condition.getQueryWrapper(phaseRemind));
				report.setRemindReportTime(one.getReportTime());

				String status = "0";
				if(one.getReportTime().before(new Date())){//汇报时间已超期
					status = "6";//超期未汇报
				}
				report.setReportStatus(status);
				flag = supervisionPhaseReportService.save(report);
			}
		}else if("lead".equals(deptType)){
			for (SupervisionPhasePlan phasePlan : supervisionPhasePlans) {
				SupervisionPhaseReport report = new SupervisionPhaseReport();
				report.setServCode(phasePlan.getServCode());
				report.setPhaseId(phasePlan.getId());
				report.setPhaseName(phasePlan.getPhaseName());
				Dept dept = sysClient.getDept(deptId).getData();
				report.setReportDept(dept.getId().toString());
				report.setReportDeptName(dept.getDeptName());
				report.setDownStatus("0");
				SupervisionPhaseRemind phaseRemind = new SupervisionPhaseRemind();
				phaseRemind.setPhaseCode(phasePlan.getPhaseCode());
				SupervisionPhaseRemind one = supervisionPhaseRemindService.getOne(Condition.getQueryWrapper(phaseRemind));
				report.setRemindReportTime(one.getReportTime());

				String status = "0";
				if(one.getReportTime().before(new Date())){//汇报时间已超期
					status = "6";//超期未汇报
				}
				report.setReportStatus(status);
				flag = supervisionPhaseReportService.save(report);
			}
		}
		return flag;
	}

	@Override
	public List<SupervisionSign> getOverdueNoSignList() {
		QueryWrapper<SupervisionSign> wrapper=new QueryWrapper<>();
		wrapper.eq("sign.sign_Status","0");
		wrapper.lt("date_format(wcsx, '%Y-%m-%d %H:%i:%s')", DateUtils.getTime());
		wrapper.ne("over_Status","1");
		List<SupervisionSign> signList = this.baseMapper.getOverdueNoSignList(wrapper);
		return signList;
	}
}
