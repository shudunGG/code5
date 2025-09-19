package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.LeaderApprise;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;

import java.util.List;

/**
 *  服务类
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:35
 */
public interface ILeaderAppriseService extends BaseService<LeaderApprise> {

	/**
	 * 按条件查询LeaderApprise表中的数据
	 * @param leaderApprise
	 * @return
	 */
	LeaderApprise selectDetail(LeaderApprise leaderApprise);



	/**
	 * 导出领导评价数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<LeaderAppriseExcel> exportLeaderApprise(Wrapper<LeaderApprise> queryWrapper);


}
