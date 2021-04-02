package com.softium.datacenter.paas.web.utils.easy.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.softium.datacenter.paas.web.utils.easy.input.*;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateServer;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateComponent;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ObjectEventListener<T extends BaseExcelReadModel, D> extends AnalysisEventListener<D> {

    Logger logger = LoggerFactory.getLogger(getClass());

    public final int batchFilterRowNumber;
    public MessageFunction<T> filter;
    public String errorToken;
    public String token;
    public String convertToken;
    public final CountDownLatch countDownLatch;
    public ValidateServer<T> validateServer;
    public ConvertExcelReadModelFunction<T> convert;
    public CacheExcelData storage;
    public CacheExcelData convertStorage;

    public CombinationFilterAndConvert<T> filterAndConvert;
    /**
     * 数据存储
     */
    public List<T> dataList = new ArrayList<>();
    /**
     *
     */
    public Map<Integer, Map<Integer, String>> validateMap = new ConcurrentHashMap<>();

    volatile boolean block = false;
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    private final boolean isValidate;

    protected final Class<T> t;

    /**
     * @param t                    topic
     * @param batchFilterRowNumber batchFilterRowNumber
     * @param countDownLatch
     */
    public ObjectEventListener(Class<T> t, int batchFilterRowNumber, CountDownLatch countDownLatch, CacheExcelData storage) {
        this.batchFilterRowNumber = batchFilterRowNumber;
        this.countDownLatch = countDownLatch;
        String s = UUID.randomUUID().toString();
        this.token = "Excel:" + s;
        this.storage = storage;
        this.convertToken = "Excel:convert:" + s;
        this.validateServer = new ValidateServer(t);
        this.isValidate = true;
        this.t = t;
    }

    public ObjectEventListener(int batchFilterRowNumber, CountDownLatch countDownLatch, CacheExcelData storage) {
        this.batchFilterRowNumber = batchFilterRowNumber;
        this.countDownLatch = countDownLatch;
        String s = UUID.randomUUID().toString();
        this.token = "Excel:" + s;
        this.storage = storage;
        this.convertToken = "Excel:convert:" + s;
        this.validateServer = new ValidateServer(Object.class);
        this.isValidate = false;
        this.t = null;
    }


    private void mergeErrorMessage(final MessageModel messageModel, final List<T> list) {

        Map<Integer, StringBuffer> objectObjectHashMap = new ConcurrentHashMap<>(list.size());
        try {
            CompletableFuture.allOf(
                    CompletableFuture.runAsync(() ->
                    {
                        //error
                        if (CollectionUtils.isNotEmpty(messageModel.error)) {
                            messageModel.error.stream().forEach(a -> {
                                if (StringUtils.isNotBlank(a.getMessage())) {
                                    Integer rowIndex = a.getRowIndex();
                                    objectObjectHashMap.merge(rowIndex, new StringBuffer(a.getMessage()), (old, newv) -> old.append(newv));
                                }
                            });
                        }

                    }),
                    CompletableFuture.runAsync(() -> {
                        if (CollectionUtils.isNotEmpty(messageModel.warn)) {
                            // warn
                            messageModel.warn.stream().forEach(a -> {
                                if (StringUtils.isNotBlank(a.getMessage())) {
                                    Integer rowIndex = a.getRowIndex();
                                    objectObjectHashMap.merge(rowIndex, new StringBuffer(a.getMessage()), (old, newv) -> old.append(newv));
                                }
                            });
                        }
                    })

            ).get();
        } catch (Exception e) {

            logger.info("合并错误信息错误 [{}]", e);
        }

        if (!objectObjectHashMap.isEmpty()) {
            // 合并
            list.stream().forEach(a -> {
                StringBuffer stringBuffer = objectObjectHashMap.get(a.getRowIndex());
                if (stringBuffer != null) {
                    if (StringUtils.isNotBlank(a.getMessage())) {
                        a.setMessage(a.getMessage() + stringBuffer.toString());
                    } else {
                        a.setMessage(stringBuffer.toString());
                    }
                }
            });

        }

    }

    @Override
    public void invoke(D data, AnalysisContext context) {

        lock.lock();
        try {
            if (block) {
                condition.await();
            }
            T t = this.convertData(data, context.readRowHolder().getRowIndex());

            // validate
            if (isValidate) {
                MessageModel validate = validateServer.validate(t, context.readRowHolder().getRowIndex());
                copyValidate(validate, context.readRowHolder().getRowIndex());
            }
            dataList.add(t);
            if (batchFilterRowNumber == dataList.size()) {
                dealWith();
            }
        } catch (Exception e) {
            validateServer.messageModel.error.add(new Message(context.readRowHolder().getRowIndex(), null, "此条数据解析失败"));

            //error
            logger.error("error=[{}]", e);
        } finally {
            lock.unlock();
        }


    }


    protected void copyValidate(final MessageModel model, final Integer rowIndex) {

        if (model != null) {
            Map<Integer, String> integerSetMap = validateMap.get(rowIndex);
            if (integerSetMap == null) {
                integerSetMap = new HashMap<>();
                validateMap.put(rowIndex, integerSetMap);
            }

            final Map<Integer, String> integerSetMapT = integerSetMap;
            final List<Message> error = model.error;
            if (CollectionUtils.isNotEmpty(error)) {
                error.stream().forEach(message -> {
                    if (StringUtils.isNotBlank(integerSetMapT.get(message.getColumnIndex()))) {
                        return;
                    } else {
                        integerSetMapT.put(message.getColumnIndex(), ValidateServer.ERROR_KEY);
                    }
                });
            }

        }


    }


    protected abstract T convertData(D data, Integer rowIndex);

    protected void dealWith() {

        lock.lock();
        // validate
        try {
            block = true;
            // 此处应该异步
            final List<T> ts = Collections.synchronizedList(new ArrayList<>(dataList));
            final ValidateComponent validateComponent = new ValidateComponent(Map.copyOf(validateMap));
            dataList.clear();
            validateMap.clear();
            block = false;
            condition.signal();
            // 校验
            if (Objects.nonNull(filter)) {
                MessageModel messageModel = filter.apply(new CustomerData<>(ts, validateComponent));
                validateServer.messageModel.copyModel(messageModel);
                mergeErrorMessage(messageModel, ts);
            }
            // 转换存储
            // Persistent storage
            if (Objects.nonNull(convert) && Objects.nonNull(convertStorage)) {
                // convert
                convertStorage.lPushAll(convertToken, convert.apply(ts));
            }

            //组合 校验 和转换
            if (Objects.nonNull(filterAndConvert) && Objects.nonNull(convertStorage)) {
                CombinationResultModel apply = filterAndConvert.apply(new CustomerData<>(ts, validateComponent));
                if (apply != null) {
                    validateServer.messageModel.copyModel(apply);
                    if (CollectionUtils.isNotEmpty(apply.list)) {
                        convertStorage.lPushAll(convertToken, apply.list);
                    }

                    // message
                    mergeErrorMessage(apply, ts);
                }

            }

            // 存储
            // Persistent storage
            storage.lPushAll(token, ts);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        logger.error("解析错误 [{}] ", exception);
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            Integer rowIndex = excelDataConvertException.getRowIndex();
            Integer columnIndex = excelDataConvertException.getColumnIndex();
            // 这里出错会丢失一条数据
            // 用来记录错误数据
            validateServer.messageModel.error.add(new Message(rowIndex, columnIndex, excelDataConvertException.getMessage()));
        }

        countDownLatch.countDown();
        throw new BusinessException(new ErrorInfo("", "解析失败"));

    }


    /**
     * 执行之后
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {


        try {
            dealWith();
            if (CollectionUtils.isNotEmpty(validateServer.messageModel.error) || CollectionUtils.isNotEmpty(validateServer.messageModel.warn)) {
                errorToken = token;
            }
        } finally {
            // 通知线程返回结果
            countDownLatch.countDown();
        }


    }


}
