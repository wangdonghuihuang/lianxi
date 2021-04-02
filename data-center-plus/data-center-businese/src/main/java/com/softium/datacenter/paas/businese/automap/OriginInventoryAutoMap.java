package com.softium.datacenter.paas.web.automap;


import com.softium.datacenter.paas.api.entity.OriginInventory;
import com.softium.framework.common.SystemContext;
import com.softium.framework.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fanfan.Gong
 **/
public class OriginInventoryAutoMap {
    private static final Map<String, String> propertyMap = new HashMap<>(20);
    static {
        propertyMap.put("库存表主键", "inventoryId");
        propertyMap.put("日期","inventoryDate");
        propertyMap.put("经销商代码", "institutionCode");
        propertyMap.put("经销商名称", "institutionName");
        propertyMap.put("产品代码", "productCode");
        propertyMap.put("产品名称", "productName");
        propertyMap.put("通用名", "generalName");
        propertyMap.put("产品规格", "productSpec");
        propertyMap.put("产品批号", "productBatchCode");
        propertyMap.put("生产日期", "productDate");
        propertyMap.put("有效期", "validDate");
        propertyMap.put("产品型号", "productModel");
        propertyMap.put("产品线", "productLine");
        propertyMap.put("数量", "quantity");
        propertyMap.put("可销数量", "saleableQuantity");
        propertyMap.put("不可销数量", "unsaleableQuantity");
        propertyMap.put("单位", "productUnit");
        propertyMap.put("单价", "price");
        propertyMap.put("金额", "amount");
        propertyMap.put("入库日期", "warehouseDate");
        propertyMap.put("订单日期", "orderDate");
        propertyMap.put("生产厂家", "producer");
        propertyMap.put("库存状态", "inventoryStatus");
        propertyMap.put("子公司名称", "companyName");
        propertyMap.put("仓库", "warehouse");
        propertyMap.put("供应商名称", "supplierName");
        propertyMap.put("物权", "realm");
        propertyMap.put("rowNum", "rowNum");
    }
    private static final Method[] methods = OriginInventory.class.getMethods();
    public static OriginInventory originInventory(Map<String, Object> data, String fileId, String userId,String projectId,String periodId,String collecType) {
        OriginInventory originInventory = new OriginInventory();
        originInventory.setId(UUIDUtils.getUUID());
        originInventory.setFileId(fileId);
        originInventory.setCreateBy(userId);
        originInventory.setCreateTime(new Date());
        originInventory.setAccessType(collecType);
        if(SystemContext.getTenantId()==null|| StringUtils.isEmpty(SystemContext.getTenantId())){
            originInventory.setTenantId(projectId);
        }else {
            originInventory.setTenantId(SystemContext.getTenantId());
        }
        originInventory.setPeriodId(periodId);
        //originInventory.setProjectId(projectId);
        data.forEach((k, v) -> {
            String clazzProperty = propertyMap.get(k);
            for(Method method : methods) {
                if (method.getName().equalsIgnoreCase("set" + clazzProperty)) {
                    try {
                        method.invoke(originInventory, v);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return originInventory;
    }
}
