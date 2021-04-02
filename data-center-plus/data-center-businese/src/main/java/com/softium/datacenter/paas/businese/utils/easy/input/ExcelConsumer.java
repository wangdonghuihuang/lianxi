package com.softium.datacenter.paas.web.utils.easy.input;

import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 消费者
 */
public abstract class ExcelConsumer<T> {


    protected final CacheExcelData cacheExcelData;
    protected final String token;
    private LongAdder start = new LongAdder();
    private LongAdder end = new LongAdder();


    public ExcelConsumer(CacheExcelData cacheExcelData, String token) {
        this.cacheExcelData = cacheExcelData;
        this.token = token;
    }

    private void add(int customerNumber) {
        if (end.longValue() != 0) {
            start.add(customerNumber);
        }

        end.add(customerNumber);
    }

    protected void pull(int customerNumber) throws IOException {
        this.add(customerNumber);
        List list = cacheExcelData.lPopList(token, start.longValue(), end.longValue()-1);
        if (CollectionUtils.isEmpty(list)) {
            return;
        } else {
            List<T> data = new ArrayList<>(list.size());
            for (Object o : list) {
                data.add(this.convert(o));
            }
            this.consumer(data);
        }
    }


    public synchronized void pullAll(int customerNumber) throws IOException {
        if (customerNumber <= 0) {
            throw new RuntimeException("000000000000000000000000000000 不允许");
        }
        this.add(customerNumber);
        List list = cacheExcelData.lPopList(token, start.longValue(), end.longValue()-1);
        if (CollectionUtils.isEmpty(list)) {
            return;
        } else {
            List<T> data = new ArrayList<>(list.size());
            for (Object o : list) {
                data.add(this.convert(o));
            }
            this.consumer(data);
            this.pullAll(customerNumber);
        }
    }


    public abstract void init();

    public abstract T convert(Object object) throws IOException;

    public abstract void consumer(List<T> data);
}
