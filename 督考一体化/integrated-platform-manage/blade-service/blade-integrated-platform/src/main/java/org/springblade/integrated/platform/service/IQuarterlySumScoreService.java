package org.springblade.integrated.platform.service;

import com.vingsoft.entity.QuarterlySumScore;
import org.springblade.core.mp.base.BaseService;

import java.util.List;
import java.util.Map;


/**
 * 服务类
 *
 * @Author JG🧸
 * @Create 2022/4/19 17:30
 */
public interface IQuarterlySumScoreService extends BaseService<QuarterlySumScore> {


	QuarterlySumScore  avgQuarterlySumScore(Map<String, Object> map);

    Map<String, Object> getTotalWeight(String responsibleUnitId,String stageYear,String stage, String jdzbType);
}
