package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionUpPlan;
import com.vingsoft.vo.SupervisionUpPlanVO;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/4/18 11:56
* @Version:        1.0
*/
public interface ISupervisionUpPlanService extends BaseService<SupervisionUpPlan> {

	boolean saveAll(SupervisionUpPlanVO vo);

	boolean updateAll(SupervisionUpPlanVO vo);
}
