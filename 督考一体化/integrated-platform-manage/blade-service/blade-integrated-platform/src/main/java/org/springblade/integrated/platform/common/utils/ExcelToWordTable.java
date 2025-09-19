package org.springblade.integrated.platform.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.spire.doc.Document;
import com.spire.doc.Table;
import com.spire.doc.TableCell;
import com.spire.doc.documents.HorizontalAlignment;
import com.spire.doc.documents.VerticalAlignment;
import com.spire.doc.fields.TextRange;
import com.spire.xls.CellRange;
import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.util.List;

public class ExcelToWordTable {

	/*
	*
	* 不要删掉这段代码，以后有大用
	 */
	/*	public static void main(String[] args) {
		List<String> row1 = CollUtil.newArrayList("aa", "bb", "cc", "dd");
		List<String> row2 = CollUtil.newArrayList("aa1", "bb1", "cc1", "dd1");
		List<String> row3 = CollUtil.newArrayList("aa2", "bb2", "cc2", "dd2");
		List<String> row4 = CollUtil.newArrayList("aa3", "bb3", "cc3", "dd3");
		List<String> row5 = CollUtil.newArrayList("aa4", "bb4", "cc4", "dd4");
		List<List<String>> rows = CollUtil.newArrayList(row1, row2, row3, row4, row5);
		//通过工具类创建writer
		ExcelWriter writer = ExcelUtil.getWriter("C:\\Users\\22727\\Desktop\\writeTest1.xlsx");
		//通过构造方法创建writer
		//ExcelWriter writer = new ExcelWriter("d:/writeTest.xls");
		//跳过当前行，既第一行，非必须，在此演示用
		writer.passCurrentRow();
		//合并单元格后的标题行，使用默认标题样式
		writer.merge(0,0,0,5, "测试标题",false);
		Font font = writer.createFont();
		font.setFontHeightInPoints((short) 40);
		font.setFontName("方正小标宋简体");
		writer.createCellStyle(0,0).setFont(font);

*//*		StyleSet style = writer.getStyleSet();
		style.setFont((short) 55, (short) 20,null,true);
		cellStyle = style.getCellStyle();*//*

		writer.merge(1,1,0,2, "标题",false);
		writer.merge(1,1,3,5, "测试",false);
		//一次性写出内容，强制输出标题
		writer.write(rows, true);
		//关闭writer，释放内存
		writer.close();
	}*/

	public static void excelToWord(String excelPath, String wordPath) {
		//加载Excel 示例文档
		Workbook workbook = new Workbook();
		workbook.loadFromFile(excelPath);
		//获取第一个工作表
		Worksheet sheet = workbook.getWorksheets().get(0);
		//复制到Word文档
		copyToWord(sheet.getAllocatedRange(), wordPath);

	}

	public static void copyToWord(CellRange cell, String fPath) {
		//添加表格
		Document doc = new Document();
		Table table = doc.addSection().addTable(true);
		table.resetCells(cell.getRowCount(), cell.getColumnCount());
		//复制表格内容
		for (int r = 1; r <= cell.getRowCount(); r++) {
			for (int c = 1; c <= cell.getColumnCount(); c++) {
				CellRange xCell = cell.get(r, c);
				CellRange mergeArea = xCell.getMergeArea();
				//合并单元格
				if (mergeArea != null && mergeArea.getRow() == r && mergeArea.getColumn() == c) {
					int rowIndex = mergeArea.getRow();
					int columnIndex = mergeArea.getColumn();
					int rowCount = mergeArea.getRowCount();
					int columnCount = mergeArea.getColumnCount();

					for (int m = 0; m < rowCount; m++) {
						table.applyHorizontalMerge(rowIndex - 1 + m, columnIndex - 1, columnIndex + columnCount - 2);
					}
					table.applyVerticalMerge(columnIndex - 1, rowIndex - 1, rowIndex + rowCount - 2);
				}
				//复制内容
				TableCell wCell = table.getRows().get(r - 1).getCells().get(c - 1);
				if (!xCell.getDisplayedText().isEmpty()) {
					TextRange textRange = wCell.addParagraph().appendText(xCell.getDisplayedText());
					copyStyle(textRange, xCell, wCell);
				} else {
					wCell.getCellFormat().setBackColor(xCell.getStyle().getColor());
				}
			}
		}
		doc.saveToFile(fPath, com.spire.doc.FileFormat.Docx);
	}

	private static void copyStyle(TextRange wTextRange, CellRange xCell, TableCell wCell) {
		//复制字体样式
		wTextRange.getCharacterFormat().setTextColor(xCell.getStyle().getFont().getColor());
		wTextRange.getCharacterFormat().setFontSize((float) xCell.getStyle().getFont().getSize());
		wTextRange.getCharacterFormat().setFontName(xCell.getStyle().getFont().getFontName());
		wTextRange.getCharacterFormat().setBold(xCell.getStyle().getFont().isBold());
		wTextRange.getCharacterFormat().setItalic(xCell.getStyle().getFont().isItalic());
		//复制背景色
		wCell.getCellFormat().setBackColor(xCell.getStyle().getColor());
		//复制排列方式
		switch (xCell.getHorizontalAlignment()) {
			case Left:
				wTextRange.getOwnerParagraph().getFormat().setHorizontalAlignment(HorizontalAlignment.Left);
				break;
			case Center:
				wTextRange.getOwnerParagraph().getFormat().setHorizontalAlignment(HorizontalAlignment.Center);
				break;
			case Right:
				wTextRange.getOwnerParagraph().getFormat().setHorizontalAlignment(HorizontalAlignment.Right);
				break;
			default:
				break;
		}
		switch (xCell.getVerticalAlignment()) {
			case Bottom:
				wCell.getCellFormat().setVerticalAlignment(VerticalAlignment.Bottom);
				break;
			case Center:
				wCell.getCellFormat().setVerticalAlignment(VerticalAlignment.Middle);
				break;
			case Top:
				wCell.getCellFormat().setVerticalAlignment(VerticalAlignment.Top);
				break;
			default:
				break;
		}
	}
}
