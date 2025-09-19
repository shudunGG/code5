package org.springblade.plugin.workflow.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.plugin.workflow.process.model.WfNode;
import org.springblade.plugin.workflow.process.model.WfProcess;
import org.springblade.core.mp.support.Query;
import org.springblade.plugin.workflow.process.model.WfTaskUser;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface IWfProcessService {

	/**
	 * 发起流程
	 *
	 * @param processDefId 流程定义id
	 * @param variables    表单参数
	 * @return processInsId
	 */
	String startProcessInstanceById(String processDefId, Map<String, Object> variables);

	/**
	 * 流程待签列表
	 */
	IPage<WfProcess> selectClaimPage(WfProcess process, Query query);

	/**
	 * 流程待办列表
	 */
	IPage<WfProcess> selectTodoPage(WfProcess process, Query query);

	/**
	 * 流程已发列表
	 */
	IPage<WfProcess> selectSendPage(WfProcess process, Query query);

	/**
	 * 流程办结列表
	 */
	IPage<WfProcess> selectDonePage(WfProcess process, Query query);

	/**
	 * 获取流转历史列表
	 */
	Future<List<WfProcess>> historyFlowList(String processInstanceId, String startActivityId, String endActivityId);

	/**
	 * 获取流程详情
	 */
	Future<WfProcess> detail(String taskId, String assignee,String candidateGroup);

	/**
	 * 完成任务
	 */
	Object completeTask(WfProcess process);

	/**
	 * 转办任务
	 */
	Object transferTask(WfProcess process);

	/**
	 * 委托任务
	 */
	Object delegateTask(WfProcess process);

	/**
	 * 签收任务
	 */
	Object claimTask(String taskId);

	/**
	 * 获取可退回节点
	 */
	List<WfNode> getBackNodes(WfProcess process);

	/**
	 * 退回到指定节点
	 */
	Object rollbackTask(WfProcess process);

	/**
	 * 终止流程
	 */
	Object terminateProcess(WfProcess process);

	/**
	 * 加签
	 */
	Object addMultiInstance(WfProcess process);

	/**
	 * 判断当前节点是否是多实例
	 */
	Boolean isMultiInstance(String taskKey, String processDefId);

	/**
	 * 获取指定节点的用户
	 *
	 * @param processDefId 流程定义id
	 * @param processInsId 流程实例id
	 * @param nodeId       节点id
	 */
	WfTaskUser getTaskUser(String processDefId, @Nullable String processInsId, String nodeId);

	/**
	 * 调度流程实例到指定节点
	 *
	 * @param processInsId 流程实例id
	 * @param nodeId       节点id
	 */
	void dispatchTaskTo(String processInsId, String nodeId);
}
