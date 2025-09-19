package org.springblade.plugin.workflow.process.model;

import lombok.Data;
import org.springblade.system.user.entity.User;

import java.io.Serializable;
import java.util.List;

@Data
public class WfTaskUser implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 所有用户列表
	 */
	private List<User> userList;

	/**
	 * 唯一审核人
	 */
	private String assignee;

	/**
	 * 候选人集合
	 */
	private List<String> candidateUserIds;

	/**
	 * 候选组集合
	 */
	private List<String> candidateGroupIds;
}
