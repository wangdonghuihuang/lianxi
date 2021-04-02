package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.FormatSaleExportDTO;
import com.softium.datacenter.paas.api.dto.InspectSaleDTO;
import com.softium.datacenter.paas.api.dto.InspectSaleExportDTO;
import com.softium.datacenter.paas.api.dto.excel.InterceptBillDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchDistribDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchMechanismDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchProductDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.entity.InspectInventory;
import com.softium.datacenter.paas.api.entity.InspectPurchase;
import com.softium.datacenter.paas.api.entity.InspectSale;
import com.softium.framework.common.dto.ActionResult;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author huashan.li
 */

public interface InspectDataService {
    List<InspectSaleDTO> saleList(OriginDataQuery pageModel);

    InspectSaleDTO getInspectId(String id);

    void inspectSalebatchInsert(String tableName,List<InspectSale> inspectSales);

    List<PurchaseDataDTO> purchaseList(OriginDataQuery pageModel);

    PurchaseDataDTO getPurchaseId(String id);

    List<InventoryDataDTO> inventoryList(OriginDataQuery pageModel);

    InventoryDataDTO getInventoryId(String id);

    void batchInsertPurchase(String tableName,List<InspectPurchase> inspectPurchases);

    void batchInsertInventory(String tableName,List<InspectInventory> inspectInventories);

    /***
     * 批量删除销售数据
     * @param ids
     * @author net
     * @since 2020-11-11 19:12:15
     */
    void batchDeleteSale(Set<String> ids);

    /***
     * 批量取消拦截
     * @param ids
     * @author net
     * @since 2020-11-11 19:57:42
     */
    void batchCancelBlocking(Set<String> ids, String status) throws IOException;

    /***
     *
     * @param
     * @author net
     * @since 2020-11-12 16:07:18
     */
    SaleCountDto getSaleCount(String status,String periodId,String businessType);

    /**
     * 清洗状态详情查看
     * @param id
     * @return
     */
    List<CleaningStatusDTO> getCleaningStatus(String id);
    ActionResult queryAllInsData(OriginDataQuery originDataQuery);
    ActionResult queryAllActiceData(OriginDataQuery originDataQuery);

    /**
     * 导出核查销售数据
     * @param pageModel
     * @return
     */
    List<InspectSaleExportDTO> exportInspectSaleList(OriginDataQuery pageModel);

    /**
     * 导出交互销售数据
     * @param originDataQuery
     * @return
     */
    List<FormatSaleExportDTO> exportFormatSaleList(OriginDataQuery originDataQuery);

    /***
     *经销商匹配列表
     */
    List<InspectSaleDTO> getFromInstitutionMatch(OriginDataQuery originDataQuery);

    /***
     *机构匹配列表
     */
    List<InspectSaleDTO> getToInstitutionMatch(OriginDataQuery originDataQuery);

    /***
     *产品匹配列表
     */
    List<InspectSaleDTO> getProductMatch(OriginDataQuery originDataQuery);

    /***
     *单位匹配列表
     */
    List<InspectSaleDTO> getProductUnitMatch(OriginDataQuery originDataQuery);
    /**经销商匹配导出*/
    List<MatchDistribDTO> queryExportDistribService(String periodId);
    /**机构匹配导出*/
    List<MatchMechanismDTO> queryExportMechanisService(String periodId);
    /**产品匹配导出*/
    List<MatchProductDTO>  queryExportProductService(String periodId);
    /**打单规则拦截经销商不存在导出*/
    List<InterceptBillDTO> queryExportBillService(String periodId);
    /**日期拦截，打单规则拦截查询业务层*/
    List<InspectSaleDTO> billDateSearchService(OriginDataQuery pageModel);
}
