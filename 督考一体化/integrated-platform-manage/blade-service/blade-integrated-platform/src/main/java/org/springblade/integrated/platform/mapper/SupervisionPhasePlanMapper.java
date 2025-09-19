package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionPhasePlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionPhasePlanMapper extends BaseMapper<SupervisionPhasePlan> {

	List<SupervisionPhasePlan> queryList(@Param("ew") QueryWrapper<SupervisionPhasePlan> ew, @Param("deptId") String deptId);

	List<SupervisionPhasePlan> queryListHB(@Param("ew") QueryWrapper<SupervisionPhasePlan> ew);

	List<SupervisionPhasePlan> queryListHBAll(@Param("ew") QueryWrapper<SupervisionPhasePlan> ew, @Param("deptId") String deptId);
}
