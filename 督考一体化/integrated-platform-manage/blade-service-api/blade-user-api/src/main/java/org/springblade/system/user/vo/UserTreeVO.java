package org.springblade.system.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.tool.node.INode;
import org.springblade.core.tool.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：mqy
 * @ Date       ：Created in 上午 9:29 2021/4/2 0002
 * @ Description：用户数据根据部门数据组合成树形对象
 */
@Data
@ApiModel(value = "用户树形对象", description = "UserTreeVO对象")
public class UserTreeVO implements INode<UserTreeVO> {
	/**
	 * 部门主键ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 部门父节点ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long parentId;
	/**
	 * 子孙节点
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<UserTreeVO> children;

	/**
	 * 是否有子孙节点
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Boolean hasChildren;

	@Override
	public List<UserTreeVO> getChildren() {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		return this.children;
	}

	/**
	 * 用户账号
	 */
	private String account;
	/**
	 * 用户姓名
	 */
	private String name;
	/**
	 * 用户ID
	 */
	private String userId;
	private Boolean disabled;

	public Boolean getDisabled() {
		return StringUtil.isBlank(this.userId);
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
}
