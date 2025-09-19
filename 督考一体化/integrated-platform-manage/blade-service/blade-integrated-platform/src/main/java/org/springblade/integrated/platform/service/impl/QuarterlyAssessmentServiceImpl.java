package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.*;
import org.springblade.integrated.platform.mapper.QuarterlyAssessmentMapper;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务实现类
 *
 * @Author zrj
 * @Create 2022/4/9 17:30
 */
@Service
public class QuarterlyAssessmentServiceImpl extends BaseServiceImpl<QuarterlyAssessmentMapper, QuarterlyAssessment> implements IQuarterlyAssessmentService {

	@Autowired
	private IStageInformationService iStageInformationService;

	@Autowired
	private IReportTimeService reportTimeService;

	@Autowired
	private ReportsBaseinfoService reportsBaseinfoService;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Resource
	private ISysClient sysClient;
	@Autowired
	private IUserClient userClient;
	@Resource
	private IUserSearchClient iUserSearchClient;

	@Resource
	private IDictBizClient dictBizClient;
	@Autowired
	private  ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importQuarterlyAssessment(List<QuarterlyAssessmentExcel> data) {
		//租户ID
		String tenantId = "000000";
		List<QuarterlyAssessment> list = new ArrayList<>();
		AtomicInteger index  = new AtomicInteger();
		index.set(1);
		data.forEach(regionExcel -> {
			index.getAndIncrement();
			System.out.println("当前行："+index.get());
			QuarterlyAssessment region = BeanUtil.copy(regionExcel, QuarterlyAssessment.class);
			region.getId();
			if (StringUtils.isEmpty(region.getToQuarter())) {
				throw new RuntimeException("所属季度不能为空！");
			}
			if (StringUtils.isEmpty(region.getCheckClassifyName())) {
				throw new RuntimeException("考核分类不能为空！");
			}

			if (StringUtils.isEmpty(region.getCheckObject())) {
				throw new RuntimeException("考核对象不能为空！");
			}
			if (StringUtils.isEmpty(region.getAppraiseDeptname())) {
				throw new RuntimeException("评价单位不能为空！");
			}
			if (StringUtils.isEmpty(region.getWeight())) {
				throw new RuntimeException("权重不能为空！");
			}


			//TODO 通过评价部门名称去查找部门id
			if (region.getAppraiseDeptname().contains("，")) {
				region.setAppraiseDeptname(region.getAppraiseDeptname().replace("，",","));
			}
			if (region.getAppraiseDeptname().contains("、")) {
				region.setAppraiseDeptname(region.getAppraiseDeptname().replace("、",","));
			}
			if (region.getAppraiseDeptname().contains(",")) {
				String[] str = region.getAppraiseDeptname().replace(" ","").split(",");
				String[] strings = new String[str.length];
				for (int i = 0; i < str.length; i++) {
					System.out.println(">>>>>>>>>>>>>"+str[i]);
					String appraiseDeptid = SysCache.getDeptIds(tenantId, str[i]);
					strings[i] = appraiseDeptid;
					System.out.println(">>>>>>>>>>>>>"+strings[i]);
				}
				String deptid = Arrays.toString(strings).replace("[","").replace("]","");
				region.setAppraiseDeptid(deptid.replace(" ",""));
			} else {
				System.out.println(">>>>>>>>>>>>>"+region.getAppraiseDeptname());
				String appraiseDeptid = SysCache.getDeptIds(tenantId, region.getAppraiseDeptname().replace(" ",""));
				region.setAppraiseDeptid(appraiseDeptid.replace(" ",""));
				System.out.println(">>>>>>>>>>>>>"+appraiseDeptid.replace(" ",""));
			}
			if (region.getAppraiseDeptid().contains("null")) {
				throw new RuntimeException("评价部门id包含空值！");
			}

			//TODO 通过考核部门名称去查找考核部门id
			if (region.getCheckObject().contains("，")) {
				region.setCheckObject(region.getCheckObject().replace("，",","));
			}
			if (region.getCheckObject().contains("、")) {
				region.setCheckObject(region.getCheckObject().replace("、",","));
			}
			if (region.getCheckObject().contains(",")) {
				String[] str = region.getCheckObject().replace(" ","").split(",");
				String[] strings = new String[str.length];
				for (int i = 0; i < str.length; i++) {
					System.out.println(">>>>>>>>>>>>>"+str[i]);
					String checkObjectid = SysCache.getDeptIds(tenantId, str[i]);
					strings[i] = checkObjectid;
					System.out.println(">>>>>>>>>>>>>"+strings[i]);
				}
				String deptid = Arrays.toString(strings).replace("[","").replace("]","");
				region.setCheckObjectId(deptid.replace(" ",""));
			} else {
				System.out.println(">>>>>>>>>>>>>"+region.getCheckObject());
				String checkObjectid = SysCache.getDeptIds(tenantId, region.getCheckObject().replace(" ",""));
				region.setCheckObjectId(checkObjectid.replace(" ",""));
				System.out.println(">>>>>>>>>>>>>"+checkObjectid.replace(" ",""));
			}
			if (region.getCheckObjectId().contains("null")) {
				throw new RuntimeException("考核对象id包含空值！");
			}
			region.setNotAppriseUser(region.getAppraiseDeptname());



			switch (region.getCheckClassifyName()) {
				case "县区":
					region.setCheckClassify("1");
					break;
				case "市直综合部门":
					region.setCheckClassify("2");
					break;
				case "市直经济部门":
					region.setCheckClassify("3");
					break;
				case "市直社会发展部门":
					region.setCheckClassify("4");
					break;
				case "市直其他部门":
					region.setCheckClassify("5");
					break;
				case "市直学校科研院所":
					region.setCheckClassify("6");
					break;
				case "市属其他事业单位":
					region.setCheckClassify("7");
					break;
				case "市属国有企业":
					region.setCheckClassify("8");
					break;
				default:
					break;
			}
			list.add(region);
		});
		QueryWrapper<QuarterlyAssessment> queryWrapper = null;
		StringBuilder sb = new StringBuilder();
		for (int t = 0; t < list.size(); t++) {
			queryWrapper = new QueryWrapper<>();
			if (list.get(t).getJdzbName().contains("党建工作")) {
				queryWrapper.select(" * ");
				//考核分类
				queryWrapper.eq(list.get(t).getCheckClassifyName()!=null,"check_classify_name",list.get(t).getCheckClassifyName());
				//评价要点
				queryWrapper.eq(list.get(t).getMajorTarget()!=null,"major_target",list.get(t).getMajorTarget());
				//考核对象
				queryWrapper.eq(list.get(t).getCheckObject()!=null,"check_object",list.get(t).getCheckObject());
				//评价单位
				queryWrapper.eq(list.get(t).getAppraiseDeptname()!=null,"appraise_deptname",list.get(t).getAppraiseDeptname());
				//权重
				queryWrapper.eq(list.get(t).getWeight()!=null,"weight",list.get(t).getWeight());
				//完成时间
				queryWrapper.eq(list.get(t).getFinishDate()!=null,"finish_date",list.get(t).getFinishDate());
				//所属季度
				queryWrapper.eq(StringUtils.isNotEmpty(list.get(t).getToQuarter()),"to_quarter",list.get(t).getToQuarter());
				//未删除的
				queryWrapper.eq("is_deleted","0");
			}else if (list.get(t).getJdzbName().contains("工作实绩")) {

				queryWrapper.select(" * ");
				//考核分类
				queryWrapper.eq(list.get(t).getCheckClassifyName()!=null,"check_classify_name",list.get(t).getCheckClassifyName());
				//指标
				queryWrapper.eq(list.get(t).getTarget()!=null,"target",list.get(t).getTarget());
				//考核对象
				queryWrapper.eq(list.get(t).getCheckObject()!=null,"check_object",list.get(t).getCheckObject());
				//评价单位
				queryWrapper.eq(list.get(t).getAppraiseDeptname()!=null,"appraise_deptname",list.get(t).getAppraiseDeptname());
				//权重
				queryWrapper.eq(list.get(t).getWeight()!=null,"weight",list.get(t).getWeight());
				//完成时间
				queryWrapper.eq(list.get(t).getFinishDate()!=null,"finish_date",list.get(t).getFinishDate());
				//所属季度
				queryWrapper.eq(StringUtils.isNotEmpty(list.get(t).getToQuarter()),"to_quarter",list.get(t).getToQuarter());
				//未删除的
				queryWrapper.eq("is_deleted","0");
			}else if (list.get(t).getJdzbName().contains("党风廉政")) {
				queryWrapper.select(" * ");
				//重点工作
				queryWrapper.eq(list.get(t).getImportWork()!=null,"import_work",list.get(t).getImportWork());
				//评价要点
				queryWrapper.eq(list.get(t).getMajorTarget()!=null,"major_target",list.get(t).getMajorTarget());
				//考核分类
				queryWrapper.eq(list.get(t).getCheckClassifyName()!=null,"check_classify_name",list.get(t).getCheckClassifyName());
				//评分细则
				queryWrapper.eq(list.get(t).getScoringRubric()!=null,"scoring_rubric",list.get(t).getScoringRubric());
				//考核对象
				queryWrapper.eq(list.get(t).getCheckObject()!=null,"check_object",list.get(t).getCheckObject());
				//评价单位
				queryWrapper.eq(list.get(t).getAppraiseDeptname()!=null,"appraise_deptname",list.get(t).getAppraiseDeptname());
				//权重
				queryWrapper.eq(list.get(t).getWeight()!=null,"weight",list.get(t).getWeight());
				//完成时间
				queryWrapper.eq(list.get(t).getFinishDate()!=null,"finish_date",list.get(t).getFinishDate());
				//所属季度
				queryWrapper.eq(StringUtils.isNotEmpty(list.get(t).getToQuarter()),"to_quarter",list.get(t).getToQuarter());
				//未删除的
				queryWrapper.eq("is_deleted","0");
			}
			QuarterlyAssessment quarterlyAssessment = list.get(t);
			this.save(quarterlyAssessment);


		}
		System.out.println(sb.toString());

	}


	@Transactional(rollbackFor = Exception.class)
	public boolean saveAssessment(QuarterlyAssessment qe){
		qe.setCheckStatus("0");//年度考评状态

		qe.setHandleStatus("0");//办理状态：0正常1催办
		//添加未评价部门
		qe.setNotAppriseUser(qe.getAppraiseDeptname());
		boolean flag=this.save(qe);
		if(flag){
			//保存阶段信息
			List<StageInformation> stageInformationList = qe.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
				/*	sif.setAssessmentType("2");//评价指标分类：1年度考核 2季度考核
					sif.setAssessmentId(qe.getId());//季度评价主键id*/
				}
				flag=iStageInformationService.saveList(stageInformationList);
			}
		}


		return flag;
	}


	@Transactional(rollbackFor = Exception.class)
	public boolean uptAssessment(QuarterlyAssessment qe) {
		if(qe.getOperateType().equals("4")){//下发操作，状态变成推进中
			qe.setTargetStatus("1");//指标状态：0暂存 1推进中 2已完成
		}
		//更新季度评价指标基本信息
		boolean flag=this.updateById(qe);
		return flag;
	}



/*	@Override
	public List<QuarterlyAssessmentExcel1> exportQuarterlyAssessment1(QuarterlyAssessment queryWrapper) {
		List<QuarterlyAssessmentExcel1> quarterlyAssessmentExcels = baseMapper.exportQuarterlyAssessment1(queryWrapper);
		return quarterlyAssessmentExcels;
	}

	@Override
	public List<QuarterlyAssessmentExcel2> exportQuarterlyAssessment2(QuarterlyAssessment queryWrapper) {
		List<QuarterlyAssessmentExcel2> quarterlyAssessmentExcels = baseMapper.exportQuarterlyAssessment2(queryWrapper);
		return quarterlyAssessmentExcels;
	}

	@Override
	public List<QuarterlyAssessmentExcel3> exportQuarterlyAssessment3(QuarterlyAssessment queryWrapper) {
		List<QuarterlyAssessmentExcel3> quarterlyAssessmentExcels = baseMapper.exportQuarterlyAssessment3(queryWrapper);
		return quarterlyAssessmentExcels;
	}*/



	@Override
	public QuarterlyAssessment details(Long id) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String deptNamestr = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		QuarterlyAssessment qe=this.getById(id);
		if(qe!=null){
			if (qe.getNotAppriseUser() != null) {
				if (qe.getAppraiseDeptname().contains(deptNamestr) && !qe.getNotAppriseUser().contains(deptNamestr)) {
					qe.setIsAppraise(1);
				}
			}
			//获取阶段信息
			QueryWrapper<StageInformation> wrapperStage=new QueryWrapper<>();
			wrapperStage.eq("evaluation_Id",qe.getId());
			List<StageInformation> stageInformationList = iStageInformationService.list(wrapperStage);
			if(!stageInformationList.isEmpty()){
				//获取阶段汇报提醒信息
				for (StageInformation sif:stageInformationList){
					//判断是否是当前阶段
					String iscurrent="N";
					Date startDate = sif.getStartDate();
					Date endDate = sif.getEndDate();
					long startTime = startDate.getTime();
					long endTime = endDate.getTime();
					long nowTime = System.currentTimeMillis();//new Date().getTime()
					if(nowTime>=startTime && startTime<=endTime){
						iscurrent="Y";
					}
					sif.setIscurrent(iscurrent);
					QueryWrapper<ReportTime> wrapper=new QueryWrapper<>();
					wrapper.eq("stage_Information_Id",sif.getId());
					List<ReportTime> reportTimeList = reportTimeService.list(wrapper);
					if(!reportTimeList.isEmpty()){
						sif.setReportTimeList(reportTimeList);
					}
				}
				qe.setStageInformationList(stageInformationList);
			}
		}
		return qe;
	}

	/**
	 * 修改阶段
	 *
	 * @param qe
	 * @return
	 */
	@Override
	public boolean uptStage(QuarterlyAssessment qe) {
		return false;
	}


/*	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptStage(QuarterlyAssessment qe) {
			boolean flag=false;
			List idsList = new ArrayList();
			//获取当前阶段信息
			List<StageInformation> stageInformationList = qe.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setAssessmentId(qe.getId());
					idsList.add(sif.getId());
				}
			}
			//根据季度评价指标id获取老的阶段信息
			Map stageMap = new HashMap<>();
			stageMap.put("evaluation_id",qe.getId());
			List<StageInformation> oldStageInfoList = iStageInformationService.listByMap(stageMap);
			if(ObjectUtil.isNotEmpty(oldStageInfoList)){
				for(StageInformation oldSif:oldStageInfoList){
					if(!idsList.contains(oldSif.getId())){//不包含就删除
						iStageInformationService.removeById(oldSif.getId());
					}
				}
				//批量更新阶段信息
				flag=iStageInformationService.uptList(stageInformationList);
			}
			return flag;
	}*/
}
