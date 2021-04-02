package com.softium.datacenter.paas.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.BillPrintDTO;
import com.softium.datacenter.paas.api.dto.BillPrintExportListDTO;
import com.softium.datacenter.paas.api.dto.PeriodDTO;
import com.softium.datacenter.paas.web.dto.BillPrintExportDTO;
import com.softium.datacenter.paas.api.dto.query.BillPrintQuery;
import com.softium.datacenter.paas.web.dto.excel.BillPrintImportDTO;
import com.softium.datacenter.paas.web.dto.query.ExcelModelQuery;
import com.softium.datacenter.paas.web.service.BillPrintService;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.datacenter.paas.web.utils.easy.DownLoadFlow;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
@RequestMapping("billPrint")
public class BillPrintController extends BaseController{
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private BillPrintService billPrintService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    ObjectMapper objectMapper;
    /**
     * 打单名单搜索
     * @return
     */
    @PostMapping("/search")
    public ActionResult search(@RequestBody BillPrintQuery billPrintQuery) {
        if(StringUtils.isEmpty(billPrintQuery.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            billPrintQuery.setPeriodId(periodDTO.getId());
        }
        PageInfo<List<BillPrintDTO>> billPrintList = billPrintService.getBillPrintList(billPrintQuery);
        return new ActionResult<>(billPrintList);
    }

    /**
     * 打单名单查看
     * @return
     */
    @GetMapping("/detail")
    public ActionResult detail(@RequestParam String id) {
        BillPrintDTO billPrintDTO = billPrintService.getId(id);
        return new ActionResult<>(billPrintDTO);
    }

    /**
     * 打单新增
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public Boolean saveOrUpdate(@RequestBody BillPrintDTO billPrintDTO) {
        return billPrintService.saveOrUpdate(billPrintDTO);
    }

    /**
     *模板下载
     * @return
     */
    @PostMapping("template/download")
    public CompletableFuture<ResponseEntity<byte[]>> export() {
        List<BillPrintExportDTO> list = new ArrayList<>();
        String filename = "BillPrint-template" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, BillPrintExportDTO.class, filename, "sheet"));
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
            throw new BusinessException(new ErrorInfo("ERROR","请选择上传文件"));
        }
        InputStream inputStream = fileName.getInputStream();
        return billPrintService.upload(inputStream, fileName.getOriginalFilename());
    }

    @PostMapping("import/commit")
    public CompletableFuture commit(@RequestBody ExcelModelQuery excelModelQuery) {
        if (excelModelQuery == null) {
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String token = excelModelQuery.dataConvertToken();
                return billPrintService.commitExcel(token, excelModelQuery.fileName());
            } catch (Exception e) {
                logger.error("error",e);
                throw new RuntimeException();
            }
        });
    }
    /**
     * 下载错误信息
     * @param response
     * @throws IOException
     */
    @GetMapping("download/error")
    public void downloadUploadExcelData(HttpServletResponse response, @RequestParam String errorToken) throws IOException{
        //创建
        DownLoadFlow downLoadFlow = DownLoadFlow.init(response.getOutputStream()).clazz(BillPrintImportDTO.class);
        // 设置token 和token 存储
        downLoadFlow.cacheExcel(errorToken, cacheExcelData, 10000);
        // 设置请求头事件
        downLoadFlow.event(() -> setExcelContentType(response, "BillPrint-templateError"));
        // ObjectMapper
        downLoadFlow.objectMapper(objectMapper);
        //执行
        downLoadFlow.customizeDealWith().downLoad();
        // 刷新流
        downLoadFlow.finish();
    }

    /**
     * 打单名单导出
     * @return
     */
    @PostMapping("/export")
    public CompletableFuture<ResponseEntity<byte[]>> exportInspectSaleList(@RequestBody BillPrintQuery billPrintQuery) {
        if(StringUtils.isEmpty(billPrintQuery.getPeriodId())){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            billPrintQuery.setPeriodId(periodDTO.getId());
        }
        List<BillPrintExportListDTO> billPrintExportDTOS = billPrintService.exportList(billPrintQuery);
        String filename = "BillPrint-data" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(billPrintExportDTOS, BillPrintExportListDTO.class, filename, "sheet"));
    }

    /**
     * 打单名单复制接口
     */
    @GetMapping("/copyBillPrintList")
    public ActionResult<?> copyBillPrint(@RequestParam String periodId){
        return new ActionResult<>(billPrintService.copyBillPrint(periodId));
    }
}
