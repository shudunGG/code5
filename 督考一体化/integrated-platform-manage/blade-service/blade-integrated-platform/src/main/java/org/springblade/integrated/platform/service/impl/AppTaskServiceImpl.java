package org.springblade.integrated.platform.service.impl;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.AppTask;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.UnifyMessage;
import com.vingsoft.vo.AppTaskVO;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.mapper.AppTaskMapper;
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
public class AppTaskServiceImpl extends BaseServiceImpl<AppTaskMapper, AppTask> implements IAppTaskService {

	private final IUserClient userClient;

	private final IUnifyMessageService messageService;

	private final ISupervisionFilesService filesService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveAll(AppTaskVO vo) {
		User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
		AppTask entity = Objects.requireNonNull(BeanUtil.copy(vo,AppTask.class));
		entity.setSenderId(user.getId());
		entity.setSenderName(userNameDecrypt);
		entity.setSendTime(new Date());//发送时间
		boolean flag = this.save(entity);

		List<SupervisionFiles> filesList = vo.getFilesList();//交办附件
		for (SupervisionFiles file : filesList) {
			file.setServCode(entity.getId().toString());
			file.setFileFrom("交办");
			file.setUploadUser(user.getId().toString());
			file.setUploadUserName(userNameDecrypt);
			file.setUploadTime(new Date());
			filesService.save(file);
		}

		String content = "【"+userNameDecrypt+"】交办了【"+entity.getServName()+"】";
		String msgType = "51";
		String appMsgType = "51";

		UnifyMessage message = new UnifyMessage();
		message.setMsgId(entity.getId());
		message.setMsgTitle("领导交办");
		message.setMsgType(msgType);
		message.setMsgStatus(0);
		message.setMsgPlatform("web");
		message.setMsgIntro(content);
		message.setCreateTime(new Date());
		message.setReceiveUser(entity.getReceiveId());
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
