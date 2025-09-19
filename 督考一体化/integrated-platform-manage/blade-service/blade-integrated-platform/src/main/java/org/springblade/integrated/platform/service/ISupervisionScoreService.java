package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.entity.SupervisionScore;
import com.vingsoft.vo.SupervisionDeptScoretVO;
import org.springblade.core.mp.base.BaseService;

import java.util.List;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionScoreService extends BaseService<SupervisionScore> {

	Boolean  calculationScore(SupervisionEvaluate supervisionEvaluate);

	List<SupervisionDeptScoretVO> deptScoreStatistics(String deptGroup, String supYear, String startTime,String endTime);

	List<SupervisionDeptScoretVO> deptScoreDetails(String deptGroup,Long deptId,String startTime,String endTime);

}
