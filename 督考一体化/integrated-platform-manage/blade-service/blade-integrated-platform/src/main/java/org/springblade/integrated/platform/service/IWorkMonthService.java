package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.pagehelper.IPage;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.WorkMonth;
import org.apache.ibatis.annotations.Param;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;
import org.springblade.integrated.platform.excel.WorkMonthExcel;

import java.util.List;
import java.util.Map;

/**
 *  服务类
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:35
 */
public interface IWorkMonthService extends BaseService<WorkMonth> {

	/**
	 * 按条件查询LeaderApprise表中的数据
	 * @param leaderApprise
	 * @return
	 */
//	LeaderApprise selectDetail(LeaderApprise leaderApprise);



	/**
	 * 导出领导评价数据
	 *
	 * @param
	 * @return
	 */
	List<WorkMonthExcel> exportWorkMonth(Wrapper<WorkMonth> queryWrapper);


	List<Map<String, Object>> workListPage(String month);


	List<Map<String, Object>> selectTime(String month, String jhqk,String deptCode);

	List<WorkMonth> detail(String month, String deptCode);

	void importWorkMonth(List<WorkMonthExcel> data);
}
