package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionSubmitAuditVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.integrated.platform.wrapper.SupervisionSubmitAuditWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/22 20:39
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/supervisionSubmitAudit")
@Api(value = "送审审核信息", tags = "送审审核信息")
public class SupervisionSubmitAuditController {

	private final ISupervisionSignService supervisionSignService;
	private final ISupervisionInfoService supervisionInfoService;
	private final ISupervisionSubmitAuditService supervisionSubmitAuditService;
	private final ISupervisionUpPlanService supervisionUpPlanService;
	private final ISupervisionPhaseReportService supervisionPhaseReportService;
	private final IProjectSummaryService projectSummaryService;
	private final IReportsService iReportsService;
	private final IQuarterlyEvaluationService iQuarterlyEvaluationService;
	private final IAnnualEvaluationService iAnnualEvaluationService;
	private final IScoreAddService iScoreAddService;
	@Resource
	private final ISysClient sysClient;
	@Resource
	private final IUserClient userClient;
	@Resource
	private final IDictBizClient dictBizClient;
	private final ISupervisionFilesService supervisionFilesService;
	private final IUnifyMessageService messageService;
	private final ISupervisionLogService supervisionLogService;
	@Resource
	private final IUserSearchClient iUserSearchClient;
	@Resource
	private ReportsBaseinfoService reportsBaseinfoService;
	@Resource
	private IUnifyMessageService unifyMessageService;
	private final IAnnualEvaluationService annualEvaluationService;
	private final ISupervisionPhaseReportAllService supervisionPhaseReportAllService;
	private final ISupervisionPhaseReportChiService supervisionPhaseReportChiService;
	private final ISupervisionPhaseReportAltService supervisionPhaseReportAltService;
	private final ISupervisionPhasePlanService supervisionPhasePlanService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;


	/**
	 * 根据业务表主键查询送审列表
	 * @param servId
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "事项审核列表", notes = "事项审核列表")
	public R list(@RequestParam String servId){
		List<SupervisionSubmitAudit> list = this.supervisionSubmitAuditService.list(new QueryWrapper<>(new SupervisionSubmitAudit()).eq("serv_id",servId).or().eq("report_id",servId).orderByDesc("create_time"));
		return R.data(SupervisionSubmitAuditWrapper.build().listVO(list));
	}

	/**
	 * 根据业务表主键查询送审列表
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "事项审核列表-app", notes = "事项审核列表-app")
	public R listApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("附件列表-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			String servId = jsonParams.getString("servId");
			List<SupervisionSubmitAudit> list = this.supervisionSubmitAuditService.list(new QueryWrapper<>(new SupervisionSubmitAudit())
				.eq("serv_id",servId).or().eq("report_id",servId).orderByDesc("create_time"));
			List<SupervisionSubmitAuditVO> supervisionSubmitAuditVOList = SupervisionSubmitAuditWrapper.build().listVO(list);
			JSONArray jsonArray = objectMapper.convertValue(supervisionSubmitAuditVOList, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, jsonArray.toJSONString(), VSTool.CHN));
		}else{
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 立项送审-审核
	 * @param id		审核数据id
	 * @param status	审核状态：0待审核；1通过；2不通过；3冻结，上一个人审核通过后转为0待审核状态
	 * @param msg		审核消息
	 * @param files		附件信息
	 * @return
	 */
	@PostMapping("/audit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "督察督办-审核", notes = "督察督办-审核")
	@Transactional
	public R audit(@RequestParam String id, @RequestParam String status, @RequestParam String msg, @RequestParam(required = false) String files){

		try {

			JSONArray fileArr = JSON.parseArray(files);

			SupervisionSubmitAudit audit = this.supervisionSubmitAuditService.getById(id);
			if (audit != null) {
				if(files!=null){
					audit.setFileUrl(fileArr.toJSONString());
				}
				audit.setMsg(msg);
				audit.setStatus(Integer.parseInt(status));
				audit.setApprovalTime(new Date());
				audit.setApprovalUser(AuthUtil.getUserId());
				this.supervisionSubmitAuditService.updateById(audit);
			}else {
				return R.fail("当前ID不存在！");
			}

			boolean isSend = false;
			if(audit.getSync().equals(StatusConstant.AUDIT_SYNC_0) && StatusConstant.AUDIT_STATUS_1.equals(status)){

				//异步并且当前环节通过时修改下一个人的状态
				this.supervisionSubmitAuditService.updateNextUserStatus(audit.getServId(),audit.getSort()+1,audit.getBatchNumber());

				//给下个环节人员发消息
				QueryWrapper queryWrapper = new QueryWrapper<>(new SupervisionSubmitAudit());
				queryWrapper.eq("serv_id",audit.getServId());
				queryWrapper.eq("sort",audit.getSort()+1);
				queryWrapper.eq("batch_number",audit.getBatchNumber());
				SupervisionSubmitAudit nextAudit = this.supervisionSubmitAuditService.getOne(queryWrapper);
				if(nextAudit!=null && nextAudit.getId()!=null){
					SupervisionInfo info = this.supervisionInfoService.getById(nextAudit.getServId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendDcSsMsg(UserCache.getUser(nextAudit.getCreateUser()).getRealName(),nextAudit.getServId()+"",nextAudit.getUserId()+"",value,info.getServName());
				}

				if(StatusConstant.OPERATION_TYPE_INFO.equals(audit.getOperationType())){
					// TODO 督察督办
					//记录日志
					this.saveSupervisionInfoLog(audit.getServId());
					//发送消息
					SupervisionInfo info = this.supervisionInfoService.getById(audit.getServId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendDcSsShMsg(audit.getServId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}else if(StatusConstant.OPERATION_TYPE_PLAN.equals(audit.getOperationType())){
					// TODO 上报计划
					//记录日志
					this.saveSupervisionInfoLog(audit.getReportId());
					//发送消息
					SupervisionUpPlan entity = this.supervisionUpPlanService.getById(audit.getServId());
					SupervisionInfo info = this.supervisionInfoService.getById(entity.getServId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendSbjhSsShMsg(info.getId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}else if(StatusConstant.OPERATION_TYPE_REPORT_CHI.equals(audit.getOperationType())){
					// TODO 阶段分派汇报
					//记录日志
					this.saveSupervisionInfoLog(audit.getReportId());
					//发送消息
//					SupervisionPhaseReport report = supervisionPhaseReportService.getById(.getServId());
					SupervisionInfo info = new SupervisionInfo();
//					info.setServCode(report.getServCode());
					info = supervisionInfoService.getById(audit.getReportId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendFphbSsShMsg(info.getId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}else if(StatusConstant.OPERATION_TYPE_REPORT.equals(audit.getOperationType())){
					// TODO 阶段汇报
					//记录日志
					this.saveSupervisionInfoLog(audit.getReportId());
					//发送消息
					SupervisionPhaseReport report = supervisionPhaseReportService.getById(audit.getServId());
					SupervisionInfo info = new SupervisionInfo();
					info.setServCode(report.getServCode());
					info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendJdhbSsShMsg(info.getId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}else if(StatusConstant.OPERATION_TYPE_REPORT_ALL.equals(audit.getOperationType())){
					// TODO 阶段汇总
					//记录日志
					this.saveSupervisionInfoLog(audit.getReportId());
					//发送消息
					SupervisionPhaseReportAll report = supervisionPhaseReportAllService.getById(audit.getServId());
					SupervisionInfo info = new SupervisionInfo();
					info.setServCode(report.getServCode());
					info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendJdhzSsShMsg(info.getId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}else if(StatusConstant.OPERATION_TYPE_REPORT_ALT.equals(audit.getOperationType())){
					// TODO 阶段分派汇总
					//记录日志
					this.saveSupervisionInfoLog(audit.getReportId());
					//发送消息
//					SupervisionPhaseReport report = supervisionPhaseReportService.getById(.getServId());
					SupervisionInfo info = new SupervisionInfo();
//					info.setServCode(report.getServCode());
					info = supervisionInfoService.getById(audit.getReportId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendFphbSsShMsg(info.getId()+"",audit.getCreateUser()+"",value,info.getServName(),"通过");
				}

			}else if(StatusConstant.AUDIT_STATUS_2.equals(status)){
				if(StatusConstant.OPERATION_TYPE_INFO.equals(audit.getOperationType())){
					// TODO 督察督办
					//不通过直接打回草稿状态
					this.supervisionInfoService.updateFlowStatus(audit.getServId(), StatusConstant.FLOW_STATUS_0);

					//发送消息
					SupervisionInfo info = this.supervisionInfoService.getById(audit.getServId());
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					this.unifyMessageService.sendDcSsShMsg(audit.getServId()+"",audit.getCreateUser()+"",value,info.getServName(),"不通过");

					//记录日志
					this.saveSupervisionInfoLog(audit.getServId());
				} else if(StatusConstant.OPERATION_TYPE_PLAN.equals(audit.getOperationType())){
					// TODO 上报计划
					SupervisionUpPlan entity = this.supervisionUpPlanService.getById(audit.getServId());
					entity.setStatus(5);//送审不通过状态
					//修改状态
					this.saveUpPlanMessage(entity);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_CHI.equals(audit.getOperationType())){
					// TODO 阶段分派汇报
					//修改状态
					SupervisionPhaseReportChi report = supervisionPhaseReportChiService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_4);//送审不通过状态
					this.saveReportChiMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT.equals(audit.getOperationType())){
					// TODO 阶段汇报
					//修改状态
					SupervisionPhaseReport report = supervisionPhaseReportService.getById(audit.getServId());
					if("3".equals(report.getReportStatus())){
						report.setReportStatus(StatusConstant.DC_REPORT_STATUS_4);//送审不通过状态
					}else{
						report.setReportStatus("10");//送审不通过状态
					}
					this.saveReportMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_ALL.equals(audit.getOperationType())){
					// TODO 阶段汇总
					//修改状态
					SupervisionPhaseReportAll report = supervisionPhaseReportAllService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_4);//送审不通过状态
					this.saveReportAllMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_ALT.equals(audit.getOperationType())){
					// TODO 阶段分派汇总
					//修改状态
					SupervisionPhaseReportAlt report = supervisionPhaseReportAltService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_4);//送审不通过状态
					this.saveReportAltMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISEHB.equals(audit.getOperationType()) || StatusConstant.OPERATION_TYPE_ANNUALAPPRISEHB.equals(audit.getOperationType())){
					// TODO 考核评价 汇报送审
					//修改状态
					Reports reports = new Reports();
					reports.setId(audit.getServId());
					reports.setReportStatus(StatusConstant.KH_REPORT_STATUS_4);//送审不通过状态
					this.iReportsService.updateById(reports);
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISESCORE.equals(audit.getOperationType())){
					// TODO 考核评价季度 改分申请
					//修改季度表状态
					//QuarterlyEvaluation quarterlyEvaluation = new QuarterlyEvaluation();
					//quarterlyEvaluation.setId(audit.getServId());
					//quarterlyEvaluation.setIsAppraise(0);//送审不通过,是否评价改为1，意为不允许二次评价
					//this.iQuarterlyEvaluationService.updateById(quarterlyEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_ANNUALAPPRISESCORE.equals(audit.getOperationType())){
					// TODO 考核评价年度 改分申请
					//修改年度表状态
					//AnnualEvaluation annualEvaluation = new AnnualEvaluation();
					//annualEvaluation.setId(audit.getServId());
					//annualEvaluation.setIsAppraise(0);//送审不通过,是否评价改为1，意为不允许二次评价
					//this.iAnnualEvaluationService.updateById(annualEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISEXF.equals(audit.getOperationType())){
					// TODO 考核评价季度 下发
					//修改季度指标表状态
					QuarterlyEvaluation quarterlyEvaluation = new QuarterlyEvaluation();
					quarterlyEvaluation.setId(audit.getServId());
					quarterlyEvaluation.setTargetStatus("0");//送审不通过,指标状态改为0,表示暂存
					this.iQuarterlyEvaluationService.updateById(quarterlyEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_ANNUALAPPRISEXF.equals(audit.getOperationType())){
					// TODO 考核评价年度 下发
					//修改年度指标表状态
					AnnualEvaluation annualEvaluation = new AnnualEvaluation();
					annualEvaluation.setId(audit.getServId());
					annualEvaluation.setTargetStatus("0");//送审不通过,指标状态改为0,表示暂存
					this.iAnnualEvaluationService.updateById(annualEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_ADDSCORE.equals(audit.getOperationType())){
					// TODO 考核评价 加分项
					//修改加分项状态
					ScoreAdd scoreAdd = new ScoreAdd();
					scoreAdd.setId(audit.getServId());
					scoreAdd.setIsok("2");//送审不通过,状态改为2
					this.iScoreAddService.updateById(scoreAdd);
				}/* else if(StatusConstant.OPERATION_TYPE_MINUSSCORE.equals(audit.getOperationType())){
					// TODO 考核评价 减分项
					//修改减分项状态
					ScoreMinus scoreMinus = new ScoreMinus();
					scoreMinus.setId(audit.getServId());
					scoreMinus.setIsok("2");//送审不通过,状态改为2
					this.iScoreMinusService.updateById(scoreMinus);
				}*/else if(StatusConstant.OPERATION_TYPE_WARE.equals(audit.getOperationType())){
					SupervisionPhaseReport supervisionPhaseReport = new SupervisionPhaseReport();
					supervisionPhaseReport.setId(Long.parseLong(id));
					supervisionPhaseReport.setReportStatus(StatusConstant.KH_REPORT_STATUS_4);//送审不通过状态
					this.supervisionPhaseReportService.updateById(supervisionPhaseReport);
					if(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getAreaCode().length() > 4){
						projectSummaryService.updateProjStatus(audit.getServId().toString(),"4");
					}else{
						projectSummaryService.updateProjStatus(audit.getServId().toString(),"6");
					}
				}
			}

			//检查审核表，有没有待审核或未通过的，没有则视为全部通过，执行下发
			Integer noAuditNum = this.supervisionSubmitAuditService.countAuditNumber(audit.getServId(),audit.getBatchNumber());
			if(noAuditNum==0){
				if(StatusConstant.OPERATION_TYPE_INFO.equals(audit.getOperationType())){
					// TODO 督察督办
					//下发改变状态
					this.supervisionInfoService.updateFlowStatus(audit.getServId(),StatusConstant.FLOW_STATUS_1);
					//创建签收数据
					SupervisionInfo info = this.supervisionInfoService.getById(audit.getServId());
					this.supervisionSignService.saveSignInfo(info);

					//发送下发消息
					List<String> userIds = new ArrayList<>();
					String[] leadUnits = info.getLeadUnit().split(",");
					for(int i=0;i<leadUnits.length;i++){
						List<User> users = userClient.getUserLeader(leadUnits[i], Constants.USER_POST_GLY_id).getData();
						if(users!=null && users.size()>0){
							for(User user : users){
								userIds.add(user.getId()+"");
							}
						}
					}
					if(StringUtils.isNotEmpty(info.getDutyUnit())){
						String[] dutyUnits = info.getDutyUnit().split(",");
						for(int i=0;i<dutyUnits.length;i++){
							List<User> users = userClient.getUserLeader(dutyUnits[i], Constants.USER_POST_GLY_id).getData();
							if(users!=null && users.size()>0){
								for(User user : users){
									userIds.add(user.getId()+"");
								}
							}
						}
					}
					if(StringUtils.isNotEmpty(info.getDutyLeader())){
						userIds.addAll(Arrays.asList(info.getDutyLeader().split(",")));
					}
					String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());
					for(String userId : userIds){
						this.unifyMessageService.sendDcXfMsg(UserCache.getUser(info.getCreateUser()).getRealName(),audit.getServId()+"",userId,value,info.getServName());
					}

				} else if(StatusConstant.OPERATION_TYPE_PLAN.equals(audit.getOperationType())){
					// TODO 上报计划
					//修改状态
					SupervisionUpPlan entity = this.supervisionUpPlanService.getById(audit.getServId());
					entity.setStatus(1);//牵头单位上报
					this.saveUpPlanMessage(entity);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_CHI.equals(audit.getOperationType())){
					// TODO 阶段分派汇报
					//修改状态
					SupervisionPhaseReportChi report = supervisionPhaseReportChiService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_1);//分派汇报
					this.saveReportChiMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT.equals(audit.getOperationType())){
					// TODO 阶段汇报
					//修改状态
					SupervisionPhaseReport report = supervisionPhaseReportService.getById(audit.getServId());
					if("3".equals(report.getReportStatus())){
						report.setReportStatus(StatusConstant.DC_REPORT_STATUS_1);//责任单位汇报
					}else{
						report.setReportStatus(StatusConstant.DC_REPORT_STATUS_7);//责任单位超期汇报
					}
					this.saveReportMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_ALL.equals(audit.getOperationType())){
					// TODO 阶段汇总
					//修改状态
					SupervisionPhaseReportAll report = supervisionPhaseReportAllService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_2);//牵头单位汇总
					this.saveReportAllMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_REPORT_ALT.equals(audit.getOperationType())){
					// TODO 阶段分派汇总
					//修改状态
					SupervisionPhaseReportAlt report = supervisionPhaseReportAltService.getById(audit.getServId());
					report.setReportStatus(StatusConstant.DC_REPORT_STATUS_1);//分派汇报
					this.saveReportAltMessage(report);
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISEHB.equals(audit.getOperationType()) || StatusConstant.OPERATION_TYPE_ANNUALAPPRISEHB.equals(audit.getOperationType())){
					// TODO 考核评价 汇报送审通过
					//修改状态
					Reports reports = iReportsService.getById(audit.getServId());
					reports.setReportStatus(StatusConstant.KH_REPORT_STATUS_3);//送审通过状态
					this.iReportsService.updateById(reports);
					String type= reports.getEvaluationType();
					//审核通过修改指标状态为推进中1
					if("1".equals(type)){//年度
						AnnualEvaluation ae = this.iAnnualEvaluationService.getById(reports.getEvaluationId());
						ae.setTargetStatus("1");
						this.iAnnualEvaluationService.updateById(ae);
					}else if("2".equals(type)){
						QuarterlyEvaluation qe = this.iQuarterlyEvaluationService.getById(reports.getEvaluationId());
						qe.setTargetStatus("1");
						this.iQuarterlyEvaluationService.updateById(qe);
					}
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISESCORE.equals(audit.getOperationType())){
					// TODO 考核评价季度 改分申请
					//修改季度表状态
					QuarterlyEvaluation quarterlyEvaluation1= this.iQuarterlyEvaluationService.getById(audit.getServId());
					QuarterlyEvaluation quarterlyEvaluation = new QuarterlyEvaluation();
					quarterlyEvaluation.setId(audit.getServId());
					String notAppraiseDept = quarterlyEvaluation1.getNotAppriseUser()+","+audit.getDeptName();
					quarterlyEvaluation.setNotAppriseUser(notAppraiseDept);//送审通过,加上当前未评价单位

					this.iQuarterlyEvaluationService.updateById(quarterlyEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_ANNUALAPPRISESCORE.equals(audit.getOperationType())){
					// TODO 考核评价年度 改分申请
					//修改年度表状态
					AnnualEvaluation annualEvaluation1= this.iAnnualEvaluationService.getById(audit.getServId());
					AnnualEvaluation annualEvaluation = new AnnualEvaluation();
					annualEvaluation.setId(audit.getServId());
					String notAppraiseDept = annualEvaluation1.getNotAppriseUser()+","+audit.getDeptName();
					annualEvaluation.setNotAppriseUser(notAppraiseDept);//送审通过,加上当前未评价单位

					this.iAnnualEvaluationService.updateById(annualEvaluation);
				} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISEXF.equals(audit.getOperationType())){
					// TODO 考核评价季度 下发
					QuarterlyEvaluation qe = iQuarterlyEvaluationService.getById(audit.getServId());
					//修改季度表状态
					qe.setTargetStatus("1");//送审通过,指标状态改为1,表示推进中
					this.iQuarterlyEvaluationService.updateById(qe);
					//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
					reportsBaseinfoService.saveForQuarter(qe);
					//发送消息
					String msgSubmit = dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
					String receiver="";
					String appraiseObjectIds= qe.getCheckObjectId();//评价对象ids
					R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
					String glyId=rgly.getData();
					if(StringUtils.isNotBlank(appraiseObjectIds)) {
						String[] ids = appraiseObjectIds.split(",");
						for (int i = 0; i < ids.length; i++) {
							R<List<User>> ruser = iUserSearchClient.listByPostAndDept(glyId, ids[i]);//获取单位下面所有管理员用户
							if (ruser != null) {
								List<User> userList = ruser.getData();
								for (User user : userList) {
									receiver += user.getId() + ",";
								}
							}
						}
						//发送消息
						BladeUser user = AuthUtil.getUser();
						String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
						String msgIntro = "";
						if (qe.getMajorTarget() != null && qe.getMajorTarget() != "") {
							msgIntro = "【" + deptName + "】下发了季度评价指标：" + qe.getMajorTarget();
						} else if (qe.getFirstTarget() != null && qe.getFirstTarget() != "") {
							msgIntro = "【" + deptName + "】下发了季度评价指标：" + qe.getFirstTarget();
						} else if (qe.getTwoTarget() != null && qe.getTwoTarget() != "") {
							msgIntro = "【" + deptName + "】下发了季度评价指标：" + qe.getTwoTarget();
						} else {
							msgIntro = "【" + deptName + "】下发了季度评价指标。";
						}
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(qe.getId()));//消息主键（业务主键）
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
				} else if(StatusConstant.OPERATION_TYPE_ANNUALAPPRISEXF.equals(audit.getOperationType())){
					// TODO 考核评价年度 下发
					//修改年度表状态
					AnnualEvaluation ae = annualEvaluationService.getById(audit.getServId());
					ae.setTargetStatus("1");//送审通过,指标状态改为1,表示推进中
					this.iAnnualEvaluationService.updateById(ae);
					//下发操作，存入reports_baseinfo基本信息（下发完成的考核对象基本信息）
					reportsBaseinfoService.saveForAnnual(ae);
					//发送消息
					String receiver="";
					String appraiseObjectIds= ae.getAppraiseObjectId();//评价对象ids
					R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
					String glyId=rgly.getData();
					if(StringUtils.isNotBlank(appraiseObjectIds)){
						String[] idss = appraiseObjectIds.split(",");
						for(int i=0;i<idss.length;i++){
							R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,idss[i]);//获取单位下面所有管理员用户
							if(ruser!=null){
								List<User> userList = ruser.getData();
								for(User user : userList){
									receiver+=user.getId()+",";
								}
							}
						}
						//发送消息
						String msgSubmit = dictBizClient.getValue("ndkp-type",ae.getType()).getData();
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
				} else if(StatusConstant.OPERATION_TYPE_ADDSCORE.equals(audit.getOperationType())){
					// TODO 考核评价 加分项
					//修改加分项状态
					ScoreAdd scoreAdd = new ScoreAdd();
					scoreAdd.setId(audit.getServId());
					scoreAdd.setIsok("1");//送审通过,状态改为1
					this.iScoreAddService.updateById(scoreAdd);
				}/* else if(StatusConstant.OPERATION_TYPE_MINUSSCORE.equals(audit.getOperationType())){
					// TODO 考核评价 减分项
					//修改减分项状态
					ScoreMinus scoreMinus = new ScoreMinus();
					scoreMinus.setId(audit.getServId());
					scoreMinus.setIsok("1");//送审通过,状态改为1
					this.iScoreMinusService.updateById(scoreMinus);
				}*/else if(StatusConstant.OPERATION_TYPE_WARE.equals(audit.getOperationType())){
					SupervisionPhaseReport supervisionPhaseReport = new SupervisionPhaseReport();
					supervisionPhaseReport.setId(Long.parseLong(id));
					supervisionPhaseReport.setReportStatus(StatusConstant.KH_REPORT_STATUS_3);//审考核评价审核通过
					this.supervisionPhaseReportService.updateById(supervisionPhaseReport);
					if(sysClient.getDept(Long.parseLong(AuthUtil.getDeptId())).getData().getAreaCode().length() > 4){
						projectSummaryService.updateProjStatus(audit.getServId().toString(),"5");
					}else{
						projectSummaryService.projectSummaryRk(audit.getServId().toString(),"1");
						User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
						String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
						String msgIntro = "【"+userNameDecrypt+"】审核通过【"+projectSummaryService.getById(audit.getServId()).getTitle()+"】纳入申请";
						UnifyMessage unifyMessage = new UnifyMessage();
						unifyMessage.setMsgId(Long.valueOf(audit.getServId()));//消息主键（业务主键）
						unifyMessage.setMsgTitle("项目审核");//消息标题
						unifyMessage.setMsgType("31");//消息类型，字典编码：web_message_type
						unifyMessage.setMsgPlatform("web");//平台：web或app
						unifyMessage.setReceiveUser(projectSummaryService.getById(audit.getServId()).getCreateUser().toString());
						unifyMessage.setMsgIntro(msgIntro);//消息简介
						unifyMessage.setMsgSubitem("");//消息分项
						unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
						unifyMessage.setCreateTime(new Date());
						unifyMessageService.sendMessageInfo(unifyMessage);
						String xmdl = projectSummaryService.getById(audit.getServId()).getXmdl();
						String xmbq = projectSummaryService.getById(audit.getServId()).getProjLabel();
						if (xmdl.equals("1")||xmdl.equals("2")){
							if (xmbq.contains("1")){
								unifyMessage.setMsgSubitem("市列重大项目清单");//消息分项
							}else if(xmbq.contains("2")){
								unifyMessage.setMsgSubitem("省列重大项目清单");//消息分项
							}else {
								unifyMessage.setMsgSubitem("投资项目清单");//消息分项
							}
						}else if (xmdl.equals("3")||xmdl.equals("4")){
							unifyMessage.setMsgSubitem("前期项目清单");//消息分项
						}else if (xmdl.equals("5")){
							unifyMessage.setMsgSubitem("中央和省级预算内项目");//消息分项
						}else if (xmdl.equals("6")) {
							unifyMessage.setMsgSubitem("政府专项债券项目");//消息分项
						}
						unifyMessage.setId(null);
						unifyMessage.setMsgPlatform("app");
						unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_XMTZ);
						unifyMessage.setTwoLevelType("31");//项目退回
						unifyMessageService.sendMessageInfo(unifyMessage);
					}
				}
			}
			//保存附件
			saveFile(fileArr, audit);


			return R.status(true);
		} catch (Exception e){
			e.printStackTrace();
			return R.fail(e.getMessage());
		}
	}

	/**
	 * 保存附件
	 * @param files
	 * @param audit
	 */
	private void saveFile(JSONArray files, SupervisionSubmitAudit audit) {
		String servCode = null;
		String  fileFrom="";
		if(StatusConstant.OPERATION_TYPE_INFO.equals(audit.getOperationType())){
			// TODO 督察督办
			SupervisionInfo supervisionInfo = this.supervisionInfoService.getById(audit.getServId());
			servCode = supervisionInfo.getServCode();
			fileFrom="3";
		} else if(StatusConstant.OPERATION_TYPE_PLAN.equals(audit.getOperationType())){
			fileFrom="6";
			// TODO 上报计划
//			Long servId = this.supervisionUpPlanService.getById(audit.getServId()).getServId();
			SupervisionInfo supervisionInfo = this.supervisionInfoService.getById(audit.getReportId());
			servCode = supervisionInfo.getServCode();
		} else if(StatusConstant.OPERATION_TYPE_REPORT.equals(audit.getOperationType())){
			fileFrom="7";
			// TODO 项目汇报
			servCode = this.supervisionPhaseReportService.getById(audit.getServId()).getServCode();
		} else if(StatusConstant.OPERATION_TYPE_REPORT_CHI.equals(audit.getOperationType())){
			fileFrom="audit_chi";
			// TODO 项目分派汇报
			servCode = this.supervisionPhaseReportChiService.getById(audit.getServId()).getServCode();
		} else if(StatusConstant.OPERATION_TYPE_REPORT_ALL.equals(audit.getOperationType())){
			fileFrom="audit_all";
			// TODO 项目汇总
			servCode = this.supervisionPhaseReportAllService.getById(audit.getServId()).getServCode();
		} else if(StatusConstant.OPERATION_TYPE_REPORT_ALT.equals(audit.getOperationType())){
			fileFrom="audit_alt";
			// TODO 项目分派汇总
			servCode = this.supervisionPhaseReportAltService.getById(audit.getServId()).getServCode();
		} else if(StatusConstant.OPERATION_TYPE_WARE.equals(audit.getOperationType())){
			fileFrom="8";
			// TODO 项目送审
			servCode = audit.getServId().toString();
		}
		User user = UserCache.getUser(AuthUtil.getUserId());
		if(files!=null && files.size()>0 && StringUtils.isNotEmpty(servCode)){
			for(int i=0;i<files.size();i++){
				JSONObject file = files.getJSONObject(i);
				String fileUrl = file.getString("fileUrl");
				String fileName = file.getString("fileName");
				if(StringUtils.isNotEmpty(fileUrl) && StringUtils.isNotEmpty(fileName)){
					SupervisionFiles supervisionFiles = new SupervisionFiles();
					supervisionFiles.setFileUrl(fileUrl);
					supervisionFiles.setFileName(fileName);
					supervisionFiles.setFileFrom(fileFrom);
					supervisionFiles.setUploadUser(user.getId()+"");
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					supervisionFiles.setUploadUserName(userNameDecrypt);
					supervisionFiles.setUploadTime(new Date());
					supervisionFiles.setServCode(servCode);
					this.supervisionFilesService.save(supervisionFiles);
				}
			}
		}
	}

	/**
	 * 我的审核列表
	 * @param userId		用户id
	 * @param operationType	业务类型 info——督察督办；plan——上报计划；report——项目汇报 apprise——考核汇报
	 * @return
	 */
	@GetMapping("/myAuditList")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "我的审核列表", notes = "我的审核列表")
	public R<List<SupervisionSubmitAuditVO>> myTaskList(@RequestParam String userId,@RequestParam(required = false) String operationType){
		QueryWrapper<SupervisionSubmitAudit> queryWrapper = new QueryWrapper<>(new SupervisionSubmitAudit()).eq("status",StatusConstant.AUDIT_STATUS_0).eq("user_id",userId);
		if(StringUtils.isNotEmpty(operationType)){
			queryWrapper.in("operation_type",Arrays.asList(operationType.split(",")));
		}
		queryWrapper.orderByDesc("create_time");
		List<SupervisionSubmitAudit> audits = this.supervisionSubmitAuditService.list(queryWrapper);
		return R.data(SupervisionSubmitAuditWrapper.build().listVO(audits));
	}

	/**
	 * 发送督察督办消息
	 * @param id		业务主键
	 * @param userId    送审人id
	 * @param status	审核状态
	 */
	private void saveSupervisionInfoMessage(String id,Long userId, String status){

		SupervisionInfo info = this.supervisionInfoService.getById(id);
		User userObj = UserCache.getUser(AuthUtil.getUserId());

		if(StatusConstant.AUDIT_STATUS_2.equals(status)){
			status = "退回";
		}else{
			status = "通过";
		}
		String title = String.format("送审%s",status);
		String content = String.format("【%s】%s【%s】事项审核。", userObj.getRealName(),status,info.getServName());
		String value = DictBizCache.getValue(info.getServTypeOne(), info.getServTypeTwo());

		String users =null;
		if(StringUtils.isNotEmpty(users)){
			String[] userArr = users.split(",");
			for(String user : userArr){
				//web消息
				this.messageService.saveMessageInfo(id,title,StatusConstant.WEB_MSG_TYPE_15,StatusConstant.MSG_PLATFORM_WEB,user,null,content,null);
				//app消息
				this.messageService.saveMessageInfo(id,title,StatusConstant.APP_MSG_TYPE_9,StatusConstant.MSG_PLATFORM_APP,user,StatusConstant.APP_TWO_MSG_TYPE_2,content,value);
			}
		}
	}

	/**
	 * 记录日志
	 * @param id		业务主键
	 */
	private void saveSupervisionInfoLog(Long id){
		SupervisionInfo info = this.supervisionInfoService.getById(id);
		//任务日志
		User user = UserCache.getUser(AuthUtil.getUserId());
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("11");
		log.setOperationTime(new Date());
		log.setContent("事项【"+info.getServName()+"】已审核");
		supervisionLogService.save(log);
	}

	/**
	 * 发送上报消息
	 * @param entity
	 */
	private void saveUpPlanMessage(SupervisionUpPlan entity) {
		SupervisionInfo info = supervisionInfoService.getById(entity.getServId());
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		User planUser = userClient.userInfoById(entity.getCreateUser()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		String planUserNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(planUser.getRealName());
		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		if(entity.getStatus()==1){
			content = "【"+planUserNameDecrypt+"】已提交【"+info.getServName()+"】阶段计划";
			msgType = "24";
			appType = "10";
			receiveUser = info.getCreateUser().toString();//督办单位
//			String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位
		}else if(entity.getStatus()==5){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段计划";
			msgType = "39";
			appType = "40";
			receiveUser = entity.getCreateUser().toString();
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段计划送审");
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
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);

		this.supervisionUpPlanService.updateById(entity);
	}

	/**
	 * 分派汇报消息
	 * @param report
	 */
	private void saveReportChiMessage(SupervisionPhaseReportChi report) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		if("1".equals(report.getReportStatus())){
			content = "【"+report.getReportUserName()+"】已提交【"+info.getServName()+"】阶段分派汇报";
			msgType = "68";
			appType = "68";
			receiveUser = report.getIssueUser();//分派人
		}else if("4".equals(report.getReportStatus())){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段分派汇报";
			receiveUser = report.getReportUser();//汇报人
			msgType = "70";
			appType= "70";
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

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
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);

		this.supervisionPhaseReportChiService.updateById(report);
	}

	/**
	 * 发送汇报消息
	 * @param report
	 */
	private void saveReportMessage(SupervisionPhaseReport report) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		if("1".equals(report.getReportStatus())||"7".equals(report.getReportStatus())){
			msgType = "19";
			appType = "5";
			if(ObjectUtil.isEmpty(info.getDutyUnit())&& ObjectUtil.isEmpty(report.getParentId())){
				if("1".equals(report.getReportStatus())){
					report.setReportStatus("2");
				}else{
					report.setReportStatus("8");
				}
				content = "【"+userClient.userInfoById(report.getCreateUser()).getData().getRealName()+"】上报【"+info.getServName()+"】阶段汇报";
				receiveUser = info.getCreateUser().toString();//督办单位

				SupervisionPhasePlan plan = supervisionPhasePlanService.getById(report.getPhaseId());
				if(ObjectUtil.isNotEmpty(plan)&&plan.getReportStatus().equals("0")){//阶段改为上报状态
					plan.setReportStatus("1");
				}
				supervisionPhasePlanService.updateById(plan);
			}else if( ObjectUtil.isNotEmpty(report.getParentId())){
				content = "【"+userClient.userInfoById(Long.parseLong(report.getReportUser())).getData().getRealName()+"】已上报【"+info.getServName()+"】阶段汇报";
				SupervisionPhaseReport re = supervisionPhaseReportService.getById(report.getParentId());
				receiveUser = re.getCreateUser().toString();
			}else{
				content = "【"+userClient.userInfoById(report.getCreateUser()).getData().getRealName()+"】已上报【"+info.getServName()+"】阶段汇报";
				String createUser = info.getCreateUser().toString();//督办单位
				String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位
				String dutyUnitUser = this.getUserIds(info.getDutyUnit());//责任单位

				receiveUser = createUser+","+leadUnitUser;//dutyUnitUser

				SupervisionPhaseReportAll supervisionPhaseReportAll = new SupervisionPhaseReportAll();
				supervisionPhaseReportAll.setPhaseId(report.getPhaseId());
				List<SupervisionPhaseReportAll> list = supervisionPhaseReportAllService.list(Condition.getQueryWrapper(supervisionPhaseReportAll));
				if(list!=null&&list.size()>0){
					if ("1".equals(report.getReportStatus())) {
						report.setReportStatus("2");
					} else {
						report.setReportStatus("8");
					}
				}

				if("1".equals(report.getDownStatus())){//未分派
					UnifyMessage message = new UnifyMessage();
					message.setMsgId(info.getId());
					message.setMsgTitle("阶段汇报");
					message.setMsgType(msgType);
					message.setMsgStatus(0);
					message.setMsgPlatform("web");
					message.setMsgIntro("【"+report.getReportUserName()+"】已上报【"+info.getServName()+"】阶段汇报");
					message.setCreateTime(new Date());
					message.setReceiveUser(quchong(dutyUnitUser));
					messageService.sendMessageInfo(message);

					String value = dictBizClient.getValue(info.getServTypeOne(), info.getServTypeTwo()).getData();
					message.setId(null);
					message.setMsgPlatform("app");
					message.setMsgType(Constants.DCDB_MAG_TYPE_APP_DB);
					message.setMsgSubitem(value);
					message.setTwoLevelType(appType);
					messageService.sendMessageInfo(message);
				}

				if (StringUtils.isEmpty(info.getDutyUnit())) {
					SupervisionPhasePlan plan = supervisionPhasePlanService.getById(report.getPhaseId());
					if(ObjectUtil.isNotEmpty(plan)&&plan.getReportStatus().equals("0")){//阶段改为上报状态
						plan.setReportStatus("1");
					}
					supervisionPhasePlanService.updateById(plan);
				}
			}
		}else if("4".equals(report.getReportStatus())||"10".equals(report.getReportStatus())){
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段汇报";
			receiveUser = report.getReportUser();
			msgType = "42";
			appType= "43";
		}

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
		message.setMsgTitle("阶段汇报");
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
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);

		this.supervisionPhaseReportService.updateById(report);
	}

	/**
	 * 分派汇总消息
	 * @param report
	 */
	private void saveReportAltMessage(SupervisionPhaseReportAlt report) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();

		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		if("1".equals(report.getReportStatus())){
			content = "【"+report.getReportUserName()+"】已提交【"+info.getServName()+"】阶段分派汇报";
			msgType = "68";
			appType = "68";
			receiveUser = report.getIssueUser();//分派人
		}else if("4".equals(report.getReportStatus())){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段分派汇报";
			receiveUser = report.getReportUser();//汇报人
			msgType = "70";
			appType= "70";
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

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
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);

		this.supervisionPhaseReportAltService.updateById(report);
	}

	/**
	 * 发送汇总消息
	 * @param report
	 */
	private void saveReportAllMessage(SupervisionPhaseReportAll report) {
		SupervisionInfo info = new SupervisionInfo();
		info.setServCode(report.getServCode());
		info = supervisionInfoService.getOne(Condition.getQueryWrapper(info));

		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		String content = "";
		String msgType = "";
		String appType = "";
		String receiveUser = "";

		if("2".equals(report.getReportStatus())){
			User u = userClient.userInfoById(report.getCreateUser()).getData();
			String uNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(u.getRealName());
			content = "【"+uNameDecrypt+"】已提交【"+info.getServName()+"】阶段汇总";
			msgType = "58";
			appType = "58";

			receiveUser = info.getCreateUser().toString();//督办单位
//			String leadUnitUser = this.getUserIds(info.getLeadUnit());//牵头单位

			SupervisionPhasePlan plan = supervisionPhasePlanService.getById(report.getPhaseId());
			if(ObjectUtil.isNotEmpty(plan)&&plan.getReportStatus().equals("0")){//阶段改为上报状态
				plan.setReportStatus("1");
			}
			supervisionPhasePlanService.updateById(plan);

			SupervisionPhaseReport report1 = new SupervisionPhaseReport();
			report1.setPhaseId(report.getPhaseId());
			report1.setReportStatus("1");
			SupervisionPhaseReport report2 = new SupervisionPhaseReport();
			report2.setReportStatus("2");
			supervisionPhaseReportService.update(report2,Condition.getQueryWrapper(report1));

			report1.setReportStatus("7");
			report2.setReportStatus("8");
			supervisionPhaseReportService.update(report2,Condition.getQueryWrapper(report1));
		}else if("4".equals(report.getReportStatus())){
			content = "【"+userNameDecrypt+"】审核不通过【"+info.getServName()+"】阶段汇总";
			receiveUser = report.getUpdateUser().toString();
			msgType = "60";
			appType= "60";
		}

		SupervisionLog log = new SupervisionLog();
		log.setServCode(info.getServCode());
		log.setOperationDept(user.getDeptId());
		log.setOperationDeptName(sysClient.getDeptName(Long.parseLong(user.getDeptId())).getData());
		log.setOperationUser(user.getId().toString());
		log.setOperationUserName(userNameDecrypt);
		log.setOperationType("3");
		log.setOperationTime(new Date());
		log.setContent(content);
		supervisionLogService.save(log);

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(info.getId());
		message.setMsgTitle("阶段汇总");
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
		message.setTwoLevelType(appType);
		messageService.sendMessageInfo(message);

		this.supervisionPhaseReportAllService.updateById(report);
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
