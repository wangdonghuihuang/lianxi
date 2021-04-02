package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.InventoryDataDTO;
import com.softium.datacenter.paas.api.dto.PeriodDTO;
import com.softium.datacenter.paas.api.dto.PurchaseDataDTO;
import com.softium.datacenter.paas.api.dto.SaleDataDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.web.service.OriginDataService;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @description: 源数据
 * @author: huashan.li
 * @create: 2020-08-05
 **/
@RestController
@RequestMapping("originData")
public class OriginDataController{

    @Autowired
    private OriginDataService originDataService;
    @Autowired
    private PeriodService periodService;

    @GetMapping("/sale/load")
    public ActionResult<PageInfo<List<SaleDataDTO>>> load(String fileId, String projectId){
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        originDataQuery.setProjectId(projectId);
        originDataQuery.setPageSize(10);
        originDataQuery.setCurrent(1);
        return saleList(originDataQuery);
    }

    @GetMapping("/sale/detail")
    public ActionResult<SaleDataDTO> detail(@RequestParam String id){
        SaleDataDTO originSaleDTO = originDataService.getId(id);
        return new ActionResult<>(originSaleDTO);
    }

    /**
     * 销售源数据搜索
     * @return
     */
    @PostMapping("/sale/search")
    public ActionResult<PageInfo<List<SaleDataDTO>>> search(@RequestBody OriginDataQuery pageModel) {
        if(StringUtils.isEmpty(pageModel.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            pageModel.setPeriodId(periodDTO.getId());
        }
        return saleList(pageModel);
    }

    /**
     * 销售数据批量删除
     * @param saleDataDTOS
     * @return
     */
    @PostMapping("/sale/delete")
    public ActionResult deleteSale(@RequestBody List<SaleDataDTO> saleDataDTOS){
        int num = originDataService.deleteSale(saleDataDTOS);
        return new ActionResult("已成功删除"+num+"条数据");
    }

    private ActionResult<PageInfo<List<SaleDataDTO>>> saleList(OriginDataQuery pageModel){
        List<SaleDataDTO> list = originDataService.saleList(pageModel);
        PageInfo<List<SaleDataDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }

    /**
     * 采购源数据
     * @param fileId
     * @param projectId
     * @return
     */
    @GetMapping("/purchase/load")
    public ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseLoad(String fileId,String projectId){
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        originDataQuery.setProjectId(projectId);
        originDataQuery.setPageSize(10);
        originDataQuery.setCurrent(1);
        return purchaseList(originDataQuery);
    }

    @GetMapping("/purchase/detail")
    public ActionResult<PurchaseDataDTO> purchaseDetail(@RequestParam String id){
        PurchaseDataDTO originPurchaseDTO = originDataService.getPurchaseId(id);
        return new ActionResult<>(originPurchaseDTO);
    }

    /**
     * 采购源数据搜索
     * @return
     */
    @PostMapping("/purchase/search")
    public ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseSearch(@RequestBody OriginDataQuery pageModel) {
        return purchaseList(pageModel);
    }

    private ActionResult<PageInfo<List<PurchaseDataDTO>>> purchaseList(OriginDataQuery pageModel){
        List<PurchaseDataDTO> list = originDataService.purchaseList(pageModel);
        PageInfo<List<PurchaseDataDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }

    /**
     * 库存源数据
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
        InventoryDataDTO inventoryDataDTO = originDataService.getInventoryId(id);
        return new ActionResult<>(inventoryDataDTO);
    }

    /**
     * 库存源数据搜索
     * @return
     */
    @PostMapping("/inventory/search")
    public ActionResult<PageInfo<List<InventoryDataDTO>>> inventorySearch(@RequestBody OriginDataQuery pageModel) {
        return inventoryList(pageModel);
    }

    private ActionResult<PageInfo<List<InventoryDataDTO>>> inventoryList(OriginDataQuery pageModel){
        List<InventoryDataDTO> list = originDataService.inventoryList(pageModel);
        PageInfo<List<InventoryDataDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(pageModel.getPageSize());
        pageInfo.setPageNum(pageModel.getCurrent());
        return  new ActionResult<>(pageInfo);
    }
    /**数据查看接口*/
    @PostMapping("/data/search")
    public ActionResult fileDataLoad(@RequestBody OriginDataQuery pageModel){
        /*OriginDataQuery queryDTO = new OriginDataQuery();
        queryDTO.setFileId(fileId);
        queryDTO.setBusinessType(businessType);*/
        return fileDataList(pageModel);
    }
    private ActionResult fileDataList(OriginDataQuery pageModel){
        ActionResult list = originDataService.queryAllData(pageModel);
        return list;
    }
}
