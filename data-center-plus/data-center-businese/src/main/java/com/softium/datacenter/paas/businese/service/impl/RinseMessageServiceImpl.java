package com.softium.datacenter.paas.web.service.impl;

import com.rabbitmq.client.Channel;
import com.softium.datacenter.paas.api.dto.RinseMessageDTO;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.api.enums.AccessType;
import com.softium.datacenter.paas.api.enums.DataScope;
import com.softium.datacenter.paas.web.service.RinseMessageService;
import com.softium.datacenter.paas.web.utils.JobConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Fanfan.Gong <740584045@qq.com>
 **/
@Service
@Slf4j
public class RinseMessageServiceImpl implements RinseMessageService {
    @Autowired
    private ConnectionFactory connectionFactory;
    private static final String MONTH = "0";
    @Value("${rinse.file-handle-rinse-queue}")
    private String fileHandleRinseQueueName;
    @Value("${rinse.manual-handle-rinse-queue}")
    private String manualHandleRinseQueueName;

    private final Map<String, RabbitTemplate> rabbitTemplateMap = new HashMap<>(2);
    private static final Lock LOCK = new ReentrantLock();

    @Override
    public void sendFileHandleMsg(String tenantId, String userId, ExcelJobDTO jobDTO, List<String> allIdList) throws IOException {
        RinseMessageDTO rinseMessage = new RinseMessageDTO();
        rinseMessage.setAccessType(AccessType.MANUAL);
        DataScope dataScope = null;
        if (MONTH.equals(jobDTO.getUploadDataType())) {
            dataScope = DataScope.MONTH;
        } else {
            dataScope = DataScope.DAY;
        }
        rinseMessage.setDataScope(dataScope);
        rinseMessage.setTimestamp(System.currentTimeMillis());
        rinseMessage.setUserId(userId);
        rinseMessage.setTenantId(tenantId);
        rinseMessage.setPeriodId(jobDTO.getPeriodId());
        rinseMessage.setFileParseLevelList(allIdList);
        RabbitTemplate rabbitTemplate = getRabbitTemplate(JobConstant.buildRinseQueueName(fileHandleRinseQueueName));
        if (null != rabbitTemplate) {
            log.info("SEND-FILE-RINSE-MESSAGE: {}", rinseMessage);
            rabbitTemplate.convertAndSend(rinseMessage);
        }
    }

    @Override
    public void sendManualHandleMsg(RinseMessageDTO rinseMessage) throws IOException {
        RabbitTemplate rabbitTemplate = getRabbitTemplate(JobConstant.buildRinseQueueName(manualHandleRinseQueueName));
        if (null != rabbitTemplate) {
            log.info("SEND-MANUAL-RINSE-MESSAGE: {}", rinseMessage);
            rabbitTemplate.convertAndSend(rinseMessage);
        }
    }

    private RabbitTemplate getRabbitTemplate(String queueName) throws IOException {
        if (rabbitTemplateMap.containsKey(queueName)) {
            return rabbitTemplateMap.get(queueName);
        }
        RabbitTemplate template = null;
        LOCK.lock();
        try {
            template = new RabbitTemplate(connectionFactory);
            template.setDefaultReceiveQueue(queueName);
            template.setRoutingKey(queueName);
            declareQueue(queueName);
            rabbitTemplateMap.put(queueName, template);
        } finally{
            LOCK.unlock();
        }
        return template;

    }

    private void declareQueue(String queueName) throws IOException {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        try {
            channel.queueDeclareNoWait(queueName, true, false, false, null);
        } finally {
            RabbitUtils.closeChannel(channel);
            RabbitUtils.closeConnection(connection);
        }
    }
}