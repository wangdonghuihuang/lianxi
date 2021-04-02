package com.softium.datacenter.paas.web.controller;

import com.softium.datacenter.paas.web.service.NotifyFileService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Fanfan.Gong
 **/
@RestController
@RequestMapping("notify-file")
public class NotifyFileController {
    @Autowired
    private NotifyFileService notifyFileService;

    @GetMapping
    public ActionResult<String> handleNotify(String id) {
        String userId = "92e39e71-fcaa-11ea-8cfb-00163e16f32a";
        long start = System.currentTimeMillis();
        notifyFileService.handleByConfigId(id, userId);
        return new ActionResult<>("任务执行完成, 总用时：" + (System.currentTimeMillis() - start) + " 毫秒");
    }
}
