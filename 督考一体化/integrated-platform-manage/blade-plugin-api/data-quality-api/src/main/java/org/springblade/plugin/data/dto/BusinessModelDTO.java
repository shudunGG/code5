package org.springblade.plugin.data.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.BusinessModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:16 2021/10/29 0029
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessModelDTO extends BusinessModel {
	private static final long serialVersionUID = 5116391582665116160L;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 当前模型下的主题表信息
	 * @Date 上午 10:23 2021/10/29 0029
	 * @Param
	 **/
	private List<ThemeTableDTO> allTablesInfo = new ArrayList<>();
}
