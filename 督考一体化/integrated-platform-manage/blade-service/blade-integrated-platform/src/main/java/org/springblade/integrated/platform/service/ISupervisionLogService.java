package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.SupervisionLog;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.secure.BladeUser;

import java.util.List;
import java.util.Map;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionLogService extends BaseService<SupervisionLog> {

	List<SupervisionLog> listQueryWrapper(Map<String, Object> entity, BladeUser user);
}
