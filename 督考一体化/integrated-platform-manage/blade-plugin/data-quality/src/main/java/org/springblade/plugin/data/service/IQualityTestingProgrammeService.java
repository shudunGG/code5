package org.springblade.plugin.data.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.plugin.data.dto.QualityTestingProgrammeDTO;
import org.springblade.plugin.data.entity.JL;
import org.springblade.plugin.data.entity.LW;
import org.springblade.plugin.data.entity.QualityTestingProgramme;
import org.springblade.plugin.data.vo.DataTreeNode;
import org.springblade.plugin.data.vo.LWVO;
import org.springblade.plugin.data.vo.QualityTestingProgrammeVO;
import org.springblade.plugin.data.vo.TJVO;

import java.util.List;
import java.util.Map;

/**
 * QualityTestingProgramme的服务接口
 *
 * @author
 */
public interface IQualityTestingProgrammeService extends BaseService<QualityTestingProgramme> {
	/**
	 * 保存质检方案
	 *
	 * @param qualityTestingProgrammeDTO
	 * @return
	 */
	boolean saveQualityTestingProgramme(QualityTestingProgrammeDTO qualityTestingProgrammeDTO);

	/**
	 * 启动定时任务
	 *
	 * @param qualityTestingProgramme 质检规则
	 * @return
	 */
	boolean startJob(QualityTestingProgramme qualityTestingProgramme);

	/**
	 * 停止定时任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	boolean stopJob(QualityTestingProgramme qualityTestingProgramme);

	/**
	 * 触发定时任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	boolean trigger(QualityTestingProgrammeDTO qualityTestingProgramme);

	/**
	 * 根据主键获取质检方案详情
	 *
	 * @param id
	 * @return
	 */
	QualityTestingProgrammeVO getDetail(String id);

	/**
	 * 根据主键集合批量删除
	 *
	 * @param ids
	 * @return
	 */
	boolean removeQualityTestingProgrammes(String ids);

	/**
	 * 根据主键删除质检方案
	 *
	 * @param id
	 * @return
	 */
	boolean removeQualityTestingProgramme(String id);

	/**
	 * 修改质检方案
	 *
	 * @param qualityTestingProgrammeDTO
	 * @return
	 */
	boolean updateQualityTestingProgramme(QualityTestingProgrammeDTO qualityTestingProgrammeDTO);

	/**
	 * 数据质量检测
	 *
	 * @param param
	 * @return
	 */
	boolean testDataQuality(String param);

	/**
	 * 分页查询统计结果表信息
	 *
	 * @param tjParam
	 * @param query
	 * @return
	 */
	IPage<TJVO> getTJPageList(Map<String, String> tjParam, Query query);

	/**
	 * 分页获取统计结果记录表的记录
	 *
	 * @param jlParam
	 * @param query
	 * @return
	 */
	IPage<JL> getJLPageList(Map<String, String> jlParam, Query query);

	/**
	 * 设为例外
	 *
	 * @param jlStr
	 * @return
	 */
	boolean setAsException(String jlStr);

	/**
	 * 分页查询例外表
	 *
	 * @param lwParam
	 * @param query
	 * @return
	 */
	IPage<LW> getLWPageList(Map<String, String> lwParam, Query query);

	/**
	 * 分页获取统计例外表的记录
	 *
	 * @param lwParam
	 * @param query
	 * @return
	 */
	IPage<LWVO> getLWStatisticPageList(Map<String, String> lwParam, Query query);

	/**
	 * 获取模型和方案树
	 *
	 * @return
	 */
	List<DataTreeNode> getModelProgrammeTree();
}
