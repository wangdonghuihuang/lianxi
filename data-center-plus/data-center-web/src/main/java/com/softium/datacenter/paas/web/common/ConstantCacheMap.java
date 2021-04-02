package com.softium.datacenter.paas.web.common;

import com.softium.datacenter.paas.api.entity.OverAllConfig;
import com.softium.datacenter.paas.api.mapper.OverAllConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 常量缓存类
 * 适用于如数据库全局配置数据等，项目启动加载
 * 无需在于数据库交互
 * */
@Component
public class ConstantCacheMap {
    @Autowired
    OverAllConfigMapper overAllConfigMapper;
    /**全局校验数据字段匹配表数据*/
    public static final Map<String, List<OverAllConfig>> SD_ALL_CONFIG_MAP=new ConcurrentHashMap<>();
    public static final Map<String, List<OverAllConfig>> PD_ALL_CONFIG_MAP=new ConcurrentHashMap<>();
    public static final Map<String, List<OverAllConfig>> ID_ALL_CONFIG_MAP=new ConcurrentHashMap<>();
    /**全局匹配originsale数据表字段缓存*/
    public static final Map<String, String> FIELD_MAPPING_MAP=new ConcurrentHashMap<>();
    /**全局匹配origin_inventory库存字段*/
    public static final Map<String,String> ORIGIN_INVENTORY_MAP=new ConcurrentHashMap<>();
    /**全局匹配origin_Purchase采购字段*/
    public static final Map<String,String> ORIGIN_PURCHASE_MAP=new ConcurrentHashMap<>();
    /**数据表查询出月销售字段名称--对应文件名称*/
    public static Map<String,String>  saleMonthcolumnMap=new ConcurrentHashMap<>();
    /**数据表查询月销售导出全部字段名*/
    public static ArrayList<String> saleMonthtitleKeyList=new ArrayList<>();
    /**初始化日销售字段名称--文件映射*/
    
    /**初始化日销售导出列*/

    /**初始化月采购字段名称--文件映射*/
    public static Map<String,String>  purseMonthcolumnMap=new ConcurrentHashMap<>();
    /**初始化月采购导出列*/
    public static ArrayList<String> purMonthtitleKeyList=new ArrayList<>();
    /**初始化日采购字段名称--文件映射*/

    /**初始化日采购导出列*/

    /**初始化月库存字段名称--文件映射*/

    /**初始化月库存导出列*/

    /**初始化日库存字段名称--文件映射*/

    /**初始化日库存导出列*/

    /**初始化月发货字段名称--文件映射*/

    /**初始化月发货导出列*/

    /**初始化日发货字段名称--文件映射*/

    /**初始化日发货导出列*/

    /**初始化缓存方法*/
    public void initCache(){
        initSDConfig();
        initPDConfig();
        initIDConfig();
        initFieldOriginSale();
        initOriginInventory();
        initOriginPurchase();
        initSaleColumn();
        initSaleTitleCol();
    }
    /**初始化全局校验字段表*/
    private void initSDConfig(){
        List<OverAllConfig> overList=overAllConfigMapper.findByProperty(OverAllConfig::getDataType,"SD");
        SD_ALL_CONFIG_MAP.put("SDConfigMap",overList);
    }
    private void initPDConfig(){
        List<OverAllConfig> overList=overAllConfigMapper.findByProperty(OverAllConfig::getDataType,"PD");
        PD_ALL_CONFIG_MAP.put("PDConfigMap",overList);
    }
    private void initIDConfig(){
        List<OverAllConfig> overList=overAllConfigMapper.findByProperty(OverAllConfig::getDataType,"ID");
        ID_ALL_CONFIG_MAP.put("IDConfigMap",overList);
    }
    /**初始化全局origin_sale字段映射*/
    public void initFieldOriginSale(){
        FIELD_MAPPING_MAP.put("客户代码", "customerCode");
        FIELD_MAPPING_MAP.put("有效期", "validDate");
        FIELD_MAPPING_MAP.put("数量", "quantity");
        FIELD_MAPPING_MAP.put("销售单号", "saleOrder");
        FIELD_MAPPING_MAP.put("经销商名称", "institutionName");
        FIELD_MAPPING_MAP.put("单位", "productUnit");
        FIELD_MAPPING_MAP.put("金额", "amount");
        FIELD_MAPPING_MAP.put("单价", "price");
        FIELD_MAPPING_MAP.put("订单日期", "orderDate");
        FIELD_MAPPING_MAP.put("产品规格", "productSpec");
        FIELD_MAPPING_MAP.put("销售备注", "remark");
        FIELD_MAPPING_MAP.put("产品名称", "productName");
        FIELD_MAPPING_MAP.put("经销商代码", "institutionCode");
        FIELD_MAPPING_MAP.put("产品代码", "productCode");
        FIELD_MAPPING_MAP.put("生产厂家", "producer");
        FIELD_MAPPING_MAP.put("销售行为", "behavior");
        FIELD_MAPPING_MAP.put("生产日期", "productDate");
        FIELD_MAPPING_MAP.put("发运单", "despatchOrder");
        FIELD_MAPPING_MAP.put("客户地址", "customerAddr");
        FIELD_MAPPING_MAP.put("产品批号", "productBatch");
        FIELD_MAPPING_MAP.put("客户名称", "customerName");
        FIELD_MAPPING_MAP.put("日期", "saleDate");
        FIELD_MAPPING_MAP.put("销售表主键", "saleId");
        FIELD_MAPPING_MAP.put("通用名", "generalName");
        FIELD_MAPPING_MAP.put("产品型号", "productModel");
        FIELD_MAPPING_MAP.put("产品线", "productLine");
        FIELD_MAPPING_MAP.put("子公司名称", "companyName");
        FIELD_MAPPING_MAP.put("仓库", "warehouse");
        FIELD_MAPPING_MAP.put("供应商名称", "vendorName");
        FIELD_MAPPING_MAP.put("科室", "department");
        FIELD_MAPPING_MAP.put("开票日期", "invoiceDate");
        FIELD_MAPPING_MAP.put("税额", "taxAmount");
        FIELD_MAPPING_MAP.put("物权", "realm");
        FIELD_MAPPING_MAP.put("销售成本", "cost");
        FIELD_MAPPING_MAP.put("rowNum", "rowNumber");
    }
    /**初始化origin_inventory字段映射*/
    public void initOriginInventory(){
        ORIGIN_INVENTORY_MAP.put("库存表主键", "inventoryId");
        ORIGIN_INVENTORY_MAP.put("日期","inventoryDate");
        ORIGIN_INVENTORY_MAP.put("经销商代码", "institutionCode");
        ORIGIN_INVENTORY_MAP.put("经销商名称", "institutionName");
        ORIGIN_INVENTORY_MAP.put("产品代码", "productCode");
        ORIGIN_INVENTORY_MAP.put("产品名称", "productName");
        ORIGIN_INVENTORY_MAP.put("通用名", "generalName");
        ORIGIN_INVENTORY_MAP.put("产品规格", "productSpec");
        ORIGIN_INVENTORY_MAP.put("产品批号", "productBatchCode");
        ORIGIN_INVENTORY_MAP.put("生产日期", "productDate");
        ORIGIN_INVENTORY_MAP.put("有效期", "validDate");
        ORIGIN_INVENTORY_MAP.put("产品型号", "productModel");
        ORIGIN_INVENTORY_MAP.put("产品线", "productLine");
        ORIGIN_INVENTORY_MAP.put("数量", "quantity");
        ORIGIN_INVENTORY_MAP.put("可销数量", "saleableQuantity");
        ORIGIN_INVENTORY_MAP.put("不可销数量", "unsaleableQuantity");
        ORIGIN_INVENTORY_MAP.put("单位", "productUnit");
        ORIGIN_INVENTORY_MAP.put("单价", "price");
        ORIGIN_INVENTORY_MAP.put("金额", "amount");
        ORIGIN_INVENTORY_MAP.put("入库日期", "warehouseDate");
        ORIGIN_INVENTORY_MAP.put("订单日期", "orderDate");
        ORIGIN_INVENTORY_MAP.put("生产厂家", "producer");
        ORIGIN_INVENTORY_MAP.put("库存状态", "inventoryStatus");
        ORIGIN_INVENTORY_MAP.put("子公司名称", "companyName");
        ORIGIN_INVENTORY_MAP.put("仓库", "warehouse");
        ORIGIN_INVENTORY_MAP.put("供应商名称", "supplierName");
        ORIGIN_INVENTORY_MAP.put("物权", "realm");
        ORIGIN_INVENTORY_MAP.put("rowNum", "rowNumber");
    }
    /**初始化采购字段*/
    public void initOriginPurchase(){
        ORIGIN_PURCHASE_MAP.put("采购表主键", "purchaseId");
        ORIGIN_PURCHASE_MAP.put("日期", "purchaseDate");
        ORIGIN_PURCHASE_MAP.put("订单日期", "orderDate");
        ORIGIN_PURCHASE_MAP.put("经销商代码", "institutionCode");
        ORIGIN_PURCHASE_MAP.put("经销商名称", "institutionName");
        ORIGIN_PURCHASE_MAP.put("供应商代码", "vendorCode");
        ORIGIN_PURCHASE_MAP.put("供应商名称", "vendorName");
        ORIGIN_PURCHASE_MAP.put("产品代码", "productCode");
        ORIGIN_PURCHASE_MAP.put("产品名称", "productName");
        ORIGIN_PURCHASE_MAP.put("通用名", "generalName");
        ORIGIN_PURCHASE_MAP.put("产品规格", "productSpec");
        ORIGIN_PURCHASE_MAP.put("产品批号", "productBatchCode");
        ORIGIN_PURCHASE_MAP.put("生产日期", "productDate");
        ORIGIN_PURCHASE_MAP.put("有效期", "validDate");
        ORIGIN_PURCHASE_MAP.put("产品型号", "productModel");
        ORIGIN_PURCHASE_MAP.put("产品线", "productLine");
        ORIGIN_PURCHASE_MAP.put("数量", "quantity");
        ORIGIN_PURCHASE_MAP.put("单位", "productUnit");
        ORIGIN_PURCHASE_MAP.put("单价", "price");
        ORIGIN_PURCHASE_MAP.put("金额", "amount");
        ORIGIN_PURCHASE_MAP.put("生产厂家", "producer");
        ORIGIN_PURCHASE_MAP.put("采购行为", "behavior");
        ORIGIN_PURCHASE_MAP.put("采购单号", "purchaseOrderNum");
        ORIGIN_PURCHASE_MAP.put("进货单号", "logisticsOrderNum");
        ORIGIN_PURCHASE_MAP.put("原厂发货清单", "deliveryList");
        ORIGIN_PURCHASE_MAP.put("子公司名称", "companyName");
        ORIGIN_PURCHASE_MAP.put("仓库", "warehouse");
        ORIGIN_PURCHASE_MAP.put("物权", "realm");
        ORIGIN_PURCHASE_MAP.put("采购备注", "purchaseRemark");
        ORIGIN_PURCHASE_MAP.put("rowNum", "rowNumber");
    }
    /**初始化月销售，字段，文件映射*/
    public void initSaleTitleCol(){
        saleMonthcolumnMap.put("sale_id","销售表主键");
        saleMonthcolumnMap.put("customer_addr","客户地址");
        saleMonthcolumnMap.put("tax_amount","税额");
        saleMonthcolumnMap.put("remark","销售备注");
        saleMonthcolumnMap.put("row_num","所在行数");
        saleMonthcolumnMap.put("product_code","产品代码");
        saleMonthcolumnMap.put("institution_name","经销商名称");
        saleMonthcolumnMap.put("product_batch","产品批号");
        saleMonthcolumnMap.put("product_date","生产日期");
        saleMonthcolumnMap.put("invoice_date","开票日期");
        saleMonthcolumnMap.put("valid_date","有效期");
        saleMonthcolumnMap.put("sale_date","日期");
        saleMonthcolumnMap.put("despatch_order","发运单");
        saleMonthcolumnMap.put("price","单价");
        saleMonthcolumnMap.put("behavior","销售行为");
        saleMonthcolumnMap.put("department","科室");
        saleMonthcolumnMap.put("customer_code","客户代码");
        saleMonthcolumnMap.put("product_model","产品型号");
        saleMonthcolumnMap.put("amount","金额");
        saleMonthcolumnMap.put("quantity","数量");
        saleMonthcolumnMap.put("cost","销售成本");
        //saleMonthcolumnMap.put("create_time","");
        //saleMonthcolumnMap.put("file_name","文件名");
        saleMonthcolumnMap.put("vendor_name","供应商名称");
        saleMonthcolumnMap.put("warehouse","仓库");
        saleMonthcolumnMap.put("product_name","产品名称");
        saleMonthcolumnMap.put("product_unit","单位");
        saleMonthcolumnMap.put("institution_code","经销商代码");
        saleMonthcolumnMap.put("order_date","订单日期");
        saleMonthcolumnMap.put("product_line","产品线");
        saleMonthcolumnMap.put("product_spec","产品规格");
        saleMonthcolumnMap.put("company_name","子公司名称");
        saleMonthcolumnMap.put("sale_order","销售单号");
        saleMonthcolumnMap.put("producer","生产厂家");
        saleMonthcolumnMap.put("realm","物权");
        saleMonthcolumnMap.put("customer_name","客户名称");
        saleMonthcolumnMap.put("general_name","通用名");
        saleMonthcolumnMap.put("failCause","错误描述");
    }
    /**初始化月销售导出字段*/
    public void initSaleColumn(){
        saleMonthtitleKeyList.add("sale_id");
        saleMonthtitleKeyList.add("customer_addr");
        saleMonthtitleKeyList.add("tax_amount");
        saleMonthtitleKeyList.add("remark");
        saleMonthtitleKeyList.add("row_num");
        saleMonthtitleKeyList.add("product_code");
        saleMonthtitleKeyList.add("institution_name");
        saleMonthtitleKeyList.add("product_batch");
        saleMonthtitleKeyList.add("product_date");
        saleMonthtitleKeyList.add("invoice_date");
        saleMonthtitleKeyList.add("valid_date");
        saleMonthtitleKeyList.add("sale_date");
        saleMonthtitleKeyList.add("despatch_order");
        saleMonthtitleKeyList.add("price");
        saleMonthtitleKeyList.add("behavior");
        saleMonthtitleKeyList.add("department");
        saleMonthtitleKeyList.add("customer_code");
        saleMonthtitleKeyList.add("product_model");
        saleMonthtitleKeyList.add("amount");
        saleMonthtitleKeyList.add("quantity");
        saleMonthtitleKeyList.add("cost");
        saleMonthtitleKeyList.add("vendor_name");
        saleMonthtitleKeyList.add("warehouse");
        saleMonthtitleKeyList.add("product_name");
        saleMonthtitleKeyList.add("product_unit");
        saleMonthtitleKeyList.add("institution_code");
        saleMonthtitleKeyList.add("order_date");
        saleMonthtitleKeyList.add("product_line");
        saleMonthtitleKeyList.add("product_spec");
        saleMonthtitleKeyList.add("company_name");
        saleMonthtitleKeyList.add("sale_order");
        saleMonthtitleKeyList.add("producer");
        saleMonthtitleKeyList.add("realm");
        saleMonthtitleKeyList.add("customer_name");
        saleMonthtitleKeyList.add("general_name");
        saleMonthtitleKeyList.add("failCause");
    }
    /**初始化月采购映射字段*/
    public void initMonthPurse(){
        purseMonthcolumnMap.put("purchaseId","采购表主键");
        purseMonthcolumnMap.put("purchaseDate","日期");
        purseMonthcolumnMap.put("orderDate","订单日期");
        purseMonthcolumnMap.put("institutionCode","经销商代码");
        purseMonthcolumnMap.put("institutionName","经销商名称");
        purseMonthcolumnMap.put("vendorCode","供应商代码");
        purseMonthcolumnMap.put("vendorName","供应商名称");
        purseMonthcolumnMap.put("productCode","产品代码");
        purseMonthcolumnMap.put("productName","产品名称");
        purseMonthcolumnMap.put("generalName","通用名");
        purseMonthcolumnMap.put("productSpec","产品规格");
        purseMonthcolumnMap.put("productBatchCode","产品批号");
        purseMonthcolumnMap.put("productDate","生产日期");
        purseMonthcolumnMap.put("validDate","有效期");
        purseMonthcolumnMap.put("productModel","产品型号");
        purseMonthcolumnMap.put("productLine","产品线");
        purseMonthcolumnMap.put("quantity","数量");
        purseMonthcolumnMap.put("productUnit","单位");
        purseMonthcolumnMap.put("price","单价");
        purseMonthcolumnMap.put("amount","金额");
        purseMonthcolumnMap.put("producer","生产厂家");
        purseMonthcolumnMap.put("behavior","采购行为");
        purseMonthcolumnMap.put("purchaseOrderNum","采购单号");
        purseMonthcolumnMap.put("logisticsOrderNum","进货单号");
        purseMonthcolumnMap.put("deliveryList","原厂发货清单");
        purseMonthcolumnMap.put("companyName","子公司名称");
        purseMonthcolumnMap.put("warehouse","仓库");
        purseMonthcolumnMap.put("realm","物权");
        purseMonthcolumnMap.put("purchaseRemark","采购备注");
        purseMonthcolumnMap.put("rowNum","所在行数");
        purseMonthcolumnMap.put("failCause","错误描述");
    }
    /**初始化月采购导出列集合*/
    public void init(){
        purMonthtitleKeyList.add("purchaseId");
        purMonthtitleKeyList.add("purchaseDate");
        purMonthtitleKeyList.add("orderDate");
        purMonthtitleKeyList.add("institutionCode");
        purMonthtitleKeyList.add("institutionName");
        purMonthtitleKeyList.add("vendorCode");
        purMonthtitleKeyList.add("vendorName");
        purMonthtitleKeyList.add("productCode");
        purMonthtitleKeyList.add("productName");
        purMonthtitleKeyList.add("generalName");
        purMonthtitleKeyList.add("productSpec");
        purMonthtitleKeyList.add("productBatchCode");
        purMonthtitleKeyList.add("productDate");
        purMonthtitleKeyList.add("validDate");
        purMonthtitleKeyList.add("productModel");
        purMonthtitleKeyList.add("productLine");
        purMonthtitleKeyList.add("quantity");
        purMonthtitleKeyList.add("productUnit");
        purMonthtitleKeyList.add("price");
        purMonthtitleKeyList.add("amount");
        purMonthtitleKeyList.add("producer");
        purMonthtitleKeyList.add("behavior");
        purMonthtitleKeyList.add("purchaseOrderNum");
        purMonthtitleKeyList.add("logisticsOrderNum");
        purMonthtitleKeyList.add("deliveryList");
        purMonthtitleKeyList.add("companyName");
        purMonthtitleKeyList.add("warehouse");
        purMonthtitleKeyList.add("realm");
        purMonthtitleKeyList.add("purchaseRemark");
        purMonthtitleKeyList.add("rowNum");
        purMonthtitleKeyList.add("failCause");
    }
    /**初始化月库存映射字段*/

    /**初始化月库存导出列集合*/

    /**初始化月发货映射字段*/

    /**初始化月发货导出列集合*/

    /**初始化日采购映射字段*/

    /**初始化日采购导出列集合*/

    /**初始化日销售映射字段*/

    /**初始化日销售导出列集合*/

    /**初始化日库存映射字段*/

    /**初始化日库存导出列集合*/

    /**初始化日发货映射字段*/

    /**初始化日发货导出列集合*/

}

