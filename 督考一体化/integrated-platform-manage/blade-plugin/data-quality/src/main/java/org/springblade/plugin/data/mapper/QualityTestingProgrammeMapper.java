package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.plugin.data.entity.QualityTestingProgramme;
import org.springblade.plugin.data.vo.DataTreeNode;

import java.util.List;

/**
 * QualityTestingProgramme的Dao接口
 *
 * @author
 */
public interface QualityTestingProgrammeMapper extends BaseMapper<QualityTestingProgramme> {
	/**
	 * 查询质检方案和模型的树形结构
	 *
	 * @return
	 */
	List<DataTreeNode> getModelProgrammeTree();
}
