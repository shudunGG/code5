package org.springblade.plugin.workflow.process.service.impl;

import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.workflow.core.constant.WfProcessConstant;
import org.springblade.plugin.workflow.process.entity.WfNotice;
import org.springblade.plugin.workflow.process.service.IWfNoticeService;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 流程消息 服务实现类
 *
 * @author ssc
 */
@Service
@AllArgsConstructor
public class WfNoticeServiceImpl implements IWfNoticeService {

	private final RuntimeService runtimeService;
	private final TaskService taskService;
	private final HistoryService historyService;

	private final IUserSearchClient userSearchService;

	@Async
	@Override
	public void sendNotice(User fromUser, User toUser, String comment, WfNotice.Type type, HistoricProcessInstance processInstance, @Nullable HistoricTaskInstance task, Map<String, Object> taskVariables) {
		System.err.println("发送人：" + fromUser.getName());
		System.err.println("接受人：" + toUser.getName());
		System.err.println("消息内容：" + buildMessage(fromUser, toUser, processInstance, task, comment, type));
		System.err.println("消息类型：" + type.getName());
		System.err.println("流程变量：" + processInstance.getProcessVariables());
		System.err.println("流程状态：" + (processInstance.getEndTime() == null ? "审核中" : "已结束"));
		System.err.println("任务变量：" + taskVariables);
		if (task != null) {
			System.err.println("任务节点：" + task.getTaskDefinitionKey());
		}
	}

	@Async
	@Override
	public Future<String> resolveNoticeInfo(WfNotice notice) {
		// 流程实例
		String processId = notice.getProcessId();
		HistoricProcessInstance processInstance = historyService
			.createHistoricProcessInstanceQuery()
			.includeProcessVariables()
			.processInstanceId(processId).singleResult();
		if (processInstance == null) return new AsyncResult<>("");

		// 当前任务实例
		String taskId = notice.getTaskId();
		HistoricTaskInstance task = null;
		if (StringUtil.isNotBlank(taskId)) {
			task = historyService.createHistoricTaskInstanceQuery()
				.taskId(taskId)
				.singleResult();
		}

		// 当前节点用户
		String fromUserId = notice.getFromUserId();
		User fromUser = UserCache.getUser(Long.valueOf(fromUserId));

		// 流程申请人
		String applyUserId = processInstance.getStartUserId();
		User applyUser = UserCache.getUser(Long.valueOf(applyUserId));

		// 审核意见
		String comment = notice.getComment();

		// 任务变量
		Map<String, Object> taskVariables = notice.getTaskVariables();

		WfNotice.Type type = notice.getType();

		switch (type) {
			case START:
			case PASS:
			case REJECT:
				this.handleCompleteTask(fromUser, applyUser, processInstance, task, comment, type, taskVariables);
				break;
			case COPY:
			case TRANSFER:
			case DELEGATE:
			case ASSIGNEE:
			case SUSPEND:
			case ACTIVE:
			case URGE:
			case DISPATCH:
			case ADD_MULTI_INSTANCE:
			case DELETE_MULTI_INSTANCE:
				String toUserId = notice.getToUserId();
				User toUser = UserCache.getUser(Long.valueOf(toUserId));
				this.sendNotice(fromUser, toUser, comment, type, processInstance, task, taskVariables);
				break;
			case TERMINATE:
				this.sendNotice(fromUser, applyUser, comment, type, processInstance, task, taskVariables);
				break;
		}
		return new AsyncResult<>("");
	}

	/**
	 * 处理审核通过/驳回信息
	 *
	 * @param fromUser        当前节点用户
	 * @param applyUser       申请用户
	 * @param processInstance 流程实例
	 * @param task            任务实例
	 */
	private void handleCompleteTask(User fromUser, User applyUser, HistoricProcessInstance processInstance, HistoricTaskInstance task, String comment, WfNotice.Type type, Map<String, Object> taskVariables) {
		if (processInstance.getEndTime() == null) {
			List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(processInstance.getId())
				.includeIdentityLinks()
				.unfinished()
				.list();
			if (taskList != null && taskList.size() > 0) {
				for (HistoricTaskInstance nextTask : taskList) {
					// 当前节点名称
					String currentNode = nextTask.getName();
					// 当前节点执行人id
					String toUserId = nextTask.getAssignee();
					if (StringUtil.isBlank(toUserId)) { // 无执行人，查看候选
						List<? extends IdentityLinkInfo> identityLinks = nextTask.getIdentityLinks();
						// 候选组，角色ID、部门ID、职位ID
						List<String> roles = new ArrayList<>();
						// 候选人
						List<String> userIds = new ArrayList<>();
						identityLinks.forEach(link -> {
							if (StringUtil.isNotBlank(link.getGroupId())) {
								roles.add(link.getGroupId());
							}
							if (StringUtil.isNotBlank(link.getUserId())) {
								userIds.add(link.getUserId());
							}
						});
						// 给候选人发送消息
						for (String userId : userIds) {
							this.sendNotice(fromUser, UserCache.getUser(Long.valueOf(userId)), comment, WfNotice.Type.CANDIDATE, processInstance, task, taskVariables);
						}
						// 给候选组发消息
						if (roles.size() > 0) {
							List<Long> groups = roles.stream().map(val -> {
								try {
									return Long.valueOf(val);
								} catch (Exception ignore) {
									return 0L;
								}
							}).collect(Collectors.toList());
							// 部门
							userSearchService.listByDept(Func.join(groups)).getData().forEach(user -> this.sendNotice(fromUser, UserCache.getUser(user.getId()), comment, WfNotice.Type.CANDIDATE, processInstance, nextTask, taskVariables));
							// 角色
							userSearchService.listByRole(Func.join(groups)).getData().forEach(user -> this.sendNotice(fromUser, UserCache.getUser(user.getId()), comment, WfNotice.Type.CANDIDATE, processInstance, nextTask, taskVariables));
							// 职位
							userSearchService.listByPost(Func.join(groups)).getData().forEach(user -> this.sendNotice(fromUser, UserCache.getUser(user.getId()), comment, WfNotice.Type.CANDIDATE, processInstance, nextTask, taskVariables));
						}
					} else {
						// 下一节点执行人
						User toUser = UserCache.getUser(Long.valueOf(toUserId));
						this.sendNotice(fromUser, toUser, comment, type, processInstance, nextTask, taskVariables);
					}
				}
			}
		} else {
			this.sendNotice(fromUser, applyUser, comment, WfNotice.Type.FINISH, processInstance, task, taskVariables);
		}
	}

	private String buildMessage(User fromUser, User applyUser, HistoricProcessInstance processInstance, HistoricTaskInstance task, String comment, WfNotice.Type type) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		StringBuilder message = new StringBuilder();
		Object sn = processInstance.getProcessVariables().get(WfProcessConstant.TASK_VARIABLE_SN);
		if (sn != null) {
			message.append("流水号：").append(sn).append(";");
		}
		message.append(processInstance.getName()).append(" - ").append(applyUser.getName()).append(" ").append(dateFormat.format(processInstance.getStartTime())).append(";");
		if (StringUtil.isNotBlank(comment)) {
			message.append("审核意见：").append(comment);
		}
		return message.toString();
	}
}
