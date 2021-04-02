package com.softium.datacenter.paas.web.utils.easy.input;

import com.alibaba.excel.EasyExcel;
import com.softium.datacenter.paas.web.utils.easy.listener.MapEventListener;
import com.softium.datacenter.paas.web.utils.easy.listener.ModelEventListener;
import com.softium.datacenter.paas.web.utils.easy.listener.ObjectEventListener;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 数据生产 清晰过滤转换
 * 2019/11/8
 *
 * @author paul
 */
public class EasyExcelService<T extends BaseExcelReadModel> {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ERROR_MESSAGE_KEY = "errorList";
    public static final String WARN_MESSAGE_KEY = "warnList";
    public static final String DATA_TOKEN_KEY = "dataToken";
    public static final String ERROR_TOKEN_KEY = "errorToken";
    public static final String DATA_CONVERT_TOKEN_KEY = "dataConvertToken";


    /**
     * 生产者监听器
     */
    private ObjectEventListener producerListener;

    /**
     * 对应一个消息队列
     * 主题
     */
    private Class<T> topic;

    /**
     * 生产者
     */
    private InputStream producer;

    /**
     * 消费者
     */
    private MessageFunction<T> filter;
    /**
     * 一次清洗多少条
     */
    private Integer batchNumber = 6000;
    /**
     * Consumer Group
     * sheet number
     */
    private Integer group = 0;

    private CacheExcelData storage;

    //等待锁
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    // 转换和存储
    private ConvertExcelReadModelFunction<T> convert;
    private CacheExcelData convertStorage;

    private CombinationFilterAndConvert<T> filterAndConvert;

    /**
     * 原始数据转换
     *
     * @param convert        convert
     * @param convertStorage convertStorage
     * @return EasyExcelService
     */
    public EasyExcelService<T> convert(ConvertExcelReadModelFunction<T> convert, CacheExcelData convertStorage) {
        this.convertStorage = convertStorage;
        this.convert = convert;
        return this;
    }

    /**
     * 开始工作
     *
     * @return EasyExcelService
     */
    public EasyExcelService<T> startWork() {
        // 创建生产者监听器
        producerListener = objectEventListener(topic, batchNumber, countDownLatch, storage);
        // 数据转换
        producerListener.convert = convert;
        // 转换之后的数据存储
        producerListener.convertStorage = convertStorage;
        producerListener.filter = filter;
        // 组合转换
        producerListener.filterAndConvert = filterAndConvert;
        if (topic == null) {

            EasyExcel.read(producer, producerListener).sheet(group).doRead();
        } else {

            EasyExcel.read(producer, topic, producerListener).sheet(group).doRead();
        }

        return this;

    }

    /**
     * @param t                    t
     * @param batchFilterRowNumber batchFilterRowNumber
     * @param countDownLatch       countDownLatch
     * @param storage              storage
     * @return ObjectEventListener
     */
    protected ObjectEventListener objectEventListener(Class<T> t, int batchFilterRowNumber, CountDownLatch countDownLatch, CacheExcelData storage) {
        if (t == null) {
            return new MapEventListener(batchNumber, countDownLatch, storage);
        } else {
            return new ModelEventListener((Class<ExcelReadModel>) topic, batchNumber, countDownLatch, storage);
        }
    }

    /**
     * 阻塞返回结果
     *
     * @return map
     * @throws InterruptedException
     */
    public Map<String, Object> waitResult() {
        try {
            if (producerListener == null) {
                throw new RuntimeException("还没有开始工作");
            }
            //3000L, TimeUnit.MILLISECONDS
            countDownLatch.await();
            ObjectEventListener jobs = this.producerListener;
            // message
            MessageModel messageModel = jobs.validateServer.getMessageModel();

            Map<String, Object> data = new HashMap<>();
            // 错误信息
            data.put(ERROR_MESSAGE_KEY, messageModel.error);
            // 警告信息
            data.put(WARN_MESSAGE_KEY, messageModel.warn);
            // 原始数据token
            data.put(DATA_TOKEN_KEY, jobs.token);
            //错误token
            data.put(ERROR_TOKEN_KEY, jobs.errorToken);
            // 转换数据token
            data.put(DATA_CONVERT_TOKEN_KEY, jobs.convertToken);
            return data;
        } catch (Exception e) {
            logger.error(" 解析异常 [{}]", e);
            throw new BusinessException(new ErrorInfo("", "解析失败"));
        }

    }


    /**
     * 生产者
     *
     * @param producer 生产者
     * @param group    消费组
     * @return EasyExcelService
     */
    public EasyExcelService<T> producer(InputStream producer, Integer group) {
        this.producer = producer;
        this.group = group;
        return this;
    }

    public EasyExcelService<T> producer(InputStream producer) {
        this.producer = producer;
        this.group = 0;
        return this;
    }

    /**
     * 过滤
     */
    public EasyExcelService<T> filter(MessageFunction<T> filter) {
        this.filter = filter;
        return this;
    }


    /**
     * 转换之后的存储
     *
     * @param convertStorage
     * @return
     */
    public EasyExcelService<T> convertStorage(CacheExcelData convertStorage) {
        this.convertStorage = convertStorage;
        return this;
    }

    /**
     * 转换之后的存储
     *
     * @param filterAndConvert
     * @return
     */
    public EasyExcelService<T> filterAndConvert(CombinationFilterAndConvert<T> filterAndConvert) {
        this.filterAndConvert = filterAndConvert;
        return this;
    }

    /**
     * 一次处理多少条 默认 6000
     *
     * @param batchNumber
     * @return
     */
    public EasyExcelService<T> batchNumber(int batchNumber) {
        this.batchNumber = batchNumber;
        return this;
    }


    /**
     * @param topic   消费主题
     * @param storage 存储
     */
    public EasyExcelService(Class<T> topic, CacheExcelData storage) {
        this.topic = topic;
        this.storage = storage;
    }

    /**
     * @param storage 存储
     */
    public EasyExcelService(CacheExcelData storage) {
        this.storage = storage;
    }


}
