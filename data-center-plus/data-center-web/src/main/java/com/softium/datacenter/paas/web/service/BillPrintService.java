package com.softium.datacenter.paas.web.service;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.BillPrintDTO;
import com.softium.datacenter.paas.api.dto.BillPrintExportListDTO;
import com.softium.datacenter.paas.api.dto.query.BillPrintQuery;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/***
 * 打单
 * @author net
 * @since 2020-11-16 19:49:38
 */
public interface BillPrintService {
    /**
     * 保存 更新打单名单
     * */
    Boolean saveOrUpdate(BillPrintDTO billPrintDTO);

    /**
     * 查询打单名单列表
     * */
    PageInfo<List<BillPrintDTO>> getBillPrintList(BillPrintQuery billPrintQuery);

    /**
     * 文件上传校验
     * @param inputStream
     * @param name
     * @return
     */
    CompletableFuture<Map> upload(InputStream inputStream, String name);

    /**
     * 文件提交
     * @param token
     * @param fileName
     * @return
     */
    Boolean commitExcel(String token, String fileName) throws IOException;

    List<BillPrintExportListDTO> exportList(BillPrintQuery billPrintQuery);

    BillPrintDTO getId(String id);

    Boolean copyBillPrint(String periodId);
}
