package com.softium.datacenter.paas.web.utils;

public class JobConstant {
    //通讯协议 mq
    public static final String PROTOCOL_MQ = "MQ";
    //通讯协议 http
    public static final String PROTOCOL_HTTP = "HTTP";
    //通讯协议 ribbon
    public static final String PROTOCOL_RIBBON = "RIBBON";
    //public static final String JOB_SERVICE_NAME = "http://std055-internal.softium.cn/api/paas-job/updateJobLog";
    //任务服务端地址
    //public static final String baseUrl ="http://std055-internal.softium.cn/api/paas-job";
    //任务执行中
    public static final String EXECUTE_EXECUTING = "EXECUTING";
    /**
     * 任务执行成功
     */
    public static final String EXECUTE_SUCCESS = "SUCCESS";
    /**
     * 任务执行失败
     */
    public static final String EXECUTE_FAILURE = "FAILURE";

    public static final String JOB = "job";
    public static final String TRIGGER = "trigger";
    public static final String JOB_GROUP_NAME = "DEFAULT_JOB";
    public static final String TRIGGER_GROUP_NAME = "DEFAULT_TRIGGER";
    public static final String EXPIRED_ERROR = "Based on configured schedule, the given trigger '%s' will never fire.";
    //启用状态
    public static final String STATUS_ENABLE = "ENABLE";
    //禁用状态
    public static final String STATUS_DISABLE = "DISABLE";
    /**
     * 构建任务队列名称
     *
     * @param appId 应用Id
     * @return 队列名称
     */
    public static String buildQueueName(String appId) {
        return "JobQueue/" + appId;
    }
    public static String buildRinseQueueName(String suffix) {return "DataCenter/" + suffix;};

}
