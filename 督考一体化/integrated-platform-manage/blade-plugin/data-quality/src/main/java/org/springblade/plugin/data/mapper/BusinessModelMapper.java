package org.springblade.plugin.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springblade.plugin.data.entity.BusinessModel;
import org.springblade.plugin.data.vo.DataTreeNode;

import java.util.List;

/**
 * BusinessModel的Dao接口
 *
 * @author
 */
public interface BusinessModelMapper extends BaseMapper<BusinessModel> {
	/**
	 * @return java.util.List<org.springblade.plugin.data.vo.DataTreeNode>
	 * @Author MaQY
	 * @Description 模型和主题表组成的树形结构
	 * @Date 下午 3:11 2021/11/3 0003
	 * @Param []
	 **/
	List<DataTreeNode> getModelThemeTree();

	/**
	 * @return java.lang.String
	 * @Author MaQY
	 * @Description 根据modelID查询数据库驱动类
	 * @Date 下午 2:32 2021/11/8 0008
	 * @Param [modelId]
	 **/
	@Select("select driver_class from blade_datasource where id =(select datasource_id from business_model where id=#{modelId})")
	String selectDriverClassByModelId(@Param("modelId") String modelId);

	/**
	 * 根据modelId查询主题表和规则树形结构数据
	 *
	 * @param modelId
	 * @return
	 */
	List<DataTreeNode> selectThemeRuleTree(String modelId);

	/**
	 * 统计表
	 *
	 * @param TJ 统计表表名
	 */
	@Update("CREATE TABLE ${TJ} (\n" +
		" `id` varchar(200) NOT NULL COMMENT '主键',\n"+
		" `statistics_time` datetime DEFAULT NULL COMMENT '统计时间',\n" +
		" `total_error` int(11) DEFAULT NULL COMMENT '错误合计',\n" +
		" `total_exception` int(11) DEFAULT NULL COMMENT '例外合计',\n" +
		" `total_repaired` int(11) DEFAULT NULL COMMENT '已修复合计',\n" +
		" `statistical_type` varchar(50) DEFAULT NULL COMMENT '统计类型',\n" +
		" `current_cycle` date DEFAULT NULL COMMENT '统计周期',\n" +
		" `theme_id` bigint(20) DEFAULT NULL COMMENT '主题表ID',\n" +
		" `manage_rule_id` bigint(20) DEFAULT NULL COMMENT '规则管理表ID',\n" +
		" `quality_testing_programme_id` bigint(20) DEFAULT NULL COMMENT '质检方案表ID',\n" +
		" `model_id` bigint(20) DEFAULT NULL COMMENT '业务模型管理表主键',\n"+
		" `period` int(9) DEFAULT NULL COMMENT '执行期数',\n" +
		"  PRIMARY KEY (`id`) USING BTREE \n"+
		") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
	void createTableTJ(@Param("TJ") String TJ);

	/**
	 * 记录表
	 *
	 * @param JL 记录表表名
	 */
	@Update("CREATE TABLE ${JL} (\n" +
		"  `id` varchar(200) NOT NULL COMMENT '主键',\n"+
		"  `check_column` varchar(500) DEFAULT NULL COMMENT '检查字段',\n" +
		"  `check_column_value` varchar(500) DEFAULT NULL COMMENT '检查字段值',\n" +
		"  `key_column` varchar(500) DEFAULT NULL COMMENT '数据表主键字段',\n" +
		"  `key_comment` varchar(500) DEFAULT NULL COMMENT '数据表主键说明',\n" +
		"  `key_value` varchar(500) DEFAULT NULL COMMENT '数据表主键值',\n" +
		"  `record_time` datetime DEFAULT NULL COMMENT '记录时间',\n" +
		"  `current_cycle` date DEFAULT NULL COMMENT '统计周期',\n" +
		"  `cycle_type` varchar(50) DEFAULT NULL COMMENT '周期类型',\n"+
		"  `if_exception` varchar(10) DEFAULT '0' COMMENT '是否例外1是0否',\n" +
		"  `manage_rule_id` bigint(20) DEFAULT NULL COMMENT '规则管理表ID',\n" +
		"  `quality_testing_programme_id` bigint(20) DEFAULT NULL COMMENT '质检方案表ID',\n" +
		"  `model_id` bigint(20) DEFAULT NULL COMMENT '业务模型管理表主键',\n"+
		"  `tj_id` varchar(200) DEFAULT NULL COMMENT '统计表Id',\n"+
		"  `period` int(9) DEFAULT NULL COMMENT '执行期数',\n" +
		"  `theme_id` bigint(20) DEFAULT NULL COMMENT '主题表ID',\n"+
		"  PRIMARY KEY (`id`) USING BTREE \n"+
		") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
	void createTableJL(@Param("JL") String JL);

	/**
	 * 例外表
	 *
	 * @param LW 例外表表名
	 */
	@Update("CREATE TABLE ${LW} (\n" +
		"  `id` varchar(200) NOT NULL COMMENT '主键',\n"+
		"  `check_column` varchar(500) DEFAULT NULL COMMENT '检查字段',\n" +
		"  `check_column_value` varchar(500) DEFAULT NULL COMMENT '检查字段值',\n" +
		"  `key_column` varchar(500) DEFAULT NULL COMMENT '数据表主键字段',\n" +
		"  `key_comment` varchar(500) DEFAULT NULL COMMENT '数据表主键说明',\n" +
		"  `key_value` varchar(500) DEFAULT NULL COMMENT '数据表主键值',\n" +
		"  `record_time` datetime DEFAULT NULL COMMENT '记录时间',\n" +
		"  `current_cycle` date DEFAULT NULL COMMENT '统计周期',\n" +
		"  `cycle_type` varchar(50) DEFAULT NULL COMMENT '周期类型',\n"+
		"  `manage_rule_id` bigint(20) DEFAULT NULL COMMENT '规则管理表ID',\n" +
		"  `quality_testing_programme_id` bigint(20) DEFAULT NULL COMMENT '质检方案表ID',\n" +
		"  `model_id` bigint(20) DEFAULT NULL COMMENT '业务模型管理表主键',\n"+
		"  `period` int(9) DEFAULT NULL COMMENT '执行期数',\n" +
		"  `theme_id` bigint(20) DEFAULT NULL COMMENT '主题表ID',\n"+
		"  `jl_id` varchar(200) DEFAULT NULL COMMENT '记录表Id',\n"+
		"  PRIMARY KEY (`id`) USING BTREE \n"+
		") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
	void createTableLW(@Param("LW") String LW);

	/**
	 * 删除表
	 *
	 * @param table 表名
	 */
	@Update("drop table ${table}")
	void dropTables(@Param("table") String table);

	/**
	 * 根据modelId获取统计类型
	 *
	 * @param modelId
	 * @return
	 */
	@Select("select statistical_type from business_model where id = #{modelId}")
	String getStatisticalType(String modelId);
}
