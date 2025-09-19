package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionSign;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;

import java.util.List;
import java.util.Map;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/16 18:01
 */
public interface ISupervisionSignService extends BaseService<SupervisionSign> {

	/**
	 * 保存签收记录
	 * @param supervisionInfo
	 * @return
	 */
	R saveSignInfo(SupervisionInfo supervisionInfo);

	/**
	 * 保存签收记录
	 * @param deptType
	 * @param deptId
	 * @param servId
	 * @return
	 */
	R saveSignInfo(String deptType,String deptId,String servId);

	/**
	 * 保存汇报记录
	 * @param deptType
	 * @param deptId
	 * @param servId
	 * @return
	 */
	boolean saveReportInfo(String deptType, Long deptId, String servId);

	List<SupervisionSign> getOverdueNoSignList();
}
