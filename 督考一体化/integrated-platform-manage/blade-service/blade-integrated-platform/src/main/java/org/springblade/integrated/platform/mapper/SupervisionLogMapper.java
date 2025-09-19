package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionLogMapper extends BaseMapper<SupervisionLog> {

	List<SupervisionLog> listQueryWrapper(@Param("ew") QueryWrapper<SupervisionLog> ew,@Param("deptId") String deptId, @Param("userId")  String userId);

}
