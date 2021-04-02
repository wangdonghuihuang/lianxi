package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.ProductUnitMappingDTO;
import com.softium.datacenter.paas.web.dto.ProductUnitMappingExportDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.web.service.ProductUnitMappingService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author huashan.li
 * 产品匹配关系
 */
@RestController
@RequestMapping("productUnitMapping")
public class ProductUnitMappingController extends BaseController{
    @Autowired
    private ProductUnitMappingService productUnitMappingService;
    @PostMapping("/search")
    public ActionResult search(@RequestBody InstitutionMappingQuery institutionMappingQuery){
        List<ProductUnitMappingDTO> productUnitMappingDTOS = productUnitMappingService.list(institutionMappingQuery);
        PageInfo pageInfo= new PageInfo(productUnitMappingDTOS);
        pageInfo.setPageNum(institutionMappingQuery.getCurrent());
        pageInfo.setSize(institutionMappingQuery.getPageSize());
        return new ActionResult<>(pageInfo);
    }

    /**
     *模板下载
     * @return
     */
    @PostMapping("template/download")
    public CompletableFuture<ResponseEntity<byte[]>> export() {
        List<ProductUnitMappingExportDTO> list = new ArrayList<>();
        String filename = "productUnitMapping" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, ProductUnitMappingExportDTO.class, filename, "sheet"));
    }
}
