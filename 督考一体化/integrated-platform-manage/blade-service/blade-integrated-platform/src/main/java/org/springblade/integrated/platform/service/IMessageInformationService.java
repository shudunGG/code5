package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.MessageInformation;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 *  服务类
 *
 * @Author JG🧸
 * @Create 2022/4/8 13:35
 */
public interface IMessageInformationService extends BaseService<MessageInformation> {

	/**
	 * 项目留言
	 * @param id
	 * @return
	 */
	List<MessageInformation> getMessageInformationListByProjId(String id);

}
