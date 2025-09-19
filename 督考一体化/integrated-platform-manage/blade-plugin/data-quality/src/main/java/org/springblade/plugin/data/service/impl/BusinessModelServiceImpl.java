package org.springblade.plugin.data.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.database.DatabaseHandler;
import org.springblade.plugin.data.dto.BusinessModelDTO;
import org.springblade.plugin.data.dto.ThemeTableDTO;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.Datasource;
import org.springblade.plugin.data.entity.ThemeTable;
import org.springblade.plugin.data.service.IDatasourceService;
import org.springblade.plugin.data.service.IStructureMetadataService;
import org.springblade.plugin.data.service.IThemeTableService;
import org.springblade.plugin.data.util.ChineseToEn;
import org.springblade.plugin.data.vo.DataTreeNode;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.service.IBusinessModelService;
import org.springblade.plugin.data.mapper.BusinessModelMapper;
import org.springblade.plugin.data.entity.BusinessModel;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BusinessModel的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class BusinessModelServiceImpl extends BaseServiceImpl<BusinessModelMapper, BusinessModel> implements IBusinessModelService {

	private final static Logger logger = LoggerFactory.getLogger(BusinessModelServiceImpl.class);

	private IDatasourceService datasourceService;
	private IStructureMetadataService structureMetadataService;
	private IThemeTableService themeTableService;
	private static final String MYSQL = "mysql";
	private static final String ORACLE = "oracle";

	@Override
	public List<Map<String, String>> getTableNames(String id) {
		Datasource datasource = datasourceService.getById(id);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		return databaseHandler.getTableNames(datasource);
	}

	@Override
	public List<StructureMetadata> getTableStructure(String datasourceId, String tableName) {
		Datasource datasource = datasourceService.getById(datasourceId);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		return databaseHandler.getTableStructure(datasource, tableName);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBusinessModelInfo(BusinessModelDTO businessModel) {
		try {
			BusinessModel model = BeanUtil.copy(businessModel, BusinessModel.class);
			save(model);
			saveThemeAndMetaData(businessModel, model.getId());
			String tableNamePrefix = ChineseToEn.getAllFirstLetter(businessModel.getModelName());
			String TJ = tableNamePrefix.concat("_TJ");
			baseMapper.createTableTJ(TJ);
			String JL = tableNamePrefix.concat("_JL");
			baseMapper.createTableJL(JL);
			String LW = tableNamePrefix.concat("_LW");
			baseMapper.createTableLW(LW);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException("保存失败！");
		}
		return true;
	}

	@Override
	public BusinessModelDTO modelDetail(String id) {
		BusinessModel businessModel = getById(id);
		BusinessModelDTO modelDTO = BeanUtil.copy(businessModel, BusinessModelDTO.class);
		List<ThemeTable> themeTables = themeTableService.getByModelId(Long.parseLong(id));
		List<ThemeTableDTO> allTablesInfo = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(themeTables)) {
			themeTables.forEach(themeTable -> {
				List<StructureMetadata> metadataList = structureMetadataService.getByThemeId(themeTable.getId());
				ThemeTableDTO themeTableDTO = BeanUtil.copy(themeTable, ThemeTableDTO.class);
				themeTableDTO.setData(metadataList);
				allTablesInfo.add(themeTableDTO);
			});
		}
		modelDTO.setAllTablesInfo(allTablesInfo);
		return modelDTO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateModelInfo(BusinessModelDTO businessModel) {
		//更新模型
		BusinessModel model = BeanUtil.copy(businessModel, BusinessModel.class);
		updateById(model);


		List<ThemeTable> oldTables = themeTableService.getByModelId(businessModel.getId());
		List<ThemeTableDTO> newTables = businessModel.getAllTablesInfo();
		newTables.forEach(themeTableDTO -> {
			if (!oldTables.stream()
				.filter(item -> item.getName().equals(themeTableDTO.getName())).findAny().isPresent()) {
				saveAnThemeAndMeta(themeTableDTO, model.getId());
			}
		});
		oldTables.forEach(themeTable -> {
			if (!newTables.stream().filter(item -> item.getName().equals(themeTable.getName())).findAny().isPresent()) {
				structureMetadataService.deleteByThemeId(themeTable.getId());
				themeTableService.deleteByThemeId(themeTable.getId());
			}
		});


		return true;
	}


	private void saveAnThemeAndMeta(ThemeTableDTO themeTableDTO, Long modelId) {
		//模型Id
		ThemeTable themeTable = BeanUtil.copy(themeTableDTO, ThemeTable.class);
		themeTable.setModelId(modelId);
		themeTable.setId(null);
		themeTable.setCreateDept(null);
		themeTable.setStatus(null);
		themeTable.setCreateUser(null);
		themeTable.setUpdateUser(null);
		themeTableService.save(themeTable);
		List<StructureMetadata> metadataList = themeTableDTO.getData();
		for (StructureMetadata metadata : metadataList) {
			metadata.setThemeId(themeTable.getId());
			metadata.setId(null);
			structureMetadataService.save(metadata);
		}
	}
	/**
	 * @return void
	 * @Author MaQY
	 * @Description 保存主题表和元数据记录
	 * @Date 下午 1:41 2021/10/29 0029
	 * @Param [businessModel, modelId]
	 **/
	private void saveThemeAndMetaData(BusinessModelDTO businessModel, Long modelId) {
		List<ThemeTableDTO> allTablesInfo = businessModel.getAllTablesInfo();
		for (ThemeTableDTO themeTableDTO : allTablesInfo) {
			//模型Id
			saveAnThemeAndMeta(themeTableDTO, modelId);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteModelInfo(String ids) {
		List<String> idList = Func.toStrList(ids);
		//删除元数据
		idList.forEach(id -> {
			List<ThemeTable> tableList = themeTableService.getByModelId(Long.parseLong(id));
			tableList.forEach(table -> {
				structureMetadataService.deleteByThemeId(table.getId());
			});
			//删除主题表记录
			themeTableService.deleteByModelId(Long.parseLong(id));
			dropTables(id);
		});
		return removeByIds(idList);
	}

	/**
	 * 删除表（统计表，记录表，例外表）
	 *
	 * @param id
	 */
	private void dropTables(String id) {
		BusinessModel model = getById(id);
		String tableNamePrefix = ChineseToEn.getAllFirstLetter(model.getModelName());
		String TJ = tableNamePrefix.concat("_TJ");
		baseMapper.dropTables(TJ);
		String JL = tableNamePrefix.concat("_JL");
		baseMapper.dropTables(JL);
		String LW = tableNamePrefix.concat("_LW");
		baseMapper.dropTables(LW);
	}

	@Override
	public boolean deleteOneModelInfo(String id) {
		List<ThemeTable> tableList = themeTableService.getByModelId(Long.parseLong(id));
		tableList.forEach(table -> {
			structureMetadataService.deleteByThemeId(table.getId());
		});
		//删除主题表记录
		themeTableService.deleteByModelId(Long.parseLong(id));
		dropTables(id);
		return removeById(id);
	}

	@Override
	public List<ThemeTableDTO> getThemeInfosByTables(BusinessModel businessModel) {
		//编辑页面正在修改的主题表
		List<String> tableNameList = Func.toStrList(businessModel.getDataTables());
		//表里已经存在的主题表
		BusinessModel oldModel = getById(businessModel.getId());
		List<String> tables = Func.toStrList(oldModel.getDataTables());
		//判断每一个正在编辑的主题表是否存在在tables中，是的话查询数据库，不存在的话就查询数据源
		ArrayList<ThemeTableDTO> themeTableDTOS = new ArrayList<>();
		//数据源
		Datasource datasource = datasourceService.getById(businessModel.getDatasourceId());
		tableNameList.forEach(tableName -> {
			if (tables.contains(tableName)) {
				ThemeTable themeTable = themeTableService.getOne(Wrappers.<ThemeTable>query().lambda().eq(ThemeTable::getModelId, businessModel.getId()).eq(ThemeTable::getName, tableName));
				ThemeTableDTO themeTableDTO = BeanUtil.copy(themeTable, ThemeTableDTO.class);
				List<StructureMetadata> metadataList = structureMetadataService.getByThemeId(themeTable.getId());
				themeTableDTO.setData(metadataList);
				themeTableDTOS.add(themeTableDTO);
			} else {
				//处理器
				DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
				List<StructureMetadata> metadataList = databaseHandler.getTableStructure(datasource, tableName);
				ThemeTableDTO themeTableDTO = new ThemeTableDTO();
				themeTableDTO.setData(metadataList);
				themeTableDTO.setName(tableName);
				themeTableDTO.setTitle(tableName);
				themeTableDTOS.add(themeTableDTO);
			}
		});
		return themeTableDTOS;
	}

	@Override
	public List<DataTreeNode> getModelThemeTree() {
		return ForestNodeMerger.merge(baseMapper.getModelThemeTree());
	}

	@Override
	public String getDataBaseType(String modelId) {
		String diverClass = baseMapper.selectDriverClassByModelId(modelId);
		if (StringUtil.containsIgnoreCase(diverClass, MYSQL)) {
			return MYSQL;
		} else if (StringUtil.containsIgnoreCase(diverClass, ORACLE)) {
			return ORACLE;
		}
		return "";
	}

	@Override
	public List<DataTreeNode> getThemeRuleTree(String modelId) {
		return ForestNodeMerger.merge(baseMapper.selectThemeRuleTree(modelId));
	}

	@Override
	public String getStatisticalType(String modelId) {
		return baseMapper.getStatisticalType(modelId);
	}
}
