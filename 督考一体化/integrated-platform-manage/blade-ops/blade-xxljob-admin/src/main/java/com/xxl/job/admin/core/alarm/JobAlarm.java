package com.xxl.job.admin.core.alarm;

import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.core.biz.model.XxlJobInfo;

/**
 * @author xuxueli 2020-01-19
 */
public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
