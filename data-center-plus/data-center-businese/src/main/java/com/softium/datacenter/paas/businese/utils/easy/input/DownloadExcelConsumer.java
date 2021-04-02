package com.softium.datacenter.paas.web.utils.easy.input;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 2019/11/11
 *
 * @author paul
 */
public class DownloadExcelConsumer extends ExcelConsumer {

    Logger logger = LoggerFactory.getLogger(getClass());

    private Class temp;
    private ObjectMapper objectMapper;
    public final ExcelWriter build;
    public final WriteSheet s;


    public DownloadExcelConsumer(Class temp, ObjectMapper objectMapper, OutputStream outputStream, CacheExcelData cacheExcelData, String token) {
        super(cacheExcelData, token);
        this.temp = temp;
        this.objectMapper = objectMapper;
        this.build = EasyExcel.write(outputStream, temp).build();
        this.s = EasyExcel.writerSheet().build();
        this.build.write(List.of(), s);
    }

    private DownloadExcelConsumer(CacheExcelData cacheExcelData, String token, ExcelWriter build, WriteSheet s) {
        super(cacheExcelData, token);
        this.build = build;
        this.s = s;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("init ");
        }
    }

    @Override
    public Object convert(Object object) throws IOException {
        if (objectMapper == null) {
            return object;
        }
        return objectMapper.readValue(object.toString(), temp);
    }

    @Override
    public void consumer(List data) {
        build.write(data, s);
    }


}
