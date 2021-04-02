package com.softium.datacenter.paas.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.entity.NotifyLog;
import com.softium.datacenter.paas.api.mapper.NotifyConfigMapper;
import com.softium.datacenter.paas.web.utils.CommonConstant;
import com.softium.datacenter.paas.web.utils.HttpUtilsCommon;
import com.softium.datacenter.paas.web.utils.JobConstant;
import com.softium.datacenter.paas.web.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 定时任务功能接口层
 */
@Slf4j
@RestController
@RequestMapping(path = "/notifyTask")
public class NotifyTaskController {

    @Autowired
    CommonConstant constant;
    @Autowired
    NotifyConfigMapper notifyConfigMapper;
    /**
     * 提供此接口手动调用，向消息服务器注册定时任务
     */
    @PostMapping(path = "registerTask")
    public String registerTask(@RequestBody JobTaskDTO dto) {
        String message = "";
        dto.setDataId(UUIDUtil.uuid());
        Map<String, String> map = new HashMap<>();
        map.put("TM-Header-TenantId", "test");
        try {
            String post = HttpUtilsCommon.post(constant.getBaseUrl() + "/save", map, JSON.toJSONString(dto));
            JSONObject object=JSON.parseObject(post);
            if(object.getString("success").equals("true")){
                String jobid=object.getString("data");
                message = "success";
                log.info("Manual insert task success:" + post);
                //注册完成后，入本服日志库
                NotifyLog notifyLog=new NotifyLog();
                notifyLog.setId(UUID.randomUUID().toString());
                notifyLog.setTaskId(dto.getJobContext());
                notifyLog.setStatus(JobConstant.EXECUTE_SUCCESS);
                notifyLog.setFailureReason(dto.getJobCode()+dto.getCronExpression());
                notifyLog.setCreateBy("123456");
                notifyLog.setCreateTime(new Date());
                notifyLog.setUpdateTime(new Date());
                notifyLog.setJobServerId(jobid);
                notifyConfigMapper.addMotifyLog(notifyLog);
                log.info("tasklog insert success");
            }else{
                message=post.toString();
            }

        } catch (Exception e) {
            message = "fail";
            log.error("manual insert task fail:" + e.getMessage());
        }
        return message;
    }
    /**删除定时任务接口*/
    @PostMapping(path = "deleteQuartz")
    public String delQuartzController(@RequestBody JobTaskDTO dto){
        String message = "";
        Map<String, String> map = new HashMap<>();
        map.put("Accept", "application/json");
        map.put("Content-type", "application/json");
        try {
            String post = HttpUtilsCommon.put(constant.getBaseUrl() + "/delete",map,JSON.toJSONString(dto), ContentType.APPLICATION_JSON);
            message = post.toString();
            log.info("delete task success:" + post);
        } catch (Exception e) {
            message = "fail";
            log.error("delete task fail:" + e.getMessage());
        }
        return message;
    }
}
