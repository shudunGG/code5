package org.springblade.integrated.platform.service.impl;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.AnnualBaseInfoVO;
import com.vingsoft.vo.QuarterBaseInfoVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.AppriseBaseinfoMapper;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.IDictBizClient;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:评价基本信息
 * @date 2022-04-18 10:29
 */
@Service
public class AppriseBaseinfoServiceImpl extends BaseServiceImpl<AppriseBaseinfoMapper,AppriseBaseinfo> implements IAppriseBaseinfoService {
	@Autowired
	private IAppriseDeptService IAppriseDeptService;
	@Autowired
	private IUnifyMessageService unifyMessageService;
	@Resource
	private ISysClient sysClient;
	@Resource
	private IUserSearchClient iUserSearchClient;
	@Autowired
	private  IAnnualEvaluationService annualEvaluationService;
	@Autowired
	private  IQuarterlyEvaluationService quarterlyEvaluationService;
	@Resource
	private IDictBizClient dictBizClient;

	@Override
	public boolean saveApprise(AppriseBaseinfo absInfo) {
		boolean flag = false;
		Long createuser = absInfo.getCreateUser();
		Long createDept = absInfo.getCreateDept();
		User user = UserCache.getUser(createuser);
		String deptName= SysCache.getDeptName(createDept);
		R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
		String glyId=rgly.getData();
		String receiver="";
		//保存部门评价信息
		List<AppriseDept> appriseDeptList = absInfo.getAppriseDeptList();
			if(ObjectUtil.isNotEmpty(appriseDeptList)){
				for(AppriseDept ad:appriseDeptList){
					ad.setAppriseBaseinfoId(absInfo.getId());
					ad.setEvaluationId(absInfo.getEvaluationId());
					ad.setEvaluationType(absInfo.getEvaluationType());
					String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
					ad.setCreateUserName(userNameDecrypt);
					ad.setCreateDeptName(deptName);
					ad.setType("1");//百分制得分
				}
				flag= IAppriseDeptService.saveOrUpdateBatch(appriseDeptList);
		}
		String evaluationType= absInfo.getEvaluationType();
		String msgType="";
		String twoLevelType="";
		String majorTarget="";
		String msgSubmit="";
		if(evaluationType.equals("1")){//年度
			AnnualEvaluation ae =annualEvaluationService.getById(absInfo.getEvaluationId());
			msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			msgType="12";
			twoLevelType="23";
			majorTarget="年度评价指标："+ae.getMajorTarget();
		}else{//季度
			QuarterlyEvaluation qe = quarterlyEvaluationService.getById(absInfo.getEvaluationId());
			msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			msgType="5";
			twoLevelType="16";
			if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getMajorTarget()+"】";
			}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getFirstTarget()+"】";
			} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getTwoTarget()+"】";
			}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
				majorTarget="季度评价指标：【"+qe.getImportWork()+"】";
			} else {
				majorTarget="季度评价指标";
			}
		}
		//发送消息
		if(StringUtils.isNotBlank(receiver)){
			String msgIntro = "【"+deptName+"】评价了"+majorTarget;
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(absInfo.getEvaluationId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("部门评价保存");//消息标题
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
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType(twoLevelType);
			unifyMessageService.sendMessageInfo(unifyMessage);
		}


		return flag;
	}

	@Override
	public List<QuarterBaseInfoVO> QuarterBaseInfoList(QuarterBaseInfoVO quarterBaseInfoVO) {
		return baseMapper.QuarterBaseInfoList(quarterBaseInfoVO);
	}

	@Override
	public List<AnnualBaseInfoVO> AnnualBaseInfoList(AnnualBaseInfoVO appriseBaseInfoVO) {
		return baseMapper.AnnualBaseInfoList(appriseBaseInfoVO);
	}


	@Override
	public boolean updateApprise(AppriseBaseinfo absInfo) {
		//更新评价基本信息
		boolean flag=this.updateById(absInfo);
		Long createuser = absInfo.getCreateUser();
		Long createDept = absInfo.getCreateDept();
		User user = UserCache.getUser(createuser);
		String deptName= SysCache.getDeptName(createDept);
		R<String> rgly = sysClient.getPostIdsByFuzzy("000000","管理员");//获取管理员岗位id
		String glyId=rgly.getData();
		String receiver="";
		if(flag){
			//保存部门评价信息
			List<AppriseDept> appriseDeptList = absInfo.getAppriseDeptList();
			if(ObjectUtil.isNotEmpty(appriseDeptList)){
				for(AppriseDept ad:appriseDeptList){
					/*R<List<User>> ruser= iUserSearchClient.listByPostAndDept(glyId,ad.getResponsibleUnitId().toString());//获取单位下面所有管理员用户
					if(ruser!=null){
						List<User> userList = ruser.getData();
						for(User userInfo : userList){
							receiver+=userInfo.getId()+",";
						}
					}*/
				}
				flag= IAppriseDeptService.saveOrUpdateBatch(appriseDeptList);
			}
		}
		String evaluationType= absInfo.getEvaluationType();
		String msgType="";
		String twoLevelType="";
		String majorTarget="";
		String msgSubmit="";
		if(evaluationType.equals("1")){//年度
			AnnualEvaluation ae =annualEvaluationService.getById(absInfo.getEvaluationId());
			msgSubmit=dictBizClient.getValue("ndkp-type",ae.getType()).getData();
			msgType="12";
			twoLevelType="23";
			majorTarget="年度评价指标："+ae.getMajorTarget();
		}else{//季度
			QuarterlyEvaluation qe = quarterlyEvaluationService.getById(absInfo.getEvaluationId());
			msgSubmit=dictBizClient.getValue("jdpj-type",qe.getJdzbType()).getData();
			msgType="5";
			twoLevelType="16";
			if (qe.getMajorTarget() != null && qe.getMajorTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getMajorTarget()+"】";
			}else if (qe.getFirstTarget() != null && qe.getFirstTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getFirstTarget()+"】";
			} else if (qe.getTwoTarget() != null && qe.getTwoTarget()!="") {
				majorTarget="季度评价指标：【"+qe.getTwoTarget()+"】";
			}else if (qe.getImportWork()!= null && qe.getImportWork()!="") {
				majorTarget="季度评价指标：【"+qe.getImportWork()+"】";
			} else {
				majorTarget="季度评价指标";
			}
		}
		//发送消息
		if(StringUtils.isNotBlank(receiver)){
			String msgIntro = "【"+deptName+"】重新评价了"+majorTarget;
			UnifyMessage unifyMessage = new UnifyMessage();
			unifyMessage.setMsgId(absInfo.getEvaluationId());//消息主键（业务主键）
			unifyMessage.setMsgTitle("部门评价保存");//消息标题
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
			unifyMessage.setMsgType(Constants.MSG_TYPE_APP_ONE_QT);
			unifyMessage.setTwoLevelType(twoLevelType);
			unifyMessageService.sendMessageInfo(unifyMessage);
		}

		return flag;
	}
}
