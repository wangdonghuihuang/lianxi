package com.softium.datacenter.paas.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.dto.JobLogUpdateDTO;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.entity.NotifyLog;
import com.softium.datacenter.paas.api.mapper.NotifyConfigMapper;
import com.softium.datacenter.paas.web.service.JobServiceProxy;
import com.softium.datacenter.paas.web.utils.CommonConstant;
import com.softium.datacenter.paas.web.utils.HttpClientUtils;
import com.softium.datacenter.paas.web.utils.JobConstant;
import com.softium.framework.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JobServiceProxyImpl implements JobServiceProxy {
    private static final Logger logger= LoggerFactory.getLogger(JobServiceProxyImpl.class);
    @Autowired
    NotifyConfigMapper notifyConfigMapper;
    @Autowired
    CommonConstant constant;
    @Override
    public void updateJobLog(String jobLogId, String status, String message) {
        //定义实体类对应解析任务修改实体类
        JobLogUpdateDTO updateDTO = new JobLogUpdateDTO();
        updateDTO.setJobLogId(jobLogId);
        updateDTO.setStatus(status);
        updateDTO.setMessage(message);

        try {
            //http调用接口，修改任务状态
            HttpClientUtils.sendHttpPost(constant.getJOB_SERVICE_NAME(), JSONObject.toJSONString(updateDTO));
        }catch (Exception e){
            logger.error("任务完毕回调状态接口失败:"+e.getMessage());
        }

    }

    @Override
    public void addJobTask(JobTaskDTO taskDTO) {
        logger.info("入库任务为:"+taskDTO.getJobContext());
        NotifyLog notifyLog=new NotifyLog();
        notifyLog.setId(UUIDUtils.getUUID());
        notifyLog.setTaskId(taskDTO.getJobContext());
        notifyLog.setStatus(JobConstant.EXECUTE_SUCCESS);
        notifyLog.setFailureReason(taskDTO.getJobCode());
        notifyLog.setCreateBy("admin");
        notifyLog.setCreateTime(new Date());
        notifyLog.setUpdateTime(new Date());
        notifyConfigMapper.addMotifyLog(notifyLog);
    }
}
