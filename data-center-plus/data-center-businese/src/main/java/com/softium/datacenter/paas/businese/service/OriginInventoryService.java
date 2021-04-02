package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.entity.OriginInventory;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
public interface OriginInventoryService {
    void batchInsertOriginInventory(String tableName,List<OriginInventory> originInventoryList);
}
