package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.InventoryDataDTO;
import com.softium.datacenter.paas.api.dto.PurchaseDataDTO;
import com.softium.datacenter.paas.api.dto.SaleDataDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.framework.common.dto.ActionResult;

import java.util.List;

/**
 * @author huashan.li
 */

public interface OriginDataService {
    List<SaleDataDTO> saleList(OriginDataQuery originDataQuery);

    SaleDataDTO getId(String id);

    List<PurchaseDataDTO> purchaseList(OriginDataQuery originDataQuery);

    PurchaseDataDTO getPurchaseId(String id);

    List<InventoryDataDTO> inventoryList(OriginDataQuery originDataQuery);

    InventoryDataDTO getInventoryId(String id);

    int deleteSale(List<SaleDataDTO> saleDataDTOS);
    ActionResult queryAllData(OriginDataQuery queryDTO);
}
