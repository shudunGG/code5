package org.springblade.integrated.platform.excel;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.vingsoft.entity.StageInformation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springblade.core.excel.support.ExcelImporter;
import org.springblade.integrated.platform.service.IProjectSummaryService;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Jianglei
 * @version 1.0
 * @description:投资项目导入
 * @date 2022-04-16 12:01
 */

@RequiredArgsConstructor
public class ProjectReadyExcel implements ExcelImporter<ProjectSummaryExcel> {
	private final IProjectSummaryService projectSummaryService;
	private final String xmType;

	@Override
	public void save(List<ProjectSummaryExcel> data) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setXmType(xmType);
			data.get(i).setPorjStatus("1");
			data.get(i).setAutoState("1");
			data.get(i).setReportStatus("1");
			data.get(i).setXmnf(String.valueOf(year));
			if(data.get(i).getCyType().equals("交通")){
				data.get(i).setCyType("1");
			}else if(data.get(i).getCyType().equals("农业农村")){
				data.get(i).setCyType("2");
			}else if(data.get(i).getCyType().equals("水利")){
				data.get(i).setCyType("3");
			}else if(data.get(i).getCyType().equals("能源")){
				data.get(i).setCyType("4");
			}else if(data.get(i).getCyType().equals("社会民生")){
				data.get(i).setCyType("5");
			}else if(data.get(i).getCyType().equals("文化旅游")){
				data.get(i).setCyType("6");
			}else if(data.get(i).getCyType().equals("生态环保")){
				data.get(i).setCyType("7");
			}else if(data.get(i).getCyType().equals("新基建")){
				data.get(i).setCyType("8");
			}else if(data.get(i).getCyType().equals("城镇化及保障性安居工程")){
				data.get(i).setCyType("9");
			}else if(data.get(i).getCyType().equals("工业制造业")){
				data.get(i).setCyType("10");
			}else if(data.get(i).getCyType().equals("经贸物流")){
				data.get(i).setCyType("11");
			}else if(data.get(i).getCyType().equals("房地产开发")){
				data.get(i).setCyType("12");
			}
			if(data.get(i).getProjLabel().equals("市列")){
				data.get(i).setProjLabel("1");
			}else if(data.get(i).getProjLabel().equals("省列")){
				data.get(i).setProjLabel("2");
			}else if(data.get(i).getProjLabel().equals("亿元以上")){
				data.get(i).setProjLabel("3");
			}else if(data.get(i).getProjLabel().equals("亿元以下")){
				data.get(i).setProjLabel("4");
			}
			if(data.get(i).getXmdl().equals("投资项目")){
				data.get(i).setXmdl("1");
			}else if(data.get(i).getXmdl().equals("新增投资项目")){
				data.get(i).setXmdl("2");
			}else if(data.get(i).getXmdl().equals("前期项目")){
				data.get(i).setXmdl("3");
			}else if(data.get(i).getXmdl().equals("新增前期项目")){
				data.get(i).setXmdl("4");
			}else if(data.get(i).getXmdl().equals("中央和省级预算内项目")){
				data.get(i).setXmdl("5");
			}else if(data.get(i).getXmdl().equals("政府专项债券项目")){
				data.get(i).setXmdl("6");
			}
			if(data.get(i).getProjMain().equals("政府投资")){
				data.get(i).setProjMain("1");
			}else if(data.get(i).getProjMain().equals("民间投资")){
				data.get(i).setProjMain("2");
			}
		}
		projectSummaryService.imimportProjectSummary(data);
	}

}
