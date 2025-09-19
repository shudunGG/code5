package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionPhaseReport;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.secure.BladeUser;

import java.util.List;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionPhaseReportService extends BaseService<SupervisionPhaseReport> {

	boolean updateAll(SupervisionPhaseReport report, boolean isLeadUnit);

	List<SupervisionPhaseReport> phaseOverdue();

	boolean issueByCrossDept(String serCode,String deptIds, BladeUser user);
}
