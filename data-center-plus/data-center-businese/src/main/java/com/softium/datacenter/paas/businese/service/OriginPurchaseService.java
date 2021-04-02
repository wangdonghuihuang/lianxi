package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.entity.OriginPurchase;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
public interface OriginPurchaseService {
    void batchInsertOriginPurchase(String tableName,List<OriginPurchase> originPurchaseList);
}
