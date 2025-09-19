package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionLog;
import com.vingsoft.entity.SupervisionPhasePlan;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.secure.BladeUser;

import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionPhasePlanService extends BaseService<SupervisionPhasePlan> {

	List<SupervisionPhasePlan> queryList(Map<String, Object> entity);

	List<SupervisionPhasePlan> queryListHB(Map<String, Object> entity, BladeUser user);

	List<SupervisionPhasePlan> queryListHBAll(Map<String, Object> entity, BladeUser user);

	 boolean saveList(List<SupervisionPhasePlan> supervisionPhasePlanList);

	/**
	 * 根据事项编号获取事项阶段信息
	 * @param servCode
	 * @return
	 */
	 List<SupervisionPhasePlan> listByservCode(String servCode);

	/**
	 * 更新阶段信息
	 * @param supervisionPhasePlanList
	 * @param servCode 事项编号
	 * @return
	 */
	 boolean updateList(List<SupervisionPhasePlan> supervisionPhasePlanList,String servCode,SupervisionInfo supervisionInfo);
}
