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
import org.springblade.integrated.platform.mapper.QuarterlyEvaluationMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.DictBiz;
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
 * @Author JG🧸
 * @Create 2022/4/9 17:30
 */
@Service
public class QuarterlyEvaluationServiceImpl extends BaseServiceImpl<QuarterlyEvaluationMapper, QuarterlyEvaluation> implements IQuarterlyEvaluationService {

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
	public void importQuarterlyEvaluation(List<QuarterlyEvaluationExcel> data) {
		//获取所有的部门分类
		List<DictBiz> deptGroup = DictBizCache.getList("dept_group");
		//租户ID
		String tenantId = "000000";
		List<QuarterlyEvaluation> list = new ArrayList<>();
		AtomicInteger index  = new AtomicInteger();
		index.set(1);
		data.forEach(regionExcel -> {
			index.getAndIncrement();
			System.out.println("当前行："+index.get());
			QuarterlyEvaluation region = BeanUtil.copy(regionExcel, QuarterlyEvaluation.class);
			region.getId();
			if (StringUtils.isEmpty(region.getToQuarter())) {
				throw new RuntimeException("所属季度不能为空！");
			}
			if (StringUtils.isEmpty(region.getCheckClassifyName())) {
				throw new RuntimeException("考核分类不能为空！");
			}
			if (StringUtils.isEmpty(region.getMajorTarget())) {
				throw new RuntimeException("评价要点不能为空！");
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

			if (region.getFirstTarget() != null) {
				switch (region.getFirstTarget()) {
					case "责任指标":
						region.setFirstTarget("1");
						break;
					case "重点工作":
						region.setFirstTarget("2");
						break;
					case "其他工作":
						region.setFirstTarget("3");
						break;
					case "推进高质量发展":
						region.setFirstTarget("4");
						break;
					case "乡村振兴":
						region.setFirstTarget("5");
						break;
					case "履行生态环保责任":
						region.setFirstTarget("6");
						break;
					default:
						break;
				}
			}
			deptGroup.forEach(deptClass -> {
				if (region.getCheckClassifyName().equals(deptClass.getDictValue())){
					region.setCheckClassify(deptClass.getDictKey());
				}
			});
			/*switch (region.getCheckClassifyName()) {
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
			}*/
			list.add(region);
		});
		QueryWrapper<QuarterlyEvaluation> queryWrapper = null;
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
				//一级指标
				queryWrapper.eq(list.get(t).getFirstTarget()!=null,"first_target",list.get(t).getFirstTarget());
				//二级指标
				queryWrapper.eq(list.get(t).getTwoTarget()!=null,"two_target",list.get(t).getTwoTarget());
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
			}else if (list.get(t).getJdzbName().contains("三抓三促")) {
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
			List<QuarterlyEvaluation> mapList = baseMapper.selectList(queryWrapper);
			QuarterlyEvaluation quarterlyEvaluation = null;
			if (mapList.size() < 1) {
				quarterlyEvaluation = list.get(t);
				this.save(quarterlyEvaluation);
				//如果QuarterlyEvaluation表中有数据，就去阶段表里查询导入的阶段信息是否存在
				QueryWrapper<StageInformation> queryWrapper1 = new QueryWrapper<>();
				System.out.println(">>>>>>>>>" + quarterlyEvaluation.getId().toString());
				queryWrapper1.eq(quarterlyEvaluation.getId() != null, "evaluation_id", quarterlyEvaluation.getId());
				queryWrapper1.eq(data.get(t).getStage() != null, "stage", data.get(t).getStage());
				queryWrapper1.eq(data.get(t).getStageRequirement() != null, "stage_requirement", data.get(t).getStageRequirement());
				queryWrapper1.eq(data.get(t).getStartDate() != null, "start_date", data.get(t).getStartDate());
				queryWrapper1.eq(data.get(t).getEndDate() != null, "end_date", data.get(t).getEndDate());
				queryWrapper1.eq("evaluation_type", "2");

				List<StageInformation> informations = iStageInformationService.getBaseMapper().selectList(queryWrapper1);
				if (informations.size() < 1) {//如果在阶段表中没有查到，就新增一条阶段纪录
					if (data.get(t).getStage() != null && data.get(t).getStage() != "") {
						StageInformation stageInformation = new StageInformation();
						stageInformation.setStage(data.get(t).getStage());
						stageInformation.setStageRequirement(data.get(t).getStageRequirement());
						stageInformation.setStartDate(data.get(t).getStartDate());
						stageInformation.setEndDate(data.get(t).getEndDate());
						stageInformation.setEvaluationType("2");//1年度考核 2季度考核
						stageInformation.setEvaluationId(quarterlyEvaluation.getId());
						iStageInformationService.save(stageInformation);
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
	@Transactional(rollbackFor = Exception.class)
	public boolean saveEvaluation(QuarterlyEvaluation qe){
		qe.setCheckStatus("0");//年度考评状态
		if(qe.getOperateType().equals("0")){
			qe.setTargetStatus("0");//指标状态：0暂存 1推进中 2已完成
		}else if(qe.getOperateType().equals("3")){//下发操作，状态变成推进中
			qe.setTargetStatus("1");//指标状态：0暂存 1推进中 2已完成
		}else if(qe.getOperateType().equals("5")){//下发申请操作，状态变成暂存
			qe.setTargetStatus("0");//指标状态：0暂存 1推进中 2已完成

		}
		qe.setHandleStatus("0");//办理状态：0正常1催办
		//添加未评价部门
		qe.setNotAppriseUser(qe.getAppraiseDeptname());
		boolean flag=this.save(qe);
		if(flag){
			//保存阶段信息
			List<StageInformation> stageInformationList = qe.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setEvaluationType("2");//评价指标分类：1年度考核 2季度考核
					sif.setEvaluationId(qe.getId());//季度评价主键id
				}
				flag=iStageInformationService.saveList(stageInformationList);
			}
		}

		if(qe.getOperateType().equals("3")){//下发
			//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
			reportsBaseinfoService.saveForQuarter(qe);
			//发送消息
			String msgSubmit = dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			String receiver="";
			String appraiseObjectIds= qe.getCheckObjectId();//评价对象ids
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
					if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.getMajorTarget();
					}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.getFirstTarget();
					} else if (qe.getTwoTarget() != null && qe.getTwoTarget() != "") {
						msgIntro = "【" + deptName + "】下发了季度评价指标：" + qe.getTwoTarget();
					} else {
						msgIntro = "【" + deptName + "】下发了季度评价指标。";
					}
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(qe.getId());//消息主键（业务主键）
					unifyMessage.setMsgTitle("季度评价下发");//消息标题
					unifyMessage.setMsgType("1");//消息类型，字典编码：web_message_type
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
					unifyMessage.setTwoLevelType("12");//季度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
			}
		}else if(qe.getOperateType().equals("5")){
			//送审
			this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(qe.getId()),qe.getTitle(),qe.getUserIds(),qe.getSync(), StatusConstant.OPERATION_TYPE_QUARTERAPPRISEXF);
			//发送消息
			String msgSubmit = dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			//发送消息
			BladeUser user = AuthUtil.getUser();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String msgIntro = "【"+deptName+"】下发送审了季度评价指标："+qe.getMajorTarget();
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(qe.getId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("季度评价下发送审");//消息标题
			unifyMessage.setMsgType("30");//消息类型，字典编码：web_message_type
			unifyMessage.setMsgPlatform("web");//平台：web或app
			unifyMessage.setReceiveUser(qe.getUserIds());
			unifyMessage.setMsgIntro(msgIntro);//消息简介
			unifyMessage.setMsgSubitem(msgSubmit);//消息分项
			unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
			unifyMessage.setCreateTime(new Date());
			unifyMessageService.sendMessageInfo(unifyMessage);

			unifyMessage.setId(null);
			unifyMessage.setMsgPlatform("app");
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType("29");//季度评价下发送审
			unifyMessageService.sendMessageInfo(unifyMessage);
		}
		return flag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptEvaluation(QuarterlyEvaluation qe) {
		if(qe.getOperateType().equals("4")){//下发操作，状态变成推进中
			qe.setTargetStatus("1");//指标状态：0暂存 1推进中 2已完成
		}
		//更新季度评价指标基本信息
		boolean flag=this.updateById(qe);
		if(flag){
			List idsList = new ArrayList();
			//获取当前阶段信息
			List<StageInformation> stageInformationList = qe.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setEvaluationId(qe.getId());
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
			}
			//批量更新阶段信息
			flag = iStageInformationService.uptList(stageInformationList);
		}
		if(qe.getOperateType().equals("4")){
			//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
			reportsBaseinfoService.saveForQuarter(qe);
			//发送消息
			String msgSubmit = dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			String receiver="";
			String appraiseObjectIds= qe.getCheckObjectId();//评价对象ids
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
					String msgIntro = "";
					if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.getMajorTarget();
					}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.getFirstTarget();
					} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
						msgIntro = "【"+deptName+"】下发了季度评价指标："+qe.getTwoTarget();
					} else {
						msgIntro = "【" + deptName + "】下发了季度评价指标。";
					}
					UnifyMessage unifyMessage = new UnifyMessage();
					unifyMessage.setMsgId(qe.getId());//消息主键（业务主键）
					unifyMessage.setMsgTitle("季度评价下发");//消息标题
					unifyMessage.setMsgType("1");//消息类型，字典编码：web_message_type
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
					unifyMessage.setTwoLevelType("12");//季度评价下发
					unifyMessageService.sendMessageInfo(unifyMessage);
				}
			}
		}
		return flag;
	}



	@Override
	public List<QuarterlyEvaluationExcel1> exportQuarterlyEvaluation1(QuarterlyEvaluation queryWrapper) {
		List<QuarterlyEvaluationExcel1> quarterlyEvaluationExcels = baseMapper.exportQuarterlyEvaluation1(queryWrapper);
		return quarterlyEvaluationExcels;
	}

	@Override
	public List<QuarterlyEvaluationExcel2> exportQuarterlyEvaluation2(QuarterlyEvaluation queryWrapper) {
		List<QuarterlyEvaluationExcel2> quarterlyEvaluationExcels = baseMapper.exportQuarterlyEvaluation2(queryWrapper);
		return quarterlyEvaluationExcels;
	}

	@Override
	public List<QuarterlyEvaluationExcel3> exportQuarterlyEvaluation3(QuarterlyEvaluation queryWrapper) {
		List<QuarterlyEvaluationExcel3> quarterlyEvaluationExcels = baseMapper.exportQuarterlyEvaluation3(queryWrapper);
		return quarterlyEvaluationExcels;
	}

	@Override
	public QuarterlyEvaluation details(Long id) {
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String deptNamestr = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		QuarterlyEvaluation qe=this.getById(id);
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


	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptStage(QuarterlyEvaluation qe) {
			boolean flag=false;
			List idsList = new ArrayList();
			//获取当前阶段信息
			List<StageInformation> stageInformationList = qe.getStageInformationList();
			if(ObjectUtil.isNotEmpty(stageInformationList)){
				for(StageInformation sif:stageInformationList){
					sif.setEvaluationId(qe.getId());
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
	}
}
