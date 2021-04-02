package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.OriginInventory;
import com.softium.datacenter.paas.api.mapper.OriginInventoryMapper;
import com.softium.datacenter.paas.web.service.OriginInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Service
public class OriginInventoryServiceImpl implements OriginInventoryService {
    @Autowired
    private OriginInventoryMapper originInventoryMapper;

    private static final Integer BATCH_SIZE = 50;

    @Override
    public void batchInsertOriginInventory(String tableName, List<OriginInventory> originInventoryList) {
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(originInventoryList)) {
            int size = originInventoryList.size();
            int fileParseBatchCount = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < fileParseBatchCount; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > originInventoryList.size()) {
                    end = originInventoryList.size();
                }
                originInventoryMapper.batchInsertOriginInventory(tableName, originInventoryList.subList(start, end));
            }
        }
        //originInventoryMapper.batchInsert(originInventoryList);
    }
}
