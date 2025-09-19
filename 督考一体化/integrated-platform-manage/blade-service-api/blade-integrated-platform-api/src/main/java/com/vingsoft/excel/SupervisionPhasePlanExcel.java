package com.vingsoft.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: SupervisionPhasePlanExcel
 * @Author: WangRJ
 * @CreateDate: 2022/7/5 8:54
 * @Version: 1.0
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
@AllArgsConstructor
public class SupervisionPhasePlanExcel implements Serializable {

	@ColumnWidth(25)
	@ExcelProperty("阶段名称")
	private String phaseName;

	@ColumnWidth(25)
	@ExcelProperty("开始时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@ColumnWidth(25)
	@ExcelProperty("截止时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;
}
