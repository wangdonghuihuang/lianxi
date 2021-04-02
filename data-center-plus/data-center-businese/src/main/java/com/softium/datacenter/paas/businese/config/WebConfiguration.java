package com.softium.datacenter.paas.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Configuration
@Slf4j
public class WebConfiguration {
    @Value("${paas.host}")
    private String host;

    @Value("${mdm.host}")
    private String mdmHost;

    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestInterceptor clientHttpRequestInterceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            log.debug(httpRequest.getURI().toString());
            log.debug(httpRequest.getHeaders().toString());
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        return new RestTemplateBuilder().interceptors(List.of(clientHttpRequestInterceptor)).rootUri(host).build();
    }

    @Bean
    public RestTemplate mdmRestTemplate() {
        ClientHttpRequestInterceptor clientHttpRequestInterceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            log.debug(httpRequest.getURI().toString());
            log.debug(httpRequest.getHeaders().toString());
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        return new RestTemplateBuilder().interceptors(List.of(clientHttpRequestInterceptor)).rootUri(mdmHost).build();
    }
}
