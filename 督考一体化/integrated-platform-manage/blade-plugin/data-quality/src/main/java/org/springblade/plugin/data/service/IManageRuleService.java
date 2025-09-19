package org.springblade.plugin.data.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.plugin.data.dto.ManageRuleDTO;
import org.springblade.plugin.data.entity.ManageRule;

import java.util.List;

/**
 * ManageRule的服务接口
 *
 * @author
 */
public interface IManageRuleService extends BaseService<ManageRule> {
	/**
	 * @return com.baomidou.mybatisplus.core.metadata.IPage<org.springblade.plugin.data.entity.ManageRule>
	 * @Author MaQY
	 * @Description 自定义分页查询
	 * @Date 下午 4:29 2021/11/3 0003
	 * @Param [rule]
	 **/
	IPage<ManageRule> selectPageList(IPage page, ManageRuleDTO rule);

	/**
	 * @return java.lang.String
	 * @Author MaQY
	 * @Description 生成规则代码
	 * @Date 下午 2:33 2021/11/4 0004
	 * @Param [type]
	 **/
	String generateCode(String type, String themeId);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 保存规则同时保存关联关系
	 * @Date 上午 10:51 2021/11/18 0018
	 * @Param [rule]
	 **/
	boolean saveRule(ManageRuleDTO rule);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 删除规则
	 * @Date 上午 11:50 2021/11/18 0018
	 * @Param [ids]
	 **/
	boolean deleteRules(String ids);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 删除一个规则
	 * @Date 上午 11:46 2021/11/19 0019
	 * @Param [ids]
	 **/
	boolean deleteRule(String ids);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 更新一个规则
	 * @Date 下午 1:40 2021/11/19 0019
	 * @Param [rule]
	 **/
	boolean updateRule(ManageRuleDTO rule);

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 保存规则和模板
	 * @Date 上午 10:44 2021/11/22 0022
	 * @Param [rule]
	 **/
	boolean saveRuleAndTemplate(ManageRuleDTO rule);

	/**
	 * 获取质检方案关联规则
	 *
	 * @param programmeId
	 * @return
	 */
	List<ManageRule> getTestingDataQualityRules(String programmeId);

	/**
	 * 获取详情
	 *
	 * @param id
	 * @return
	 */
	ManageRuleDTO getRuleDetail(String id);
}
