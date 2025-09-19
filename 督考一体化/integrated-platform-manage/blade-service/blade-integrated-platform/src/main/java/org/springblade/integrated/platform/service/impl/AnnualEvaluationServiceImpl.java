package org.springblade.integrated.platform.service.impl;/**
 * @author TangYanXing
 * @date 2022-04-09 14:17
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.excel.*;
import org.springblade.integrated.platform.mapper.AnnualEvaluationMapper;
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
 * @author TangYanXing
 * @version 1.0
 * @description:年度考评
 * @date 2022-04-09 14:17
 */
@Service
public class AnnualEvaluationServiceImpl extends BaseServiceImpl<AnnualEvaluationMapper,AnnualEvaluation> implements IAnnualEvaluationService {

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
	public boolean saveAnnualEvaluation(AnnualEvaluation ae){
		ae.setCheckStatus("0");//年度考评状态
		if(ae.getOperateType().equals("0")){
			ae.setTargetStatus("0");//指标状态：0暂存 1推进中 2已完成
		}else if(ae.getOperateType().equals("3")){//下发操作，状态变成推进中
			ae.setTargetStatus("1");//指标状态：0暂存 1推进中 2已完成
		}else if(ae.getOperateType().equals("5")){//下发申请操作，状态变成暂存
			ae.setTargetStatus("0");//指标状态：0暂存 1推进中 2已完成
		}
		//添加未评价部门
		ae.setNotAppriseUser(ae.getAppraiseDeptname());
		boolean flag=this.save(ae);
		if(flag){
			//保存阶段信息
			List<StageInformation> stageInformationList = ae.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setEvaluationType("1");//评价指标分类：1年度考核 2季度考核
					sif.setEvaluationId(ae.getId());//年度、季度评价主键id
				}
				flag=stageInformationService.saveList(stageInformationList);
			}
		}


		if(ae.getOperateType().equals("3")){
			//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
			reportsBaseinfoService.saveForAnnual(ae);

			//发送消息
			String msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			String receiver="";
			String appraiseObjectIds= ae.getAppraiseObjectId();//评价对象ids
			R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
			String glyId=rgly.getData();
			if(StringUtils.isNotBlank(appraiseObjectIds)){
				String[] ids = appraiseObjectIds.split(",");
				for(int i=0;i<ids.length;i++){
					R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,ids[i]);//获取单位下面所有管理员用户
					if(ruser!=null){
						List<User> userList = ruser.getData();
						for(User user : userList){
							receiver+=user.getId()+",";
						}
					}
					//发送消息
					BladeUser user = AuthUtil.getUser();
					String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
					String msgIntro = "";
					if (ae.getMajorTarget() != null && ae.getMajorTarget()!="") {
						msgIntro = "【"+deptName+"】下发了年度评价指标："+ae.getMajorTarget();
					}else{
						msgIntro = "【"+deptName+"】下发了年度评价指标。";
					}
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(ae.getId());//消息主键（业务主键）
					unifyMessage.setMsgTitle("年度评价下发");//消息标题
					unifyMessage.setMsgType("8");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(receiver);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem(msgSubmit);//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);

					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
					unifyMessage.setTwoLevelType("19");//年度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
			}
		}else if(ae.getOperateType().equals("5")){
			String msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(ae.getId()),ae.getTitle(),ae.getUserIds(),ae.getSync(), StatusConstant.OPERATION_TYPE_ANNUALAPPRISEXF);
			//发送消息
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】下发送审了年度评价指标："+ae.getMajorTarget();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(ae.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("年度评价下发送审");//消息标题
			unifyMessage.setMsgType("28");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(ae.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("27");//年度评价下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);

		}
		return flag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptAnnualEvaluation(AnnualEvaluation ae) {
		if(ae.getOperateType().equals("4")){//下发操作，状态变成推进中
			ae.setTargetStatus("1");//指标状态：0暂存 1推进中 2已完成
		}
		//更新年度评价指标基本信息
		boolean flag=this.updateById(ae);
		if(flag){
			List<Long> idsList = new ArrayList();
			//获取当前阶段信息
			List<StageInformation> stageInformationList = ae.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setEvaluationId(ae.getId());
					idsList.add(sif.getId());
				}
			}
			//根据年度评价指标id获取老的阶段信息
			Map<String, Object> stageMap = new HashMap<>();
			stageMap.put("evaluation_id",ae.getId());
			List<StageInformation> oldStageInfoList = stageInformationService.listByMap(stageMap);
			if(ObjectUtil.isNotEmpty(oldStageInfoList)){
				for(StageInformation oldSif:oldStageInfoList){
					if(!idsList.contains(oldSif.getId())){//不包含就删除
						stageInformationService.removeById(oldSif.getId());
					}
				}
			}
			//批量更新阶段信息
			flag=stageInformationService.uptList(stageInformationList);
		}

		if(ae.getOperateType().equals("4")){
			//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
			reportsBaseinfoService.saveForAnnual(ae);

			//发送消息
			String msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			String receiver="";
			String appraiseObjectIds= ae.getAppraiseObjectId();//评价对象ids
			R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
			String glyId=rgly.getData();
			if(StringUtils.isNotBlank(appraiseObjectIds)){
				String[] ids = appraiseObjectIds.split(",");
				for(int i=0;i<ids.length;i++){
					R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,ids[i]);//获取单位下面所有管理员用户
					if(ruser!=null){
						List<User> userList = ruser.getData();
						for(User user : userList){
							receiver+=user.getPostId()+",";
						}
					}
					//发送消息
					BladeUser user = AuthUtil.getUser();
					String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
					String msgIntro = deptName+"下发了年度评价指标："+ae.getMajorTarget();
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(Long.valueOf(ae.getId()));//消息主键（业务主键）
					unifyMessage.setMsgTitle("年度评价下发");//消息标题
					unifyMessage.setMsgType("8");//消息类型，字典编码：web_message_type
					unifyMessage.setMsgPlatform("web");//平台：web或app
					unifyMessage.setReceiveUser(receiver);
					unifyMessage.setMsgIntro(msgIntro);//消息简介
					unifyMessage.setMsgSubitem(msgSubmit);//消息分项
					unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
					unifyMessage.setCreateTime(new Date());
					unifyMessageService.sendMessageInfo(unifyMessage);

					unifyMessage.setId(null);
					unifyMessage.setMsgPlatform("app");
					unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
					unifyMessage.setTwoLevelType("19");//年度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
			}
		}
		return flag;
	}

	@Override
	public List<ZzsxjsExcel> exportZzsxjsEvaluation(AnnualEvaluation ae) {
		List<ZzsxjsExcel> aeList = baseMapper.exportZzsxjsEvaluation(ae);
		return aeList;
	}

	@Override
	public List<LdnlExcel> exportLdnlEvaluation(AnnualEvaluation ae) {
		List<LdnlExcel> aeList = baseMapper.exportLdnlEvaluation(ae);
		return aeList;
	}

	@Override
	public List<DdjsExcel> exportDdjsEvaluation(AnnualEvaluation ae) {
		List<DdjsExcel> aeList = baseMapper.exportDdjsEvaluation(ae);
		return aeList;
	}

	@Override
	public List<SgzlfzExcel> exportSgzlfzEvaluation(AnnualEvaluation ae) {
		List<SgzlfzExcel> aeList = baseMapper.exportSgzlfzEvaluation(ae);
		return aeList;
	}

	@Override
	public List<QxgzlfzExcel> exportQxgzlfzEvaluation(AnnualEvaluation ae) {
		List<QxgzlfzExcel> aeList = baseMapper.exportQxgzlfzEvaluation(ae);
		return aeList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importZzsxjsEvaluation(List<AnnualEvaluationExcel> data) {
		//租户ID
		String tenantId = "000000";
		List<AnnualEvaluation> list = new ArrayList<>();
		AtomicInteger index  = new AtomicInteger();
		index.set(1);
		data.forEach(regionExcel -> {
			index.getAndIncrement();
			System.out.println("当前行："+index.get());
			AnnualEvaluation region = BeanUtil.copy(regionExcel, AnnualEvaluation.class);
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
		QueryWrapper<AnnualEvaluation> queryWrapper = null;
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
			List<AnnualEvaluation> mapList = baseMapper.selectList(queryWrapper);
			AnnualEvaluation annualEvaluation = null;
			if (mapList.size() < 1) {
				annualEvaluation = list.get(t);
				this.save(annualEvaluation);
				//如果QuarterlyEvaluation表中有数据，就去阶段表里查询导入的阶段信息是否存在
				QueryWrapper<StageInformation> queryWrapper1 = new QueryWrapper<>();
				queryWrapper1.eq(annualEvaluation.getId()!=null,"evaluation_id",annualEvaluation.getId());
				queryWrapper1.eq(data.get(t).getStage()!=null,"stage",data.get(t).getStage());
				queryWrapper1.eq(data.get(t).getStageRequirement()!=null,"stage_requirement",data.get(t).getStageRequirement());
				queryWrapper1.eq(data.get(t).getStartDate()!=null,"start_date",data.get(t).getStartDate());
				queryWrapper1.eq(data.get(t).getEndDate()!=null,"end_date",data.get(t).getEndDate());
				queryWrapper1.eq("evaluation_type","1");//1年度考核 2季度考核

				List<StageInformation> informations = stageInformationService.getBaseMapper().selectList(queryWrapper1);
				if (informations!=null && informations.size() < 1) {//如果在阶段表中没有查到，就新增一条阶段纪录
					if (data.get(t).getStage() != null && data.get(t).getStage() != "") {
						StageInformation stageInformation = new StageInformation();
						stageInformation.setStage(data.get(t).getStage());
						stageInformation.setStageRequirement(data.get(t).getStageRequirement());
						stageInformation.setStartDate(data.get(t).getStartDate());
						stageInformation.setEndDate(data.get(t).getEndDate());
						stageInformation.setEvaluationType("1");//1年度考核 2季度考核
						stageInformation.setEvaluationId(annualEvaluation.getId());
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
	public AnnualEvaluation details(Long id) {
		AnnualEvaluation ae=this.getById(id);
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
			wrapperStage.eq("evaluation_id",ae.getId());
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
	public boolean uptStage(AnnualEvaluation ae) {
		boolean flag=false;
		List idsList = new ArrayList();
		//获取当前阶段信息
		List<StageInformation> stageInformationList = ae.getStageInformationList();
		if(ObjectUtil.isNotEmpty(stageInformationList)){
			for(StageInformation sif:stageInformationList){
				sif.setEvaluationId(ae.getId());
				idsList.add(sif.getId());
			}
		}
		//根据年度评价指标id获取老的阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("evaluation_id",ae.getId());
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
