package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionPhaseRemind;
import org.springblade.core.mp.base.BaseService;

import java.util.List;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionPhaseRemindService extends BaseService<SupervisionPhaseRemind> {

	/**
	 * 更新阶段信息
	 * @param supervisionPhaseRemindList
	 * @param servCode 事项编号
	 * @return
	 */
	boolean updateList(List<SupervisionPhaseRemind> supervisionPhaseRemindList, String servCode,String phaseCode);
}
