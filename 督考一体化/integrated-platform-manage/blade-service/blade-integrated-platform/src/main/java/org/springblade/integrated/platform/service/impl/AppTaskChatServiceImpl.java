package org.springblade.integrated.platform.service.impl;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.AppTask;
import com.vingsoft.entity.AppTaskChat;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.UnifyMessage;
import com.vingsoft.vo.AppTaskChatVO;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.mapper.AppTaskChatMapper;
import org.springblade.integrated.platform.service.IAppTaskChatService;
import org.springblade.integrated.platform.service.IAppTaskService;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.service.IUnifyMessageService;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/9 21:23
* @Version:        1.0
*/
@Service
@AllArgsConstructor
public class AppTaskChatServiceImpl extends BaseServiceImpl<AppTaskChatMapper, AppTaskChat> implements IAppTaskChatService {

	private final IUserClient userClient;

	private final IUnifyMessageService messageService;

	private final ISupervisionFilesService filesService;

	private final IAppTaskService appTaskService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveAll(AppTaskChatVO vo) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		AppTaskChat entity = Objects.requireNonNull(BeanUtil.copy(vo,AppTaskChat.class));
		entity.setReceiveId(user.getId());
		entity.setReceiveName(userNameDecrypt);
		entity.setReplayTime(new Date());//回复时间
		boolean flag = this.save(entity);

		List<SupervisionFiles> filesList = vo.getFilesList();//交办附件
		for (SupervisionFiles file : filesList) {
			file.setServCode(entity.getId().toString());
			file.setFileFrom("回复");
			file.setUploadUser(user.getId().toString());
			file.setUploadUserName(userNameDecrypt);
			file.setUploadTime(new Date());
			filesService.save(file);
		}

		AppTask appTask = appTaskService.getById(entity.getTaskId());

		String content = "【"+userNameDecrypt+"】回复了【"+appTask.getServName()+"】";
		String msgType = "51";
		String appMsgType = "51";

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(appTask.getId());
		message.setMsgTitle("领导交办");
		message.setMsgType(msgType);
		message.setMsgStatus(0);
		message.setMsgPlatform("web");
		message.setMsgIntro(content);
		message.setCreateTime(new Date());
		message.setReceiveUser(appTask.getSenderId().toString());
		messageService.sendMessageInfo(message);

		message.setId(null);
		message.setMsgPlatform("app");
		message.setMsgType(Constants.MSG_TYPE_APP_ONE_DB);
		message.setMsgSubitem("领导交办");
		message.setTwoLevelType(appMsgType);
		messageService.sendMessageInfo(message);
		return flag;
	}
}
