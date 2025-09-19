package org.springblade.plugin.data.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.XxlJobInfo;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.config.YmlUtil;
import org.springblade.plugin.data.database.DatabaseHandler;
import org.springblade.plugin.data.database.result.JDBCHandler;
import org.springblade.plugin.data.dto.QualityTestingProgrammeDTO;
import org.springblade.plugin.data.entity.*;
import org.springblade.plugin.data.service.*;
import org.springblade.plugin.data.util.ChineseToEn;
import org.springblade.plugin.data.util.DateTimeToCron;
import org.springblade.plugin.data.util.ExpressionUtil;
import org.springblade.plugin.data.vo.DataTreeNode;
import org.springblade.plugin.data.vo.LWVO;
import org.springblade.plugin.data.vo.QualityTestingProgrammeVO;
import org.springblade.plugin.data.vo.TJVO;
import org.springblade.plugin.data.wrapper.QualityTestingProgrammeWrapper;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.entity.DictBiz;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springframework.stereotype.Service;

import org.springblade.plugin.data.mapper.QualityTestingProgrammeMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * QualityTestingProgramme的服务接口的实现类
 *
 * @author
 */
@Service
@AllArgsConstructor
public class QualityTestingProgrammeServiceImpl extends BaseServiceImpl<QualityTestingProgrammeMapper, QualityTestingProgramme> implements IQualityTestingProgrammeService {
	private IQualityTestingProgrammeRuleService programmeRuleService;
	private IManageRuleService manageRuleService;
	private YmlUtil ymlUtil;
	private IBusinessModelService modelService;
	private IQualityTestingLogService logService;
	private IDatasourceService datasourceService;
	private IThemeTableService themeTableService;
	private IRelationRuleService relationRuleService;
	private ExpressionUtil expressionUtil;
	private final static Logger logger = LoggerFactory.getLogger(QualityTestingProgrammeServiceImpl.class);

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveQualityTestingProgramme(QualityTestingProgrammeDTO qualityTestingProgrammeDTO) {
		//保存方案
		QualityTestingProgramme qualityTestingProgramme = BeanUtil.copyProperties(qualityTestingProgrammeDTO, QualityTestingProgramme.class);
		save(qualityTestingProgramme);
		if (StringUtil.isNotBlank(qualityTestingProgrammeDTO.getChoseRule())) {
			Func.toLongList(qualityTestingProgrammeDTO.getChoseRule()).forEach(ruleId -> {
				QualityTestingProgrammeRule rule = new QualityTestingProgrammeRule();
				rule.setManageRuleId(ruleId);
				rule.setQualityTestingProgrammeId(qualityTestingProgramme.getId());
				programmeRuleService.save(rule);
			});
		}
		addJob(qualityTestingProgramme);
		qualityTestingProgrammeDTO.setId(qualityTestingProgramme.getId());
		qualityTestingProgrammeDTO.setJobId(qualityTestingProgramme.getJobId());
		return true;
	}

	@Override
	public boolean startJob(QualityTestingProgramme qualityTestingProgramme) {
		AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
		ReturnT<String> start = adminBizClient.start(qualityTestingProgramme.getJobId());
		if (start.getCode() == 200) {
			qualityTestingProgramme.setPlanTask("1");
		} else {
			qualityTestingProgramme.setPlanTask("0");
		}
		updateById(qualityTestingProgramme);
		return start.getCode() == 200;
	}

	@Override
	public boolean stopJob(QualityTestingProgramme qualityTestingProgramme) {
		AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
		ReturnT<String> returnT = adminBizClient.stop(qualityTestingProgramme.getJobId());
		if (returnT.getCode() == 200) {
			//停止成功，状态改变成0
			qualityTestingProgramme.setPlanTask("0");
			return updateById(qualityTestingProgramme);
		}
		return false;
	}

	@Override
	public boolean trigger(QualityTestingProgrammeDTO qualityTestingProgramme) {
		//参数 qualityTestingProgramme.getId()
		AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
		XxlJobInfo xxlJobInfo = new XxlJobInfo();
		xxlJobInfo.setId(qualityTestingProgramme.getJobId());
		String param1 = qualityTestingProgramme.getId().toString();//方案ID
		String param2 = "";
		Long userId = AuthUtil.getUserId();
		if (userId != null) {
			User user = UserCache.getUser(userId);
			//userName作为参数
			param2 = user.getName();//执行人员
		}
		xxlJobInfo.setExecutorParam(new StringBuffer(param1).append("|").append(param2).append("|").append(qualityTestingProgramme.getStatisticalCycle()).toString());
		ReturnT<String> trigger = adminBizClient.trigger(xxlJobInfo);
		return trigger.getCode() == 200;
	}

	@Override
	public QualityTestingProgrammeVO getDetail(String id) {
		QualityTestingProgramme programme = getById(id);
		QualityTestingProgrammeVO programmeVO = BeanUtil.copyProperties(programme, QualityTestingProgrammeVO.class);
		//获取choseRule 选中的规则和qualityTestingRuleData 关联的规则数据
		List<String> ruleIds = programmeRuleService.getRuleIdByProgrammeId(id);
		programmeVO.setChoseRule(Func.join(ruleIds));
		programmeVO.setQualityTestingRuleData(manageRuleService.listByIds(ruleIds));
		programmeVO.setCycleStr(QualityTestingProgrammeWrapper.getCycleStr(programme));
		return programmeVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeQualityTestingProgrammes(String ids) {
		Func.toStrList(ids).forEach(id -> {
			removeQualityTestingProgramme(id);
		});
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeQualityTestingProgramme(String id) {
		QualityTestingProgramme programme = getById(id);
		//第一步 删除质检方案和规则关联表记录
		//第二步 删除定时任务
		//第三步 删除质检方案
		AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
		ReturnT<String> remove = adminBizClient.remove(programme.getJobId());
		return programmeRuleService.removeByProgrammeId(id) && remove.getCode() == 200 && removeById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateQualityTestingProgramme(QualityTestingProgrammeDTO qualityTestingProgrammeDTO) {
		QualityTestingProgramme newProgramme = BeanUtil.copyProperties(qualityTestingProgrammeDTO, QualityTestingProgramme.class);
		//判断质检方案和规则关联表记录需不需要修改
		List<String> newRules = Func.toStrList(qualityTestingProgrammeDTO.getChoseRule());
		List<String> oldRules = programmeRuleService.getRuleIdByProgrammeId(qualityTestingProgrammeDTO.getId().toString());
		newRules.forEach(id -> {
			if (!oldRules.contains(id)) {
				//旧的里面没有的需要添加
				QualityTestingProgrammeRule programmeRule = new QualityTestingProgrammeRule();
				programmeRule.setQualityTestingProgrammeId(qualityTestingProgrammeDTO.getId());
				programmeRule.setManageRuleId(Long.parseLong(id));
				programmeRuleService.save(programmeRule);
			}
		});
		oldRules.forEach(id -> {
			if (!newRules.contains(id)) {
				//新的里面没有的需要删除
				programmeRuleService.removeByIds(qualityTestingProgrammeDTO.getId().toString(), id);
			}
		});
		QualityTestingProgramme oldProgramme = getById(qualityTestingProgrammeDTO.getId());
		//判断定时任务是否需要修改
		if (!(StringUtil.equals(oldProgramme.getCycle(), qualityTestingProgrammeDTO.getCycle())
			&& StringUtil.equals(oldProgramme.getDatetime(), qualityTestingProgrammeDTO.getDatetime())
			&& StringUtil.equals(oldProgramme.getDate(), qualityTestingProgrammeDTO.getDate())
			&& StringUtil.equals(oldProgramme.getTime(), qualityTestingProgrammeDTO.getTime())
			&& StringUtil.equals(oldProgramme.getMonth(), qualityTestingProgrammeDTO.getMonth())
			&& StringUtil.equals(oldProgramme.getWeek(), qualityTestingProgrammeDTO.getWeek()))) {
			//需要修改
			AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
			ReturnT<String> returnT = adminBizClient.updateJob(getJob(newProgramme));
			if (returnT.getCode() != 200) {
				throw new ServiceException("定时任务修改失败！");
			}
		}
		//对比新的质检方案和旧的质检方案，判断定时任务要不要停止或者启动
		if (!StringUtil.equals(oldProgramme.getPlanTask(), newProgramme.getPlanTask())) {
			//新旧定时任务状态不一样 则需要修改状态
			AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
			if (StringUtil.equals("1", newProgramme.getPlanTask())) {
				//启动任务
				ReturnT<String> start = adminBizClient.start(qualityTestingProgrammeDTO.getJobId());
				if (start.getCode() != 200) {
					//启动失败，则任务状态改为停止
					newProgramme.setPlanTask("0");
				}
			} else {
				//停止任务
				ReturnT<String> stop = adminBizClient.stop(qualityTestingProgrammeDTO.getJobId());
				if (stop.getCode() != 200) {
					newProgramme.setPlanTask("1");
				}
			}
		}
		//修改质检方案
		return updateById(newProgramme);
	}

	@Override
	public boolean testDataQuality(String param) {
		//第一步 获取当前时间，以便保存日志
		long start = new Date().getTime();
		//准备工作判断是自动执行还是人为触发，人为触发需要切割参数
		String programmeId = "";
		String executor = "";
		String currentCycle = "";
		if (!param.contains("|")) {
			//自动
			programmeId = param;
			executor = "自动执行";
			currentCycle = preCycle(programmeId);
		} else {
			String[] split = param.split("\\|");
			programmeId = split[0];//第一个是方案Id
			executor = split[1];//执行者
			currentCycle = split[2];//周期第一天
		}
		QualityTestingLog qualityTestingLog = new QualityTestingLog();
		qualityTestingLog.setErrorVolume(0l);
		qualityTestingLog.setDataVolume(0l);
		qualityTestingLog.setQualityTestingProgrammeId(Long.parseLong(programmeId));
		qualityTestingLog.setQualityTestingTime(new Date());
		qualityTestingLog.setExecutor(executor);
		qualityTestingLog.setCurrentCycle(DateUtil.parse(currentCycle, DateUtil.PATTERN_DATE));
		qualityTestingLog.setStatisticalTime(new Date());
		try {
			//第二步 获取质检方案信息，以便查询质检规则、判断是否同步上期例外、是否通知并获取模型相关数据库和结果表名等信息
			QualityTestingProgramme programme = getById(programmeId);
			//第三步获取数据库信息、结果表名
			BusinessModel businessModel = modelService.getById(programme.getModelId());
			qualityTestingLog.setModelId(businessModel.getId());
			qualityTestingLog.setCycleType(businessModel.getStatisticalType());
			String tableNamePrefix = ChineseToEn.getAllFirstLetter(businessModel.getModelName());
			//第四步目标数据库信息和处理器
			Datasource datasource = datasourceService.getById(businessModel.getDatasourceId());
			//第五步 获取启用状态的质检规则 并遍历查询
			List<ManageRule> testingDataQualityRules = manageRuleService.getTestingDataQualityRules(programmeId);
			testingDataQualityRules.forEach(manageRule -> {
				//根据类型开始做相应的查询
				switch (manageRule.getType()) {
					case "Null":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + NullCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "RANGE":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + rangeCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "LOGIC":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + logicCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "REPEAT":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + repeatCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "TIMELY":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + timelyCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "STANDARD":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + standardCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
					case "QUOTE":
						qualityTestingLog.setErrorVolume(qualityTestingLog.getErrorVolume() + quoteCheck(tableNamePrefix, programme, manageRule, datasource, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
						break;
				}
				//数据量
				qualityTestingLog.setDataVolume(qualityTestingLog.getDataVolume() + dataVolume(datasource, manageRule, businessModel, DateUtil.format(qualityTestingLog.getCurrentCycle(), DateUtil.PATTERN_DATE)));
			});
			//最后一步 存入日志
			qualityTestingLog.setQualityTestingResult("1");
			qualityTestingLog.setTimeConsuming(new Date().getTime() - start);
			logService.save(qualityTestingLog);
			return true;
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			qualityTestingLog.setQualityTestingResult("2");//质检失败
			qualityTestingLog.setTimeConsuming(new Date().getTime() - start);
			logService.save(qualityTestingLog);
			return false;
		}

	}

	@Override
	public IPage<TJVO> getTJPageList(Map<String, String> tjParam, Query query) {
		//获取表名，组装查询语句
		String table_tj = tableName(tjParam, "_TJ");
		StringBuffer querySql = new StringBuffer("select * from ").append(table_tj).append(" where 1=1 ");
		StringBuffer totalSql = new StringBuffer("select count(1) total from ").append(table_tj).append(" where 1=1 ");
		StringBuffer conditions = SQLConditions(tjParam);
		//分页计算 起始条数
		StringBuffer limit = SQLLimit(query);
		Long totalCount = JDBCHandler.getTotalCount(totalSql.append(conditions).toString());
		List<TJ> tjList = JDBCHandler.getTJList(querySql.append(conditions).append(" order by statistics_time ").append(limit).toString());
		ArrayList<TJVO> tjvos = new ArrayList<>();
		tjList.forEach(tj -> {
			TJVO tjvo = BeanUtil.copyProperties(tj, TJVO.class);
			ManageRule manageRule = manageRuleService.getById(tjvo.getManage_rule_id());
			tjvo.setCode(manageRule.getCode());
			tjvo.setDescribe(manageRule.getDescribe());
			tjvos.add(tjvo);
		});
		IPage<TJVO> page = Condition.getPage(query);
		page.setTotal(totalCount);
		page.setRecords(tjvos);
		return page;
	}

	/**
	 * 拼接分页语句
	 *
	 * @param query
	 * @return
	 */
	private StringBuffer SQLLimit(Query query) {
		Integer pageSize = query.getSize();
		Integer currentPage = query.getCurrent();
		return new StringBuffer(" limit ").append((currentPage - 1) * pageSize).append(",").append(pageSize);
	}

	/**
	 * 拼接查询条件
	 *
	 * @param param
	 * @return
	 */
	private StringBuffer SQLConditions(Map<String, String> param) {
		StringBuffer conditions = new StringBuffer();
		for (Map.Entry<String, String> entry : param.entrySet()) {
			if ((!StringUtil.equals("current", entry.getKey())) && (!StringUtil.equals("size", entry.getKey()))) {
				if (Func.isNotEmpty(entry.getValue())) {
					conditions.append(" and ").append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
				}
			}
		}
		return conditions;
	}

	/**
	 * 获取查询表表名
	 *
	 * @param param
	 * @param suf
	 * @return
	 */
	private String tableName(Map<String, String> param, String suf) {
		String model_id = param.get("model_id");
		BusinessModel model = modelService.getById(model_id);
		return ChineseToEn.getAllFirstLetter(model.getModelName()) + suf;
	}

	@Override
	public IPage<JL> getJLPageList(Map<String, String> jlParam, Query query) {
		//获取表名，组装查询语句
		String table_jl = tableName(jlParam, "_JL");
		StringBuffer querySql = new StringBuffer("select * from ").append(table_jl).append(" where 1=1 ");
		StringBuffer limit = SQLLimit(query);
		StringBuffer totalSql = new StringBuffer("select count(1) total from ").append(table_jl).append(" where 1=1 ");
		StringBuffer conditions = SQLConditions(jlParam);
		Long totalCount = JDBCHandler.getTotalCount(totalSql.append(conditions).toString());
		List<JL> jlList = JDBCHandler.getJLList(querySql.append(conditions).append(" order by record_time ").append(limit).toString());
		IPage<JL> page = Condition.getPage(query);
		page.setTotal(totalCount);
		page.setRecords(jlList);
		return page;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean setAsException(String jlStr) {
		//jl修改是否例外为例外
		//lw表插入数据
		//tj表修改例外合计
		HashMap hashMap = JSONObject.parseObject(jlStr, HashMap.class);
		hashMap.put("if_exception", "1");
		JL jl = JSONObject.parseObject(jlStr, JL.class);
		String tableName = ChineseToEn.getAllFirstLetter(modelService.getById(jl.getModel_id()).getModelName());
		return JDBCHandler.updateJLById(hashMap, tableName.concat("_JL")) > 0 && JDBCHandler.saveLWByJL(jl, tableName.concat("_LW")) > 0 && JDBCHandler.totalColumnAddOne(tableName.concat("_TJ"), "total_exception", jl.getTj_id()) > 0;
	}

	@Override
	public IPage<LW> getLWPageList(Map<String, String> lwParam, Query query) {
		String table_lw = tableName(lwParam, "_LW");
		StringBuffer limit = SQLLimit(query);
		StringBuffer querySql = new StringBuffer("select * from ").append(table_lw).append(" where 1=1 ");
		StringBuffer totalSql = new StringBuffer("select count(1) total from ").append(table_lw).append(" where 1=1 ");
		StringBuffer conditions = SQLConditions(lwParam);
		Long totalCount = JDBCHandler.getTotalCount(totalSql.append(conditions).toString());
		List<LW> lwList = JDBCHandler.getLWList(querySql.append(conditions).append(" order by record_time ").append(limit).toString());
		IPage<LW> page = Condition.getPage(query);
		page.setTotal(totalCount);
		page.setRecords(lwList);
		return page;
	}

	@Override
	public IPage<LWVO> getLWStatisticPageList(Map<String, String> lwParam, Query query) {
		String table_lw = tableName(lwParam, "_LW");
		StringBuffer limit = SQLLimit(query);
		StringBuffer conditions = SQLConditions(lwParam);
		StringBuffer querySql = new StringBuffer("select manage_rule_id,COUNT(1) total from ").append(table_lw)
			.append(" where 1=1 ").append(conditions).append(" GROUP BY manage_rule_id ").append(limit);
		StringBuffer totalSql = new StringBuffer("SELECT count(1) total from (select manage_rule_id from ").append(table_lw)
			.append(" where 1=1 ").append(conditions).append(" GROUP BY manage_rule_id) t");
		Long totalCount = JDBCHandler.getTotalCount(totalSql.toString());
		List<LWVO> lws = JDBCHandler.getLWStatisticList(querySql.toString());
		lws.forEach(lwvo -> {
			ManageRule manageRule = manageRuleService.getById(lwvo.getManage_rule_id());
			lwvo.setCode(manageRule.getCode());
			lwvo.setDescribe(manageRule.getDescribe());
		});
		IPage<LWVO> page = Condition.getPage(query);
		page.setTotal(totalCount);
		page.setRecords(lws);
		return page;
	}

	@Override
	public List<DataTreeNode> getModelProgrammeTree() {
		return ForestNodeMerger.merge(baseMapper.getModelProgrammeTree());
	}

	/**
	 * 获取总数据量
	 *
	 * @param datasource
	 * @param manageRule
	 * @param businessModel
	 * @param cycleDate
	 * @return
	 */
	private Long dataVolume(Datasource datasource, ManageRule manageRule, BusinessModel businessModel, String cycleDate) {
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		StringBuffer sql = new StringBuffer("select count(1) data_volume from ").append(themeTable).append(" where 1=1").append(timeCondition(businessModel, manageRule, cycleDate));
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		return databaseHandler.countTotal(datasource, sql.toString());
	}

	/**
	 * 获取上个周期的第一天
	 *
	 * @param programmeId
	 * @return
	 */
	private String preCycle(String programmeId) {
		QualityTestingProgramme programme = getById(programmeId);
		BusinessModel businessModel = modelService.getById(programme.getModelId());
		String cycle = "";
		Date now = new Date();
		Calendar calendar = Calendar.getInstance();
		switch (businessModel.getStatisticalType()) {
			case "year"://去年一月一号
				cycle = (Integer.parseInt(DateUtil.format(now, "yyyy")) - 1) + "-01-01";
				break;
			case "quarter"://上个季度第一天
				calendar.set(Calendar.MONTH, ((int) calendar.get(Calendar.MONTH) / 3 - 1) * 3);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				cycle = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
			case "month"://上个月第一天
				calendar.add(Calendar.MONTH, -1);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				cycle = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
			case "date":
				calendar.add(Calendar.DATE, -1);
				cycle = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
		}
		return cycle;
	}

	/**
	 * 引用完整性检查
	 *
	 * @param tableNamePrefix 表名前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源数据源
	 * @return 不合格数量
	 */
	private Integer quoteCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//组装SQL
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		//先获取查询条件集
		StringBuffer conditions = new StringBuffer(" not in (");
		if (StringUtil.equals("1", manageRule.getReferenceMode())) {
			//字典值
			List<DictBiz> dictBizList = DictBizCache.getList(DictBizCache.getById(manageRule.getDicBizId()).getCode());
			dictBizList.forEach(dictBiz -> {
				conditions.append("'").append(dictBiz.getDictKey()).append("',");
			});
			conditions.deleteCharAt(conditions.length() - 1);
			conditions.append(")");
		} else {
			//表字段 查询
			Datasource referenceDatasource = datasourceService.getById(manageRule.getDatasourceId());
			DatabaseHandler referenceHandler = DatabaseHandler.HANDLER_MAP.get(referenceDatasource.getDriverClass());
			conditions.append(referenceHandler.getConditions(referenceDatasource, manageRule.getTableName(), manageRule.getFieldName()));
		}
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value ,")
			.append(manageRule.getCheckField()).append(" as check_column_value from ").append(themeTable)
			.append(" where ").append(manageRule.getCheckField()).append(conditions).append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 规范检查
	 *
	 * @param tableNamePrefix 结果表前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源数据源
	 * @return 不合格数量
	 */
	private Integer standardCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//组装正则表达式
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		StringBuffer regexp = new StringBuffer();
		switch (manageRule.getSpecificationType()) {
			case "1"://身份证号码
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[1-9][[:digit:]]{5}(18|19|20|(3[[:digit:]]))[[:digit:]]{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)[[:digit:]]{3}[0-9Xx]$").append("'");
				break;
			case "2"://手机号码
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[1][35678][0-9]{9}$").append("'");
				break;
			case "3"://IP地址
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[0-9]{1,3}\\\\.[0-9]{1,3}\\\\.[0-9]{1,3}\\\\.[0-9]{1,3}$").append("'");
				break;
			case "4"://网址
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^(https?|ftp):\\/\\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\\/($|[a-zA-Z0-9.,?\\'\\\\+&%$#=~_-]+))*$").append("'");
				break;
			case "5"://邮编
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[1-9][[:digit:]]{5}$").append("'");
				break;
			case "6"://固定电话
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^([0-9]{3,4}-)?[0-9]{7,8}$").append("'");
				break;
			case "7"://日期
				switch (manageRule.getSpecificationSubtype()) {
					case "1"://yyyy-MM-dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "2"://yyyyMMdd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "3"://yyyy.MM.dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}.(0[1-9]|1[0-2]).(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "4"://yyyy/MM/dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "5"://yyMMdd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{1}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "6"://yy-MM-dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{1}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "7"://yy.MM.dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{1}.(0[1-9]|1[0-2]).(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
					case "8"://yy/MM/dd
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{1}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1])$").append("'");
						break;
				}
				break;
			case "8"://时间
				switch (manageRule.getSpecificationSubtype()) {
					case "1"://HHmmss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^(20|21|22|23|[0-1][[:digit:]])[0-5][[:digit:]][0-5][[:digit:]]$").append("'");
						break;
					case "2"://HH:mm:ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^(20|21|22|23|[0-1][[:digit:]]):[0-5][[:digit:]]:[0-5][[:digit:]]$").append("'");
						break;
					case "3"://HH mm ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^(20|21|22|23|[0-1][[:digit:]]) [0-5][[:digit:]] [0-5][[:digit:]]$").append("'");
						break;
				}
				break;
			case "9"://日期时间
				switch (manageRule.getSpecificationSubtype()) {
					case "1"://yyyy-MM-dd HH:mm:ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (20|21|22|23|[0-1][[:digit:]]):[0-5][[:digit:]]:[0-5][[:digit:]]$").append("'");
						break;
					case "2"://yyyyMMddHH:mm:ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])(20|21|22|23|[0-1][[:digit:]]):[0-5][[:digit:]]:[0-5][[:digit:]]$").append("'");
						break;
					case "3"://yyyy/MM/dd HH:mm:ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}/(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[0-1]) (20|21|22|23|[0-1][[:digit:]]):[0-5][[:digit:]]:[0-5][[:digit:]]$").append("'");
						break;
					case "4"://yyyy.MM.dd HH:mm:ss
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^[1-9][[:digit:]]{3}.(0[1-9]|1[0-2]).(0[1-9]|[1-2][0-9]|3[0-1]) (20|21|22|23|[0-1][[:digit:]]):[0-5][[:digit:]]:[0-5][[:digit:]]$").append("'");
						break;
				}
				break;
			case "10"://时间戳
			case "11"://整数
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^-?[0-9][[:digit:]]*$").append("'");
				break;
			case "12"://正整数
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[1-9][[:digit:]]*$").append("'");
				break;
			case "13"://小数
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[0-9][[:digit:]]*.[0-9][[:digit:]]*$").append("'");
				break;
			case "14"://字母
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^[a-zA-Z]*$").append("'");
				break;
			case "15"://长度范围
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append("^.{").append(manageRule.getMinLength()).append(",").append(manageRule.getMaxLength()).append("}$").append("'");
				break;
			case "16"://包含字符串
				switch (manageRule.getCharPosition()) {
					case "prefix"://开头
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^").append(manageRule.getContainString()).append(".*$").append("'");
						break;
					case "suffix"://结尾
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^.*").append(manageRule.getContainString()).append("$").append("'");
						break;
					case "any"://任意位置
						regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
						regexp.append("^.*").append(manageRule.getContainString()).append(".*$").append("'");
						break;
					case "specific"://指定位置
						regexp.append(" instr(").append(manageRule.getCheckField()).append(",").append(manageRule.getContainString()).append(") !=").append(manageRule.getSpecificPosition());
						break;
				}
				break;
			case "17"://正则表达式
				regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '");
				regexp.append(manageRule.getRegex()).append("'");
				break;
		}
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value ,")
			.append(manageRule.getCheckField()).append(" as check_column_value from ").append(themeTable).append(" where ").append(regexp).append(timeCondition(businessModel, manageRule, cycleDate));
//		regexp.append(manageRule.getCheckField()).append(" NOT REGEXP '").append(regexp).append("'").append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 及时性检查
	 *
	 * @param tableNamePrefix 结果表前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源数据源
	 * @return 不合格数量
	 */
	private Integer timelyCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//拼接SQL
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		String checkField = manageRule.getCheckField();//oracle中如果是字符类型则需要转换成date类型
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value ,")
			.append(manageRule.getCheckField()).append(" as check_column_value from ").append(themeTable)
			.append(" where abs(DATEDIFF(").append(manageRule.getCheckFormula()).append(",")
			.append(checkField).append(")) > ").append(manageRule.getMaxAllowableErrorDays()).append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 重复性检查
	 *
	 * @param tableNamePrefix 结果表前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源数据源
	 * @return 不合格数量
	 */
	private Integer repeatCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//拼接SQL
		StringBuffer concatStr = new StringBuffer("concat(");
		List<String> checkFields = Func.toStrList(manageRule.getCheckField());
		checkFields.forEach(checkField -> {
			concatStr.append(checkField).append(",'|',");
		});
		//todo 看看有没有删错
		concatStr.delete(concatStr.length() - 5, concatStr.length());
		concatStr.append(")");
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value ,").append(concatStr)
			.append(" as check_column_value FROM ").append(themeTable).append(" where ").append(concatStr)
			.append(" in ( select ").append(concatStr).append(" from ").append(themeTable).append(" GROUP BY ").append(manageRule.getCheckField())
			.append(" HAVING COUNT(1)>1 )").append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 逻辑检查
	 *
	 * @param tableNamePrefix 结果表前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源数据源
	 * @return 错误数量
	 */
	private Integer logicCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//拼接SQL
		String themeTable = themeTableService.getById(manageRule.getThemeId()).getName();
		StringBuffer sql = new StringBuffer("select ").append(themeTable).append(".").append(jl.getKey_column()).append(" as key_value , null as check_column_value FROM ").append(themeTable).append(" ").append(themeTable);
		//拼接关联关系
		List<RelationRule> relations = relationRuleService.list(Wrappers.<RelationRule>query().lambda().eq(RelationRule::getRuleId, manageRule.getId()));
		relations.forEach(relation -> {
			switch (relation.getJoinType()) {
				case "1":
					sql.append(" left join ");
					break;
				case "2":
					sql.append(" right join ");
					break;
				case "3":
					sql.append(" inner join ");
					break;
				default:
					sql.append(" full join ");
					break;
			}
			sql.append(relation.getJoinTableName()).append(" ").append(relation.getJoinTableName()).append(" on ")
				.append(themeTable).append(".").append(relation.getCheckJoinField()).append(" = ").append(relation.getJoinTableName()).append(".").append(relation.getThemeJoinFieldName());
		});
		//翻译成正确的SQL
		String checkFormula = expressionUtil.translate(manageRule.getCheckFormula(), datasource.getDriverClass());
		sql.append(" where ").append(checkFormula).append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 范围检查
	 *
	 * @param tableNamePrefix 结果表表名前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源表数据源
	 * @return 错误总数量
	 */
	private Integer rangeCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//拼接SQL
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value , ")
			.append(manageRule.getCheckField()).append(" as check_column_value from ")
			.append(themeTableService.getById(manageRule.getThemeId()).getName()).append(" where ")
			.append(manageRule.getCheckField()).append(" < '").append(manageRule.getMinValue())
			.append("' or ").append(manageRule.getCheckField()).append(" > '").append(manageRule.getMaxValue()).append("'").append(timeCondition(businessModel, manageRule, cycleDate));
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 空值检查
	 *
	 * @param tableNamePrefix 结果表表名前缀
	 * @param programme       质检方案
	 * @param manageRule      质检规则
	 * @param datasource      源表数据源
	 * @return 错误总数量
	 */
	private Integer NullCheck(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		JL jl = getJLInfo(tableNamePrefix, programme, manageRule, datasource, businessModel, cycleDate);
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		//第4步 按照规则查询源表
		//拼接SQL
		StringBuffer sql = new StringBuffer("select ").append(jl.getKey_column()).append(" as key_value , concat(");
		List<String> checkFields = Func.toStrList(manageRule.getCheckField());
		checkFields.forEach(checkField -> {
			sql.append(checkField).append(",'|',");
		});
		sql.delete(sql.length() - 5, sql.length());
		sql.append(") as check_column_value from ").append(themeTableService.getById(manageRule.getThemeId()).getName()).append(" where 1=1 ");
		checkFields.forEach(checkField -> {
			sql.append(" and ").append(checkField).append(" is not null ");
		});
		Integer errorTotal = databaseHandler.errorTotal(datasource, sql.append(timeCondition(businessModel, manageRule, cycleDate)).toString(), programme.getSync(), jl, tableNamePrefix);
		return errorTotal;
	}

	/**
	 * 获取结果表关键信息 用JL封装
	 *
	 * @return
	 */
	private JL getJLInfo(String tableNamePrefix, QualityTestingProgramme programme, ManageRule manageRule, Datasource datasource, BusinessModel businessModel, String cycleDate) {
		//第1步 获取结果表所在库的数据源（即本模块所在数据源）
		Datasource currentDatasource = ymlUtil.currentDatasource();
		DatabaseHandler currentDatabaseHandler = DatabaseHandler.HANDLER_MAP.get(currentDatasource.getDriverClass());
		//第2步 获取上期期数（查询TJ表，按照period倒序获取一个，null是0）
		Integer period = currentDatabaseHandler.getPeriod(currentDatasource, tableNamePrefix.concat("_TJ"), programme.getId(), manageRule.getId(), manageRule.getThemeId());
		//第3步 先查询出主题表名字按照规则的themeId,然后查询出表主键等，待用
		ThemeTable themeTable = themeTableService.getById(manageRule.getThemeId());
		String tableName = themeTable.getName();
		DatabaseHandler databaseHandler = DatabaseHandler.HANDLER_MAP.get(datasource.getDriverClass());
		Map<String, String> pk = databaseHandler.getPK(datasource, tableName);
		String pk_name = pk.get("COLUMN_NAME");
		String pk_remarks = pk.get("REMARKS");
		JL jl = new JL();
		jl.setCheck_column(manageRule.getCheckField());
		jl.setKey_column(pk_name);
		jl.setKey_comment(pk_remarks);
		jl.setManage_rule_id(manageRule.getId());
		jl.setQuality_testing_programme_id(programme.getId());
		jl.setPeriod(period);
		jl.setCycle_type(businessModel.getStatisticalType());
		jl.setCurrent_cycle(DateUtil.parse(cycleDate, DateUtil.PATTERN_DATE));
		jl.setModel_id(businessModel.getId());
		String id = UUID.randomUUID().toString().replaceAll("-", "");
		jl.setTj_id(id + new Date().getTime());
		jl.setTheme_id(manageRule.getThemeId());
		return jl;
	}

	/**
	 * 组装质检语句的时间范围条件
	 *
	 * @param businessModel 业务模型
	 * @param manageRule    质检规则
	 * @param cycleDate     周期字段（质检周期第一天 字符串 yyyy-MM-dd）
	 * @return 时间范围SQL条件
	 */
	private StringBuffer timeCondition(BusinessModel businessModel, ManageRule manageRule, String cycleDate) {
		ThemeTable themeTable = themeTableService.getById(manageRule.getThemeId());
		String endDate = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DateUtil.parse(cycleDate, DateUtil.PATTERN_DATE));
		switch (businessModel.getStatisticalType()) {
			case "date":
				endDate = cycleDate;
				break;
			case "month":
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				endDate = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
			case "quarter":
				calendar.set(Calendar.MONTH, ((calendar.get(Calendar.MONTH)) / 3) * 3 + 2);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				endDate = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
			case "year":
				calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
				endDate = DateUtil.format(calendar.getTime(), DateUtil.PATTERN_DATE);
				break;
		}
		return new StringBuffer(" and ")
			.append(themeTable.getQualityTestingTimeField()).append(" >= '").append(cycleDate).append(" 00:00:00'")
			.append(" and ").append(themeTable.getQualityTestingTimeField()).append(" <= '").append(endDate).append(" 23:59:59'");
	}

	/**
	 * 根据质检方案拼接定时任务表达式
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	private String Cron(QualityTestingProgramme qualityTestingProgramme) {
		String cron = "";
		switch (qualityTestingProgramme.getCycle()) {
			case "1":
				cron = DateTimeToCron.datetimeToCron(qualityTestingProgramme.getDatetime());
				break;
			case "2":
				cron = DateTimeToCron.timeToCron(qualityTestingProgramme.getTime());
				break;
			case "3":
				cron = DateTimeToCron.weekTimeToCron(qualityTestingProgramme.getWeek(), qualityTestingProgramme.getTime());
				break;
			case "4":
				cron = DateTimeToCron.dayTimeToCron(qualityTestingProgramme.getDate(), qualityTestingProgramme.getTime());
				break;
			case "5":
				cron = DateTimeToCron.monthDayTimeToCron(qualityTestingProgramme.getMonth(), qualityTestingProgramme.getDate(), qualityTestingProgramme.getTime());
				break;
			default:
				throw new ServiceException("执行周期不存在！");
		}
		return cron;
	}

	/**
	 * 组装定时任务
	 *
	 * @param qualityTestingProgramme
	 * @return
	 */
	private XxlJobInfo getJob(QualityTestingProgramme qualityTestingProgramme) {
		XxlJobInfo xxlJobInfo = new XxlJobInfo();
		xxlJobInfo.setExecutorParam(qualityTestingProgramme.getId().toString());//参数直接携带质检方案ID，方便执行任务时查询
		xxlJobInfo.setJobGroup(1);//执行器主键id--示例执行器
		xxlJobInfo.setJobDesc(qualityTestingProgramme.getName());//任务描述
		//执行器路由策略FIRST、LAST、ROUND、RANDOM、CONSISTENT_HASH、LEAST_FREQUENTLY_USED、LEAST_RECENTLY_USED、FAILOVER、BUSYOVER、SHARDING_BROADCAST
		xxlJobInfo.setExecutorRouteStrategy("FIRST");
		xxlJobInfo.setJobCron(Cron(qualityTestingProgramme));
		xxlJobInfo.setGlueType("BEAN");//运行模式
		xxlJobInfo.setExecutorHandler("qualityTestingHandler");//执行器
		xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");//阻塞处理策略：SERIAL_EXECUTION（单机串行）、DISCARD_LATER（丢弃后续调度）、COVER_EARLY（覆盖之前调度）
		xxlJobInfo.setAuthor(UserCache.getUser(qualityTestingProgramme.getCreateUser()).getRealName());//作者
		if (Func.isNotEmpty(qualityTestingProgramme.getJobId())) {
			xxlJobInfo.setId(qualityTestingProgramme.getJobId());
		}
		return xxlJobInfo;
	}

	/**
	 * 添加定时任务
	 *
	 * @param qualityTestingProgramme
	 */
	private void addJob(QualityTestingProgramme qualityTestingProgramme) {
		AdminBizClient adminBizClient = new AdminBizClient(ymlUtil.getAddresses(), ymlUtil.getAccessToken());
		ReturnT<String> t = adminBizClient.addJob(getJob(qualityTestingProgramme));
		//不成功则抛异常回滚
		if (t.getCode() != 200) {
			throw new ServiceException("定时任务添加失败！");
		}
		//成功则保存定时任务ID
		qualityTestingProgramme.setJobId(Integer.parseInt(t.getContent()));
		updateById(qualityTestingProgramme);
	}
}
