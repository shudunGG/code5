package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.vo.SupervisionDeptPlanReportVO;
import com.vingsoft.vo.SupervisionInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.system.user.entity.User;

import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionInfoService extends BaseService<SupervisionInfo> {


	/**
	 * 修改流程状态
	 * @param id
	 * @param status
	 * @return
	 */
	boolean updateFlowStatus(Long id,String status);

	/**
	 * 督查督办查询
	 * @param entity
	 * @return
	 */
	List<SupervisionInfo> queryList(Map<String, Object> entity);
	/**
	 * 督查督办查询
	 * @param entity
	 * @return
	 */
	PageInfo queryListPage(Query query,Map<String, Object> entity);


	/**
	 * 保存(多表)
	 * @param supervisionInfo
	 * @return
	 */
	boolean savebus(SupervisionInfo supervisionInfo,String userIds, String sync,String title);

	/**
	 * 保存(多表)
	 * @param list
	 * @return
	 */
	boolean saveAll(List<SupervisionInfo> list);

	/**
	 * 根据 事项ID 查询明细
	 * @param id
	 * @return
	 */
	SupervisionInfo details(Long id);


	SupervisionInfoVO detailsNew(Long servId,String servCode,String tbBus,BladeUser user);



	/**
	 * 更新(多表)
	 * @param supervisionInfo
	 * @return
	 */
	boolean updatebus(SupervisionInfo supervisionInfo,String userIds, String sync,String title);

	/**
	 * 删除(多表)
	 * @param ids
	 * @return
	 */
	boolean deletebus(String ids,String servCodes);

	/**
	 * 办结
	 * @return
	 */
	boolean finish(Long id,BladeUser user);

	/**
	 * 部门办结
	 * @return
	 */
	boolean finishDept(String  deptIds ,Long servId,BladeUser user);

	Map<String,Object> listStatistics(String servTypeThree,String deptId,String year);

	List<SupervisionInfo> listStatisticsdhb(Map<String, Object> entity  );
	List<SupervisionInfo> listStatisticsycq(Map<String, Object> entity  );


	Map<String,Object> mySupervision(Map<String, Object> entity, BladeUser user);


	PageInfo supervisionFollow(Query query, Map<String, Object> entity, BladeUser user);

	PageInfo supervisionMyFollow(Query query, Map<String, Object> entity, BladeUser user);

	List<SupervisionSign> supervisionNoSign(Map<String, Object> entity, BladeUser user);

	List<SupervisionInfo> supervisionNoReport(Map<String, Object> entity,BladeUser user);

	List<SupervisionInfo> supervisionOverdue(Map<String, Object> entity,BladeUser user);

	List<SupervisionDeptPlanReportVO> servDeptPlanReport(Map<String, Object> entity);

	List<User> getMember(Long id);

	/**
	 * 获取事项需要发送消息的人员
	 * @param id
	 * @param user
	 * @return
	 */
	String getMagUserTest(Long id,BladeUser user);

	/**
	 *
	 * @param id  事项ID
	 * @param userId 事项创建人
	 * @return
	 */
	String getMagUserTest(Long id,Long userId);

	boolean updateDuty(long id,String servCode,String dutyUnit,String dutyUnitName);

	boolean deletedept(@Param("ew") QueryWrapper<SupervisionSign> ew );

	PageInfo cbqueryListPage(Query query, Map<String, Object> entity);
}
