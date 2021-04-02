package com.softium.datacenter.paas.web.controller;

import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;
import com.softium.datacenter.paas.web.service.FileJudgeTaskService;
import com.softium.datacenter.paas.web.service.JudgeInventoryService;
import com.softium.datacenter.paas.web.service.JudgePurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异步任务调度处理接口层
 */
@Slf4j
@RestController
@RequestMapping(path = "asyncTask")
public class AsyncTaskController {
    @Autowired
    FileJudgeTaskService fileJudgeTaskService;
    @Autowired
    JudgeInventoryService judgeInventoryService;
    @Autowired
    JudgePurchaseService judgePurchaseService;
    /**执行解析销售源数据任务
     * 同步接口调用*/
    @RequestMapping(path = "syncjudge")
    public String syncjudgeController(@RequestParam("type") String type){
        String message="";
        StopWatch awatch=new StopWatch();
        awatch.start();
        try {
            switch (type){
                case "judgeOriginSaleData":
                    fileJudgeTaskService.judgeOriginSaleData(new JudgeDataQuery());
                    break;
                case "judgeOriginInventoryData":
                    judgeInventoryService.judgeInventData(new JudgeDataQuery());
                    break;
                case "judgeOriginPurchaseData":
                    judgePurchaseService.judgePurchaseData(new JudgeDataQuery());
                    break;
            }
            awatch.stop();
            message="同步解析成功,时间:"+awatch.getTotalTimeSeconds()+"秒";
        }catch (Exception e){
            log.error("同步接口出现异常:"+e.getMessage());
            message=e.getMessage();
        }
        return message;
    }
}
