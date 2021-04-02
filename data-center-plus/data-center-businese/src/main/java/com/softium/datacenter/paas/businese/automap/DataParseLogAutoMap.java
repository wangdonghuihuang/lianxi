package com.softium.datacenter.paas.web.automap;

import com.softium.datacenter.paas.api.dto.InventoryDataDTO;
import com.softium.datacenter.paas.api.dto.PurchaseDataDTO;
import com.softium.datacenter.paas.api.dto.SaleDataDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huashan.li
 */
public class DataParseLogAutoMap {
    public static List<List> saleDataLog(List<SaleDataDTO> saleDataLogs){
        List<List> result = new ArrayList<>();
        for(SaleDataDTO s : saleDataLogs){
            List item = new ArrayList();
            item.add(0,s.getSaleId());
            item.add(1,s.getSaleDate());
            item.add(2,s.getOrderDate());
            item.add(3,s.getInstitutionCode());
            item.add(4,s.getInstitutionName());
            item.add(5,s.getCustomerCode());
            item.add(6,s.getCustomerName());
            item.add(7,s.getCustomerAddr());
            item.add(8,s.getProductCode());
            item.add(9,s.getProductName());
            item.add(10,s.getGeneralName());
            item.add(11,s.getProductSpec());
            item.add(12,s.getProductBatch());
            item.add(13,s.getProductDate());
            item.add(14,s.getValidDate());
            item.add(15,s.getProductModel());
            item.add(16,s.getProductLine());
            item.add(17,s.getQuantity());
            item.add(18,s.getProductUnit());
            item.add(19,s.getPrice());
            item.add(20,s.getAmount());
            item.add(21,s.getProducer());
            item.add(22,s.getBehavior());
            item.add(23,s.getSaleOrder());
            item.add(24,s.getDespatchOrder());
            item.add(25,s.getCompanyName());
            item.add(26,s.getWarehouse());
            item.add(27,s.getVendorName());
            item.add(28,s.getDepartment());
            item.add(29,s.getInvoiceDate());
            item.add(30,s.getTaxAmount());
            item.add(31,s.getRealm());
            item.add(32,s.getCost());
            item.add(33,s.getRemark());
            item.add(34,s.getFailCause());
            result.add(item);
        }
        return result;
    }
    public static List<List> purchaseDataLog(List<PurchaseDataDTO> dataLogs){
        List<List> result = new ArrayList<>();
        for(PurchaseDataDTO s : dataLogs){
            List item = new ArrayList();
            item.add(0,s.getPurchaseId());
            item.add(1,s.getPurchaseDate());
            item.add(2,s.getOrderDate());
            item.add(3,s.getInstitutionCode());
            item.add(4,s.getInstitutionName());
            item.add(5,s.getVendorCode());
            item.add(6,s.getVendorName());
            item.add(7,s.getProductCode());
            item.add(8,s.getProductName());
            item.add(9,s.getGeneralName());
            item.add(10,s.getProductSpec());
            item.add(11,s.getProductBatchCode());
            item.add(12,s.getProductDate());
            item.add(13,s.getValidDate());
            item.add(14,s.getProductModel());
            item.add(15,s.getProductLine());
            item.add(16,s.getQuantity());
            item.add(17,s.getProductUnit());
            item.add(18,s.getPrice());
            item.add(19,s.getAmount());
            item.add(20,s.getProducer());
            item.add(21,s.getBehavior());
            item.add(22,s.getPurchaseOrderNum());
            item.add(23,s.getLogisticsOrderNum());
            item.add(24,s.getDeliveryList());
            item.add(25,s.getCompanyName());
            item.add(26,s.getWarehouse());
            item.add(27,s.getRealm());
            item.add(28,s.getPurchaseRemark());
            item.add(29,s.getFailCause());
            result.add(item);
        }
        return result;
    }

    public static List<List> inventoryDataLog(List<InventoryDataDTO> dataLogs){
        List<List> result = new ArrayList<>();
        for(InventoryDataDTO s : dataLogs){
            List item = new ArrayList();
            item.add(0,s.getInventoryId());
            item.add(1,s.getInventoryDate());
            item.add(2,s.getInstitutionCode());
            item.add(3,s.getInstitutionName());
            item.add(4,s.getProductCode());
            item.add(5,s.getProductName());
            item.add(6,s.getGeneralName());
            item.add(7,s.getProductSpec());
            item.add(8,s.getProductBatchCode());
            item.add(9,s.getProductDate());
            item.add(10,s.getValidDate());
            item.add(11,s.getProductModel());
            item.add(12,s.getProductLine());
            item.add(13,s.getQuantity());
            item.add(14,s.getSaleableQuantity());
            item.add(15,s.getUnsaleableQuantity());
            item.add(16,s.getProductUnit());
            item.add(17,s.getPrice());
            item.add(18,s.getAmount());
            item.add(19,s.getWarehouseDate());
            item.add(20,s.getOrderDate());
            item.add(21,s.getProducer());
            item.add(22,s.getInventoryStatus());
            item.add(23,s.getCompanyName());
            item.add(24,s.getWarehouse());
            item.add(25,s.getSupplierName());
            item.add(26,s.getRealm());
            item.add(27,s.getFailCause());
            result.add(item);
        }
        return result;
    }
}
