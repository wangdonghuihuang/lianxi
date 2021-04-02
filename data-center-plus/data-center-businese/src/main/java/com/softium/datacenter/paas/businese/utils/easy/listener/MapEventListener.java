package com.softium.datacenter.paas.web.utils.easy.listener;

import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.MapExcelReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 * 2019/11/11
 *
 * @author paul
 */
public class MapEventListener extends ObjectEventListener<MapExcelReadModel, Map<Integer, Object>> {

    Logger logger = LoggerFactory.getLogger(getClass());


    public MapEventListener(int batchFilterRowNumber, CountDownLatch countDownLatch, CacheExcelData storage) {
        super(batchFilterRowNumber, countDownLatch, storage);
    }

    @Override
    protected MapExcelReadModel convertData(Map<Integer, Object> data, Integer rowIndex) {
        MapExcelReadModel mapExcelReadModel = new MapExcelReadModel(data);
        mapExcelReadModel.setRowIndex(rowIndex);
        if (logger.isDebugEnabled()) {
            logger.debug(" rowIndex [{}] data [{}] ", rowIndex, data);
        }
        return mapExcelReadModel;
    }


}
