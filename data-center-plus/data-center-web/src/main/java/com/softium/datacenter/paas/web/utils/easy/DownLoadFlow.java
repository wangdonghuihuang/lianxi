package com.softium.datacenter.paas.web.utils.easy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.DownloadExcelConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DownLoadFlow {

    Logger logger = LoggerFactory.getLogger(getClass());

    private String token;
    private CacheExcelData cacheExcelData;
    private OutputStream outputStream;
    private ObjectMapper objectMapper;
    private int batchNumber = 10000;
    private Class clazz;
    DownloadExcelConsumer downloadExcelConsumer;

    public DownLoadFlow(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public static DownLoadFlow init(OutputStream outputStream) {
        return new DownLoadFlow(outputStream);
    }

    public DownLoadFlow event(DownLoadEvent downLoadEvent) {
        downLoadEvent.event();
        return this;
    }

    public DownLoadFlow clazz(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public DownLoadFlow objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public DownLoadFlow cacheExcel(String token, CacheExcelData cacheExcelData, int batchNumber) {
        this.token = token;
        this.cacheExcelData = cacheExcelData;
        this.batchNumber = batchNumber;
        return this;
    }


    public DownLoadFlow customizeDealWith() {
        this.downloadExcelConsumer = new DownloadExcelConsumer(
                clazz,
                objectMapper,
                outputStream,
                cacheExcelData,
                token);
        return this;

    }


    public void downLoad(List list) {
        this.downloadExcelConsumer.consumer(list);
    }

    public DownLoadFlow downLoad() throws IOException {
        final long start = System.currentTimeMillis();
        downloadExcelConsumer.pullAll(batchNumber);
        final long end = System.currentTimeMillis();
        logger.info("start [{}] end [{}]", start, end);

        return this;
    }

    public void finish() {
        downloadExcelConsumer.build.finish();
    }

}
