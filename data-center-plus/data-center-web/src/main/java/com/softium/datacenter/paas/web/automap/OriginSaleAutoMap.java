package com.softium.datacenter.paas.web.automap;

import com.softium.datacenter.paas.api.entity.OriginSale;
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
public class OriginSaleAutoMap {
    private static final Map<String, String> propertyMap = new HashMap<>(20);
    static {
        propertyMap.put("客户代码", "customerCode");
        propertyMap.put("有效期", "validDate");
        propertyMap.put("数量", "quantity");
        propertyMap.put("销售单号", "saleOrder");
        propertyMap.put("经销商名称", "institutionName");
        propertyMap.put("单位", "productUnit");
        propertyMap.put("金额", "amount");
        propertyMap.put("单价", "price");
        propertyMap.put("订单日期", "orderDate");
        propertyMap.put("产品规格", "productSpec");
        propertyMap.put("销售备注", "remark");
        propertyMap.put("产品名称", "productName");
        propertyMap.put("经销商代码", "institutionCode");
        propertyMap.put("产品代码", "productCode");
        propertyMap.put("生产厂家", "producer");
        propertyMap.put("销售行为", "behavior");
        propertyMap.put("生产日期", "productDate");
        propertyMap.put("发运单", "despatchOrder");
        propertyMap.put("客户地址", "customerAddr");
        propertyMap.put("产品批号", "productBatch");
        propertyMap.put("客户名称", "customerName");
        propertyMap.put("日期", "saleDate");
        propertyMap.put("销售表主键", "saleId");
        propertyMap.put("通用名", "generalName");
        propertyMap.put("产品型号", "productModel");
        propertyMap.put("产品线", "productLine");
        propertyMap.put("子公司名称", "companyName");
        propertyMap.put("仓库", "warehouse");
        propertyMap.put("供应商名称", "vendorName");
        propertyMap.put("科室", "department");
        propertyMap.put("开票日期", "invoiceDate");
        propertyMap.put("税额", "taxAmount");
        propertyMap.put("物权", "realm");
        propertyMap.put("销售成本", "cost");
        propertyMap.put("rowNum", "rowNum");
    }
    private static final Method[] methods = OriginSale.class.getMethods();
    public static OriginSale originSale(Map<String, Object> data, String fileId, String userId,String projectId,String periodId,String collecType) {
        OriginSale originSale = new OriginSale();
        originSale.setId(UUIDUtils.getUUID());
        originSale.setFileId(fileId);
        originSale.setCreateBy(userId);
        originSale.setAccessType(collecType);
        if(SystemContext.getTenantId()==null|| StringUtils.isEmpty(SystemContext.getTenantId())){
            originSale.setTenantId(projectId);
        }else {
            originSale.setTenantId(SystemContext.getTenantId());
        }

        originSale.setCreateTime(new Date());
        originSale.setPeriodId(periodId);
        //originSale.setProjectId(projectId);
        data.forEach((k, v) -> {
            String clazzProperty = propertyMap.get(k);
            for(Method method : methods) {
                if (method.getName().equalsIgnoreCase("set" + clazzProperty)) {
                    try {
                        method.invoke(originSale, v);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return originSale;
    }
}
