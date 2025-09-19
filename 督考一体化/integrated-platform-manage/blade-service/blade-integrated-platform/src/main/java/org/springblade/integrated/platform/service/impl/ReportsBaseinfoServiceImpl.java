package org.springblade.integrated.platform.service.impl;

import com.vingsoft.entity.*;
import com.vingsoft.vo.ReportsBaseinfoVo;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.excel.ZzsxjsExcel;
import org.springblade.integrated.platform.mapper.ReportsBaseinfoMapper;
import org.springblade.integrated.platform.service.IStageInformationService;
import org.springblade.integrated.platform.service.ReportsBaseinfoService;
import org.springblade.system.feign.ISysClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-20 10:24
 */
@Service
public class ReportsBaseinfoServiceImpl extends BaseServiceImpl<ReportsBaseinfoMapper, ReportsBaseinfo> implements ReportsBaseinfoService {

	@Autowired
	private IStageInformationService stageInformationService;

	@Autowired
	private ISysClient iSysClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveForQuarter(QuarterlyEvaluation qe) {
		//获取考核对象
		String khdx = qe.getCheckObjectId();
		//根据评价指标id获取阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("evaluation_id",qe.getId());
		List<StageInformation> stageList = stageInformationService.listByMap(stageMap);
		String[] khdxStr = khdx.split(",");
		if(ObjectUtil.isNotEmpty(stageList)){
			for(StageInformation sif : stageList){//循环阶段
				for(String deptid : khdxStr){//循环考核对象
					ReportsBaseinfo rb = new ReportsBaseinfo();
					rb.setEvaluationId(qe.getId());
					rb.setEvaluationType("2");
					rb.setStage(sif.getStage());
					rb.setStageId(sif.getId());
					rb.setDeptId(Long.parseLong(deptid));
					//获取单位名称
					R<String> result =iSysClient.getDeptName(Long.parseLong(deptid));
					if(result.isSuccess()){
						rb.setDeptName(result.getData());
					}
					rb.setReportStatus("1");
					this.save(rb);
				}
			}
		}
	}

	@Override
	public void saveForQuarter(QuarterlyAssessment qe) {
		//获取考核对象
		String khdx = qe.getCheckObjectId();
		//根据评价指标id获取阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("evaluation_id",qe.getId());
		List<StageInformation> stageList = stageInformationService.listByMap(stageMap);
		String[] khdxStr = khdx.split(",");
		if(ObjectUtil.isNotEmpty(stageList)){
			for(StageInformation sif : stageList){//循环阶段
				for(String deptid : khdxStr){//循环考核对象
					ReportsBaseinfo rb = new ReportsBaseinfo();
					rb.setEvaluationId(qe.getId());
					rb.setEvaluationType("2");
					rb.setStage(sif.getStage());
					rb.setStageId(sif.getId());
					rb.setDeptId(Long.parseLong(deptid));
					//获取单位名称
					R<String> result =iSysClient.getDeptName(Long.parseLong(deptid));
					if(result.isSuccess()){
						rb.setDeptName(result.getData());
					}
					rb.setReportStatus("1");
					this.save(rb);
				}
			}
		}

	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveForAnnual(AnnualEvaluation ae) {
		//获取考核对象
		String khdx = ae.getAppraiseObjectId();
		//根据评价指标id获取阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("evaluation_id",ae.getId());
		List<StageInformation> stageList = stageInformationService.listByMap(stageMap);
		String[] khdxStr = khdx.split(",");
		if(ObjectUtil.isNotEmpty(stageList)){
			for(StageInformation sif : stageList){//循环阶段
				for(String deptid : khdxStr){//循环考核对象
					ReportsBaseinfo rb = new ReportsBaseinfo();
					rb.setEvaluationId(ae.getId());
					rb.setEvaluationType("1");
					rb.setStage(sif.getStage());
					rb.setStageId(sif.getId());
					rb.setDeptId(Long.parseLong(deptid.trim()));
					//获取单位名称
					R<String> result =iSysClient.getDeptName(Long.parseLong(deptid.trim()));
					if(result.isSuccess()){
						rb.setDeptName(result.getData());
					}
					rb.setReportStatus("1");
					this.save(rb);
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveForAnnualAssessment(AnnualAssessment ae) {
		//获取考核对象
		String khdx = ae.getAppraiseObjectId();
		//根据评价指标id获取阶段信息
		Map stageMap = new HashMap<>();
		stageMap.put("assessment_id",ae.getId());
		List<StageInformation> stageList = stageInformationService.listByMap(stageMap);
		String[] khdxStr = khdx.split(",");
		if(ObjectUtil.isNotEmpty(stageList)){
			for(StageInformation sif : stageList){//循环阶段
				for(String deptid : khdxStr){//循环考核对象
					ReportsBaseinfo rb = new ReportsBaseinfo();
					rb.setEvaluationId(ae.getId());
					rb.setEvaluationType("1");
					rb.setStage(sif.getStage());
					rb.setStageId(sif.getId());
					rb.setDeptId(Long.parseLong(deptid.trim()));
					//获取单位名称
					R<String> result =iSysClient.getDeptName(Long.parseLong(deptid.trim()));
					if(result.isSuccess()){
						rb.setDeptName(result.getData());
					}
					rb.setReportStatus("1");
					this.save(rb);
				}
			}
		}
	}



	@Override
	public List<ReportsBaseinfoVo> findList(ReportsBaseinfo reportsBaseinfo){
		List<ReportsBaseinfoVo> rpList = baseMapper.findList(reportsBaseinfo);
		return rpList;
	}
	@Override
	public List<ReportsBaseinfoVo> findListAnnual(ReportsBaseinfo reportsBaseinfo){
		List<ReportsBaseinfoVo> rpList = baseMapper.findListAnnual(reportsBaseinfo);
		return rpList;
	}
}
