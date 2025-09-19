package org.springblade.integrated.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vingsoft.entity.FollowInformation;
import com.vingsoft.vo.FollowProjectInformationVO;
import com.vingsoft.vo.MapPorjectVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * Mapper 接口
 *
 * @Author Adam
 * @Create 2022-4-9 18:03:05
 */
public interface FollowInformationMapper extends BaseMapper<FollowInformation> {

	IPage<FollowProjectInformationVO> getProjectFollow(IPage<MapPorjectVO> page, @Param("entity") Map<String, Object> entity);
}
