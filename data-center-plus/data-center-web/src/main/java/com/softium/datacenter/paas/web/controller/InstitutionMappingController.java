package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.InstitutionMappingDTO;
import com.softium.datacenter.paas.web.dto.InstitutionMappingExportDTO;
import com.softium.datacenter.paas.web.dto.query.ExcelModelQuery;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.web.service.InstitutionMappingService;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author huashan.li
 */
@RestController
@RequestMapping("institutionMapping")
public class InstitutionMappingController extends BaseController{
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private InstitutionMappingService institutionMappingService;
    @PostMapping("/search")
    public ActionResult search(@RequestBody InstitutionMappingQuery institutionMappingQuery){
        List<InstitutionMappingDTO> institutionMappingDTOList = institutionMappingService.list(institutionMappingQuery);
        PageInfo pageInfo= new PageInfo(institutionMappingDTOList);
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
        List<InstitutionMappingExportDTO> list = new ArrayList<>();
        String filename = "institutionMapping" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, InstitutionMappingExportDTO.class, filename, "sheet"));
    }

    /**
     * 导入
     * @param fileName
     * @return
     * @throws IOException
     */
    @PostMapping("import")
    public CompletableFuture<Map> upload(MultipartFile fileName) throws IOException {
        if (fileName == null || fileName.getInputStream() == null) {
            throw new BusinessException(new ErrorInfo("","请选择上传文件"));
        }
        InputStream inputStream = fileName.getInputStream();
        return institutionMappingService.upload(inputStream, fileName.getOriginalFilename());
    }

    @PostMapping("import/commit")
    public CompletableFuture commit(@RequestBody ExcelModelQuery excelModelQuery) {
        if (excelModelQuery == null) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String token = excelModelQuery.dataConvertToken();
                return institutionMappingService.commitExcel(token, excelModelQuery.fileName());
            } catch (Exception e) {
                logger.error("error",e);
                throw new RuntimeException();
            }
        });
    }

    /**
     * 经销商主数据pocket
     * @return
     */
    @PostMapping("/institutionsPocket")
    public ActionResult<?> institutionsPocket(){
        return new ActionResult<>(institutionMappingService.institutionsPocket());
    }
}
