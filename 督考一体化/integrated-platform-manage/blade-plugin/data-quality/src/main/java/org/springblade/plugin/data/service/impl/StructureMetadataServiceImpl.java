package org.springblade.plugin.data.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.mapper.StructureMetadataMapper;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IStructureMetadataService;

import java.util.List;

/**
 * StructureMetadata的服务接口的实现类
 *
 * @author
 */
@Service
public class StructureMetadataServiceImpl extends BaseServiceImpl<StructureMetadataMapper, StructureMetadata> implements IStructureMetadataService {


	@Override
	public List<StructureMetadata> getByThemeId(Long themeId) {
		return baseMapper.getByThemeId(themeId);
	}

	@Override
	public boolean deleteByThemeId(Long themeId) {
		return baseMapper.deleteByThemeId(themeId) > 0;
	}

	@Override
	public List<StructureMetadata> getCheckFieldsByThemeId(String themeId) {
		return baseMapper.getCheckFieldsByThemeId(themeId);
	}

	@Override
	public List<StructureMetadata> getSpecificCheckFieldsByThemeId(String themeId) {
		return baseMapper.getSpecificCheckFieldsByThemeId(themeId);
	}

	@Override
	public List<StructureMetadata> getDateTypeCheckFieldsByThemeId(String themeId) {
		return baseMapper.getDateTypeCheckFieldsByThemeId(themeId);
	}
}
