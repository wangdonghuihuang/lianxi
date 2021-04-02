package com.softium.datacenter.paas.web.automap;


import com.softium.datacenter.paas.api.entity.OriginPurchase;
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
public class OriginPurchaseAutoMap {
    private static final Map<String, String> propertyMap = new HashMap<>(20);
    static {
        propertyMap.put("采购表主键", "purchaseId");
        propertyMap.put("日期", "purchaseDate");
        propertyMap.put("订单日期", "orderDate");
        propertyMap.put("经销商代码", "institutionCode");
        propertyMap.put("经销商名称", "institutionName");
        propertyMap.put("供应商代码", "vendorCode");
        propertyMap.put("供应商名称", "vendorName");
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
        propertyMap.put("单位", "productUnit");
        propertyMap.put("单价", "price");
        propertyMap.put("金额", "amount");
        propertyMap.put("生产厂家", "producer");
        propertyMap.put("采购行为", "behavior");
        propertyMap.put("采购单号", "purchaseOrderNum");
        propertyMap.put("进货单号", "logisticsOrderNum");
        propertyMap.put("原厂发货清单", "deliveryList");
        propertyMap.put("子公司名称", "companyName");
        propertyMap.put("仓库", "warehouse");
        propertyMap.put("物权", "realm");
        propertyMap.put("采购备注", "purchaseRemark");
        propertyMap.put("rowNum", "rowNum");
    }
    private static final Method[] methods = OriginPurchase.class.getMethods();
    public static OriginPurchase originPurchase(Map<String, Object> data, String fileId, String userId,String projectId,String periodId,String collecType) {
        OriginPurchase originPurchase = new OriginPurchase();
        originPurchase.setId(UUIDUtils.getUUID());
        originPurchase.setFileId(fileId);
        originPurchase.setCreateBy(userId);
        originPurchase.setAccessType(collecType);
        if(SystemContext.getTenantId()==null|| StringUtils.isEmpty(SystemContext.getTenantId())){
            originPurchase.setTenantId(projectId);
        }else {
            originPurchase.setTenantId(SystemContext.getTenantId());
        }

        originPurchase.setCreateTime(new Date());
        originPurchase.setPeriodId(periodId);
        //originPurchase.setProjectId(projectId);
        data.forEach((k, v) -> {
            String clazzProperty = propertyMap.get(k);
            for(Method method : methods) {
                if (method.getName().equalsIgnoreCase("set" + clazzProperty)) {
                    try {
                        method.invoke(originPurchase, v);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return originPurchase;
    }
}
