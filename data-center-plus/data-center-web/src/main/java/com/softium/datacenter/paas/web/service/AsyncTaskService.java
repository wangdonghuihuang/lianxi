package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;

/**异步任务调度处理业务层*/
public interface AsyncTaskService {
    public void asyncFtpProcess(JobTaskDTO dto,ExcelJobDTO jobDTO);
}
