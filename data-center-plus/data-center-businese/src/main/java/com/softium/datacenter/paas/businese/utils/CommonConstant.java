package com.softium.datacenter.paas.web.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class CommonConstant {
    @Value("${job.serviceName}")
    private String JOB_SERVICE_NAME;
    @Value("${job.baseUrl}")
    private String baseUrl;
}
