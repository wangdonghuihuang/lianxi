package com.softium.datacenter.paas.web.config;

import com.softium.datacenter.paas.web.common.ConstantCacheMap;
import com.softium.datacenter.paas.web.common.GetBeanClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**项目启动执行加载全局map*/
@Component
@Order(1)
@Slf4j
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动初始化操作...");
        ConstantCacheMap cacheMap= GetBeanClass.getBean(ConstantCacheMap.class);
        cacheMap.initCache();
    }
}
