package org.springblade.plugin.workflow.core.handler;

import lombok.AllArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.springblade.plugin.workflow.core.utils.ObjectUtil;
import org.springblade.plugin.workflow.process.model.WfTaskUser;
import org.springblade.plugin.workflow.process.service.IWfProcessService;
import org.springblade.system.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 多实例人员配置处理
 *
 * @author ssc
 */
@Component
@AllArgsConstructor
public class WfMultiInstanceHandler {

	private final IWfProcessService processService;

	public List<String> getList(DelegateExecution execution) {
		HashSet<String> candidateUserIds = new HashSet<>();

		WfTaskUser taskUser = processService.getTaskUser(execution.getProcessDefinitionId(), execution.getProcessInstanceId(), execution.getCurrentActivityId());
		List<User> userList = taskUser.getUserList();
		if (ObjectUtil.isNotEmpty(userList)) {
			userList.forEach(user -> candidateUserIds.add(user.getId() + ""));
		}

		return new ArrayList<>(candidateUserIds);
	}
}
