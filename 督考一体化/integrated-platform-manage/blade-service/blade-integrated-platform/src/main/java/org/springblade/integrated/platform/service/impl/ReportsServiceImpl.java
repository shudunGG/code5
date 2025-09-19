package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.ReportsMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Post;
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

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-17 16:28
 */
@Service
public class ReportsServiceImpl extends BaseServiceImpl<ReportsMapper, Reports> implements IReportsService {

	@Autowired
	IAppriseFilesService appriseFilesService;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Autowired
	private IAnnualEvaluationService annualEvaluationService;

	@Autowired
	private IQuarterlyEvaluationService iQuarterlyEvaluationService;

	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserSearchClient iUserSearchClient;

	@Autowired
	private ReportsBaseinfoService reportsBaseinfoService;

	@Resource
	private IDictBizClient dictBizClient;
	@Autowired
	private ISupervisionSubmitAuditService supervisionSubmitAuditService;
	@Autowired
	private  IUserClient userClient;


	@Override
	@Transactional(rollbackFor = Exception.class)
	public  boolean saveReports(Reports rp){
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		QueryWrapper<ReportsBaseinfo> queryWrapperInfo = new QueryWrapper<>();
		queryWrapperInfo.eq("stage_id",rp.getStageInformationId());
		queryWrapperInfo.eq("dept_id",rp.getDeptId());

		//获取汇报基本信息表的主键id
		ReportsBaseinfo rb  =reportsBaseinfoService.getOne(queryWrapperInfo);
		rb.setReportStatus("3");//汇报状态 3 已汇报
		reportsBaseinfoService.updateById(rb);
		rp.setBaseid(rb.getId());
		boolean flag = false;
		if (StringUtils.isNotNull(rp.getId())) {
			Reports reports = this.getById(rp.getId());
			if (reports != null) {
				flag = this.updateById(rp);
			}
		} else {
			flag = this.save(rp);
		}

		if(flag){
			//保存附件信息
			List<AppriseFiles> appriseFilesList = rp.getAppriseFilesList();
			if(ObjectUtil.isNotEmpty(appriseFilesList)){
				for(AppriseFiles af:appriseFilesList){
					af.setFileFrom("PC端");
					af.setBusinessId(rp.getId());
					af.setBusinessTable("Reports");
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(currentUser.getRealName());
					af.setUploadUserName(userNameDecrypt);
				}
			}
			appriseFilesService.saveOrUpdateBatch(appriseFilesList);
		}
		String evaluationType = rp.getEvaluationType();
		Long evaluationId = rp.getEvaluationId();
		String name="";
		String pjdwid="";//评价单位id
		String majorTarget="";//评价要点
		R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
		String glyId=rgly.getData();
		String msgType="";//2季度评价汇报 9 年度评价汇报
		String receiver="";//接收人
		String twoLevelType="";//消息二级分类
		String msgSubmit="";
		if(evaluationType.equals("1")){//年度汇报送审
			name="年度";
			AnnualEvaluation ae = annualEvaluationService.getById(evaluationId);
			msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			pjdwid=ae.getAppraiseDeptid();
			majorTarget=ae.getMajorTarget();
			msgType="9";
			twoLevelType="13";
			if("2".equals(rp.getReportStatus())){
				//送审
				this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(rp.getId()),rp.getTitle(),rp.getUserIds(),rp.getSync(), StatusConstant.OPERATION_TYPE_ANNUALAPPRISEHB);
				String title1 = "送审年度评价阶段数据-汇报";
				String businessId = String.valueOf(rp.getId());
				String businessTable = "Reports";
				int businessType = BusinessType.UPDATE.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

				//发送消息
				String msgSubmit1=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
				BladeUser user = AuthUtil.getUser();
				String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
				String msgIntro = "【"+deptName+"】送审了年度评价指标汇报信息：【"+ae.getMajorTarget()+"】";

				UnifyMessage unifyMessage = new UnifyMessage();
				unifyMessage.setMsgId(rp.getEvaluationId());//消息主键（业务主键）
				unifyMessage.setMsgTitle("年度汇报送审");//消息标题
				unifyMessage.setMsgType("10");//消息类型，字典编码：web_message_type
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(rp.getUserIds());
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem(msgSubmit1);//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);

				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
				unifyMessage.setTwoLevelType("21");//年度汇报送审
				unifyMessageService.sendMessageInfo(unifyMessage);
			}
		}else if(evaluationType.equals("2")){//季度
			name="季度";
			QuarterlyEvaluation qe =iQuarterlyEvaluationService.getById(evaluationId);
			msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			pjdwid=qe.getAppraiseDeptid();
			msgType="2";
			twoLevelType="20";

			if("2".equals(rp.getReportStatus())){
				//送审
				this.supervisionSubmitAuditService.saveSubmitAudit(String.valueOf(rp.getId()),rp.getTitle(),rp.getUserIds(),rp.getSync(), StatusConstant.OPERATION_TYPE_QUARTERAPPRISEHB);
				String title1 = "送审季度评价-汇报";
				String businessId = String.valueOf(rb.getId());
				String businessTable = "Reports";
				int businessType = BusinessType.OTHER.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title1,businessId,businessTable,businessType);

				//发送消息
				String msgSubmit1=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
				BladeUser user = AuthUtil.getUser();
				String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
				String msgIntro="";
				if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
					majorTarget=qe.getMajorTarget();
					msgIntro = "【"+deptName+"】送审了季度评价指标汇报信息：【"+qe.getMajorTarget()+"】";
				}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
					majorTarget=qe.getFirstTarget();
					msgIntro = "【"+deptName+"】送审了季度评价指标汇报信息：【"+qe.getFirstTarget()+"】";
				} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
					majorTarget=qe.getTwoTarget();
					msgIntro = "【"+deptName+"】送审了季度评价指标汇报信息：【"+qe.getTwoTarget()+"】";
				}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
					majorTarget=qe.getImportWork();
					msgIntro = "【"+deptName+"】送审了季度评价指标汇报信息：【"+qe.getImportWork()+"】";
				} else {
					msgIntro = "【"+deptName+"】送审了季度评价指标汇报信息";
				}

				UnifyMessage unifyMessage = new UnifyMessage();
				unifyMessage.setMsgId(rp.getEvaluationId());//消息主键（业务主键）
				unifyMessage.setMsgTitle("季度汇报送审");//消息标题
				unifyMessage.setMsgType("3");//消息类型，字典编码：web_message_type
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(rp.getUserIds());
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem(msgSubmit1);//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);

				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
				unifyMessage.setTwoLevelType("14");//季度汇报送审
				unifyMessageService.sendMessageInfo(unifyMessage);
			}
		}
		if(StringUtils.isNotBlank(pjdwid)){
			String[] ids = pjdwid.split(",");
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
				String msgIntro="";
				if(StringUtils.isNotBlank(majorTarget)){
					msgIntro= "【"+deptName+"】汇报了"+name+"评价指标：【"+majorTarget+"】";
				}else{
					msgIntro= "【"+deptName+"】汇报了"+name+"评价指标";
				}

				UnifyMessage unifyMessage = new UnifyMessage();
				unifyMessage.setMsgId(Long.valueOf(evaluationId));//消息主键（业务主键）
				unifyMessage.setMsgTitle(name+"评价汇报");//消息标题
				unifyMessage.setMsgType(msgType);//消息类型，字典编码：web_message_type
				unifyMessage.setMsgPlatform("web");//平台：web或app
				unifyMessage.setReceiveUser(receiver);
				unifyMessage.setMsgIntro(msgIntro);//消息简介
				unifyMessage.setMsgSubitem(msgSubmit);//消息分项
				unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
				unifyMessage.setCreateTime(new Date());
				unifyMessageService.sendMessageInfo(unifyMessage);

				unifyMessage.setId(null);
				unifyMessage.setMsgPlatform("app");
				unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_HB);
				unifyMessage.setTwoLevelType(twoLevelType);//汇报
				unifyMessageService.sendMessageInfo(unifyMessage);
			}
		}
		return  flag;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean uptReports(Reports rp) {
		boolean flag = this.saveOrUpdate(rp);//更新汇报信息
		if(flag){
			List idsList = new ArrayList();
			//获取附件
			List<AppriseFiles> appriseFilesList = rp.getAppriseFilesList();
			if(ObjectUtil.isNotEmpty(appriseFilesList)){
				for(AppriseFiles af:appriseFilesList){
					af.setBusinessId(rp.getId());
					af.setBusinessTable("Reports");
					idsList.add(af.getId());
				}
			}

			//根据汇报信息获取之前的附件信息
			Map fileMap = new HashMap<>();
			fileMap.put("business_id",rp.getId());
			fileMap.put("business_table","Reports");
			List<AppriseFiles> oldAppriseFilesList = appriseFilesService.listByMap(fileMap);
			if(ObjectUtil.isNotEmpty(oldAppriseFilesList)){
				for(AppriseFiles oldAf:oldAppriseFilesList){
					if(!idsList.contains(oldAf.getId())){//不包含就删除
						appriseFilesService.removeById(oldAf.getId());
					}
				}
				//批量更新附件信息
				flag = appriseFilesService.saveOrUpdateBatch(appriseFilesList);
			}
		}
		return flag;
	}
}
