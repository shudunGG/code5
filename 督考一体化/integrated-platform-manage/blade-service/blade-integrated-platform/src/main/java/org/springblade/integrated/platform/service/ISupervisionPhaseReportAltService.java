package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionPhaseReportAlt;
import org.springblade.core.mp.base.BaseService;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionPhaseReportAltService extends BaseService<SupervisionPhaseReportAlt> {

	boolean updateAll(SupervisionPhaseReportAlt report);

	boolean deleteAll(SupervisionPhaseReportAlt report);

	boolean delete(SupervisionPhaseReportAlt entity);
}
