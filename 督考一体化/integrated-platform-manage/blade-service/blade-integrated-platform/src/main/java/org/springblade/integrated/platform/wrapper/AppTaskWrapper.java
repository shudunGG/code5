package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.*;
import com.vingsoft.vo.AppTaskVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.IAppTaskChatService;
import org.springblade.integrated.platform.service.ISupervisionFilesService;

import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/10 15:30
* @Version:        1.0
*/
public class AppTaskWrapper extends BaseEntityWrapper<AppTask, AppTaskVO> {

	public static AppTaskWrapper build(){
		return new AppTaskWrapper();
	}

	@Override
	public AppTaskVO entityVO(AppTask entity) {
		AppTaskVO vo = Objects.requireNonNull(BeanUtil.copy(entity,AppTaskVO.class));

		AppTaskChat chat = new AppTaskChat();
		chat.setTaskId(entity.getId());
		chat.setReceiveId(AuthUtil.getUserId());
		List<AppTaskChat> list = SpringUtil.getBean(IAppTaskChatService.class).list(Condition.getQueryWrapper(chat));
		if(list.size()>0){
			vo.setIsTask("1");
		}else{
			vo.setIsTask("0");
		}

		SupervisionFiles file = new SupervisionFiles();
		file.setServCode(entity.getId().toString());
		file.setFileFrom("交办");
		List<SupervisionFiles> filesList = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(file));
		vo.setFilesList(filesList);
		return vo;
	}
}
