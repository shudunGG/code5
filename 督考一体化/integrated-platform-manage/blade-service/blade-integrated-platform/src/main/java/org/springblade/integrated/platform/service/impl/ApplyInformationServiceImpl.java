package org.springblade.integrated.platform.service.impl;

import com.vingsoft.entity.AnnualEvaluation;
import com.vingsoft.entity.ApplyInformation;
import com.vingsoft.entity.AppriseFiles;
import com.vingsoft.entity.StageInformation;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.mapper.AnnualSumScoreMapper;
import org.springblade.integrated.platform.mapper.ApplyInformationMapper;
import org.springblade.integrated.platform.service.IApplyInformationService;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springblade.integrated.platform.service.ReportsBaseinfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-21 12:05
 */
@Service
public class ApplyInformationServiceImpl extends BaseServiceImpl<ApplyInformationMapper, ApplyInformation> implements IApplyInformationService {

	@Autowired
	private IAppriseFilesService appriseFilesService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveApply(ApplyInformation aif){
		boolean flag = this.save(aif);
		if(flag){
			//保存附件信息
			List<AppriseFiles> appriseFilesList = aif.getAppriseFilesList();
			if(ObjectUtil.isNotEmpty(appriseFilesList)){
				for(AppriseFiles af:appriseFilesList){
					af.setBusinessId(aif.getId());
					af.setBusinessTable("apply_information");
				}
			}
			appriseFilesService.saveOrUpdateBatch(appriseFilesList);
		}
		return flag;
	}
}
