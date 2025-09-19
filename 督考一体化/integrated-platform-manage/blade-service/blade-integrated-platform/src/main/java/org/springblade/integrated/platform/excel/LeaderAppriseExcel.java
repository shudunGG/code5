package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于导出 LeaderApprise Excel
 *
 * @Author JG🧸
 * @Create 2022/4/9 13:23
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class LeaderAppriseExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 单位表id
	 */
	@ExcelIgnore
	@ColumnWidth(20)
	@ExcelProperty("单位表id")
	private Integer id;

	/**
	 * 单位名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("单位名称")
	private String deptName;

	/**
	 * 单位id
	 */
	@ExcelIgnore
	@ColumnWidth(20)
	@ExcelProperty("单位id")
	private String deptId;

	/**
	 * 满意度:满意1，比较满意2，基本满意3，不满意4
	 */
	@ColumnWidth(20)
	@ExcelProperty("满意度")
	private String satisfaction;

	/**
	 * 得分
	 */
	@ColumnWidth(20)
	@ExcelProperty("得分")
	private String score;

	/**
	 * 评价内容
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价内容")
	private String appriseContent;

	/**
	 * 评价领导
	 */
	@ColumnWidth(20)
	@ExcelProperty("评价领导")
	private String appriseLeader;

	/**
	 * 评价时间
	 */
	@ExcelIgnore
	@ColumnWidth(20)
	@ExcelProperty("评价时间")
	private Date appriseDate;






}
