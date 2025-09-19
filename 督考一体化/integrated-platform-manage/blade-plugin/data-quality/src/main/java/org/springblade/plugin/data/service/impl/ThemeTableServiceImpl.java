package org.springblade.plugin.data.service.impl;

import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.plugin.data.dto.ThemeTableDTO;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.ThemeTable;
import org.springblade.plugin.data.mapper.ThemeTableMapper;
import org.springblade.plugin.data.service.IStructureMetadataService;
import org.springblade.plugin.data.vo.ThemeTableTree;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IThemeTableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ThemeTable的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class ThemeTableServiceImpl extends BaseServiceImpl<ThemeTableMapper, ThemeTable> implements IThemeTableService {
	private IStructureMetadataService structureMetadataService;

	@Override
	public List<ThemeTable> getByModelId(Long modelId) {
		return baseMapper.getByModelId(modelId);
	}

	@Override
	public boolean deleteByThemeId(Long id) {
		return baseMapper.deleteByThemeId(id) > 0;
	}

	@Override
	public boolean deleteByModelId(Long modelId) {
		return baseMapper.deleteByModelId(modelId) > 0;
	}

	@Override
	public List<ThemeTable> getOtherThemeTablesByThemeId(String themeId) {
		return baseMapper.selectOtherThemeTablesByThemeId(themeId);
	}

	@Override
	public List<ThemeTableTree> getThemeTableTree(List<String> themeIds) {
		ArrayList<ThemeTableTree> arrayList = new ArrayList<>();
		themeIds.forEach(themeId -> {
			ThemeTable themeTable = getById(themeId);
			ThemeTableTree themeTableTree = BeanUtil.copy(themeTable, ThemeTableTree.class);
			List<StructureMetadata> metadataList = structureMetadataService.getByThemeId(Long.parseLong(themeId));
			themeTableTree.setChildren(metadataList);
			themeTableTree.setHasChildren(CollectionUtil.isNotEmpty(metadataList));
			arrayList.add(themeTableTree);
		});
		return arrayList;
	}

	@Override
	public List<Map<String, String>> getTitleIdList() {
		return baseMapper.getTitleIdList();
	}
}
