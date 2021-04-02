package com.softium.datacenter.paas.web.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

/**自定义线程池配置类*/
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    @Value("${ddi.thread-pool.core-pool-size}")
    private int corePoolSize;
    @Value("${ddi.thread-pool.max-pool-size}")
    private int maxPoolSize;
    @Value("${ddi.thread-pool.keep-alive-seconds}")
    private int keepAliveSeconds;
    @Value("${ddi.thread-pool.queue-capacity}")
    private int queueCapacity;
    @Value("${ddi.thread-pool.daemon}")
    private boolean daemon;
    @Value("${ddi.thread-pool.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean("taskExecutorPool")
    public ExecutorService taskExecutor(){
        ThreadFactory threadFactory=new ThreadFactoryBuilder().setNameFormat(threadNamePrefix).build();
        return new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveSeconds,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(queueCapacity),threadFactory,new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
