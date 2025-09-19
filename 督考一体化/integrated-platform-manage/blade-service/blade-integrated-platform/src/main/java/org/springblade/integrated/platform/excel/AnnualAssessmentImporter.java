/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.integrated.platform.excel;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springblade.core.excel.support.ExcelImporter;
import org.springblade.integrated.platform.service.IAnnualAssessmentService;

import java.util.List;

/**
 * 年度评价数据导入类
 * @Author zrj
 * @Create 2022/4/15 17:30
 */
@RequiredArgsConstructor
public class AnnualAssessmentImporter implements ExcelImporter<AnnualAssessmentExcel> {

	private final IAnnualAssessmentService annualAssessmentService;
	private final String type;

	@Override
	public void save(List<AnnualAssessmentExcel> data) {
		for (int i = 0; i < data.size(); i++) {
			if (type.equals("1")) {
				data.get(i).setType("1");
			}else if (type.equals("2")) {
				data.get(i).setType("2");
			}else if (type.equals("3")) {
				data.get(i).setType("3");
			}else if (type.equals("4")) {
				data.get(i).setType("4");
			}else if (type.equals("5")) {
				data.get(i).setType("5");
			}
			//项目分类
			if(data.get(i).getProjectName().equals("政治思想建设")){
				data.get(i).setProjectId("1");
			}else if(data.get(i).getProjectName().equals("领导能力")){
				data.get(i).setProjectId("2");
			}else if(data.get(i).getProjectName().equals("党的建设")){
				data.get(i).setProjectId("3");
			}else if(data.get(i).getProjectName().equals("高质量发展实绩")){
				data.get(i).setProjectId("4");
			}else if(data.get(i).getProjectName().equals("政治素质")){
				data.get(i).setProjectId("5");
			}else if(data.get(i).getProjectName().equals("经营业绩")){
				data.get(i).setProjectId("6");
			}else if(data.get(i).getProjectName().equals("专项任务")){
				data.get(i).setProjectId("7");
			}else if(data.get(i).getProjectName().equals("团结协作")){
				data.get(i).setProjectId("8");
			}else if(data.get(i).getProjectName().equals("作风形象")){
				data.get(i).setProjectId("9");
			}else if(data.get(i).getProjectName().equals("党建工作")){
				data.get(i).setProjectId("10");
			}else if(data.get(i).getProjectName().equals("重点工作")){
				data.get(i).setProjectId("11");
			}else if(data.get(i).getProjectName().equals("推进高质量发展")){
				data.get(i).setProjectId("12");
			}else if(data.get(i).getProjectName().equals("乡村振兴")){
				data.get(i).setProjectId("13");
			}else if(data.get(i).getProjectName().equals("履行生态环保责任")){
				data.get(i).setProjectId("14");
			}else if(data.get(i).getProjectName().equals("经济建设")){
				data.get(i).setProjectId("15");
			}else if(data.get(i).getProjectName().equals("生态文明建设")){
				data.get(i).setProjectId("16");
			}else if(data.get(i).getProjectName().equals("文化建设")){
				data.get(i).setProjectId("17");
			}else if(data.get(i).getProjectName().equals("社会建设")){
				data.get(i).setProjectId("18");
			}else if(data.get(i).getProjectName().equals("脱贫攻坚")){
				data.get(i).setProjectId("19");
			}else if(data.get(i).getProjectName().equals("民族宗教")){
				data.get(i).setProjectId("20");
			}

		}
		annualAssessmentService.importZzsxjsAssessment(data);
	}

}
