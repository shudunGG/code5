package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.LeaderApprise;
import org.apache.ibatis.annotations.Param;
import org.springblade.integrated.platform.excel.LeaderAppriseExcel;

import java.util.List;

/**
 * Mapper 接口
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:15
 */
public interface LeaderAppriseMapper extends BaseMapper<LeaderApprise> {

	/**
	 * 导出领导评价数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<LeaderAppriseExcel> exportLeaderApprise(@Param("qw") Wrapper<LeaderApprise> queryWrapper);


}
