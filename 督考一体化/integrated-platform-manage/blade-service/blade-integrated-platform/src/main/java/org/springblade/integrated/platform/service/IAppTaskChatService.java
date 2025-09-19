package org.springblade.integrated.platform.service;

import com.vingsoft.entity.AppTaskChat;
import com.vingsoft.vo.AppTaskChatVO;
import org.springblade.core.mp.base.BaseService;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:21
* @Version:        1.0
*/
public interface IAppTaskChatService extends BaseService<AppTaskChat> {

	boolean saveAll(AppTaskChatVO vo);
}
