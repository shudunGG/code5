package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.FollowInformation;
import com.vingsoft.vo.FollowProjectInformationVO;
import com.vingsoft.vo.MapPorjectVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.FollowInformationMapper;
import org.springblade.integrated.platform.service.IFollowInformationService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class FollowInformationServiceImpl extends BaseServiceImpl<FollowInformationMapper, FollowInformation> implements IFollowInformationService {

	@Override
	public IPage<FollowProjectInformationVO> getProjectFollow(IPage<MapPorjectVO> page, Map<String, Object> entity) {
		return baseMapper.getProjectFollow(page, entity);
	}

}
