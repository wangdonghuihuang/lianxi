package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.ProductMappingDTO;
import com.softium.datacenter.paas.web.dto.ProductMappingExportDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.web.service.ProductMappingService;
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
@RequestMapping("productMapping")
public class ProductMappingController extends BaseController{
    @Autowired
    private ProductMappingService productMappingService;
    @PostMapping("/search")
    public ActionResult search(@RequestBody InstitutionMappingQuery institutionMappingQuery){
        List<ProductMappingDTO> productMappings = productMappingService.list(institutionMappingQuery);
        PageInfo pageInfo= new PageInfo(productMappings);
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
        List<ProductMappingExportDTO> list = new ArrayList<>();
        String filename = "productMapping" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, ProductMappingExportDTO.class, filename, "sheet"));
    }
}
