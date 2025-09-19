package org.springblade.integrated.platform.service.impl;/**
 * @author TangYanXing
 * @date 2022-04-09 14:17
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.AnnualAssessment;
import com.vingsoft.entity.ReportTime;
import com.vingsoft.entity.StageInformation;
import com.vingsoft.entity.UnifyMessage;
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
import org.springblade.integrated.platform.mapper.AnnualAssessmentMapper;
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
 * @author zrj
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:17
 */
@Service
public class AnnualAssessmentServiceImpl extends BaseServiceImpl<AnnualAssessmentMapper,AnnualAssessment> implements IAnnualAssessmentService {

	@Autowired
	private IStageInformationService stageInformationService;

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
	public boolean saveAnnualAssessment(AnnualAssessment ae){

		//添加未评价部门
		ae.setNotAppriseUser(ae.getAppraiseDeptname());
		boolean flag=this.save(ae);


		return flag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptAnnualAssessment(AnnualAssessment ae) {

		//更新年度评价指标基本信息
		boolean flag=this.updateById(ae);
		return flag;
	}

	@Override
	public List<ZzsxjsExcel> exportZzsxjsAssessment(AnnualAssessment ae) {
		List<ZzsxjsExcel> aeList = baseMapper.exportZzsxjsAssessment(ae);
		return aeList;
	}

	@Override
	public List<LdnlExcel> exportLdnlAssessment(AnnualAssessment ae) {
		List<LdnlExcel> aeList = baseMapper.exportLdnlAssessment(ae);
		return aeList;
	}

	@Override
	public List<DdjsExcel> exportDdjsAssessment(AnnualAssessment ae) {
		List<DdjsExcel> aeList = baseMapper.exportDdjsAssessment(ae);
		return aeList;
	}

	@Override
	public List<SgzlfzExcel> exportSgzlfzAssessment(AnnualAssessment ae) {
		List<SgzlfzExcel> aeList = baseMapper.exportSgzlfzAssessment(ae);
		return aeList;
	}

	@Override
	public List<QxgzlfzExcel> exportQxgzlfzAssessment(AnnualAssessment ae) {
		List<QxgzlfzExcel> aeList = baseMapper.exportQxgzlfzAssessment(ae);
		return aeList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importZzsxjsAssessment(List<AnnualAssessmentExcel> data) {
		//租户ID
		String tenantId = "000000";
		List<AnnualAssessment> list = new ArrayList<>();
		AtomicInteger index  = new AtomicInteger();
		index.set(1);
		data.forEach(regionExcel -> {
			index.getAndIncrement();
			System.out.println("当前行："+index.get());
			AnnualAssessment region = BeanUtil.copy(regionExcel, AnnualAssessment.class);
			region.getId();
			if (StringUtils.isEmpty(region.getAppraiseClassifyName())) {
				throw new RuntimeException("考核分类不能为空！");
			}
			if (StringUtils.isEmpty(region.getMajorTarget())) {
				throw new RuntimeException("评价要点不能为空！");
			}
			if (StringUtils.isEmpty(region.getAppraiseObject())) {
				throw new RuntimeException("考核对象不能为空！");
			}
			if (StringUtils.isEmpty(region.getAppraiseDeptname())) {
				throw new RuntimeException("评价单位不能为空！");
			}
			if (!"5".equals(region.getType())) {
				if (StringUtils.isEmpty(region.getWeight())) {
					throw new RuntimeException("权重不能为空！");
				}
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
			if (region.getAppraiseObject().contains("，")) {
				region.setAppraiseObject(region.getAppraiseObject().replace("，",","));
			}
			if (region.getAppraiseObject().contains("、")) {
				region.setAppraiseObject(region.getAppraiseObject().replace("、",","));
			}
			if (region.getAppraiseObject().contains(",")) {
				String[] str = region.getAppraiseObject().replace(" ","").split(",");
				String[] strings = new String[str.length];
				for (int i = 0; i < str.length; i++) {
					System.out.println(">>>>>>>>>>>>>"+str[i]);
					String appraiseObjectid = SysCache.getDeptIds(tenantId, str[i]);
					strings[i] = appraiseObjectid;
					System.out.println(">>>>>>>>>>>>>"+strings[i]);
				}
				String deptid = Arrays.toString(strings).replace("[","").replace("]","");
				region.setAppraiseObjectId(deptid.replace(" ",""));
			} else {
				System.out.println(">>>>>>>>>>>>>"+region.getAppraiseObject());
				String appraiseObjectid = SysCache.getDeptIds(tenantId, region.getAppraiseObject().replace(" ",""));
				region.setAppraiseObjectId(appraiseObjectid.replace(" ",""));
				System.out.println(">>>>>>>>>>>>>"+appraiseObjectid.replace(" ",""));

			}
			if (region.getAppraiseObjectId().contains("null")) {
				throw new RuntimeException("考核对象id包含空值！");
			}
			region.setNotAppriseUser(region.getAppraiseDeptname());

			switch (region.getAppraiseClassifyName()) {
				case "县区":
					region.setAppraiseClassify("1");
					break;
				case "市直综合部门":
					region.setAppraiseClassify("2");
					break;
				case "市直经济部门":
					region.setAppraiseClassify("3");
					break;
				case "市直社会发展部门":
					region.setAppraiseClassify("4");
					break;
				case "市直其他部门":
					region.setAppraiseClassify("5");
					break;
				case "市直学校科研院所":
					region.setAppraiseClassify("6");
					break;
				case "市属其他事业单位":
					region.setAppraiseClassify("7");
					break;
				case "市属国有企业":
					region.setAppraiseClassify("8");
					break;
				default:
					break;
			}

			list.add(region);
		});
		QueryWrapper<AnnualAssessment> queryWrapper = null;
		StringBuilder sb = new StringBuilder();
		for (int t = 0; t < list.size(); t++) {
			queryWrapper = new QueryWrapper<>();
			if (list.get(t).getType().equals("1") || list.get(t).getType().equals("2") || list.get(t).getType().equals("3") || list.get(t).getType().equals("4")) {
				queryWrapper.select(" * ");
				//项目
				queryWrapper.eq(list.get(t).getProjectName()!=null,"project_name",list.get(t).getProjectName());
				//考核分类
				queryWrapper.eq(list.get(t).getAppraiseClassifyName()!=null,"appraise_classify_name",list.get(t).getAppraiseClassify());
				//主要指标及评价要点
				queryWrapper.eq(list.get(t).getMajorTarget()!=null,"major_target",list.get(t).getMajorTarget());
				//考核对象
				queryWrapper.eq(list.get(t).getAppraiseObject()!=null,"appraise_object",list.get(t).getAppraiseObject());
				//评价单位
				queryWrapper.eq(list.get(t).getAppraiseDeptname()!=null,"appraise_deptname",list.get(t).getAppraiseDeptname());
				//权重
				queryWrapper.eq(list.get(t).getWeight()!=null,"weight",list.get(t).getWeight());
				//未删除的
				queryWrapper.eq("is_deleted","0");
			}else if (list.get(t).getType().equals("5")) {
				queryWrapper.select(" * ");
				//考核分类
				queryWrapper.eq(list.get(t).getAppraiseClassifyName()!=null,"appraise_classify_name",list.get(t).getAppraiseClassify());
				//项目
				queryWrapper.eq(list.get(t).getProjectName()!=null,"project_name",list.get(t).getProjectName());
				//主要指标及评价要点
				queryWrapper.eq(list.get(t).getMajorTarget()!=null,"major_target",list.get(t).getMajorTarget());
				//考核对象
				queryWrapper.eq(list.get(t).getAppraiseObject()!=null,"appraise_object",list.get(t).getAppraiseObject());
				//评价单位
				queryWrapper.eq(list.get(t).getAppraiseDeptname()!=null,"appraise_deptname",list.get(t).getAppraiseDeptname());
				//甘州区
				queryWrapper.eq(list.get(t).getGanzhouqu()!=null,"ganzhouqu",list.get(t).getGanzhouqu());
				//临泽县
				queryWrapper.eq(list.get(t).getLinzexian()!=null,"linzexian",list.get(t).getLinzexian());
				//高台县
				queryWrapper.eq(list.get(t).getGaotaixian()!=null,"gaotaixian",list.get(t).getGaotaixian());
				//山丹县
				queryWrapper.eq(list.get(t).getShandanxian()!=null,"shandanxian",list.get(t).getShandanxian());
				//民乐县
				queryWrapper.eq(list.get(t).getMinlexian()!=null,"minlexian",list.get(t).getMinlexian());
				//肃南县
				queryWrapper.eq(list.get(t).getSunanxian()!=null,"sunanxian",list.get(t).getSunanxian());
				//未删除的
				queryWrapper.eq("is_deleted","0");
			}
			List<AnnualAssessment> mapList = baseMapper.selectList(queryWrapper);
			AnnualAssessment annualAssessment = null;
			if (mapList.size() < 1) {
				annualAssessment = list.get(t);
				this.save(annualAssessment);
				//如果QuarterlyAssessment表中有数据，就去阶段表里查询导入的阶段信息是否存在
				QueryWrapper<StageInformation> queryWrapper1 = new QueryWrapper<>();
				queryWrapper1.eq(annualAssessment.getId()!=null,"assessment_id",annualAssessment.getId());
				queryWrapper1.eq(data.get(t).getStage()!=null,"stage",data.get(t).getStage());
				queryWrapper1.eq(data.get(t).getStageRequirement()!=null,"stage_requirement",data.get(t).getStageRequirement());
				queryWrapper1.eq(data.get(t).getStartDate()!=null,"start_date",data.get(t).getStartDate());
				queryWrapper1.eq(data.get(t).getEndDate()!=null,"end_date",data.get(t).getEndDate());
				queryWrapper1.eq("assessment_type","1");//1年度考核 2季度考核

				List<StageInformation> informations = stageInformationService.getBaseMapper().selectList(queryWrapper1);
				if (informations!=null && informations.size() < 1) {//如果在阶段表中没有查到，就新增一条阶段纪录
					if (data.get(t).getStage() != null && data.get(t).getStage() != "") {
						StageInformation stageInformation = new StageInformation();
						stageInformation.setStage(data.get(t).getStage());
						stageInformation.setStageRequirement(data.get(t).getStageRequirement());
						stageInformation.setStartDate(data.get(t).getStartDate());
						stageInformation.setEndDate(data.get(t).getEndDate());
						/*stageInformation.setAssessmentType("1");//1年度考核 2季度考核
						stageInformation.setAssessmentId(annualAssessment.getId());*/
						stageInformationService.save(stageInformation);
					} else {
						throw new RuntimeException("阶段信息不能为空！");
					}
				}
			} else {
				sb.append("重复：>>>>>>>>>>>>>>第"+ (t+2)+"行数据已存在！\n");
			}
		}
		System.out.println(sb.toString());
	}

	@Override
	public AnnualAssessment details(Long id) {
		AnnualAssessment ae=this.getById(id);
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String deptNamestr = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		if(ae!=null){
			if (ae.getNotAppriseUser() != null) {
				if (ae.getAppraiseDeptname().contains(deptNamestr) && !ae.getNotAppriseUser().contains(deptNamestr)) {
					ae.setIsAppraise(1);
				}
			}
			//获取阶段信息
			QueryWrapper<StageInformation> wrapperStage=new QueryWrapper<>();
			wrapperStage.eq("assessment_id",ae.getId());
			List<StageInformation> stageInformationList = stageInformationService.list(wrapperStage);
			if(!stageInformationList.isEmpty()){
				//获取阶段汇报提醒信息
				for (StageInformation sif:stageInformationList){
					//判断是否是当前阶段
					String iscurrent="N";
					Date startDate = sif.getStartDate();
					Date endDate = sif.getEndDate();
					long startTime = startDate.getTime();
					long endTime = endDate.getTime();
					long nowTime = System.currentTimeMillis();
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
				ae.setStageInformationList(stageInformationList);
			}
		}
		return ae;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptStage(AnnualAssessment ae) {
		boolean flag=false;
		List idsList = new ArrayList();
		//获取当前阶段信息
		List<StageInformation> stageInformationList = ae.getStageInformationList();
	/*	if(ObjectUtil.isNotEmpty(stageInformationList)){
			for(StageInformation sif:stageInformationList){
				sif.setAssessmentId(ae.getId());
				idsList.add(sif.getId());
			}
		}*/
		//根据年度评价指标id获取老的阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("assessment_id",ae.getId());
		List<StageInformation> oldStageInfoList = stageInformationService.listByMap(stageMap);
		if(ObjectUtil.isNotEmpty(oldStageInfoList)){
			for(StageInformation oldSif:oldStageInfoList){
				if(!idsList.contains(oldSif.getId())){//不包含就删除
					stageInformationService.removeById(oldSif.getId());
				}
			}
			//批量更新阶段信息
			flag=stageInformationService.uptList(stageInformationList);
		}
		return flag;
	}
}
