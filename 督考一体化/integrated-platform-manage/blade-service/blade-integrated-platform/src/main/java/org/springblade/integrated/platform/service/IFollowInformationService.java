package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.FollowInformation;
import com.vingsoft.vo.FollowProjectInformationVO;
import com.vingsoft.vo.MapPorjectVO;
import org.springblade.core.mp.base.BaseService;

import java.util.Map;

/**
 *  服务类
 *
 * @Author Adam
 * @Create 2022-4-9 18:10:17
 */
public interface IFollowInformationService extends BaseService<FollowInformation> {
	IPage<FollowProjectInformationVO> getProjectFollow(IPage<MapPorjectVO> page, Map<String, Object> entity);
}
