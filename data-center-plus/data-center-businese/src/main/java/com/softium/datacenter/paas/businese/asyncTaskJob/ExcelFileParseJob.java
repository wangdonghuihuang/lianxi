package com.softium.datacenter.paas.web.asyncTaskJob;

import com.softium.datacenter.paas.web.common.GetBeanClass;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.web.service.ExcelParseJobService;
import com.softium.datacenter.paas.web.service.JobServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.StopWatch;

@Slf4j
public class ExcelFileParseJob implements Runnable {
    private static JobServiceProxy jobService;
private static ExcelParseJobService excelParseJobService;
    private ExcelJobDTO jobDTO;
    static {
        jobService= GetBeanClass.getBean(JobServiceProxy.class);
        excelParseJobService=GetBeanClass.getBean(ExcelParseJobService.class);
    }
    public ExcelFileParseJob(ExcelJobDTO dto){
        super();
        this.jobDTO=dto;
    }
    @Override
    public void run() {
        try {
            StopWatch watch=new StopWatch();
            watch.start();
            //开始解析excel文件任务
            excelParseJobService.judgeExcelFileData(jobDTO);
            watch.stop();
            //记录任务如任务日志表
            JobTaskDTO taskDTO=new JobTaskDTO();
            taskDTO.setJobContext("ExcelFileParse");
            taskDTO.setJobCode(" fileUpload");
            jobService.addJobTask(taskDTO);
            log.info("文件上传解析任务:"+Thread.currentThread().getName()+"结束执行，共耗时:"+watch.getTotalTimeSeconds()+"秒");
        }catch (Exception e){
            String  stackTrace= ExceptionUtils.getStackTrace(e);
            log.error("上传解析文件任务异常:"+stackTrace);
        }
    }
}
