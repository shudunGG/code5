package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vingsoft.entity.*;
import org.checkerframework.checker.units.qual.A;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.mapper.SupervisionPhasePlanMapper;
import org.springblade.integrated.platform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionPhasePlanServiceImpl extends BaseServiceImpl<SupervisionPhasePlanMapper, SupervisionPhasePlan> implements ISupervisionPhasePlanService {

	@Autowired
	private ISupervisionPhaseRemindService supervisionPhaseRemindService;

	@Autowired
	private ISupervisionFilesService supervisionFilesService;

	@Lazy
	@Autowired
	private ISupervisionSignService supervisionSignService;

	@Autowired
	private ISupervisionSubmitAuditService  supervisionSubmitAuditService;

	@Lazy
	@Autowired
	private ISupervisionPhaseReportService supervisionPhaseReportService;

	@Lazy
	@Autowired
	private ISupervisionPhaseReportAltService iSupervisionPhaseReportAltService;

	@Override
	public List<SupervisionPhasePlan> queryList(Map<String, Object> entity) {
		QueryWrapper<SupervisionPhasePlan> wrapperPhase= new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("servCode"))){
			wrapperPhase.eq("phase.serv_code",entity.get("servCode"));
		}
		String deptId="";
		if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
			deptId= (String) entity.get("deptId");
		}
		boolean a = true;
		List<SupervisionPhasePlan> supervisionPhasePlanList = this.baseMapper.queryList(wrapperPhase,deptId);
		for (SupervisionPhasePlan plan:supervisionPhasePlanList) {
			QueryWrapper<SupervisionPhaseReport> wrapper=new QueryWrapper<>();
			wrapper.eq("phase_id",plan.getId());
			wrapper.eq("report_Dept",deptId);
			SupervisionPhaseReport report = supervisionPhaseReportService.getOne(wrapper);
			if(ObjectUtil.isNotEmpty(report)){
				plan.setSupervisionPhaseReport(report);
			}

			QueryWrapper<SupervisionPhaseReportAlt> wrapperAlt=new QueryWrapper<>();
			wrapperAlt.eq("phase_id",plan.getId());
			wrapperAlt.eq("issue_dept",deptId);
			List<SupervisionPhaseReportAlt> listAlt = iSupervisionPhaseReportAltService.list(wrapperAlt);
			plan.setHuizong(listAlt!=null&&listAlt.size()>0?1:0);
		}
		if (a != false) {
			for (SupervisionPhasePlan plan1:supervisionPhasePlanList) {
				Date now = new Date();
				if (now.compareTo(plan1.getEndTime())>0) {
					plan1.setIsEnable("1");
				}
			}
		}
		return supervisionPhasePlanList;
	}

	@Override
	public List<SupervisionPhasePlan> queryListHB(Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionPhasePlan> wrapperPhase= new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("servCode"))){
			wrapperPhase.eq("phase.serv_code",entity.get("servCode"));
		}
		if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
			wrapperPhase.eq("report.report_dept",entity.get("deptId"));
		}
		boolean b = true;
		List<SupervisionPhasePlan> supervisionPhasePlanList = this.baseMapper.queryListHB(wrapperPhase);
		for (SupervisionPhasePlan plan:supervisionPhasePlanList) {
			if ("1".equals(plan.getIsEnable())){
				b = false;
				break;
			}
		}
		if (b != false) {
			if(supervisionPhasePlanList.size() > 1){
				SupervisionPhasePlan c = supervisionPhasePlanList.get(supervisionPhasePlanList.size() - 1);
				c.setIsEnable("1");
			}
		}

		for(SupervisionPhasePlan plan: supervisionPhasePlanList){
			List<SupervisionPhaseReport> supervisionPhaseReportList = plan.getSupervisionPhaseReportList();
			for(SupervisionPhaseReport report:supervisionPhaseReportList){
				//获取汇报材料
				QueryWrapper<SupervisionFiles> wrapper=new QueryWrapper<>();
				wrapper.eq("file_From","5");
				wrapper.eq("phase_id",report.getPhaseId());
				if(ObjectUtil.isNotEmpty(report.getReportDept()))
					wrapper.eq("create_dept",report.getReportDept());
				List<SupervisionFiles> supervisionFiles = supervisionFilesService.list(wrapper);
				report.setSupervisionFilesList(supervisionFiles);

				//获取送审信息
				QueryWrapper<SupervisionSubmitAudit> wrapperA=new QueryWrapper<>();
				wrapperA.eq("serv_id",report.getId());
				wrapperA.eq("status",0);
				SupervisionSubmitAudit audit = supervisionSubmitAuditService.getOne(wrapperA);
				report.setSupervisionSubmitAudit(audit);


			}
		}

		return supervisionPhasePlanList;
	}

	@Override
	public List<SupervisionPhasePlan> queryListHBAll(Map<String, Object> entity, BladeUser user) {
		QueryWrapper<SupervisionPhasePlan> wrapperPhase= new QueryWrapper<>();
		if(ObjectUtil.isNotEmpty(entity.get("servCode"))){
			wrapperPhase.eq("phase.serv_code",entity.get("servCode"));
		}
		String detpId="";
		if(ObjectUtil.isNotEmpty(entity.get("deptId"))){
			detpId= (String) entity.get("deptId");
		}

		List<SupervisionPhasePlan> supervisionPhasePlanList = this.baseMapper.queryListHBAll(wrapperPhase,detpId);
		for(SupervisionPhasePlan plan: supervisionPhasePlanList){
			List<SupervisionPhaseReport> supervisionPhaseReportList = plan.getSupervisionPhaseReportList();
			for(SupervisionPhaseReport report:supervisionPhaseReportList){
				//获取汇报材料
				QueryWrapper<SupervisionFiles> wrapper=new QueryWrapper<>();
				wrapper.eq("file_From","6");
				wrapper.eq("phase_id",report.getPhaseId());
				if(ObjectUtil.isNotEmpty(report.getReportDept()))
					wrapper.eq("create_dept",report.getReportDept());
				List<SupervisionFiles> supervisionFiles = supervisionFilesService.list(wrapper);
				report.setSupervisionFilesList(supervisionFiles);

			}
		}

		return supervisionPhasePlanList;
	}

	@Override
	public boolean saveList(List<SupervisionPhasePlan> supervisionPhasePlanList) {

		boolean flag=this.saveBatch(supervisionPhasePlanList);

		if(flag){
			for(SupervisionPhasePlan plan:supervisionPhasePlanList){
				//保存事项汇报阶段
				List<SupervisionPhaseRemind> supervisionPhaseRemindList = plan.getSupervisionPhaseRemindList();
				if(ObjectUtil.isNotEmpty(supervisionPhaseRemindList)){
					supervisionPhaseRemindList.stream().forEach(item->{
						item.setServCode(plan.getServCode());
						item.setPhaseId(plan.getId());
					});
					flag=supervisionPhaseRemindService.saveOrUpdateBatch(supervisionPhaseRemindList);
				}
			}
		}
		return flag;
	}

	@Override
	public List<SupervisionPhasePlan> listByservCode(String servCode) {
		QueryWrapper<SupervisionPhasePlan> wrapper=new QueryWrapper<>();
		wrapper.eq("serv_code",servCode);
		List<SupervisionPhasePlan> list = this.list(wrapper);
		return list;
	}

	@Override
	public boolean updateList(List<SupervisionPhasePlan> supervisionPhasePlanList,String servCode,SupervisionInfo supervisionInfo) {
		QueryWrapper<SupervisionPhasePlan> wrapperPlan = new QueryWrapper<>();
		wrapperPlan.eq("serv_code", servCode);
		List<SupervisionPhasePlan> supervisionPhasePlanListOld = this.list(wrapperPlan);
		Set delIds = new HashSet();
		for (SupervisionPhasePlan planOld : supervisionPhasePlanListOld) {
			Long idOld = planOld.getId();
			boolean isdel = false;
			for (SupervisionPhasePlan plan : supervisionPhasePlanList) {
				Long id = plan.getId();
				if (idOld == id) {
					isdel = false;
					break;
				} else {
					isdel = true;
				}
				if (ObjectUtil.isEmpty(id)) {
					plan.setServCode(servCode);
				}
			}
			if (isdel) {
				delIds.add(idOld);
			}
		}
		for (SupervisionPhasePlan plan : supervisionPhasePlanList) {
			plan.setServCode(servCode);
		}
		//根据传入的阶段数据匹配需要删除的阶段数据信息
		boolean flag = false;
		if (!delIds.isEmpty()) {
			flag = this.removeByIds(delIds);
		}
		flag = this.saveOrUpdateBatch(supervisionPhasePlanList);
		QueryWrapper<SupervisionSign> ss = new QueryWrapper();
		ss.eq("serv_id", supervisionInfo.getId());
		ss.eq("dept_type", "duty");
		List<SupervisionSign> supervisionSignList = supervisionSignService.list(ss);
		List<SupervisionSign> dutySignList = null;
		List<SupervisionSign> leadSignList = null;
		boolean dept_flag = false;
		if (ObjectUtil.isEmpty(supervisionSignList)) {
			dept_flag = true;
			QueryWrapper<SupervisionSign> sg = new QueryWrapper();
			sg.eq("serv_id", supervisionInfo.getId());
			sg.eq("dept_type", "lead");
			sg.eq("sign_status", 1);
			leadSignList = supervisionSignService.list(sg);
		} else {
			ss.eq("sign_status", 1);
			dutySignList = supervisionSignService.list(ss);
		}
		if (flag) {
			//更新阶段汇报提醒
			for (SupervisionPhasePlan plan : supervisionPhasePlanList) {
				//保存事项汇报阶段
				List<SupervisionPhaseRemind> supervisionPhaseRemindList = plan.getSupervisionPhaseRemindList();
				if (ObjectUtil.isNotEmpty(supervisionPhaseRemindList)) {
					flag = supervisionPhaseRemindService.updateList(supervisionPhaseRemindList, servCode, plan.getPhaseCode());
					if (ObjectUtil.isNotEmpty(leadSignList) || ObjectUtil.isNotEmpty(dutySignList)) {
						QueryWrapper<SupervisionPhaseReport> rr = new QueryWrapper();
						rr.eq("serv_code", servCode);
						List<SupervisionPhaseReport> supervisionPhaseReport = supervisionPhaseReportService.list(rr);
						boolean flag1 = false;
						for (SupervisionPhaseReport report1 : supervisionPhaseReport) {
							if (report1.getPhaseId().equals(plan.getId())) {
								flag1 = true;
								break;
							}
						}
						if (flag1) {
							if (dept_flag) {
								UpdateWrapper<SupervisionPhaseReport> ur = new UpdateWrapper();
								ur.eq("phase_id", plan.getId());
								SupervisionPhaseReport report1 = new SupervisionPhaseReport();
								report1.setRemindReportTime(supervisionPhaseRemindList.get(0).getReportTime());
								flag = supervisionPhaseReportService.update(report1, ur);
							} else {
								UpdateWrapper<SupervisionPhaseReport> ur = new UpdateWrapper();
								ur.eq("phase_id", plan.getId());
								SupervisionPhaseReport report1 = new SupervisionPhaseReport();
								report1.setRemindReportTime(supervisionPhaseRemindList.get(0).getReportTime());
								flag = supervisionPhaseReportService.update(report1, ur);
								/*UpdateWrapper<SupervisionPhaseReportAll> ur1 = new UpdateWrapper();
								ur1.eq("phase_id", plan.getId());
								SupervisionPhaseReportAll report2 = new SupervisionPhaseReportAll();
								report1.setRemindReportTime(supervisionPhaseRemindList.get(0).getReportTime());
								flag = supervisionPhaseReportAllService.update(report2, ur1);*/
							}
						} else {
							if (dept_flag) {
								for (SupervisionSign lead : leadSignList) {
									if (ObjectUtil.isNotEmpty(lead)) {
										QueryWrapper<SupervisionPhaseReport> pr = new QueryWrapper();
										pr.eq("report_dept", lead.getSignDept());
										pr.eq("serv_code", plan.getServCode());
										pr.last("limit 1");
										SupervisionPhaseReport report = supervisionPhaseReportService.getOne(pr);
										if (ObjectUtil.isNotEmpty(report)) {
											SupervisionPhaseReport report1 = new SupervisionPhaseReport();
											report1.setPhaseId(plan.getId());
											report1.setServCode(plan.getServCode());
											report1.setPhaseName(plan.getPhaseName());
											report1.setReportDept(String.valueOf(lead.getSignDept()));
											report1.setReportDeptName(report.getReportDeptName());
											report1.setRemindReportTime(supervisionPhaseRemindList.get(0).getReportTime());
											report1.setCreateUser(report.getCreateUser());
											report1.setCreateDept(report.getCreateDept());
											flag = supervisionPhaseReportService.save(report1);
										}
									}
								}
							} else {
								for (SupervisionSign duty : dutySignList) {
									if (ObjectUtil.isNotEmpty(duty)) {
										QueryWrapper<SupervisionPhaseReport> pr = new QueryWrapper();
										pr.eq("report_dept", duty.getSignDept());
										pr.eq("serv_code", plan.getServCode());
										pr.last("limit 1");
										SupervisionPhaseReport report = supervisionPhaseReportService.getOne(pr);
										if (ObjectUtil.isNotEmpty(report)) {
											SupervisionPhaseReport report1 = new SupervisionPhaseReport();
											report1.setPhaseId(plan.getId());
											report1.setServCode(plan.getServCode());
											report1.setPhaseName(plan.getPhaseName());
											report1.setReportDept(String.valueOf(duty.getSignDept()));
											report1.setReportDeptName(report.getReportDeptName());
											report1.setRemindReportTime(supervisionPhaseRemindList.get(0).getReportTime());
											report1.setCreateUser(report.getCreateUser());
											report1.setCreateDept(report.getCreateDept());
											flag = supervisionPhaseReportService.save(report1);

										}
									}
								}
							}
						}
					}
				}
			}
		}

		return flag;
	}
}
