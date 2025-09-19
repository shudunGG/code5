package org.springblade.plugin.data.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.ThemeTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:17 2021/10/29 0029
 * @ Description：
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ThemeTableDTO extends ThemeTable {
	/**
	 * @return
	 * @Author MaQY
	 * @Description 主题表下的元数据信息
	 * @Date 上午 10:23 2021/10/29 0029
	 * @Param
	 **/
	private List<StructureMetadata> data = new ArrayList<>();
	/**
	 * @return
	 * @Author MaQY
	 * @Description 是否有子节点
	 * @Date 上午 10:42 2021/11/8 0008
	 * @Param
	 **/
	private Boolean hasChildren = false;
}
