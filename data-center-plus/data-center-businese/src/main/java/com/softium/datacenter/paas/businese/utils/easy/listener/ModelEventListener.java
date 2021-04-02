package com.softium.datacenter.paas.web.utils.easy.listener;

import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.ExcelReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 2019/11/11
 *
 * @author paul
 */
public class ModelEventListener extends ObjectEventListener<ExcelReadModel, ExcelReadModel> {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param t                    topic
     * @param batchFilterRowNumber batchFilterRowNumber
     * @param countDownLatch
     * @param storage
     */
    public ModelEventListener(Class<ExcelReadModel> t, int batchFilterRowNumber, CountDownLatch countDownLatch, CacheExcelData storage) {
        super(t, batchFilterRowNumber, countDownLatch, storage);
    }

    @Override
    protected ExcelReadModel convertData(ExcelReadModel data, Integer rowIndex) {
        data.setRowIndex(rowIndex);
        if (logger.isDebugEnabled()) {
            logger.debug(" rowIndex [{}] data [{}] ", rowIndex, data);
        }
        return data;
    }


}
