package org.springblade.plugin.workflow.process.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.util.ExecutionGraphUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.history.HistoricVariableInstanceQuery;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.workflow.core.constant.WfExtendConstant;
import org.springblade.plugin.workflow.core.utils.ObjectUtil;
import org.springblade.plugin.workflow.core.utils.WfModelUtil;
import org.springblade.plugin.workflow.design.service.IWfSerialService;
import org.springblade.plugin.workflow.process.entity.WfNotice;
import org.springblade.plugin.workflow.process.model.WfNode;
import org.springblade.plugin.workflow.process.model.WfProcess;
import org.springblade.plugin.workflow.core.cache.WfProcessCache;
import org.springblade.plugin.workflow.core.constant.WfProcessConstant;
import org.springblade.plugin.workflow.core.utils.WfTaskUtil;
import org.springblade.plugin.workflow.process.model.WfTaskUser;
import org.springblade.plugin.workflow.process.service.IWfCopyService;
import org.springblade.plugin.workflow.process.service.IWfDraftService;
import org.springblade.plugin.workflow.process.service.IWfNoticeService;
import org.springblade.plugin.workflow.process.service.IWfProcessService;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WfProcessService implements IWfProcessService {

	private final RuntimeService runtimeService;
	private final IdentityService identityService;
	private final HistoryService historyService;
	private final TaskService taskService;
	private final RepositoryService repositoryService;
	private final ManagementService managementService;

	private final IWfCopyService wfCopyService;
	private final IWfSerialService wfSerialService;
	private final IWfNoticeService wfNoticeService;
	private final IWfDraftService wfDraftService;

	private final IUserSearchClient userSearchService;

	@Override
	public String startProcessInstanceById(String processDefId, Map<String, Object> variables) {
		String userId = WfTaskUtil.getTaskUser();
		User user = UserCache.getUser(Long.parseLong(userId));
		variables.put(WfProcessConstant.TASK_VARIABLE_APPLY_USER, userId);
		variables.put(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, user.getName());

		ProcessDefinition definition = WfProcessCache.getProcessDefinition(processDefId);
		if (definition == null) {
			throw new RuntimeException("查询不到此部署的流程");
		}
		// 流水号
		String sn = wfSerialService.getNextSN(definition.getDeploymentId());
		if (StringUtil.isNotBlank(sn)) {
			variables.put(WfProcessConstant.TASK_VARIABLE_SN, sn);
		}

		// 启动流程
		identityService.setAuthenticatedUserId(userId);
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefId, variables);
		try {
			// 修改流程示例名称，方便查询
			runtimeService.setProcessInstanceName(processInstance.getId(), definition.getName());
		} catch (Exception ignore) {
			return processInstance.getId();
		}

		// 自动跳过第一节点
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
		String skip = WfModelUtil.getProcessExtensionAttribute(bpmnModel, WfExtendConstant.SKIP_FIRST_NODE);
		if (StringUtil.isNotBlank(skip) && "true".equals(skip)) {
			List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
			taskList.forEach(task -> taskService.complete(task.getId()));
		}
		// 消息
		wfNoticeService.resolveNoticeInfo(new WfNotice()
			.setFromUserId(userId)
			.setProcessId(processInstance.getId())
			.setType(WfNotice.Type.START));

		// 指定下一步审核人
		Object assignee = variables.get(WfProcessConstant.TASK_VARIABLE_ASSIGNEE);
		if (ObjectUtil.isNotEmpty(assignee)) {
			List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
			this.handleNextNodeAssignee(taskList, assignee.toString());
		}
		// 处理抄送
		Object copyUser = variables.get(WfProcessConstant.TASK_VARIABLE_COPY_USER);
		if (ObjectUtil.isNotEmpty(copyUser)) {
			List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
			if (taskList.size() > 0) {
				Task task = taskList.get(0);
				WfProcess process = new WfProcess();
				process.setAssignee(WfTaskUtil.getTaskUser());
				process.setAssigneeName(WfTaskUtil.getNickName());
				process.setTaskId(task.getId());
				process.setTaskName(processInstance.getProcessDefinitionName() + "-" + task.getName());
				process.setProcessInstanceId(processInstance.getId());
				process.setCopyUser(copyUser.toString());
				wfCopyService.resolveCopyUser(process);
			}
		}
		// 删除草稿箱
		wfDraftService.deleteByProcessDefId(processDefId, WfTaskUtil.getTaskUser());

		return processInstance.getId();
	}

	@Override
	public IPage<WfProcess> selectClaimPage(WfProcess wfProcess, Query query) {
		IPage<WfProcess> page = new Page<>();

		TaskQuery taskQuery = taskService.createTaskQuery()
			.taskCandidateUser(WfTaskUtil.getTaskUser())
			.taskCandidateGroupIn(Arrays.asList(WfTaskUtil.getCandidateGroup().split(",")))
//			.includeProcessVariables()
			.active()
			.orderByTaskCreateTime()
			.desc();

		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionName())) {
			taskQuery.processDefinitionNameLike("%" + wfProcess.getProcessDefinitionName() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionKey())) {
			taskQuery.processDefinitionKey(wfProcess.getProcessDefinitionKey());
		}
		if (StringUtil.isNotBlank(wfProcess.getSerialNumber())) {
			taskQuery.processVariableValueLike(WfProcessConstant.TASK_VARIABLE_SN, "%" + wfProcess.getSerialNumber() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getStartUsername())) {
			taskQuery.processVariableValueLike(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, "%" + wfProcess.getStartUsername() + "%");
		}
		if (wfProcess.getCategory() != null) {
			taskQuery.processCategoryIn(Func.toStrList(wfProcess.getCategory()));
		}
		if (wfProcess.getBeginDate() != null) {
			taskQuery.taskCreatedAfter(wfProcess.getBeginDate());
		}
		if (wfProcess.getEndDate() != null) {
			taskQuery.taskCreatedBefore(wfProcess.getEndDate());
		}

		long count = taskQuery.count();
		if (count > 0) {
			List<WfProcess> list = new LinkedList<>();
			buildProcessList(list, taskQuery, query, WfProcessConstant.STATUS_CLAIM);
			page.setRecords(list);
		}
		page.setTotal(taskQuery.count());
		return page;
	}

	@Override
	public IPage<WfProcess> selectTodoPage(WfProcess wfProcess, Query query) {
		IPage<WfProcess> page = new Page<>();

		TaskQuery taskQuery = taskService.createTaskQuery()
			.taskCandidateOrAssigned(WfTaskUtil.getTaskUser())
			.taskCandidateGroupIn(Arrays.asList(WfTaskUtil.getCandidateGroup().split(",")))
//			.includeProcessVariables()
			.active()
			.orderByTaskCreateTime()
			.desc();

		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionName())) {
			taskQuery.processDefinitionNameLike("%" + wfProcess.getProcessDefinitionName() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionKey())) {
			taskQuery.processDefinitionKey(wfProcess.getProcessDefinitionKey());
		}
		if (StringUtil.isNotBlank(wfProcess.getSerialNumber())) {
			taskQuery.processVariableValueLike(WfProcessConstant.TASK_VARIABLE_SN, "%" + wfProcess.getSerialNumber() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getStartUsername())) {
			taskQuery.processVariableValueLike(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, "%" + wfProcess.getStartUsername() + "%");
		}
		if (wfProcess.getCategory() != null) {
			taskQuery.processCategoryIn(Func.toStrList(wfProcess.getCategory()));
		}
		if (wfProcess.getBeginDate() != null) {
			taskQuery.taskCreatedAfter(wfProcess.getBeginDate());
		}
		if (wfProcess.getEndDate() != null) {
			taskQuery.taskCreatedBefore(wfProcess.getEndDate());
		}

		long count = taskQuery.count();
		if (count > 0) {
			List<WfProcess> list = new LinkedList<>();
			buildProcessList(list, taskQuery, query, WfProcessConstant.STATUS_TODO);
			page.setRecords(list);
		}
		page.setTotal(taskQuery.count());
		return page;
	}

	@Override
	public IPage<WfProcess> selectSendPage(WfProcess wfProcess, Query query) {
		IPage<WfProcess> page = new Page<>();

		String taskUser = WfTaskUtil.getTaskUser();
		HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery()
			.startedBy(taskUser)
//			.includeProcessVariables()
			.orderByProcessInstanceStartTime()
			.desc();

		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionName())) {
			historyQuery.processInstanceNameLike("%" + wfProcess.getProcessDefinitionName() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionKey())) {
			historyQuery.processDefinitionKey(wfProcess.getProcessDefinitionKey());
		}
		if (StringUtil.isNotBlank(wfProcess.getSerialNumber())) {
			historyQuery.variableValueLike(WfProcessConstant.TASK_VARIABLE_SN, "%" + wfProcess.getSerialNumber() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getStartUsername())) {
			historyQuery.variableValueLike(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, "%" + wfProcess.getStartUsername() + "%");
		}
		if (wfProcess.getCategory() != null) {
			historyQuery.processDefinitionCategory(wfProcess.getCategory());
		}
		if (wfProcess.getBeginDate() != null) {
			historyQuery.startedAfter(wfProcess.getBeginDate());
		}
		if (wfProcess.getEndDate() != null) {
			historyQuery.startedBefore(wfProcess.getEndDate());
		}

		long count = historyQuery.count();
		if (count > 0) {
			List<WfProcess> list = new LinkedList<>();
			buildProcessHistoryList(list, historyQuery, query);
			page.setRecords(list);
		}
		page.setTotal(count);
		return page;
	}

	@Override
	public IPage<WfProcess> selectDonePage(WfProcess wfProcess, Query query) {
		IPage<WfProcess> page = new Page<>();

		String taskUser = WfTaskUtil.getTaskUser();
		HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery()
			.involvedUser(taskUser)
//			.includeProcessVariables()
			.orderByProcessInstanceStartTime()
			.desc();

		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionName())) {
			historyQuery.processInstanceNameLike("%" + wfProcess.getProcessDefinitionName() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getProcessDefinitionKey())) {
			historyQuery.processDefinitionKey(wfProcess.getProcessDefinitionKey());
		}
		if (StringUtil.isNotBlank(wfProcess.getSerialNumber())) {
			historyQuery.variableValueLike(WfProcessConstant.TASK_VARIABLE_SN, "%" + wfProcess.getSerialNumber() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getStartUsername())) {
			historyQuery.variableValueLike(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, "%" + wfProcess.getStartUsername() + "%");
		}
		if (StringUtil.isNotBlank(wfProcess.getStatus()) && "done".equals(wfProcess.getStatus())) {
			historyQuery.finished();
		}
		if (wfProcess.getCategory() != null) {
			historyQuery.processDefinitionCategory(wfProcess.getCategory());
		}
		if (wfProcess.getBeginDate() != null) {
			historyQuery.startedAfter(wfProcess.getBeginDate());
		}
		if (wfProcess.getEndDate() != null) {
			historyQuery.startedBefore(wfProcess.getEndDate());
		}

		long count = historyQuery.count();
		if (count > 0) {
			List<WfProcess> list = new LinkedList<>();
			buildProcessHistoryList(list, historyQuery, query);
			page.setRecords(list);
		}
		page.setTotal(count);
		return page;
	}

	@Async
	@Override
	public Future<List<WfProcess>> historyFlowList(String processInstanceId, String startActivityId, String endActivityId) {
		List<WfProcess> flowList = new LinkedList<>();
		List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
			.processInstanceId(processInstanceId)
			.orderByHistoricActivityInstanceStartTime().asc()
			.orderByHistoricActivityInstanceEndTime().asc()
			.list();
		List<Comment> commentList = taskService.getProcessInstanceComments(processInstanceId);
		boolean start = false;
		Map<String, Integer> activityMap = new HashMap<>(16);
		for (int i = 0; i < historicActivityInstanceList.size(); i++) {
			HistoricActivityInstance historicActivityInstance = historicActivityInstanceList.get(i);
			// 过滤开始节点前的节点
			if (StringUtil.isNotBlank(startActivityId) && startActivityId.equals(historicActivityInstance.getActivityId())) {
				start = true;
			}
			if (StringUtil.isNotBlank(startActivityId) && !start) {
				continue;
			}
			// 显示开始节点和结束节点，并且执行人不为空的任务
			if (WfProcessConstant.USER_TASK.equals(historicActivityInstance.getActivityType())
				|| WfProcessConstant.START_EVENT.equals(historicActivityInstance.getActivityType())
				|| WfProcessConstant.END_EVENT.equals(historicActivityInstance.getActivityType())
				|| WfProcessConstant.SEQUENCE_FLOW.equals(historicActivityInstance.getActivityType())) {
				// 给节点增加序号
				Integer activityNum = activityMap.get(historicActivityInstance.getActivityId());
				if (activityNum == null) {
					activityMap.put(historicActivityInstance.getActivityId(), activityMap.size());
				}
				WfProcess flow = new WfProcess();
				flow.setHistoryActivityId(historicActivityInstance.getActivityId());
				flow.setHistoryActivityName(historicActivityInstance.getActivityName());
				flow.setHistoryActivityType(historicActivityInstance.getActivityType());
				flow.setCreateTime(historicActivityInstance.getStartTime());
				flow.setEndTime(historicActivityInstance.getEndTime());
				String durationTime = DateUtil.secondToTime(Func.toLong(historicActivityInstance.getDurationInMillis(), 0L) / 1000);
				flow.setHistoryActivityDurationTime(durationTime);
				// 获取流程发起人名称
				if (WfProcessConstant.START_EVENT.equals(historicActivityInstance.getActivityType())) {
					List<HistoricProcessInstance> processInstanceList = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).orderByProcessInstanceStartTime().asc().list();
					if (processInstanceList.size() > 0) {
						if (StringUtil.isNotBlank(processInstanceList.get(0).getStartUserId())) {
							String taskUser = processInstanceList.get(0).getStartUserId();
							User user = UserCache.getUser(WfTaskUtil.getUserId(taskUser));
							if (user != null) {
								flow.setAssignee(historicActivityInstance.getAssignee());
								flow.setAssigneeName(user.getName());
							}
						}
					}
				} else if (WfProcessConstant.USER_TASK.equals(historicActivityInstance.getActivityType())) {
					// 获取任务执行人名称
					if (StringUtil.isNotBlank(historicActivityInstance.getAssignee())) {
						User user = UserCache.getUser(WfTaskUtil.getUserId(historicActivityInstance.getAssignee()));
						if (user != null) {
							flow.setAssignee(historicActivityInstance.getAssignee());
							flow.setAssigneeName(user.getName());
						}
					} else {
						WfTaskUser taskUser = this.getTaskUser(historicActivityInstance.getProcessDefinitionId(), historicActivityInstance.getProcessInstanceId(), historicActivityInstance.getActivityId());
						List<User> userList = taskUser.getUserList();
						if (ObjectUtil.isNotEmpty(userList)) {
							if (userList.size() == 1) {
								flow.setAssignee(userList.get(0).getId() + "");
								flow.setAssigneeName(userList.get(0).getName());
							} else {
								flow.setAssigneeName("候选人：" + userList.stream().map(User::getName).collect(Collectors.joining("/")));
							}
						}
					}
				}

				// 获取意见评论内容
				if (StringUtil.isNotBlank(historicActivityInstance.getTaskId())) {
					List<Comment> comments = new ArrayList<>();
					for (Comment comment : commentList) {
						if (comment.getTaskId().equals(historicActivityInstance.getTaskId())) {
							comments.add(comment);
						}
					}
					flow.setComments(comments);
				}
				flowList.add(flow);
			}
			// 过滤结束节点后的节点
			if (StringUtils.isNotBlank(endActivityId) && endActivityId.equals(historicActivityInstance.getActivityId())) {
				boolean temp = false;
				Integer activityNum = activityMap.get(historicActivityInstance.getActivityId());
				// 该活动节点，后续节点是否在结束节点之前，在后续节点中是否存在
				for (int j = i + 1; j < historicActivityInstanceList.size(); j++) {
					HistoricActivityInstance hi = historicActivityInstanceList.get(j);
					Integer activityNumA = activityMap.get(hi.getActivityId());
					boolean numberTemp = activityNumA != null && activityNumA < activityNum;
					boolean equalsTemp = StringUtils.equals(hi.getActivityId(), historicActivityInstance.getActivityId());
					if (numberTemp || equalsTemp) {
						temp = true;
					}
				}
				if (!temp) {
					break;
				}
			}
		}
		// 处理未流转到的节点
		if (historicActivityInstanceList.size() > 0) {
			String processDefId = historicActivityInstanceList.get(0).getProcessDefinitionId();
			String processInsId = historicActivityInstanceList.get(0).getProcessInstanceId();
			BpmnModel model = repositoryService.getBpmnModel(processDefId);

			List<FlowElement> elements = new ArrayList<>();
			model.getMainProcess().getFlowElements().forEach(flowElement -> {
				if (flowElement instanceof UserTask) {
					WfProcess wfProcess = flowList.stream().filter(flow -> flow.getHistoryActivityId().equals(flowElement.getId())).findFirst().orElse(null);
					if (wfProcess == null) {
						elements.add(flowElement);
					}
				}
			});
			if (elements.size() > 0) {
				elements.forEach(element -> {
					WfProcess flow = new WfProcess();
					flow.setHistoryActivityId(element.getId());
					flow.setHistoryActivityName(element.getName());
					flow.setHistoryActivityType("candidate");
					List<User> userList = this.getTaskUser(processDefId, processInsId, element.getId()).getUserList();
					if (ObjectUtil.isNotEmpty(userList)) {
						flow.setAssigneeName("候选人：" + userList.stream().map(User::getName).collect(Collectors.joining("/")));
					}
					flowList.add(flow);
				});
			}
		}
		return new AsyncResult<>(flowList);
	}

	@Async
	@Override
	public Future<WfProcess> detail(String taskId, String assignee, String candidateGroup) {
		WfProcess process = new WfProcess();

		HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery() // 是否待办
			.taskId(taskId)
			.includeProcessVariables()
//			.includeTaskLocalVariables()
			.includeIdentityLinks()
			.singleResult();
		if (task == null) {
			return new AsyncResult<>(process);
		}
		if (assignee.equals(task.getAssignee())) { // 我的任务
			if (task.getEndTime() == null) {
				process.setStatus(WfProcessConstant.STATUS_TODO);
			} else {
				process.setStatus(WfProcessConstant.STATUS_DONE);
			}
		} else { // 候选或者已办
			List<? extends IdentityLinkInfo> identityLinks = task.getIdentityLinks();
			// 候选组
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
			List<String> candidateGroups = Arrays.asList(candidateGroup.split(","));
			if ((userIds.contains(assignee) || roles.stream().anyMatch(candidateGroups::contains)) && task.getEndTime() == null) { // 是否选人或候选组
				process.setStatus(WfProcessConstant.STATUS_TODO);
			} else {
				process.setStatus(WfProcessConstant.STATUS_DONE);
			}
		}

		process.setIsMultiInstance(this.isMultiInstance(task.getTaskDefinitionKey(), task.getProcessDefinitionId()));
		process.setTaskId(task.getId());
		process.setTaskDefinitionKey(task.getTaskDefinitionKey());
		process.setTaskName(task.getName());
		process.setAssignee(task.getAssignee());
		process.setCreateTime(task.getCreateTime());
		process.setExecutionId(task.getExecutionId());
		process.setHistoryTaskEndTime(task.getEndTime());
		Map<String, Object> variables = task.getProcessVariables();
		variables.putAll(task.getTaskLocalVariables());
		process.setVariables(variables);
		process.setProcessInstanceId(task.getProcessInstanceId());

		ProcessDefinition processDefinition = WfProcessCache.getProcessDefinition(task.getProcessDefinitionId());

		process.setProcessDefinitionId(processDefinition.getId());
		process.setProcessDefinitionName(processDefinition.getName());
		process.setProcessDefinitionKey(processDefinition.getKey());
		process.setProcessDefinitionVersion(processDefinition.getVersion());
		process.setCategory(processDefinition.getCategory());

		// 流程状态
		if (process.getStatus().equals(WfProcessConstant.STATUS_DONE)) {
			HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.singleResult();
			if (processInstance.getEndTime() == null) {
				process.setProcessIsFinished(WfProcessConstant.STATUS_UNFINISHED);
			} else {
				process.setProcessIsFinished(WfProcessConstant.STATUS_FINISHED);
			}
		} else {
			process.setProcessIsFinished(WfProcessConstant.STATUS_UNFINISHED);
		}

		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
		String taskKey = task.getTaskDefinitionKey();
		process.setXml(new String(new BpmnXMLConverter().convertToXML(bpmnModel)));

		// 外置表单
		String exFormKey = WfModelUtil.getUserTaskExtensionAttribute(taskKey, bpmnModel, WfExtendConstant.EX_FORM_KEY);
		if (StringUtil.isNotBlank(exFormKey)) {
			process.setFormKey(WfProcessConstant.EX_FORM_PREFIX + exFormKey);
		}

		// 外置表单url
		String exFormUrl = WfModelUtil.getUserTaskExtensionAttribute(taskKey, bpmnModel, WfExtendConstant.EX_FORM_URL);
		if (StringUtil.isNotBlank(exFormUrl)) {
			process.setFormKey(exFormUrl);
		}

		// 隐藏抄送人选项
		String hideCopy = WfModelUtil.getUserTaskExtensionAttribute(taskKey, bpmnModel, WfExtendConstant.HIDE_COPY);
		if (StringUtil.isNotBlank(hideCopy) && "true".equals(hideCopy)) {
			process.setHideCopy(true);
		}

		// 隐藏下一步审核人选项
		String hideExamine = WfModelUtil.getUserTaskExtensionAttribute(taskKey, bpmnModel, WfExtendConstant.HIDE_EXAMINE);
		if (StringUtil.isNotBlank(hideExamine) && "true".equals(hideExamine)) {
			process.setHideExamine(true);
		}

		// 默认抄送人
		List<ExtensionElement> copyUsers = WfModelUtil.getUserTaskExtensionElements(taskKey, bpmnModel, WfExtendConstant.COPY_USER);
		if (copyUsers != null && copyUsers.size() > 0) {
			List<String> values = new ArrayList<>();
			List<String> texts = new ArrayList<>();
			copyUsers.forEach(copyUser -> {
				String value = copyUser.getAttributes().get("value").get(0).getValue();
				String text = copyUser.getAttributes().get("text").get(0).getValue();
				if (StringUtil.isNoneBlank(value, text)) {
					values.add(value);
					texts.add(text);
				}
			});
			if (values.size() > 0 && texts.size() > 0) {
				process.setCopyUser(String.join(",", values));
				process.setCopyUserName(String.join(",", texts));
			}
		}

		return new AsyncResult<>(process);
	}

	@Override
	public Object completeTask(WfProcess process) {
		String taskId = process.getTaskId();

		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		taskService.setVariable(task.getId(), WfProcessConstant.PASS_KEY, process.isPass());
		process.setTaskName(process.getProcessDefinitionName() + "-" + task.getName());
		process.setAssigneeName(WfTaskUtil.getNickName());
		process.setTaskDefinitionKey(task.getTaskDefinitionKey());

		if (process.isPass()) { // 审核通过
			this.passTask(process, task);
		} else { // 审核不通过
			this.rejectTask(process);
		}

		// 处理抄送
		if (StringUtil.isNotBlank(process.getCopyUser())) {
			process.setAssignee(WfTaskUtil.getTaskUser());
			wfCopyService.resolveCopyUser(process);
		}

		return R.success("操作成功");
	}

	@Override
	public Object transferTask(WfProcess process) {
		String taskId = process.getTaskId();
		String acceptUser = process.getAssignee();
		String comment = process.getComment();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		if (StringUtil.isNotBlank(comment)) {
			User fromUser = UserCache.getUser(Long.valueOf(WfTaskUtil.getTaskUser()));
			User toUser = UserCache.getUser(Long.valueOf(acceptUser));
			if (fromUser != null && toUser != null) {
				comment = fromUser.getName() + "→" + toUser.getName() + "：" + comment;
			}
			taskService.addComment(taskId, task.getProcessInstanceId(), WfProcessConstant.COMMENT_TYPE_TRANSFER, comment);
		}
		taskService.setOwner(taskId, WfTaskUtil.getTaskUser());
		taskService.setAssignee(taskId, acceptUser);

		// 处理抄送
		if (StringUtil.isNotBlank(process.getCopyUser())) {
			process.setTaskName(process.getProcessDefinitionName() + "-" + task.getName());
			process.setAssigneeName(WfTaskUtil.getNickName());
			process.setAssignee(WfTaskUtil.getTaskUser());
			wfCopyService.resolveCopyUser(process);
		}

		// 处理消息
		wfNoticeService.resolveNoticeInfo(new WfNotice()
			.setFromUserId(WfTaskUtil.getTaskUser())
			.setToUserId(acceptUser)
			.setProcessId(task.getProcessInstanceId())
			.setTaskId(taskId)
			.setComment(comment)
			.setType(WfNotice.Type.TRANSFER));
		return R.success("转办成功");
	}

	@Override
	public Object delegateTask(WfProcess process) {
		String taskId = process.getTaskId();
		String acceptUser = process.getAssignee();
		String comment = process.getComment();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		if (StringUtil.isNotBlank(comment)) {
			User fromUser = UserCache.getUser(Long.valueOf(WfTaskUtil.getTaskUser()));
			User toUser = UserCache.getUser(Long.valueOf(acceptUser));
			if (fromUser != null && toUser != null) {
				comment = fromUser.getName() + "→" + toUser.getName() + "：" + comment;
			}
			taskService.addComment(taskId, task.getProcessInstanceId(), WfProcessConstant.COMMENT_TYPE_DELEGATE, comment);
		}
		taskService.setOwner(taskId, WfTaskUtil.getTaskUser());
		taskService.delegateTask(taskId, acceptUser);

		// 处理抄送
		if (StringUtil.isNotBlank(process.getCopyUser())) {
			process.setTaskName(process.getProcessDefinitionName() + "-" + task.getName());
			process.setAssigneeName(WfTaskUtil.getNickName());
			process.setAssignee(WfTaskUtil.getTaskUser());
			wfCopyService.resolveCopyUser(process);
		}

		// 处理消息
		wfNoticeService.resolveNoticeInfo(new WfNotice()
			.setFromUserId(WfTaskUtil.getTaskUser())
			.setToUserId(acceptUser)
			.setProcessId(task.getProcessInstanceId())
			.setTaskId(taskId)
			.setComment(comment)
			.setType(WfNotice.Type.DELEGATE));
		return R.success("委托成功");
	}

	@Override
	public Object claimTask(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		taskService.claim(taskId, WfTaskUtil.getTaskUser());
		return R.success("签收成功");
	}

	@Override
	public List<WfNode> getBackNodes(WfProcess wfProcess) {
		String taskId = wfProcess.getTaskId();
		String processInstanceId = wfProcess.getProcessInstanceId();
		Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();

		String currActId = taskEntity.getTaskDefinitionKey();
		String processDefinitionId = taskEntity.getProcessDefinitionId();
		Process process = repositoryService.getBpmnModel(processDefinitionId).getMainProcess();
		FlowNode currentFlowElement = (FlowNode) process.getFlowElement(currActId, true);
		List<ActivityInstance> activities =
			runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId).finished().orderByActivityInstanceStartTime().asc().list();
		List<String> activityIds =
			activities.stream().filter(activity -> activity.getActivityType().equals(BpmnXMLConstants.ELEMENT_TASK_USER) || activity.getActivityType().equals(BpmnXMLConstants.ELEMENT_EVENT_START)).map(ActivityInstance::getActivityId).filter(activityId -> !activityId.equals(currActId)).distinct().collect(Collectors.toList());
		List<WfNode> result = new ArrayList<>();
		for (String activityId : activityIds) {
			FlowNode toBackFlowElement = (FlowNode) process.getFlowElement(activityId, true);
			if (toBackFlowElement != null && ExecutionGraphUtil.isReachable(process, toBackFlowElement, currentFlowElement, Sets.newHashSet())) {
				WfNode vo = new WfNode();
				vo.setNodeName(toBackFlowElement.getName());
				vo.setNodeId(activityId);
				result.add(vo);
			}
		}
		return result;
	}

	@Override
	public Object rollbackTask(WfProcess process) {
		String taskId = process.getTaskId();
		String nodeId = process.getNodeId();
		String comment = process.getComment();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		if (StringUtil.isBlank(task.getAssignee())) {
			this.claimTask(taskId);
		}

		// 增加评论
		if (StringUtil.isNoneBlank(task.getProcessInstanceId(), comment)) {
			taskService.addComment(taskId, task.getProcessInstanceId(), WfProcessConstant.COMMENT_TYPE_ROLLBACK, comment);
		}

		ActivityInstance targetRealActivityInstance = runtimeService
			.createActivityInstanceQuery()
			.processInstanceId(task.getProcessInstanceId())
			.activityId(nodeId).list().get(0);
		if (targetRealActivityInstance.getActivityType().equals(BpmnXMLConstants.ELEMENT_EVENT_START)) {
			process.setProcessInstanceId(task.getProcessInstanceId());
			this.terminateProcess(process);
		} else {
			this.dispatchTaskTo(task.getProcessInstanceId(), nodeId);

			// 处理消息
			wfNoticeService.resolveNoticeInfo(new WfNotice()
				.setFromUserId(WfTaskUtil.getTaskUser())
				.setProcessId(task.getProcessInstanceId())
				.setTaskId(taskId)
				.setComment(comment)
				.setType(WfNotice.Type.REJECT));
		}

		return R.success("退回成功");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object terminateProcess(WfProcess process) {
		String taskId = process.getTaskId();
		String comment = process.getComment();
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		// 增加评论
		if (StringUtil.isNoneBlank(task.getProcessInstanceId(), comment)) {
			taskService.addComment(taskId, task.getProcessInstanceId(), WfProcessConstant.COMMENT_TYPE_TERMINATE, comment);
		}
		//1、获取终止节点
		List<EndEvent> endNodes = findEndFlowElement(task.getProcessDefinitionId());
		String endId = endNodes.get(0).getId();
		//2、执行终止
		this.dispatchTaskTo(task.getProcessInstanceId(), endId);

		// 处理消息
		wfNoticeService.resolveNoticeInfo(new WfNotice()
			.setFromUserId(WfTaskUtil.getTaskUser())
			.setProcessId(task.getProcessInstanceId())
			.setTaskId(taskId)
			.setComment(comment)
			.setType(WfNotice.Type.TERMINATE));

		return R.success("终止成功");
	}

	@Override
	public Object addMultiInstance(WfProcess process) {
		String taskId = process.getTaskId();
		String comment = StringUtil.isBlank(process.getComment()) ? "" : process.getComment();

		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return R.fail("查询不到此任务");
		}
		String assignee = process.getAssignee();
		String[] ids = assignee.split(",");
		for (String id : ids) {
			User user = UserCache.getUser(Long.valueOf(id));
			if (user == null) continue;

			// 执行加签
			runtimeService.addMultiInstanceExecution(task.getTaskDefinitionKey(), task.getProcessInstanceId(), Collections.singletonMap("assignee", id));

			// 增加评论
			if (StringUtil.isBlank(comment) || comment.contains("管理员操作：")) {
				comment += "添加审核人：" + user.getName();
			}
			taskService.addComment(taskId, task.getProcessInstanceId(), WfProcessConstant.COMMENT_TYPE_ADD_MULTI_INSTANCE, comment);

			// 处理消息
			wfNoticeService.resolveNoticeInfo(new WfNotice()
				.setFromUserId(WfTaskUtil.getTaskUser())
				.setToUserId(id)
				.setProcessId(task.getProcessInstanceId())
				.setTaskId(taskId)
				.setComment(comment)
				.setType(WfNotice.Type.ADD_MULTI_INSTANCE));
		}
		return R.success("操作成功");
	}

	@Override
	public Boolean isMultiInstance(String taskKey, String processDefId) {
		boolean isMultiInstance = false;

		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
		Process process = bpmnModel.getMainProcess();
		FlowElement flowElement = process.getFlowElement(taskKey);
		if (flowElement instanceof UserTask) {
			UserTask userTask = (UserTask) flowElement;
			if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
				ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
				if (behavior != null && behavior.getCollectionExpression() != null) {
					isMultiInstance = true;
				}
			}
		}
		return isMultiInstance;
	}

	@Override
	public WfTaskUser getTaskUser(String processDefId, @Nullable String processInsId, String nodeId) {
		WfTaskUser taskUser = new WfTaskUser();

		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
		if (bpmnModel == null) {
			return taskUser;
		}

		List<ExtensionElement> elements = WfModelUtil.getUserTaskExtensionElements(nodeId, bpmnModel, WfExtendConstant.ASSIGNEE);
		if (elements == null) {
			return taskUser;
		}

		// 人员 - 多个 请赋值candidateUserIds，表示多个候选人
		// 角色/部门/职位 请赋值candidateGroupIds，表示多个候选组
		// 人员 - 单个 请赋值assignee，表示审核人唯一。！！赋值assignee后，候选组和候选人将失效！！
		// userList所有用户，包含组查询出来的人员和配置的人员，可用于多实例、流程图显示未到达节点的候选人。
		// Q：为什么需要配置两遍（配置了组同时又要配置人员）？ A：普通节点需要配置组，多实例节点只能配置人。为了两个方法通用，所以需要配置两遍。
		Set<User> userList = new HashSet<>(); // 所有用户，包含角色、部门、岗位查询出的用户，审核人等。
		Set<String> candidateUserIds = new HashSet<>(); // 候选用户集合
		Set<String> candidateGroupIds = new HashSet<>(); // 候选组集合
		String assignee = null; // 唯一审核人

		for (ExtensionElement element : elements) {
			String type = element.getAttributes().get("type").get(0).getValue();
			String value = element.getAttributes().get("value").get(0).getValue();

			switch (type) {
				case "role": // 角色
					candidateGroupIds.addAll(Func.toStrList(value));
					userList.addAll(userSearchService.listByRole(value).getData());
					break;
				case "dept": // 部门
					candidateGroupIds.addAll(Func.toStrList(value));
					userList.addAll(userSearchService.listByDept(value).getData());
					break;
				case "post": // 岗位
					candidateGroupIds.addAll(Func.toStrList(value));
					userList.addAll(userSearchService.listByPost(value).getData());
					break;
				case "user": // 用户
					candidateUserIds.addAll(Func.toStrList(value));
					userList.addAll(userSearchService.listByUser(value).getData());
					break;
				case "custom": // 自定义
					if (StringUtil.isBlank(processInsId)) {
						break;
					}
					HistoricVariableInstanceQuery variableQuery = historyService
						.createHistoricVariableInstanceQuery()
						.processInstanceId(processInsId);

					switch (value) {
						case "applyUser": // 发起人
							HistoricVariableInstance applyUser = variableQuery.variableName(WfProcessConstant.TASK_VARIABLE_APPLY_USER).singleResult();
							if (applyUser != null) {
								List<User> listByUser = userSearchService.listByUser(applyUser.getValue().toString()).getData();
								if (listByUser.size() > 0) {
									userList.addAll(listByUser);
									assignee = listByUser.get(0).getId() + "";
								}
							}
							break;
						case "currentUser": // 当前操作人
							userList.addAll(userSearchService.listByUser(WfTaskUtil.getTaskUser()).getData());
							assignee = WfTaskUtil.getTaskUser();
							break;
						case "leader": // 示例，请自行修改
							userList.addAll(userSearchService.listByUser("1123598821738675201").getData());
							assignee = "1123598821738675201";
							break;
					}
					break;
			}
		}
		taskUser.setUserList(new ArrayList<>(userList));
		taskUser.setAssignee(assignee);
		taskUser.setCandidateUserIds(new ArrayList<>(candidateUserIds));
		taskUser.setCandidateGroupIds(new ArrayList<>(candidateGroupIds));

		return taskUser;
	}

	@Override
	public void dispatchTaskTo(String processInsId, String nodeId) {
		List<Execution> executions = runtimeService.createExecutionQuery().parentId(processInsId).list();
		List<String> executionIds = new ArrayList<>();
		executions.forEach(execution -> executionIds.add(execution.getId()));
		runtimeService.createChangeActivityStateBuilder().moveExecutionsToSingleActivityId(executionIds, nodeId).changeState();
	}

	/**
	 * 审核通过
	 */
	private void passTask(WfProcess process, Task task) {
		String taskId = task.getId();
		String processInstanceId = process.getProcessInstanceId();
		String processDefinitionId = process.getProcessDefinitionId();
		String assignee = process.getAssignee();
		String comment = process.getComment();

		// 创建变量
		Map<String, Object> variables = process.getVariables();
		if (variables == null) {
			variables = new HashMap<>();
		}
		variables.put(WfProcessConstant.PASS_KEY, process.isPass());

		// 增加评论
		if (StringUtil.isNoneBlank(processInstanceId, comment)) {
			taskService.addComment(taskId, processInstanceId, comment);
		}

		boolean needComplete = true;
		// 重新提交回到驳回人
		Object nodeId = runtimeService.getVariable(processInstanceId, WfExtendConstant.BACK_TO_REJECTER);
		if (ObjectUtil.isNotEmpty(nodeId) && StringUtil.isNotBlank(processDefinitionId)) {
			BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
			String backToRejecter = WfModelUtil.getUserTaskExtensionAttribute(task.getTaskDefinitionKey(), bpmnModel, WfExtendConstant.BACK_TO_REJECTER);
			if (StringUtil.isNotBlank(backToRejecter) && "true".equals(backToRejecter)) {
				taskService.setVariables(taskId, variables);
				this.dispatchTaskTo(processInstanceId, nodeId.toString());
				runtimeService.removeVariable(processInstanceId, WfExtendConstant.BACK_TO_REJECTER);
				needComplete = false;
			}
		}

		if (needComplete) {
			if (StringUtil.isNotBlank(task.getOwner())) { // 转办/委托设置了owner
				DelegationState delegationState = task.getDelegationState();
				if (delegationState != null) {
					switch (delegationState) {
						case PENDING: // 委托任务先处理，处理完成后会回到委派人的任务中，再执行完成
							taskService.resolveTask(taskId, variables);
//						taskService.complete(taskId, variables);
							break;
						case RESOLVED: // 已处理委托
						default: // 无委托
							taskService.complete(taskId, variables);
							break;
					}
				} else {
					taskService.complete(taskId, variables);
				}
			} else if (StringUtil.isEmpty(task.getAssignee())) { // 待签任务，先签收
				this.claimTask(taskId);
				taskService.complete(taskId, variables);
			} else {
				taskService.complete(taskId, variables);
			}
		}

		if (StringUtil.isNotBlank(assignee)) { // 指定下一步审批人
			List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
			this.handleNextNodeAssignee(taskList, assignee);
		}

		// 处理消息
		wfNoticeService.resolveNoticeInfo(new WfNotice()
			.setFromUserId(WfTaskUtil.getTaskUser())
			.setProcessId(processInstanceId)
			.setTaskId(taskId)
			.setComment(comment)
			.setTaskVariables(variables)
			.setType(WfNotice.Type.PASS));
	}

	/**
	 * 审核不通过
	 */
	private void rejectTask(WfProcess process) {
		String taskId = process.getTaskId();
		String taskKey = process.getTaskDefinitionKey();
		String processInstanceId = process.getProcessInstanceId();
		String processDefinitionId = process.getProcessDefinitionId();
		String rollbackNode = null;

		BpmnModel bpmnModel = null;
		if (StringUtil.isNotBlank(processDefinitionId)) {
			bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

			// 判断节点上是否配置了驳回节点，若没有配置则使用流程上配置的驳回节点，若流程也没有配置则驳回到上一节点
			List<ExtensionAttribute> attributes;
			rollbackNode = WfModelUtil.getUserTaskExtensionAttribute(taskKey, bpmnModel, WfExtendConstant.ROLLBACK_NODE);
			if (StringUtil.isEmpty(rollbackNode)) {
				attributes = WfModelUtil.getProcessExtensionAttributes(bpmnModel, WfExtendConstant.ROLLBACK_NODE);
				if (attributes != null) {
					rollbackNode = attributes.get(0).getValue();
				}
			}
		}
		List<WfNode> backNodes = this.getBackNodes(process);
		if (backNodes.size() > 0) {
			String finalRollbackNode = rollbackNode;
			// 配置了默认退回节点并且可退回节点中包含配置的退回节点
			if (StringUtil.isNotBlank(rollbackNode) && backNodes.stream().filter(wfNode -> wfNode.getNodeId().equals(finalRollbackNode)).findAny().orElse(null) != null) {
				process.setNodeId(rollbackNode);
			} else {
				WfNode node = backNodes.get(backNodes.size() - 1);
				process.setNodeId(node.getNodeId());
			}
			// 被驳回的节点是否配置了 重新提交回到驳回人
			if (bpmnModel != null) {
				String backToRejecter = WfModelUtil.getUserTaskExtensionAttribute(process.getNodeId(), bpmnModel, WfExtendConstant.BACK_TO_REJECTER);
				if (StringUtil.isNotBlank(backToRejecter) && "true".equals(backToRejecter)) {
					taskService.setVariable(taskId, WfExtendConstant.BACK_TO_REJECTER, taskKey);
				}
			}
			this.rollbackTask(process);
		}
	}

	/**
	 * 构建流程
	 *
	 * @param wfProcessList 流程列表
	 * @param taskQuery     任务查询类
	 * @param status        状态
	 */
	private void buildProcessList(List<WfProcess> wfProcessList, TaskQuery taskQuery, Query query, String status) {
		List<Task> taskList = taskQuery.listPage(Func.toInt((query.getCurrent() - 1) * query.getSize()), Func.toInt(query.getSize()));

		taskList.forEach(task -> {
			WfProcess flow = new WfProcess();
			flow.setTaskId(task.getId());
			flow.setTaskDefinitionKey(task.getTaskDefinitionKey());
			flow.setTaskName(task.getName());
//			flow.setAssignee(task.getAssignee());
			flow.setCreateTime(task.getCreateTime());
//			flow.setClaimTime(task.getClaimTime());
//			flow.setExecutionId(task.getExecutionId());
			// Variables
			Map<String, Object> variables = new HashMap<>();
			HistoricVariableInstanceQuery variableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(task.getProcessInstanceId());
			HistoricVariableInstance applyUsername = variableInstanceQuery.variableName(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME).singleResult();
			if (ObjectUtil.isNotEmpty(applyUsername)) {
				variables.put(WfProcessConstant.TASK_VARIABLE_APPLY_USER_NAME, applyUsername.getValue());
				flow.setStartUsername(applyUsername.getValue().toString());
			}
			HistoricVariableInstance sn = variableInstanceQuery.variableName(WfProcessConstant.TASK_VARIABLE_SN).singleResult();
			if (ObjectUtil.isNotEmpty(sn)) {
				variables.put(WfProcessConstant.TASK_VARIABLE_SN, sn.getValue());
			}
			flow.setVariables(variables);

			ProcessDefinition processDefinition = WfProcessCache.getProcessDefinition(task.getProcessDefinitionId());
			flow.setCategory(processDefinition.getCategory());
//			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionKey(processDefinition.getKey());
//			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setProcessInstanceId(task.getProcessInstanceId());
			flow.setStatus(status);
			flow.setProcessIsFinished(WfProcessConstant.STATUS_UNFINISHED);

			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			String exFormKey = WfModelUtil.getUserTaskExtensionAttribute(bpmnModel, WfExtendConstant.EX_FORM_KEY);
			if (StringUtil.isNotBlank(exFormKey)) {
				flow.setFormKey(WfProcessConstant.EX_FORM_PREFIX + exFormKey);
			}

			String exFormUrl = WfModelUtil.getUserTaskExtensionAttribute(bpmnModel, WfExtendConstant.EX_FORM_URL);
			if (StringUtil.isNotBlank(exFormUrl)) {
				flow.setFormKey(exFormUrl);
			}

			wfProcessList.add(flow);
		});
	}

	private void buildProcessHistoryList(List<WfProcess> wfProcessList, HistoricProcessInstanceQuery historyQuery, Query query) {
		// 查询列表
		List<HistoricProcessInstance> historyList = historyQuery.listPage(Func.toInt((query.getCurrent() - 1) * query.getSize()), Func.toInt(query.getSize()));

		historyList.forEach(historicProcessInstance -> {
			WfProcess historyWfProcess = new WfProcess();
			// historicProcessInstance
			historyWfProcess.setStartUsername(UserCache.getUser(Long.valueOf(historicProcessInstance.getStartUserId())).getName());
			historyWfProcess.setCreateTime(historicProcessInstance.getStartTime());
//			historyWfProcess.setEndTime(historicProcessInstance.getEndTime());
//			historyWfProcess.setHistoryActivityName(historicProcessInstance.getName());
			historyWfProcess.setProcessInstanceId(historicProcessInstance.getId());
//			historyWfProcess.setHistoryProcessInstanceId(historicProcessInstance.getId());
			// Variables
			Map<String, Object> variables = new HashMap<>();
			HistoricVariableInstanceQuery variableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(historicProcessInstance.getId());
			HistoricVariableInstance sn = variableInstanceQuery.variableName(WfProcessConstant.TASK_VARIABLE_SN).singleResult();
			if (ObjectUtil.isNotEmpty(sn)) {
				variables.put(WfProcessConstant.TASK_VARIABLE_SN, sn.getValue());
			}
			historyWfProcess.setVariables(variables);
			// ProcessDefinition
			ProcessDefinition processDefinition = WfProcessCache.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
//			historyWfProcess.setProcessDefinitionId(processDefinition.getId());
			historyWfProcess.setProcessDefinitionName(processDefinition.getName());
//			historyWfProcess.setProcessDefinitionVersion(processDefinition.getVersion());
			historyWfProcess.setProcessDefinitionKey(processDefinition.getKey());
			historyWfProcess.setCategory(processDefinition.getCategory());
			// HistoricTaskInstance
			List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(historicProcessInstance.getId())
				.orderByTaskCreateTime().desc()
				.orderByHistoricTaskInstanceEndTime().desc()
				.list();
			if (Func.isNotEmpty(historyTasks)) {
				HistoricTaskInstance historyTask = historyTasks.iterator().next();
				historyWfProcess.setTaskId(historyTask.getId());
				historyWfProcess.setTaskName(historyTask.getName());
//				historyWfProcess.setTaskDefinitionKey(historyTask.getTaskDefinitionKey());

				BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
				String exFormKey = WfModelUtil.getUserTaskExtensionAttribute(bpmnModel, WfExtendConstant.EX_FORM_KEY);
				if (StringUtil.isNotBlank(exFormKey)) {
					historyWfProcess.setFormKey(WfProcessConstant.EX_FORM_PREFIX + exFormKey);
				}

				String exFormUrl = WfModelUtil.getUserTaskExtensionAttribute(bpmnModel, WfExtendConstant.EX_FORM_URL);
				if (StringUtil.isNotBlank(exFormUrl)) {
					historyWfProcess.setFormKey(exFormUrl);
				}
			}
			// Status
			if (historicProcessInstance.getEndActivityId() != null) {
				historyWfProcess.setProcessIsFinished(WfProcessConstant.STATUS_FINISHED);
				historyWfProcess.setTaskName("结束");
			} else {
				historyWfProcess.setProcessIsFinished(WfProcessConstant.STATUS_UNFINISHED);
			}
//			historyWfProcess.setStatus(WfProcessConstant.STATUS_FINISH);
			wfProcessList.add(historyWfProcess);
		});
	}

	private List findEndFlowElement(String processDefId) {
		Process mainProcess = repositoryService.getBpmnModel(processDefId).getMainProcess();
		Collection<FlowElement> list = mainProcess.getFlowElements();
		if (CollectionUtils.isEmpty(list)) {
			return Collections.EMPTY_LIST;
		}
		return list.stream().filter(f -> f instanceof EndEvent).collect(Collectors.toList());
	}

	private void handleNextNodeAssignee(List<Task> list, String assignee) {
		if (StringUtil.isNotBlank(assignee)) {
			int index = 0;
			String[] ids = assignee.split(",");
			for (Task task : list) {
				taskService.setAssignee(task.getId(), ids[index]);
				index++;
				if (index > ids.length - 1) index = 0;
			}
		}
	}

}
