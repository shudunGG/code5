package org.springblade.plugin.data.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.plugin.data.entity.QualityTestingProgrammeRule;
import org.springblade.plugin.data.mapper.QualityTestingProgrammeRuleMapper;
import org.springblade.plugin.data.service.IQualityTestingProgrammeRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MaQiuyun
 * @date 2021/12/16 14:57
 * @description:
 */
@Service
public class QualityTestingProgrammeRuleServiceImpl extends BaseServiceImpl<QualityTestingProgrammeRuleMapper, QualityTestingProgrammeRule> implements IQualityTestingProgrammeRuleService {
	@Override
	public List<String> getRuleIdByProgrammeId(String programmeId) {
		return baseMapper.selectRuleIdByProgrammeId(programmeId);
	}

	@Override
	public boolean removeByProgrammeId(String programmeId) {
		return baseMapper.deleteByProgrammeId(programmeId) >= 0;
	}

	@Override
	public boolean removeByIds(String programmeId, String ruleId) {
		return baseMapper.deleteByIds(programmeId, ruleId) > 0;
	}
}
