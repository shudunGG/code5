package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.*;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.mapper.StageInformationMapper;
import org.springblade.integrated.platform.service.IReportTimeService;
import org.springblade.integrated.platform.service.IStageInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-09 16:50
 */
@Service
public class StageInformationServiceImpl extends BaseServiceImpl<StageInformationMapper, StageInformation> implements IStageInformationService {

	@Autowired
	private IReportTimeService reportTimeService;

	@Override
	public boolean saveList(List<StageInformation> stageInformationList) {
		boolean flag=this.saveOrUpdateBatch(stageInformationList);
		if(flag){
			for(StageInformation sif:stageInformationList){
				//保存阶段汇报时间信息
				List<ReportTime> reportTimeList = sif.getReportTimeList();
				if(ObjectUtil.isNotEmpty(reportTimeList)){
					for(ReportTime rt : reportTimeList){
						rt.setStageInformationId(sif.getId());
					}
					flag=reportTimeService.saveOrUpdateBatch(reportTimeList);
				}
			}
		}
		return flag;
	}

	@Override
	public boolean uptList(List<StageInformation> stageInformationList) {
		boolean flag=this.saveOrUpdateBatch(stageInformationList);
		if(flag){
			List idsList = new ArrayList();
			for(StageInformation sif:stageInformationList){
				//获取汇报时间信息
				List<ReportTime> reportTimeList = sif.getReportTimeList();
				if(ObjectUtil.isNotEmpty(reportTimeList)){
					for(ReportTime rpt:reportTimeList){
						rpt.setStageInformationId(sif.getId());
						idsList.add(rpt.getId());
					}
				}
				//根据年度评价指标阶段id获取老的阶段汇报时间信息
				Map rptMap = new HashMap<>();
				rptMap.put("stage_information_id",sif.getId());
				List<ReportTime> oldReportTimeList = reportTimeService.listByMap(rptMap);
				if(ObjectUtil.isNotEmpty(oldReportTimeList)){
					for(ReportTime oldRpt:oldReportTimeList){
						if(!idsList.contains(oldRpt.getId())){//不包含就删除
							reportTimeService.removeById(oldRpt.getId());
						}
					}
				}
				flag=reportTimeService.saveOrUpdateBatch(reportTimeList);
			}
		}
		return flag;
	}

}
