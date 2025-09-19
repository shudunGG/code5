package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.*;
import com.vingsoft.vo.ReportsBaseinfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.excel.ZzsxjsExcel;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-19 22:11
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("ReportsBaseinfo")
@Api(value = "汇报基本信息", tags = "汇报基本信息控制层代码")
public class ReportsBaseinfoController {

	private final ReportsBaseinfoService reportsBaseinfoService;
	private final IAppriseFilesService iAppriseFilesService;
	private final IMessageInformationService iMessageInformationService;

	private final IQuarterlySumScoreService iQuarterlySumScoreService;
	private final IAnnualSumScoreService iAnnualSumScoreService;
	@Resource
	private final  IUserClient userClient;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
	 * 季度单位阶段汇报信息
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "季度单位阶段汇报信息", notes = "")
	public R<List<ReportsBaseinfoVo>> list(ReportsBaseinfo reportsBaseinfo) {
		List<ReportsBaseinfoVo> list = reportsBaseinfoService.findList(reportsBaseinfo);
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		for (ReportsBaseinfoVo reportsBaseinfoVo : list) {
			LambdaQueryWrapper<QuarterlySumScore> lambdaQueryWrapper = Wrappers.<QuarterlySumScore>query().lambda()
				.eq(QuarterlySumScore::getAppraiseDeptid,currentUser.getDeptId())
				.eq(QuarterlySumScore::getResponsibleUnitId,reportsBaseinfoVo.getDeptId().toString())
				.eq(QuarterlySumScore::getQuarterlyEvaluationId,reportsBaseinfoVo.getEvaluationId())
				.eq(QuarterlySumScore::getStageId,reportsBaseinfoVo.getStageId().toString());
			List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(lambdaQueryWrapper);
			if (quarterlySumScoreList.size() > 0) {
				reportsBaseinfoVo.setIsApprise(1);
			}else {
				reportsBaseinfoVo.setIsApprise(0);
			}
			if (reportsBaseinfoVo.getReportsId() != null) {
				QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("business_id",Long.valueOf(reportsBaseinfoVo.getReportsId()));
				//queryWrapper.eq(StringUtils.isNotNull(reportsBaseinfoVo.getBusinessTable()),"business_table",reportsBaseinfo.getBusinessTable());
				List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapper);
				reportsBaseinfoVo.setAppriseFilesList(appriseFilesList);
			}
			QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<>();
			if (reportsBaseinfo.getChildId() != null) {
				queryWrapper.eq("child_id", reportsBaseinfo.getChildId());
			}else{
				queryWrapper.eq("business_id",list.get(0).getEvaluationId());
				queryWrapper.eq("business_table","QuarterlyEvaluation");
			}
			List<MessageInformation> messageInformationList = iMessageInformationService.list(queryWrapper);
			if(messageInformationList.size()>0){
				for(MessageInformation mif :messageInformationList){
					QueryWrapper<AppriseFiles> queryWrapperInfo = new QueryWrapper<>();
					queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessId()),"business_id", mif.getBusinessId());
					queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessTable()),"business_table",mif.getBusinessTable());
					List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapperInfo);
					mif.setAppriseFilesList(appriseFilesList);
				}
			}
			reportsBaseinfoVo.setMessageInformationList(messageInformationList);
		}

		return R.data(list);
	}

	/**
	 * 季度单位阶段汇报信息
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "季度单位阶段汇报信息", notes = "")
	public R listApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("季度单位阶段汇报信息-app",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());

			ReportsBaseinfo reportsBaseinfo = objectMapper.convertValue(jsonParams, ReportsBaseinfo.class);
			List<ReportsBaseinfoVo> list = reportsBaseinfoService.findList(reportsBaseinfo);
			User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
			for (ReportsBaseinfoVo reportsBaseinfoVo : list) {
				LambdaQueryWrapper<QuarterlySumScore> lambdaQueryWrapper = Wrappers.<QuarterlySumScore>query().lambda()
					.eq(QuarterlySumScore::getAppraiseDeptid,currentUser.getDeptId())
					.eq(QuarterlySumScore::getResponsibleUnitId,reportsBaseinfoVo.getDeptId().toString())
					.eq(QuarterlySumScore::getQuarterlyEvaluationId,reportsBaseinfoVo.getEvaluationId())
					.eq(QuarterlySumScore::getStageId,reportsBaseinfoVo.getStageId().toString());
				List<QuarterlySumScore> quarterlySumScoreList = iQuarterlySumScoreService.list(lambdaQueryWrapper);
				if (quarterlySumScoreList.size() > 0) {
					reportsBaseinfoVo.setIsApprise(1);
				}else {
					reportsBaseinfoVo.setIsApprise(0);
				}
				if (reportsBaseinfoVo.getReportsId() != null) {
					QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<>();
					queryWrapper.eq("business_id",Long.valueOf(reportsBaseinfoVo.getReportsId()));
					List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapper);
					reportsBaseinfoVo.setAppriseFilesList(appriseFilesList);
				}
				QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<>();
				if (reportsBaseinfo.getChildId() != null) {
					queryWrapper.eq("child_id", reportsBaseinfo.getChildId());
				}else{
					queryWrapper.eq("business_id",list.get(0).getEvaluationId());
					queryWrapper.eq("business_table","QuarterlyEvaluation");
				}
				List<MessageInformation> messageInformationList = iMessageInformationService.list(queryWrapper);
				if(messageInformationList.size()>0){
					for(MessageInformation mif :messageInformationList){
						QueryWrapper<AppriseFiles> queryWrapperInfo = new QueryWrapper<>();
						queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessId()),"business_id", mif.getBusinessId());
						queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessTable()),"business_table",mif.getBusinessTable());
						List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapperInfo);
						mif.setAppriseFilesList(appriseFilesList);
					}
				}
				reportsBaseinfoVo.setMessageInformationList(messageInformationList);
			}
			JSONArray pageJson = objectMapper.convertValue(list, JSONArray.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 年度单位阶段汇报信息
	 * @return
	 */
	@GetMapping("/listAnnual")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "年度单位阶段汇报信息", notes = "")
	public R<List<ReportsBaseinfoVo>> listAnnual(ReportsBaseinfo reportsBaseinfo) {
		List<ReportsBaseinfoVo> list = reportsBaseinfoService.findListAnnual(reportsBaseinfo);
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();

		for (ReportsBaseinfoVo reportsBaseinfoVo : list) {
			LambdaQueryWrapper<AnnualSumScore> lambdaQueryWrapper = Wrappers.<AnnualSumScore>query().lambda()
				.eq(AnnualSumScore::getAppraiseDeptid,currentUser.getDeptId())
				.eq(AnnualSumScore::getResponsibleUnitId,reportsBaseinfoVo.getDeptId().toString())
				.eq(AnnualSumScore::getAnnualEvaluationId,reportsBaseinfoVo.getEvaluationId())
				.eq(AnnualSumScore::getStageId,reportsBaseinfoVo.getStageId().toString());
			List<AnnualSumScore> annualSumScoreList = iAnnualSumScoreService.list(lambdaQueryWrapper);
			if (annualSumScoreList.size() > 0) {
				reportsBaseinfoVo.setIsApprise(1);
			}else {
				reportsBaseinfoVo.setIsApprise(0);
			}
			if (reportsBaseinfoVo.getReportsId() != null) {
				QueryWrapper<AppriseFiles> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("business_id",Long.valueOf(reportsBaseinfoVo.getReportsId()));
				//queryWrapper.eq(StringUtils.isNotNull(reportsBaseinfoVo.getBusinessTable()),"business_table",reportsBaseinfo.getBusinessTable());
				List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapper);
				reportsBaseinfoVo.setAppriseFilesList(appriseFilesList);
			}
			QueryWrapper<MessageInformation> queryWrapper = new QueryWrapper<>();
			if (reportsBaseinfo.getChildId() != null) {
				queryWrapper.eq("child_id", reportsBaseinfo.getChildId());
			}else{
				queryWrapper.eq("business_id",list.get(0).getEvaluationId());
				queryWrapper.eq("business_table","AnnualEvaluation");
			}
			List<MessageInformation> messageInformationList = iMessageInformationService.list(queryWrapper);
			if(messageInformationList.size()>0){
				for(MessageInformation mif :messageInformationList){
					QueryWrapper<AppriseFiles> queryWrapperInfo = new QueryWrapper<>();
					queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessId()),"business_id", mif.getBusinessId());
					queryWrapperInfo.eq(StringUtils.isNotNull(mif.getBusinessTable()),"business_table",mif.getBusinessTable());
					List<AppriseFiles> appriseFilesList = iAppriseFilesService.list(queryWrapperInfo);
					mif.setAppriseFilesList(appriseFilesList);
				}
			}
			reportsBaseinfoVo.setMessageInformationList(messageInformationList);
		}

		return R.data(list);
	}

}
