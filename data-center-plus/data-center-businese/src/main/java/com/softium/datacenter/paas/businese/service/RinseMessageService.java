package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.RinseMessageDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.api.enums.TodoType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Fanfan.Gong <740584045@qq.com>
 **/
public interface RinseMessageService {
    /**
     * 发送文件消息通知
     * @param tenantId
     * @param userId
     * @param jobDTO
     * @param allIdList
     */

    void sendFileHandleMsg(String tenantId, String userId, ExcelJobDTO jobDTO, List<String> allIdList) throws IOException;

    /**
     * 发送手工处理消息
     * @param rinseMessage
     * @throws IOException
     */
    void sendManualHandleMsg(RinseMessageDTO rinseMessage) throws IOException;
}
