package org.springblade.plugin.data.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.ManageRule;
import org.springblade.plugin.data.entity.QualityTestingProgramme;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:07 2021/12/2 0002
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QualityTestingProgrammeVO extends QualityTestingProgramme {
	private static final long serialVersionUID = 1559350153744163264L;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 执行周期字符串
	 * @Date 上午 10:09 2021/12/2 0002
	 * @Param
	 **/
	private String cycleStr;
	/**
	 * 选中的规则，查询时可以将质检方案和规则关联表中的manage_rule_id放进来
	 */
	@ApiModelProperty(value = "所属规则ID")
	private String choseRule;
	/**
	 * 关联的规则数据
	 */
	private List<ManageRule> qualityTestingRuleData = new ArrayList<>();
}
