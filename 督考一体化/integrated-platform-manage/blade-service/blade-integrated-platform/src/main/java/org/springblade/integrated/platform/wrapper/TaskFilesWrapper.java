package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.TaskFiles;
import com.vingsoft.vo.TaskFilesVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;


/**
 * 包装类,返回视图层所需的字段
 *
 * @Author JG🧸
 * @Create 2022/4/18 13:33
 */
public class TaskFilesWrapper extends BaseEntityWrapper<TaskFiles, TaskFilesVO> {

	public static TaskFilesWrapper build() {
		return new TaskFilesWrapper();
	}


	@Override
	public TaskFilesVO entityVO(TaskFiles taskFiles) {
		TaskFilesVO taskFilesVO = Objects.requireNonNull(BeanUtil.copy(taskFiles, TaskFilesVO.class));
		return taskFilesVO;
	}
}
