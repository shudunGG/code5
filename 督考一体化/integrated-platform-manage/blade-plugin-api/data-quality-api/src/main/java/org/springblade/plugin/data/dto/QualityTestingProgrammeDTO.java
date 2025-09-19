package org.springblade.plugin.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.QualityTestingProgramme;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MaQiuyun
 * @date 2021/12/1420:30
 * @description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "质检方案数据传输实体类", description = "QualityTestingProgrammeDTO")
public class QualityTestingProgrammeDTO extends QualityTestingProgramme {
	private static final long serialVersionUID = 3211687170130645518L;
	/**
	 * 选中的规则，查询时可以将质检方案和规则关联表中的manage_rule_id放进来
	 */
	@ApiModelProperty(value = "所属规则ID")
	private String choseRule;
	/**
	 * 要查询的统计周期
	 */
	@ApiModelProperty(value = "要查询的统计周期")
	private String statisticalCycle;
}
