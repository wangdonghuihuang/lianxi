package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.OriginSale;
import com.softium.datacenter.paas.api.mapper.OriginSaleMapper;
import com.softium.datacenter.paas.web.service.OriginSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Service
public class OriginSaleServiceImpl implements OriginSaleService {
    @Autowired
    private OriginSaleMapper originSaleMapper;

    private static final Integer BATCH_SIZE = 50;

    @Override
    public void batchInsertOriginSale(String tableName,List<OriginSale> originSaleList) {
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(originSaleList)) {
            int size = originSaleList.size();
            int fileParseBatchCount = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < fileParseBatchCount; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > originSaleList.size()) {
                    end = originSaleList.size();
                }
                originSaleMapper.batchInsertOriginSale(tableName,originSaleList.subList(start, end));
            }
        }
        //originSaleMapper.batchInsert(originSaleList);
    }
}
