package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.OriginPurchase;
import com.softium.datacenter.paas.api.mapper.OriginPurchaseMapper;
import com.softium.datacenter.paas.web.service.OriginPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Service
public class OriginPurchaseServiceImpl implements OriginPurchaseService {

    @Autowired
    private OriginPurchaseMapper originPurchaseMapper;

    private static final Integer BATCH_SIZE = 50;

    @Override
    public void batchInsertOriginPurchase(String tableName,List<OriginPurchase> originPurchaseList) {
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(originPurchaseList)) {
            int size = originPurchaseList.size();
            int fileParseBatchCount = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < fileParseBatchCount; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > originPurchaseList.size()) {
                    end = originPurchaseList.size();
                }
                originPurchaseMapper.batchInsertOriginPurchase(tableName,originPurchaseList.subList(start, end));
            }
        }
       //originPurchaseMapper.batchInsert(originPurchaseList);
    }
}
