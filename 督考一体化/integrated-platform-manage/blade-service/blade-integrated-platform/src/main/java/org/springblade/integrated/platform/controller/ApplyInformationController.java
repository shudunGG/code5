package org.springblade.integrated.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import com.vingsoft.vo.ReportsBaseinfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.IAnnualEvaluationService;
import org.springblade.integrated.platform.service.IApplyInformationService;
import org.springblade.integrated.platform.service.IQuarterlyEvaluationService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-21 12:02
 */

@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/applyInformation")
@Api(value = "指标申请", tags = "指标申请控制层代码")
public class ApplyInformationController extends BladeController {

	private final IApplyInformationService applyInformationService;

	private final IAnnualEvaluationService annualEvaluationService;

	private final IQuarterlyEvaluationService quarterlyEvaluationService;

	@Autowired
	private IUnifyMessageService unifyMessageService;

	@Autowired
	private  IUserClient userClient;


	/**
	 * 指标申请新增接口
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "保存", notes = "vo")
	public R save(@Valid @RequestBody ApplyInformation aif) throws ParseException {
		String receiveId=aif.getReceiveId();
		String receiveName="";
		if(StringUtils.isNotBlank(receiveId)){
			String[] receiveIdStr = receiveId.split(",");
			for(String id : receiveIdStr){
				User user = userClient.userInfoById(Long.valueOf(id)).getData();
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
				receiveName+=userNameDecrypt+",";
			}
		}
		aif.setReceiveName(receiveName);
		String applyType = aif.getApplyType();//申请类型 1申请办结 2申请中止
		String evaluationType= aif.getEvaluationType();
		String targetStatus ="0";//指标状态：0暂存 1推进中 2已完成 3申请办结 4申请中止（字典编码zb_status）
		String majorTarget = "";//主要指标及评价要点
		String msgIntro="";//消息简介
		String applyTypeName="";//申请类型名称
		String msgType="";//消息类型，字典编码：web_message_type
		if(applyType.equals("1")){
			targetStatus="3";
			applyTypeName="申请办结";
			if(evaluationType.equals("1")){
				msgType="13";
			}else{
				msgType="6";
			}
		}else if(applyType.equals("2")){
			targetStatus="4";
			applyTypeName="2申请中止";
			if(evaluationType.equals("1")){
				msgType="14";
			}else{
				msgType="7";
			}
		}
		boolean flag = applyInformationService.saveApply(aif);
		if(flag){//保存成功后更新指标表状态
			if(evaluationType.equals("1")){//年度
				AnnualEvaluation ae =annualEvaluationService.getById(aif.getEvaluationId());
				ae.setTargetStatus(targetStatus);
				annualEvaluationService.updateById(ae);
				majorTarget=ae.getMajorTarget();
				msgIntro=applyTypeName+"年度评价指标：";

				String title = "新增指标申请";
				String businessId = String.valueOf(aif.getEvaluationId());
				String businessTable = "AnnualEvaluation";
				int businessType = BusinessType.INSERT.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
			}else if(evaluationType.equals("2")){//季度
				QuarterlyEvaluation qe = quarterlyEvaluationService.getById(aif.getEvaluationId());
				qe.setTargetStatus(targetStatus);
				quarterlyEvaluationService.updateById(qe);
				majorTarget=qe.getMajorTarget();
				msgIntro=applyTypeName+"季度评价指标：";

				String title = "新增指标申请";
				String businessId = String.valueOf(aif.getEvaluationId());
				String businessTable = "QuarterlyEvaluation";
				int businessType = BusinessType.INSERT.ordinal();
				SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
			}
		}

		//发送消息
		BladeUser user = AuthUtil.getUser();
		String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
		msgIntro = deptName+msgIntro+majorTarget;
		UnifyMessage unifyMessage = new UnifyMessage();
		unifyMessage.setMsgId(Long.valueOf(aif.getEvaluationId()));//消息主键（业务主键）
		unifyMessage.setMsgTitle(aif.getEvaluationType().equals("1")?"年度指标申请":"季度指标申请");//消息标题
		unifyMessage.setMsgType(msgType);//消息类型，字典编码：web_message_type
		unifyMessage.setMsgPlatform("web");//平台：web或app
		unifyMessage.setReceiveUser(aif.getReceiveId());
		unifyMessage.setMsgIntro(msgIntro);//消息简介
		unifyMessage.setMsgSubitem(aif.getEvaluationType().equals("1")?"年度指标申请":"季度指标申请");//消息分项
		unifyMessage.setMsgStatus(StatusConstant.UNIFY_MESSAGE_0);
		unifyMessage.setCreateTime(new Date());
		unifyMessageService.sendMessageInfo(unifyMessage);



		return R.status(flag);
	}

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/details")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "详情", notes = "vo")
	public R<ApplyInformation> details(@Valid @RequestParam Long id) {
		String title = "查看指标申请详情";
		String businessId = String.valueOf(id);
		String businessTable = "ApplyInformation";
		int businessType = BusinessType.LOOK.ordinal();
		SpringUtil.getBean(IOperLogService.class).saveLog(title,businessId,businessTable,businessType);
		return R.data(applyInformationService.getById(id));
	}

	/**
	 * 分页查询
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "指标申请列表查询", notes = "")
	public R<IPage<ApplyInformation>> list(@ApiIgnore @RequestParam Map<String, Object> entity, Query query) {
		//sql查询条件
		String evaluationType = (String) entity.get("evaluationType");//年度/季度指标类型：1年度 2季度
		String evaluationId = (String) entity.get("evaluationId");//年度/季度指标id
		QueryWrapper<ApplyInformation> queryWrapper = new QueryWrapper<ApplyInformation>();
		queryWrapper= Condition.getQueryWrapper(entity, ApplyInformation.class);
		if(StringUtils.isNotBlank(evaluationType)){
			queryWrapper.eq("evaluation_type",evaluationType);
		}
		if(StringUtils.isNotBlank(evaluationId)){
			queryWrapper.eq("evaluation_id",evaluationId);
		}
		IPage<ApplyInformation> pages = applyInformationService.page(Condition.getPage(query), queryWrapper);
		return R.data(pages);
	}


}
