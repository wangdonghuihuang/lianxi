package com.softium.datacenter.paas.web.service;

/**
 * @author Fanfan.Gong
 **/
public interface NotifyFileService {
    /**
     * 执行某一个配置中的任务
     * @param notifyConfigId
     */
    void handleByConfigId(String notifyConfigId, String userId);
}
