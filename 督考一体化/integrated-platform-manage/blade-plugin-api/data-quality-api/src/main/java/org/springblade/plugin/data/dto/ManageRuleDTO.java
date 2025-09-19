package org.springblade.plugin.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.ManageRule;
import org.springblade.plugin.data.entity.RelationRule;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 4:09 2021/11/3 0003
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageRuleDTO extends ManageRule {
	private static final long serialVersionUID = 5582284342108959554L;
	//查询条件 主题表ID集合
	private List<String> themeIds = new ArrayList<>();
	/**
	 * @return
	 * @Author MaQY
	 * @Description 逻辑检查是携带的关联关系数据
	 * @Date 下午 2:18 2021/11/16 0016
	 * @Param
	 **/
	private List<RelationRule> relations = new ArrayList<>();
	/**
	 * 主题表名称
	 */
	private String themeTableName;
}
