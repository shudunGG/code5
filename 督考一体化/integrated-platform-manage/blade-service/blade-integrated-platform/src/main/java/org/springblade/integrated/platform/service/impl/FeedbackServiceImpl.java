package org.springblade.integrated.platform.service.impl;

import com.vingsoft.entity.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.FeedbackMapper;
import org.springblade.integrated.platform.service.IFeedbackService;
import org.springframework.stereotype.Service;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 18:08
 */
@Slf4j
@Service
public class FeedbackServiceImpl extends BaseServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {
}
