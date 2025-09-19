package org.springblade.integrated.platform.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serializable;

/**
 * 用于导出 ScoreAdd Excel
 * @Author JG🧸
 * @Create 2022/4/9 10:45
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ScoreAddExcel implements Serializable {
	private static final long serialVersionUID = 1L;

	@ExcelIgnore
	@ColumnWidth(15)
	@ExcelProperty("序号")
	private int id;


	@ExcelProperty("单位名称")
	private String deptName;

	@ExcelIgnore
	@ColumnWidth(20)
	@ExcelProperty("单位表id")
	private String deptId;

	@ColumnWidth(15)
	@ExcelProperty("考核方式")
	private String checkWay;

	@ColumnWidth(20)
	@ExcelProperty("获奖项目名称")
	private String winProject;

	@ColumnWidth(15)
	@ExcelProperty("获奖级别")
	private String winLevel;

	@ExcelIgnore
	@ExcelProperty("佐证资料")
	private String proofMater;

	@ColumnWidth(15)
	@ExcelProperty("分值")
	private String score;

	@ExcelIgnore
	@ExcelProperty("创建人")
	private String createUser;

	@ExcelIgnore
	@ExcelProperty("创建时间")
	private String createDate;


}
