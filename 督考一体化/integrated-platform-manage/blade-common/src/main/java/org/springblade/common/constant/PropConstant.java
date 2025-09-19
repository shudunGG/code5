package org.springblade.common.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * nacos配置获取
 */
@Configuration
@Component
@RefreshScope//实现配置文件修改，自动刷新功能
@Data
public class PropConstant {


	private static String swldDeptId;//市委领导(部门id)
	private static String szfldDeptId;//市政务领导(部门id)
	private static String bmldPostId;//部门领导(岗位id)
	private static String bmglyPostId;//部门领导(岗位id)
	private static String projectShzhs;//项目管理审核账号id
	private static String swsjRoleId;//市委书记id（卢小亨id）
	private static String szRoleId;//市长id（赵立香id）

	private static String monthWorkModifyStatus; //工作月调度汇报 修改按钮控制 true可修改 false不可修改

	public static String getSwldDeptId() {
		return swldDeptId;
	}
	@Value("${properties.common.swld_dept_id:}")
	public void setSwldDeptId(String swldDeptId) {
		PropConstant.swldDeptId = swldDeptId;
	}

	public static String getSzfldDeptId() {
		return szfldDeptId;
	}

	@Value("${properties.common.szfld_dept_id:}")
	public void setSzfldDeptId(String szfldDeptId) {
		PropConstant.szfldDeptId = szfldDeptId;
	}

	public static String getBmldPostId() {
		return bmldPostId;
	}

	@Value("${properties.common.bmld_post_id:}")
	public void setBmldPostId(String bmldPostId) {
		PropConstant.bmldPostId = bmldPostId;
	}

	public static String getBmglyPostId() {
		return bmglyPostId;
	}

	@Value("${properties.common.bmgly_post_id:}")
	public void setBmglyPostId(String bmglyPostId) {
		PropConstant.bmglyPostId = bmglyPostId;
	}

	public static String getProjectShzhId(String key) {
		String[] keys = projectShzhs.split(",");
		try {
			for (String item : keys) {
				if (!item.contains(key)) {
					continue;
				}
				return item.split(":")[1];
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}
		return null;
	}
	public static String getProjectShzhName(String key) {
		String[] keys = projectShzhs.split(",");
		try {
			for (String item : keys) {
				if (!item.contains(key)) {
					continue;
				}
				return item.split(":")[2];
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
			return null;
		}
		return null;
	}

	@Value("${properties.project.shzh_user_id:}")
	public void setProjectShzhs(String projectShzhs) {
		PropConstant.projectShzhs = projectShzhs;
	}


	public static String getSwsjRoleId() {
		return swsjRoleId;
	}
	@Value("${properties.common.swsj_role_id:}")
	public void setSwsjRoleId(String swsjRoleId) {
		PropConstant.swsjRoleId = swsjRoleId;
	}

	public static String getSzRoleId() {
		return szRoleId;
	}
	@Value("${properties.common.sz_role_id:}")
	public void setSzRoleId(String szRoleId) {
		PropConstant.szRoleId = szRoleId;
	}

	public static String getMonthWorkModifyStatus() {
		return monthWorkModifyStatus;
	}
	@Value("${properties.monthWorkModifyStatus:}")
	public void setMonthWorkModifyStatus(String monthWorkModifyStatus) {
		PropConstant.monthWorkModifyStatus = monthWorkModifyStatus;
	}
}
