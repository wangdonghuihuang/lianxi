package com.softium.datacenter.paas.web.config;

import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.web.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqJobClient extends JobExecutorTemplate{
    @Autowired
    AsyncTaskService taskService;

    @Override
    protected void executeJob(JobTaskDTO dto, ExcelJobDTO jobDTO) {
        //调用异步方法执行任务
        taskService.asyncFtpProcess(dto,jobDTO);
    }
}
