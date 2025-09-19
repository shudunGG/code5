package com.vingsoft.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: SupervisionInfoExcel
 * @Author: WangRJ
 * @CreateDate: 2022/7/4 16:53
 * @Version: 1.0
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SupervisionInfoExcel implements Serializable {

	@ColumnWidth(25)
	@ExcelProperty("事项类型")
	@ContentStyle(wrapped = true)
	private String servType;

	@ColumnWidth(25)
	@ExcelProperty("事项名称")
	@ContentStyle(wrapped = true)
	private String servName;

	@ColumnWidth(25)
	@ExcelProperty("责任领导")
	@ContentStyle(wrapped = true)
	private String dutyLeader;

	@ColumnWidth(25)
	@ExcelProperty("牵头单位")
	@ContentStyle(wrapped = true)
	private String leadUnit;

	@ColumnWidth(25)
	@ExcelProperty("责任单位")
	@ContentStyle(wrapped = true)
	private String dutyUnit;

	@ColumnWidth(25)
	@ExcelProperty("督办人")
	@ContentStyle(wrapped = true)
	private String supervisor;

	@ColumnWidth(25)
	@ExcelProperty("评价人")
	@ContentStyle(wrapped = true)
	private String evaluator;

	@ColumnWidth(25)
	@ExcelProperty("相关要求")
	@ContentStyle(wrapped = true)
	private String requirement;

	@ColumnWidth(25)
	@ExcelProperty("阶段信息")
	@ContentStyle(wrapped = true)
	private String phaseList;

	/*@ColumnWidth(25)
	@ExcelProperty("开始时间")
	private String startTime;

	@ColumnWidth(25)
	@ExcelProperty("截止时间")
	private String endTime;*/

//	@ColumnWidth(75)
//	@ExcelProperty("阶段信息")
//	private List<SupervisionPhasePlanExcel> phasePlanExcels;
}
