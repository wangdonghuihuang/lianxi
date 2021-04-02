package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.web.dto.query.ProjectInsExcelQuery;
import com.softium.datacenter.paas.api.entity.BusinessType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @description: 列映射关系
 * @author: york
 * @create: 2020-08-04 11:14
 **/
@Repository
public interface FieldMappingService {
    List<FieldMappingDTO> loadFieldMapping(CommonQuery commonQuery);

    List<BusinessType> getBusiType(String projectId);

    void saveBusinessTypeMapping(FieldMappingDTO model);

    List<SalsesColumnMappingExportDTO> getTemplate(String businessType);

    CompletableFuture<Map> upload(InputStream inputStream, String originalFilename, String businessType);

    Boolean commitExcel(String token, ProjectInsExcelQuery excelModelQuery) throws IOException;

    /**
     * @description 删除特殊经销商列映射配置
     * @param projectId
     * @param businessType
     * @return
     */
    Boolean batchDeleteFieldMapping(String projectId, String businessType, String projectInstitutionCode);

    /***
     * @description 删除默认列映射配置
     * @param projectId
     * @param businessType
     * @return
     */
    Boolean batchDeleteFieldMapping(String projectId, String businessType);

    Boolean deleteMapping(CommonQuery commonQuery);

    List<FieldMappingDTO> load(CommonQuery commonQuery);

    List<FieldDTO> viewFields(CommonQuery commonQuery);

    List<ColumnMappingDTO> getTemplateColumn(String businessType);

    /**
     * @description 由导入改造->手动编辑添加 (以模板为基准，无重复校验)
     * @param columnMappingEditDTO
     */
    void saveFields(ColumnMappingEditDTO columnMappingEditDTO);

    /**
     * 列映射配置列表（配置中心）
     * @param commonQuery
     * @return
     */
    List<FieldMappingDTO> getFieldMapping(CommonQuery commonQuery);

    /**
     * 列映射配置提交接口（配置中心）
     * @param fieldMappingDTOList
     */
    void updateFieldMappingList(List<FieldMappingDTO> fieldMappingDTOList);

}
