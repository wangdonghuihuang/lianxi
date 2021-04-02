package com.softium.datacenter.paas.web.config;

import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.web.service.JobServiceProxy;
import com.softium.datacenter.paas.web.utils.JobConstant;
import com.softium.framework.common.SystemConstant;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract  class JobExecutorTemplate {
    private static Logger logger= LoggerFactory.getLogger(JobExecutorTemplate.class);
    @Autowired
    protected JobServiceProxy jobService;
    protected abstract void executeJob(JobTaskDTO dto, ExcelJobDTO jobDTO);
    public final void execute(String context){
        logger.info("任务开始执行");
        if(StringUtils.isEmpty(context)){
            logger.error("context is null");
            return;
        }
        JobTaskDTO taskDTO= JSONObject.parseObject(context,JobTaskDTO.class);
        if(taskDTO==null||taskDTO.getTenantId()==null){
            throw new BusinessException(new ErrorInfo(SystemConstant.SERVER_ERROR));
        }
        String tenantId = taskDTO.getTenantId();
        //SystemContext.setTenantId(tenantId);
        String jobLogId = taskDTO.getJobLogId();
        try {
                executeJob(taskDTO,new ExcelJobDTO());
            //更新任务执行状态,处理回报状态放到AsyncTaskServiceImpl中去做
            //jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_SUCCESS, null);
        }catch (Exception e){
            logger.error("任务执行失败:"+e.getMessage());
            String stackTrace = ExceptionUtils.getStackTrace(e);
            jobService.updateJobLog(jobLogId, JobConstant.EXECUTE_FAILURE, stackTrace.length() < 200 ? stackTrace : stackTrace.substring(0, 200));
        }finally {
            logger.info("任务执行完毕");
        }
    }
}
