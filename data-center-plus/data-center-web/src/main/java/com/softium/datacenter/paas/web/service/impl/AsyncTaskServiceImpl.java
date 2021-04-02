package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.api.mapper.NotifyConfigMapper;
import com.softium.datacenter.paas.web.service.AsyncTaskService;
import com.softium.datacenter.paas.web.asyncTaskJob.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 异步任务调度处理业务层实现
 */
@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {
    @Resource(name = "taskExecutorPool")
    private ExecutorService taskExecutorService;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Async("taskExecutorPool")
    @Override
    public void asyncFtpProcess(JobTaskDTO jobTaskDTO,ExcelJobDTO jobDTO) {
        //根据taskDTO中参数，执行对应任务，可基于此扩展
        //String jobContext=taskDTO.getJobContext();
        //judgeOriginSaleData为质检清洗数据，目前只有清洗数据和解析ftp文件两个任务
        /*if(jobContext.equals("judgeOriginSaleData")){
            taskExecutorService.execute(new JudgeDataJob(taskDTO));
        }else if(jobContext.equals("judgeOriginInventoryData")){
            taskExecutorService.execute(new JudgeInventoryJob(taskDTO));
        }else if(jobContext.equals("judgeOriginPurchaseData")){

        }else{
            *//*TODO 1.获取调度框架所传任务参数,调用异步线程执行任务*//*
            taskExecutorService.execute(new AsyncTaskJob(taskDTO));
        }*/
        if(jobTaskDTO.getJobContext().equals("judgeOriginSaleData")){
           taskExecutorService.execute(new JudgeDataJob(jobTaskDTO));
             taskExecutorService.execute(new JudgeInventoryJob(jobTaskDTO));
            taskExecutorService.execute(new JudgePurchaseJob(jobTaskDTO));
        }else if (jobTaskDTO.getJobContext().equals("excelParse")){
            //解析excel文件任务
            taskExecutorService.execute(new ExcelFileParseJob(jobDTO));
        }else {
                List<String> allConfigList = notifyConfigMapper.queryAllIds();
                for (String str : allConfigList) {
                    JobTaskDTO newDto = new JobTaskDTO();
                    BeanUtils.copyProperties(jobTaskDTO,newDto);
                    newDto.setJobContext(str);
                    taskExecutorService.execute(new AsyncTaskJob(newDto));
                }
        }
    }
}

