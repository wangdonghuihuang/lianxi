package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.entity.OriginSale;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
public interface OriginSaleService {
    void batchInsertOriginSale(String tableName,List<OriginSale> originSaleList);
}
