package com.softium.datacenter.paas.web.asyncTaskJob;

import com.softium.datacenter.paas.web.common.GetBeanClass;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.web.service.JobServiceProxy;
import com.softium.datacenter.paas.web.service.NotifyFileService;
import com.softium.datacenter.paas.web.utils.JobConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.StopWatch;

/**
 * 异步任务执行器
 */
@Slf4j
public class AsyncTaskJob implements Runnable {
    private static JobServiceProxy jobService;
    private static NotifyFileService notifyFileService;
    static {
        jobService = GetBeanClass.getBean(JobServiceProxy.class);
        notifyFileService=GetBeanClass.getBean(NotifyFileService.class);
    }

    private volatile JobTaskDTO taskDTO;

    public AsyncTaskJob(JobTaskDTO dto) {
        super();
        this.taskDTO = dto;
    }

    @Override
    public void run() {
        //log.info("进入任务前参数:"+taskDTO.getJobContext());
        String jobLogId = taskDTO.getJobLogId();
        //获取任务参数,目前第一版中参数为任务表id
        String taskId=taskDTO.getJobContext();
        try {
            StopWatch watch=new StopWatch();
            watch.start();
            /*1.调用凡凡处理ftp信息方法
            2.扫描目录全部解析完毕,调用任务框架回调接口，通知框架任务执行状态*/
            log.info("任务:" + Thread.currentThread().getName() +taskId+ "开始执行");
            //直接调用凡凡方法
            notifyFileService.handleByConfigId(taskId,"1");
            //全部逻辑执行完毕，执行如下代码进行回调
            jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_SUCCESS, null);
            watch.stop();
            //任务结束，将任务记录入库
            jobService.addJobTask(taskDTO);
            log.info("任务:" + Thread.currentThread().getName() +taskId+ "结束执行,共耗时:"+watch.getTotalTimeSeconds()+"秒");

        } catch (Exception e) {
            log.error("异步执行器出错:" + e);
            String stackTrace = ExceptionUtils.getStackTrace(e);
            //System.out.println("打印异常:" + stackTrace);
            jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_FAILURE, stackTrace.length() < 200 ? stackTrace : stackTrace.substring(0, 200));
        }


    }
}
