package org.springblade.plugin.workflow.core.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.impl.FlowableEntityWithVariablesEventImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.workflow.core.utils.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * flowable全局监听
 *
 * @author ssc
 */
@Component
public class WfGlobalListener extends AbstractFlowableEngineEventListener {

	@Autowired
	private TaskService taskService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private HistoryService historyService;

	/**
	 * 任务创建时的监听
	 */
	@Override
	protected void taskCreated(FlowableEngineEntityEvent event) {
		super.taskCreated(event);

		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (event instanceof FlowableEntityEventImpl) {
				// 流程定义id
				String processDefinitionId = event.getProcessDefinitionId();
				// 流程实例id
				String processInstanceId = event.getProcessInstanceId();

				// 流程定义
				ProcessDefinition processDefinition = repositoryService
					.createProcessDefinitionQuery()
					.processDefinitionId(processDefinitionId)
					.singleResult();

				// 流程实例
				ProcessInstance process = runtimeService
					.createProcessInstanceQuery()
					.includeProcessVariables()
					.processInstanceId(processInstanceId)
					.singleResult();

				// TODO 流程变量
				process.getProcessVariables();

				// 任务实例
				TaskEntity entity = (TaskEntity) event.getEntity();

				if (StringUtil.isBlank(entity.getAssignee())) { // 当前节点没有审核人
					Task task = taskService.createTaskQuery().taskId(entity.getId())
						.includeIdentityLinks()
						.singleResult();
					if (task != null) {
						System.err.println("当前节点没有审核人");

						// TODO 当前节点无审核人，可通知候选人或候选组

						// 候选组
						List<String> roles = new ArrayList<>();
						// 候选人
						List<String> userIds = new ArrayList<>();
						List<? extends IdentityLinkInfo> identityLinks = task.getIdentityLinks();
						identityLinks.forEach(link -> {
							if (StringUtil.isNotBlank(link.getGroupId())) {
								roles.add(link.getGroupId());
							}
							if (StringUtil.isNotBlank(link.getUserId())) {
								userIds.add(link.getUserId());
							}
						});
						System.err.println("=============可选候选组============");
						System.err.println(roles);
						System.err.println("=============可选候选组============");

						System.err.println("=============可选候选人============");
						System.err.println(userIds);
						System.err.println("=============可选候选人============");
					}
				} else {
					// TODO 有节点审核人
					System.err.println("收到 " + processDefinition.getName() + " - " + entity.getName() + " 任务");
				}
			}
		}).start();
	}

	@Override
	protected void taskAssigned(FlowableEngineEntityEvent event) { // 主动设置审批人员，如转办，委托
		super.taskAssigned(event);
		if (event instanceof org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl) {
			TaskEntity entity = (TaskEntity) event.getEntity();

			if (StringUtil.isNotBlank(entity.getOwner())) { // owner不为空可能是转办/委托
				new Thread(() -> {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 流程定义
					ProcessDefinition processDefinition = repositoryService
						.createProcessDefinitionQuery()
						.processDefinitionId(event.getProcessDefinitionId())
						.singleResult();

					// 转办人/委托人
					String owner = entity.getOwner();
					// 接受人
					String assignee = entity.getAssignee();

					// TODO 委托或转办任务，注意：候选人可委托/转办给自己，审核人无法委托/转办给自己
					DelegationState delegationState = entity.getDelegationState();
					if (ObjectUtil.isNotEmpty(delegationState)) { // 委托
						System.err.println(entity.getOwner() + " 委托给了您 " + processDefinition.getName() + " - " + entity.getName() + " 任务");
					} else { // 转办
						System.err.println(entity.getOwner() + " 转办给了您 " + processDefinition.getName() + " - " + entity.getName() + " 任务");
					}
				}).start();
			}
		}
	}

	@Override
	protected void taskCompleted(FlowableEngineEntityEvent event) {
		super.taskCompleted(event);
		if (event instanceof FlowableEntityWithVariablesEventImpl) {
			TaskEntity entity = (TaskEntity) event.getEntity();

			// TODO 变量
			System.err.println(entity.getVariables());

			new Thread(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 评论
				List<Comment> comment = taskService.getTaskComments(entity.getId());
			}).start();

		}
	}

	@Override
	protected void processCompleted(FlowableEngineEntityEvent event) {
		super.processCompleted(event);

		System.err.println("流程正常结束");
	}

	@Override
	protected void processCompletedWithTerminateEnd(FlowableEngineEntityEvent event) {
		super.processCompletedWithTerminateEnd(event);

		System.err.println("流程被终止");
	}
}
