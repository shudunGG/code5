package org.springblade.plugin.data.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.FunctionComparison;
import org.springblade.plugin.data.entity.FunctionParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 3:47 2021/11/11 0011
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionComparisonDTO extends FunctionComparison {
	private static final long serialVersionUID = 1650337343090142402L;

	private List<FunctionParameter> args = new ArrayList<>();
	private Boolean hasChildren=false;
	private List<FunctionComparisonDTO> children;
}
