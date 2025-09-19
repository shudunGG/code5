package org.springblade.integrated.platform.service;

import com.vingsoft.entity.AppTask;
import com.vingsoft.vo.AppTaskVO;
import org.springblade.core.mp.base.BaseService;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:22
* @Version:        1.0
*/
public interface IAppTaskService extends BaseService<AppTask> {

	boolean saveAll(AppTaskVO vo);
}
