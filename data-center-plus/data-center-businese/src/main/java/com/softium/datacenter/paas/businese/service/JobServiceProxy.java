package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.JobTaskDTO;

public interface JobServiceProxy {
    void updateJobLog(String jobLogId,String status,String message);
    //新增定时任务执行记录入库
    void addJobTask(JobTaskDTO taskDTO);
}
