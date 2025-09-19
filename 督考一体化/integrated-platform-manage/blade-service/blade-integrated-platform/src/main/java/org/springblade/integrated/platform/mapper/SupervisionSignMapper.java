package org.springblade.integrated.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.SupervisionSign;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/16 17:59
 */
public interface SupervisionSignMapper extends BaseMapper<SupervisionSign> {

	@InterceptorIgnore(tenantLine = "1")
	@Select("SELECT sign.*,info.wcsx from supervision_sign sign " +
			"LEFT JOIN supervision_info info on info.id=sign.serv_id  " +
			"where ${ew.sqlSegment} ")
	List<SupervisionSign> getOverdueNoSignList(@Param("ew") QueryWrapper<SupervisionSign> ew);
}
