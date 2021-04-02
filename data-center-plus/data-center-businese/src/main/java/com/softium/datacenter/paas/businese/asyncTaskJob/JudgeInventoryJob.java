package com.softium.datacenter.paas.web.asyncTaskJob;

import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;
import com.softium.datacenter.paas.web.service.JobServiceProxy;
import com.softium.datacenter.paas.web.service.JudgeInventoryService;
import com.softium.datacenter.paas.web.utils.JobConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.StopWatch;
import com.softium.datacenter.paas.web.common.GetBeanClass;
/**库存数据质检任务*/
@Slf4j
public class JudgeInventoryJob implements Runnable {
    private static JobServiceProxy jobService;
    private static JudgeInventoryService judgeInventoryService;
    private JobTaskDTO taskDTO;
    static {
        jobService= GetBeanClass.getBean(JobServiceProxy.class);
        judgeInventoryService=GetBeanClass.getBean(JudgeInventoryService.class);
    }
    public JudgeInventoryJob(JobTaskDTO dto){
        super();
        this.taskDTO=dto;
    }
    @Override
    public void run() {
        String jobLogId = taskDTO.getJobLogId();
        try {
            StopWatch watch=new StopWatch();
            watch.start();
            //开始执行库存源数据清洗
            judgeInventoryService.judgeInventData(new JudgeDataQuery());
            //全部逻辑执行完毕，执行如下代码进行回调
            jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_SUCCESS, null);
            watch.stop();
            //任务结束，将任务记录入库
            jobService.addJobTask(taskDTO);
            log.info("库存数据清洗任务:" + Thread.currentThread().getName() + "结束执行,共耗时:"+watch.getTotalTimeSeconds()+"秒");

        } catch (Exception e) {
            log.error("库存异步执行器出错:" + e);
            String stackTrace = ExceptionUtils.getStackTrace(e);
            jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_FAILURE, stackTrace.length() < 200 ? stackTrace : stackTrace.substring(0, 200));
        }
    }
}
