package org.springblade.plugin.data.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.plugin.data.dto.ThemeTableDTO;
import org.springblade.plugin.data.entity.StructureMetadata;

import java.util.List;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:38 2021/11/8 0008
 * @ Description：主题表和其元数据树形结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ThemeTableTree extends ThemeTableDTO {
	private static final long serialVersionUID = -3960553083034967306L;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 是否有子节点
	 * @Date 上午 10:45 2021/11/8 0008
	 * @Param
	 **/
	private Boolean hasChildren;
	/**
	 * @return
	 * @Author MaQY
	 * @Description 子节点
	 * @Date 上午 10:46 2021/11/8 0008
	 * @Param
	 **/
	private List<StructureMetadata> children;
}
