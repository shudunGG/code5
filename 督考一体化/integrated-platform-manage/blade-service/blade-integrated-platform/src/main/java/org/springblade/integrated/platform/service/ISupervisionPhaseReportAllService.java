package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionPhaseReportAll;
import org.springblade.core.mp.base.BaseService;

import java.util.List;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionPhaseReportAllService extends BaseService<SupervisionPhaseReportAll> {

	boolean updateAll(SupervisionPhaseReportAll report);
}
