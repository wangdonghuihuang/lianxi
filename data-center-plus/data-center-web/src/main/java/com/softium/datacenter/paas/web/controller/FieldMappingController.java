package com.softium.datacenter.paas.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.web.dto.excel.FieldMappingImportDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.web.dto.query.ExcelModelQuery;
import com.softium.datacenter.paas.web.dto.query.ProjectInsExcelQuery;
import com.softium.datacenter.paas.web.service.FieldMappingService;
import com.softium.datacenter.paas.web.utils.easy.DownLoadFlow;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @description: 列映射
 * @author: york
 * @create: 2020-08-04 11:15
 **/
@Slf4j
@RestController
@RequestMapping("fieldMapping")
public class FieldMappingController extends BaseController {

    @Autowired
    private FieldMappingService fieldMappingService;
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    ObjectMapper objectMapper;

    /***
     * @description 列映射默认配置
     * @param projectId
     * @return
     */
    //@SpecialPocket({BusinessTypePocket.class})
    @GetMapping("load")
    public ActionResult load(@RequestParam(value = "projectId") String projectId) {
        CommonQuery commonQuery = new CommonQuery();
        commonQuery.setProjectId(projectId);
        commonQuery.setCurrent(1);
        commonQuery.setPageSize(10);
        return fieldDefaultList(commonQuery);
    }

    @PostMapping("add")
    public ActionResult defaultAdd(@RequestBody FieldMappingDTO fieldMappingDTO){
        fieldMappingService.saveBusinessTypeMapping(fieldMappingDTO);
        /*if(StringUtil.isEmpty(fieldMappingDTO.getProjectInstitutionCode())){
            return this.load(fieldMappingDTO.getProjectId());
        }else {
            CommonQuery commonQuery = new CommonQuery();
            commonQuery.setCurrent(1);
            commonQuery.setPageSize(10);
            commonQuery.setProjectId(fieldMappingDTO.getProjectId());
            return this.listSpecial(commonQuery);
        }*/
        return new ActionResult(true,"success");
    }

    @PostMapping("template/download")
    public CompletableFuture<ResponseEntity<byte[]>> export(@RequestBody FieldMappingDTO fieldMappingDTO) {
        List<SalsesColumnMappingExportDTO> list = fieldMappingService.getTemplate(fieldMappingDTO.getBusinessType());
        String prfix = "";
        switch (fieldMappingDTO.getBusinessType()){
            case "SD":
                prfix = "SalesColumnMappingTemplate";
                break;
            case "PD":
                prfix = "PurchesColumnMappingTemplate";
                break;
            case "ID":
                prfix = "InventoryColumnMappingTemplate";
                break;
            default:
                log.info("不支持的模板类型:{}",fieldMappingDTO.getBusinessType());
                break;
        }
        String filename = prfix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, SalsesColumnMappingExportDTO.class, filename, "sheet"));
    }

    @PostMapping("import")
    public CompletableFuture<Map> upload(MultipartFile fileName, String businessType) throws IOException {
        if (fileName == null || fileName.getInputStream() == null) {
            throw new BusinessException(new ErrorInfo("ERROR", "请选择上传文件"));
        }
        InputStream inputStream = fileName.getInputStream();
        return fieldMappingService.upload(inputStream,fileName.getOriginalFilename(),businessType);
    }

    /**
     * 下载错误信息
     * @param response
     * @param excelModelQuery
     * @throws IOException
     */
    @PostMapping("download/error")
    public void downloadUploadExcelData(HttpServletResponse response, @RequestBody ExcelModelQuery excelModelQuery) throws IOException{
        //创建
        DownLoadFlow downLoadFlow = DownLoadFlow.init(response.getOutputStream()).clazz(FieldMappingImportDTO.class);
        // 设置token 和token 存储
        downLoadFlow.cacheExcel(excelModelQuery.errorToken(), cacheExcelData, 10000);
        // 设置请求头事件
        downLoadFlow.event(() -> setExcelContentType(response, "SalesColumnMappingTemplateError"));
        // ObjectMapper
        downLoadFlow.objectMapper(objectMapper);
        //执行
        downLoadFlow.customizeDealWith().downLoad();
        // 刷新流
        downLoadFlow.finish();
    }

    @PostMapping("import/commit")
    public CompletableFuture commit(@RequestBody ProjectInsExcelQuery excelModelQuery) {
        if (excelModelQuery == null) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String token = excelModelQuery.dataConvertToken();
                return fieldMappingService.commitExcel(token, excelModelQuery);
            } catch (Exception e) {
                log.error("error",e);
                throw new RuntimeException();
            }
        });

    }

    @PostMapping("saveFieldsMapping")
    public ActionResult saveFieldsMapping(@RequestBody ColumnMappingEditDTO columnMappingEditDTO){
        fieldMappingService.saveFields(columnMappingEditDTO);
        return new ActionResult(true,"success");
    }

    private ActionResult fieldDefaultList(CommonQuery commonQuery) {
        List<FieldMappingDTO> list = fieldMappingService.loadFieldMapping(commonQuery);
        /*PageInfo<List<ProjectDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(commonQuery.getPageSize());
        pageInfo.setPageNum(commonQuery.getCurrent());*/
        return new ActionResult<>(list);
        //resultModel.setPocket(Map.of("businessType",fieldMappingService.getBusiType(projectId)));
    }

    //@SpecialPocket({BusinessTypePocket.class})
    @PostMapping("loadSpecial")
    public ActionResult loadSpecial(@RequestBody CommonQuery commonQuery) {
        return listSpecial(commonQuery);
    }

    @PostMapping("searchSpecial")
    public ActionResult searchSpecial(@RequestBody CommonQuery commonQuery) {
        return listSpecial(commonQuery);
    }

    @PostMapping("viewFieldList")
    public ActionResult viewFieldList(@RequestBody CommonQuery commonQuery){
        return viewFields(commonQuery);
    }

    @PostMapping("deleteDefaultFieldMapping")
    public ActionResult deleteFieldMapping(@RequestBody CommonQuery commonQuery){
        fieldMappingService.deleteMapping(commonQuery);
        return load(commonQuery.getProjectId());
    }

    @PostMapping("deleteSpecialFieldMapping")
    public ActionResult deleteSpecialFieldMapping(@RequestBody CommonQuery commonQuery){
        fieldMappingService.deleteMapping(commonQuery);
        /*commonQuery.setCurrent(1);
        commonQuery.setPageSize(10);
        return listSpecial(commonQuery);*/
        return new ActionResult(true,"success");
    }

    private ActionResult listSpecial(CommonQuery commonQuery){
        List<FieldMappingDTO> list = fieldMappingService.load(commonQuery);
        PageInfo<List<ProjectDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(commonQuery.getPageSize());
        pageInfo.setPageNum(commonQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    private ActionResult viewFields(CommonQuery commonQuery){
        List<FieldDTO> list = fieldMappingService.viewFields(commonQuery);
        List<ColumnMappingDTO> listTemplate = fieldMappingService.getTemplateColumn(commonQuery.getBusinessType());
        return new ActionResult<>(Map.of("dataList",list,"tempList",listTemplate));
    }

    @PostMapping("/getFieldMappingList")
    public ActionResult search(@RequestBody CommonQuery commonQuery){
        PageInfo pageInfo = new PageInfo(fieldMappingService.getFieldMapping(commonQuery));
        return new ActionResult(pageInfo);
    }

    @PostMapping("/updateFieldMappingList")
    public ActionResult update(@RequestBody List<FieldMappingDTO> fieldMappingDTOList){
        fieldMappingService.updateFieldMappingList(fieldMappingDTOList);
        return new ActionResult(true,"success");
    }
}
