package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.InstitutionMappingDTO;
import com.softium.datacenter.paas.api.dto.InstitutionPocketDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author huashan.li
 */
public interface InstitutionMappingService {
    List<InstitutionMappingDTO> list(InstitutionMappingQuery institutionMappingQuery);

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

    List<InstitutionPocketDTO> institutionsPocket();
}
