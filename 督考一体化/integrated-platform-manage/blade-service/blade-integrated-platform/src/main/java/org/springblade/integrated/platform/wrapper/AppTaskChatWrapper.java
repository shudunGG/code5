package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.AppTaskChat;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.vo.AppTaskChatVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.ISupervisionFilesService;

import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/11 16:43
* @Version:        1.0
*/
public class AppTaskChatWrapper extends BaseEntityWrapper<AppTaskChat, AppTaskChatVO> {

	public static AppTaskChatWrapper build(){
		return new AppTaskChatWrapper();
	}

	@Override
	public AppTaskChatVO entityVO(AppTaskChat entity) {
		AppTaskChatVO vo = Objects.requireNonNull(BeanUtil.copy(entity,AppTaskChatVO.class));

		SupervisionFiles file = new SupervisionFiles();
		file.setServCode(entity.getId().toString());
		file.setFileFrom("回复");
		List<SupervisionFiles> filesList = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(file));
		vo.setFilesList(filesList);
		return vo;
	}
}
