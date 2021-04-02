//package com.softium.datacenter.paas.web.config;
//
//import com.rabbitmq.client.Channel;
//import com.softium.datacenter.paas.web.utils.JobConstant;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.Connection;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.connection.RabbitUtils;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
//import org.springframework.amqp.support.converter.SimpleMessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.AutoConfigureAfter;
//import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//
//@Configuration
//@AutoConfigureAfter(RabbitAutoConfiguration.class)
//public class JobClientAutoConfiguration {
//    @Value("${rabbitmq.host}")
//    private String host;
//    @Value("${rabbitmq.port}")
//    private String port;
//    @Value("${rabbitmq.username}")
//    private String username;
//    @Value("${rabbitmq.password}")
//    private String password;
//    @Value("${rabbitmq.virtualHost}")
//    private String virtualHost;
//    @Value("${job.appid}")
//    private String appid;
//    @Bean
//    public ConnectionFactory connectionFactory(){
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setAddresses(host);
//        connectionFactory.setUsername(username);
//        connectionFactory.setPassword(password);
//        connectionFactory.setVirtualHost(virtualHost);
//        return connectionFactory;
//    }
//    @Bean("jobMessageListenerContainer")
//    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory, JobExecutorTemplate executorTemplate) throws IOException {
//        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer();
//        messageListenerContainer.setConnectionFactory(connectionFactory);
//        String queueName = JobConstant.buildQueueName(appid);
//        declareQueue(connectionFactory, queueName);
//        Queue queue = new Queue(queueName, true);
//        messageListenerContainer.setQueues(queue);
//        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(executorTemplate);
//        messageListenerAdapter.setMessageConverter(new SimpleMessageConverter());
//        messageListenerAdapter.setDefaultListenerMethod("execute");
//        messageListenerContainer.setMessageListener(messageListenerAdapter);
//        return messageListenerContainer;
//    }
//    /**声明队列，初始化绑定注册队列*/
//    private void declareQueue(ConnectionFactory connectionFactory, String queueName) throws IOException {
//        Connection connection = connectionFactory.createConnection();
//        Channel channel = connection.createChannel(false);
//        try {
//            channel.queueDeclareNoWait(queueName, true, false, false, null);
//        } finally {
//            RabbitUtils.closeChannel(channel);
//            RabbitUtils.closeConnection(connection);
//        }
//    }
//}
