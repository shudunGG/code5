package org.springblade.integrated.platform.service.impl;

import com.vingsoft.entity.QuarterlySumScore;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.QuarterlySumScoreMapper;
import org.springblade.integrated.platform.service.IQuarterlySumScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/9 17:30
 */
@Service
public class QuarterlySumScoreServiceImpl extends BaseServiceImpl<QuarterlySumScoreMapper, QuarterlySumScore> implements IQuarterlySumScoreService {


	@Override
	public QuarterlySumScore avgQuarterlySumScore(Map<String, Object> map) {
		return baseMapper.avgQuarterlySumScore(map);
	}

	@Override
	public Map<String, Object> getTotalWeight(String responsibleUnitId,String stageYear,String stage, String jdzbType) {
		return baseMapper.getTotalWeight(responsibleUnitId,stageYear,stage, jdzbType);
	}
}
