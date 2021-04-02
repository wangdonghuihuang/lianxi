package com.softium.datacenter.paas.web.utils.easy.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 2019/11/12
 *
 * @author paul
 */
public class SimpExcelConsumer<T> extends ExcelConsumer<T> {
    Logger logger = LoggerFactory.getLogger(getClass());

    Consumer<List<T>> consumer;
    Function<Object, T> convert;
    ObjectMapper objectMapper;
    Class<T> clazz;

    public SimpExcelConsumer(CacheExcelData cacheExcelData, String token) {
        super(cacheExcelData, token);
    }

    public SimpExcelConsumer<T> objectMapper(ObjectMapper objectMapper, Class<T> clazz) {
        this.objectMapper = objectMapper;
        this.clazz = clazz;
        return this;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug(" SimpExcelConsumer init");
        }
    }

    @Override
    public T convert(Object object) throws IOException {
        if (convert == null) {
            return objectMapper.readValue((String) object, clazz);
        }
        return convert.apply(object);
    }

    @Override
    public void consumer(List<T> data) {
        consumer.accept(data);
    }


    /**
     * 类型转换器
     *
     * @param convert
     * @return
     */
    public SimpExcelConsumer<T> convert(Function<Object, T> convert) {
        this.convert = convert;
        return this;
    }

    /**
     * 消费者
     *
     * @param consumer
     * @return
     */
    public SimpExcelConsumer<T> consumer(Consumer<List<T>> consumer) {
        this.consumer = consumer;
        return this;
    }

}
