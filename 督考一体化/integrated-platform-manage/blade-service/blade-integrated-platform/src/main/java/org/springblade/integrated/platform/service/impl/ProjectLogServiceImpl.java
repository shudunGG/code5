package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ProjectLog;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.ProjectLogMapper;
import org.springblade.integrated.platform.service.IProjectLogService;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @Author Adam
 * @Create 2022-4-9 18:15:29
 */
@Service
public class ProjectLogServiceImpl extends BaseServiceImpl<ProjectLogMapper, ProjectLog> implements IProjectLogService {

}
