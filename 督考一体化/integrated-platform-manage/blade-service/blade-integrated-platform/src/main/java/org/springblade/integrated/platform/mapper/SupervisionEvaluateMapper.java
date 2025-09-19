package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.vo.SupervisionEvaluateVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SupervisionEvaluateMapper extends BaseMapper<SupervisionEvaluate> {


	List<SupervisionEvaluateVo> queryDcdbList(@Param("year") String year,@Param("ids") List<String> ids);
}
