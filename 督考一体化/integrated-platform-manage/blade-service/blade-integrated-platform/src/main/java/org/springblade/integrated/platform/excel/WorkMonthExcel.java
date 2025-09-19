package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @className: WorkMonthExcel
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/2/28 14:27 星期二
 * @Version 1.0
 **/
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class WorkMonthExcel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 计划所属月份
	 */
	@ColumnWidth(30)
	@ExcelProperty("计划所属月份")
	private String month;

	/**
	 * 工作内容
	 */
	@ColumnWidth(100)
	@ExcelProperty("工作内容")
	private String concent;

	/**
	 * 责任人
	 */
	@ColumnWidth(20)
	@ExcelProperty("责任人")
	private String personLiable;

	/**
	 *
	 */
	@ColumnWidth(20)
	@ExcelProperty("月调度类型（1-市委办，2-市政府办）")
	private String type;

	/**
	 * 计划完成时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("计划完成时间")
	private Date planTime;

	/**
	 * 完成情况
	 */
	@ColumnWidth(20)
	@ExcelProperty("完成情况")
	private String completion;

	/**
	 * 备注
	 */
	@ColumnWidth(20)
	@ExcelProperty("备注")
	private String remarks;
}
