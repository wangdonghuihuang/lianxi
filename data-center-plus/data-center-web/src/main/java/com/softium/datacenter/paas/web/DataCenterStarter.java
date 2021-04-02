package com.softium.datacenter.paas.web;

import com.softium.datacenter.paas.api.mongo.repository.CleaningRulesSettingRepository;
import com.softium.framework.orm.common.mybatis.MapperBeanNameGenerator;
import com.softium.framework.rpc.service.ServiceProxyScan;
import com.softium.framework.starter.autoconfigure.DbInspect;
import com.softium.framework.starter.autoconfigure.mybatis.EnableSharding;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Fanfan.Gong
 **/
@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = {CleaningRulesSettingRepository.class})
@MapperScan(value = "com.softium.datacenter.paas.**.mapper", nameGenerator = MapperBeanNameGenerator.class)
@DbInspect("com.softium.datacenter.paas.api.entity")
@ServiceProxyScan(basePackages = "com.softium.datacenter.paas.web.manage")
@EnableSharding
//@EnableCasClient
public class DataCenterStarter {
    public static void main(String[] args) {
        SpringApplication.run(DataCenterStarter.class, args);
    }
}
