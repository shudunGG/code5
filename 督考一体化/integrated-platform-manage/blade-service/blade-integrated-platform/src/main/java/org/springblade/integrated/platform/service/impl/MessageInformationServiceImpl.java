package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.MessageInformation;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.MessageInformationMapper;
import org.springblade.integrated.platform.service.IMessageInformationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @Author JG🧸
 * @Create 2022/4/9 13:45
 */
@Service
public class MessageInformationServiceImpl extends BaseServiceImpl<MessageInformationMapper, MessageInformation> implements IMessageInformationService {

	/**
	 * 项目留言
	 * @param id
	 * @return
	 */
	@Override
	public List<MessageInformation> getMessageInformationListByProjId(String id){
		return baseMapper.getMessageInformationListByProjId(id);
	}

}
