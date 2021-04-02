package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.excel.InterceptBillDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.enums.InspectStatus;
import com.softium.datacenter.paas.web.service.InspectDataService;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


/**
 * @description: 质检数据
 * @author: huashan.li
 * @create: 2020-08-05
 **/
@RestController
@RequestMapping("inspectData")
public class InspectDataController extends BaseController{

    @Autowired
    private InspectDataService inspectDataService;
    @Autowired
    private PeriodService periodService;

    @GetMapping("/sale/load")
    public ActionResult load(String fileId, String projectId){
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        originDataQuery.setProjectId(projectId);
        originDataQuery.setPageSize(10);
        originDataQuery.setCurrent(1);
        return saleList(originDataQuery);
    }

    @GetMapping("/sale/detail")
    public ActionResult<InspectSaleDTO> detail(@RequestParam String id){
        InspectSaleDTO inspectSaleDTO = inspectDataService.getInspectId(id);
        return new ActionResult<>(inspectSaleDTO);
    }

    /**
     * 销售核查数据搜索
     * @return
     */
    @PostMapping("/sale/search")
    public ActionResult search(@RequestBody OriginDataQuery pageModel) {
        if(StringUtils.isEmpty(pageModel.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            pageModel.setPeriodId(periodDTO.getId());
        }
        return saleList(pageModel);
    }

    /**
     * 销售核查数据删除
     * @return
     */
    @PostMapping("/sale/batchDeleteSale")
    public ActionResult batchDeleteSale(@RequestBody Set<String> ids) {
        inspectDataService.batchDeleteSale(ids);
        return new ActionResult<>();
    }

    /**
     * 批量拦截账期
     * @return
     */
    @PostMapping("/sale/batchCancelBlockingPeriod")
    public ActionResult batchCancelBlockingPeriod(@RequestBody Set<String> ids) throws IOException {
        inspectDataService.batchCancelBlocking(ids, InspectStatus.period.toString());
        return new ActionResult<>();
    }

    /**
     * 批量拦截打单
     * @return
     */
    @PostMapping("/sale/batchCancelBlockingBill")
    public ActionResult batchCancelBlockingBill(@RequestBody Set<String> ids) throws IOException {
        inspectDataService.batchCancelBlocking(ids, InspectStatus.bill.toString());
        return new ActionResult<>();
    }

    /**
     * 查询打单，账期统计数据
     * @return
     */
    @GetMapping("/sale/getSaleCount")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status",value = "period,bill",required = true,dataType = "InspectStatus",paramType="query")
    })
    public SaleCountDto getSaleCount(@RequestParam(value = "status",required = true) String status,
                                     @RequestParam(value = "periodId") String periodId,
                                     @RequestParam(value = "businessType") String businessType) {
        if(StringUtils.isEmpty(periodId)){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            periodId=periodDTO.getId();
        }
        return inspectDataService.getSaleCount(status,periodId,businessType);
    }

    /**
     * 销售交付数据搜索
     * @return
     */
    @PostMapping("/formatSale/search")
    public ActionResult searchFormat(@RequestBody OriginDataQuery pageModel) {
        if(StringUtils.isEmpty(pageModel.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            pageModel.setPeriodId(periodDTO.getId());
        }
        pageModel.setRinseStatus("SUCCESS");
        return saleList(pageModel);
    }

    @GetMapping("/sale/getCleaningStatus")
    public ActionResult cleaningLoad(@RequestParam String id){
        List<CleaningStatusDTO> cleaningStatusDTOS = inspectDataService.getCleaningStatus(id);
        return new ActionResult<>(cleaningStatusDTOS);
    }

    /**
     * 销售核查数据导出
     * @returns
     */
    @PostMapping("/sale/export")
    public CompletableFuture<ResponseEntity<byte[]>> exportInspectSaleList(@RequestBody OriginDataQuery pageModel) {
        List<InspectSaleExportDTO> inspectSaleExportDTOS = inspectDataService.exportInspectSaleList(pageModel);
        String filename = "InspectSaleTemplate" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(inspectSaleExportDTOS, InspectSaleExportDTO.class, filename, "sheet"));
    }

    /**
     * 销售交付数据导出
     * @return
     */
    @PostMapping("/formatSale/export")
    public CompletableFuture<ResponseEntity<byte[]>> exportFormatSaleList(@RequestBody OriginDataQuery originDataQuery) {
        originDataQuery.setRinseStatus("SUCCESS");
        List<FormatSaleExportDTO> formatSaleExportDTOS = inspectDataService.exportFormatSaleList(originDataQuery);
        String filename = "FormatSaleTemplate" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(formatSaleExportDTOS, InspectSaleExportDTO.class, filename, "sheet"));
    }

    private ActionResult saleList(OriginDataQuery pageModel){
        List<InspectSaleDTO> list = inspectDataService.saleList(pageModel);
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }

    /**
     * 采购质检后数据
     * @param fileId
     * @param projectId
     * @return
     */
    @GetMapping("/purchase/load")
    public ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseLoad(String fileId, String projectId){
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        originDataQuery.setProjectId(projectId);
        originDataQuery.setPageSize(10);
        originDataQuery.setCurrent(1);
        return purchaseList(originDataQuery);
    }

    @GetMapping("/purchase/detail")
    public ActionResult<PurchaseDataDTO> purchaseDetail(@RequestParam String id){
        PurchaseDataDTO inspectPurchaseDTO = inspectDataService.getPurchaseId(id);
        return new ActionResult<>(inspectPurchaseDTO);
    }

    @PostMapping("/purchase/search")
    public  ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseSearch(@RequestBody OriginDataQuery pageModel) {
        return purchaseList(pageModel);
    }

    private ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseList(OriginDataQuery pageModel){
        List<PurchaseDataDTO> list = inspectDataService.purchaseList(pageModel);
        PageInfo<List<PurchaseDataDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }

    /**
     * 库存质检后数据
     * @param fileId
     * @param projectId
     * @return
     */
    @GetMapping("/inventory/load")
    public ActionResult<PageInfo<List<InventoryDataDTO>>> inventoryLoad(String fileId, String projectId){
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        originDataQuery.setProjectId(projectId);
        originDataQuery.setPageSize(10);
        originDataQuery.setCurrent(1);
        return inventoryList(originDataQuery);
    }

    @GetMapping("/inventory/detail")
    public ActionResult<InventoryDataDTO> inventoryDetail(@RequestParam String id){
        InventoryDataDTO inventoryDataDTO = inspectDataService.getInventoryId(id);
        return new ActionResult<>(inventoryDataDTO);
    }

    @PostMapping("/inventory/search")
    public ActionResult<PageInfo<List<InventoryDataDTO>>> inventorySearch(@RequestBody OriginDataQuery pageModel) {
        return inventoryList(pageModel);
    }

    private ActionResult<PageInfo<List<InventoryDataDTO>>> inventoryList(OriginDataQuery pageModel){
        PageInfo<List<InventoryDataDTO>> pageInfo = new PageInfo(inspectDataService.inventoryList(pageModel));
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }
    /**八种核查数据查看接口*/
    @PostMapping("checkData")
    public ActionResult inspectDataSearch(@RequestBody OriginDataQuery originDataQuery){
        ActionResult result=inspectDataService.queryAllInsData(originDataQuery);
        return result;
    }
    /**八种交付数据查看接口*/
    @PostMapping("activeData")
    public ActionResult activeDataSearch(@RequestBody OriginDataQuery originDataQuery){
        originDataQuery.setRinseStatus("SUCCESS");
        ActionResult activeResult=inspectDataService.queryAllActiceData(originDataQuery);
        return activeResult;
    }
    /**导出经销商不存在数据*/
    @PostMapping("/sale/exportUnExistInstitution")
    public CompletableFuture<ResponseEntity<byte[]>>  exportUnExistInstitution(@RequestBody OriginDataQuery originDataQuery){
        if(originDataQuery.getPeriodId()==null|| StringUtils.isEmpty(originDataQuery.getPeriodId())){
            throw new BusinessException(new ErrorInfo("periodId is null","账期必选"));
        }
        List<InterceptBillDTO> billDTOS=inspectDataService.queryExportBillService(originDataQuery.getPeriodId());
        String fileName="BillInstitution"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(billDTOS,InterceptBillDTO.class,fileName,"sheet"));
    }
    private ActionResult billDateSearchList(OriginDataQuery pageModel){
        List<InspectSaleDTO> list = inspectDataService.billDateSearchService(pageModel);
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }
    /**
     * 销售核查数据搜索
     * @return
     */
    @PostMapping("/sale/pathSearch")
    public ActionResult pathSearch(@RequestBody OriginDataQuery pageModel) {
        if(StringUtils.isEmpty(pageModel.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            pageModel.setPeriodId(periodDTO.getId());
        }
//        else{
//            //todo 由于现有很多地方有的用账期名，有的账期id,但是此接口传的是账期id,所以为了适应只能根据账期id获取账期名称
//           String perioName= periodService.queryPeriodNameService(pageModel.getPeriodId(), SystemContext.getTenantId());
//           pageModel.setPageSize(perioName);
//        }
        return billDateSearchList(pageModel);
    }
}
