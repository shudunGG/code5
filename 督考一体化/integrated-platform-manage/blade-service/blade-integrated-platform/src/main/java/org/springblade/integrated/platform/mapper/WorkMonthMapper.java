package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.IPage;
import com.vingsoft.entity.LeaderApprise;
import com.vingsoft.entity.WorkMonth;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;
import org.springblade.integrated.platform.excel.WorkMonthExcel;

import java.util.List;
import java.util.Map;

/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
public interface WorkMonthMapper extends BaseMapper<WorkMonth> {

	/**
	 * 导出领导评价数据
	 *
	 * @param
	 * @return
	 */
	List<WorkMonthExcel> exportWorkMonth(@Param("qw") Wrapper<WorkMonth> queryWrapper);

	List<Map<String, Object>> workListPage(@Param("month") String month);

	List<Map<String, Object>> selectTime(@Param("month")String month, @Param("jhqk")String jhqk,@Param("deptCode")String deptCode);


	List<WorkMonth> detail(@Param("month")String month, @Param("deptCode")String deptCode);

}
