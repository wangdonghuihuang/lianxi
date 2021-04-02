package com.softium.datacenter.paas.web.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.mapper.BusinessTypeMappingMapper;
import com.softium.datacenter.paas.api.mapper.FieldMappingMapper;
import com.softium.datacenter.paas.api.mapper.ProjectMapper;
import com.softium.datacenter.paas.api.mapper.TemplateMapper;
import com.softium.datacenter.paas.web.dto.excel.FieldMappingImportDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.web.dto.query.ProjectInsExcelQuery;
import com.softium.datacenter.paas.api.entity.BusinessType;
import com.softium.datacenter.paas.api.entity.BusinessTypeMapping;
import com.softium.datacenter.paas.api.entity.FieldMapping;
import com.softium.datacenter.paas.api.entity.Template;
import com.softium.datacenter.paas.web.service.FieldMappingService;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.CombinationResultModel;
import com.softium.datacenter.paas.web.utils.easy.input.EasyExcelService;
import com.softium.datacenter.paas.web.utils.easy.input.Message;
import com.softium.datacenter.paas.web.utils.easy.input.SimpExcelConsumer;
import com.softium.datacenter.paas.web.utils.easy.validate.MessageUtils;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.mapping.Mapper;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.StringUtil;
import com.softium.framework.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: york
 * @create: 2020-08-04 11:14
 **/
@Slf4j
@Service
public class FieldMappingServiceImpl implements FieldMappingService {
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private BusinessTypeMappingMapper businessTypeMappingMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private Mapper mapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<FieldMappingDTO> loadFieldMapping(CommonQuery commonQuery) {
        //PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        return fieldMappingMapper.getDefaultFieldMapping(commonQuery.getProjectId(),SystemContext.getTenantId());
    }

    @Override
    public List<BusinessType> getBusiType(String projectId) {
        return fieldMappingMapper.getBusiType(projectId);
    }

    @Override
    public void saveBusinessTypeMapping(FieldMappingDTO model) {
        int cnt = businessTypeMappingMapper.selectBusCountByParam(model.getProjectId(),model.getBusinessType(),model.getProjectInstitutionCode(),SystemContext.getTenantId());
        if (cnt >0) throw new BusinessException(new ErrorInfo("ERROR","数据类型已存在"));

        BusinessTypeMapping businessTypeMapping = new BusinessTypeMapping();
        businessTypeMapping.setBusinessType(model.getBusinessType());
        businessTypeMapping.setDdiBusinessType(model.getBusinessType());
        //businessTypeMapping.setProjectInstitutionId(model.getProjectInstitutionId());
        businessTypeMapping.setProjectInstitutionCode(model.getProjectInstitutionCode());
        businessTypeMapping.setProjectInstitutionName(model.getProjectInstitutionName());
        businessTypeMapping.setProjectId(model.getProjectId());
        businessTypeMapping.setFileNameRepeat("NEW");
        businessTypeMapping.setCreateTime(new Date());
        businessTypeMapping.setCreateBy(SystemContext.getUserId());
        businessTypeMapping.setCreateName(SystemContext.getUserName());
        businessTypeMappingMapper.insert(businessTypeMapping);
    }

    @Override
    public List<SalsesColumnMappingExportDTO> getTemplate(String businessType) {
        return templateMapper.getSaleTemplate(businessType);
    }

    @Override
    public List<ColumnMappingDTO> getTemplateColumn(String businessType) {
        return templateMapper.getTemplateByBusinessType(businessType);
    }

    @Override
    public void saveFields(ColumnMappingEditDTO columnMappingEditDTO) {
        List<FieldMapping> insertList = new ArrayList<>();
        Map<String,String> checkMap = new HashMap<>();
        String res = null;
        for (ColumnMappingDTO columnMappingDTO : columnMappingEditDTO.getColumnMappingDTOS()) {
            if(StringUtils.isEmpty(columnMappingDTO.getTitleName())) continue;
            res = checkMap.put(columnMappingDTO.getTitleName(),columnMappingDTO.getTitleName());
            if(!StringUtil.isEmpty(res)) throw new BusinessException(new ErrorInfo("ERROR","文件名称：【"+columnMappingDTO.getTitleName() + "】重复"));
            FieldMapping fieldMapping = new FieldMapping();
            fieldMapping.setBusinessType(columnMappingEditDTO.getBusinessType());
            fieldMapping.setProjectId(columnMappingEditDTO.getProjectId());
            fieldMapping.setCreateTime(new Date());
            fieldMapping.setCreateBy(SystemContext.getUserId());
            fieldMapping.setCreateName(SystemContext.getUserName());
            fieldMapping.setTitleName(columnMappingDTO.getTitleName());
            fieldMapping.setPropertyName(columnMappingDTO.getPropertyName());
            fieldMapping.setProjectInstitutionCode(columnMappingEditDTO.getProjectInstitutionCode());
            fieldMapping.setProjectInstitutionName(columnMappingEditDTO.getProjectInstitutionName());
            fieldMapping.setSort(columnMappingDTO.getSort());
            insertList.add(fieldMapping);
        }
        if(!insertList.isEmpty()){
            batchDeleteFieldMapping(columnMappingEditDTO.getProjectId(),columnMappingEditDTO.getBusinessType(),
                    columnMappingEditDTO.getProjectInstitutionCode());
            fieldMappingMapper.batchInsert(insertList);
        }
    }

    @Override
    public List<FieldMappingDTO> getFieldMapping(CommonQuery commonQuery) {
        List<FieldMappingDTO> fieldMappingDTOS = fieldMappingMapper.getFieldMapping(commonQuery,SystemContext.getTenantId());
        //根据业务类型查询模板字段
        Criteria<Template> criteria = Criteria.from(Template.class);
        criteria.and(Template::getBusinessType, Operator.equal,commonQuery.getBusinessType());
        List<Template> templates = templateMapper.findByCriteria(criteria);
        //判断是否初始化项目 1租户-1项目
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        if(fieldMappingDTOS.isEmpty()){
            List<FieldMapping> fieldMappingList = new ArrayList<>();
            if(templates.isEmpty()){
                throw new BusinessException(new ErrorInfo("ERROR", "此业务类型没有模板字段"));
            }
            templates.forEach(template -> {
                FieldMapping fieldMapping = new FieldMapping();
                fieldMapping.setId(UUIDUtils.getUUID());
                fieldMapping.setProjectId(projectDTOS.get(0).getId());
                fieldMapping.setPropertyName(template.getColumnName());
                fieldMapping.setTitleName(template.getColumnName());
                fieldMapping.setBusinessType(template.getBusinessType());
                fieldMapping.setSort(template.getSort());
                fieldMapping.setCreateName(SystemContext.getUserName());
                fieldMappingList.add(fieldMapping);
            });
            fieldMappingMapper.batchInsert(fieldMappingList);
            fieldMappingDTOS = fieldMappingMapper.getFieldMapping(commonQuery,SystemContext.getTenantId());
        }
        //字段模板添加字段，列映射也对应添加字段
        if(fieldMappingDTOS.size() < templates.size()){
            List<FieldMapping> fieldMappings = new ArrayList<>();
            Map<String,Object> templateMap = new HashMap<>();
            Map<String,Object> fieldMappingMap = new HashMap<>();
            templates.forEach(template -> {
                templateMap.put(String.valueOf(template.getSort()),template.getColumnName());
            });
            fieldMappingDTOS.forEach(fieldMappingDTO -> {
                fieldMappingMap.put(String.valueOf(fieldMappingDTO.getSort()),fieldMappingDTO.getPropertyName());
            });
            Map<String, Object> map = getDifferenceSetByGuava(templateMap,fieldMappingMap);
            for(Map.Entry<String,Object> entry : map.entrySet()){
                FieldMapping fieldMapping = new FieldMapping();
                fieldMapping.setId(UUIDUtils.getUUID());
                fieldMapping.setProjectId(projectDTOS.get(0).getId());
                fieldMapping.setPropertyName(String.valueOf(entry.getValue()));
                fieldMapping.setTitleName(String.valueOf(entry.getValue()));
                fieldMapping.setBusinessType(commonQuery.getBusinessType());
                fieldMapping.setSort(Integer.parseInt(entry.getKey()));
                fieldMapping.setCreateName(SystemContext.getUserName());
                fieldMappings.add(fieldMapping);
            }
            fieldMappingMapper.batchInsert(fieldMappings);
        }
        return fieldMappingMapper.getFieldMapping(commonQuery,SystemContext.getTenantId());
    }

    @Override
    public void updateFieldMappingList(List<FieldMappingDTO> fieldMappingDTOList) {
        List<FieldMapping> fieldMappings = new ArrayList<>();
        fieldMappingDTOList.forEach(fieldMappingDTO -> {
            FieldMapping fieldMapping = new FieldMapping();
            mapper.map(fieldMappingDTO,fieldMapping);
            fieldMappings.add(fieldMapping);
        });
        fieldMappingMapper.batchUpdateSelective(fieldMappings);
    }

    @Override
    public CompletableFuture<Map> upload(InputStream inputStream, String originalFilename, String businessType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> map = new EasyExcelService<>(FieldMappingImportDTO.class, cacheExcelData)
                        .producer(inputStream)
                        .batchNumber(1000)
                        .convertStorage(cacheExcelData)
                        .filterAndConvert((data) -> {
                            CombinationResultModel combinationResultModel = new CombinationResultModel();
                            List<FieldMappingImportDTO> list = new ArrayList<>();
                            combinationResultModel.list = list;

                            //字段名称(文件)文件内判重
                            List<FieldMappingImportDTO> fieldMappingImportDTO = data.getList();
                            int size_file = CollectionUtils.isEmpty(fieldMappingImportDTO)?0:fieldMappingImportDTO.size()-1;
                            for (int i = 0; i < size_file; i++){
                                String columnNameFile = fieldMappingImportDTO.get(i).getColumnNameFile();
                                if(StringUtil.isEmpty(columnNameFile)){
                                    continue;
                                }
                                for (int j = i+1; j < size_file; j++){
                                    if(columnNameFile.equals(fieldMappingImportDTO.get(j).getColumnNameFile())){
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(j+1, 0, "字段名称(文件) "+ columnNameFile +" 文件中重复"));
                                        return combinationResultModel;
                                    }
                                }
                            }
                            for (int i = 0; i < size_file; i++){
                                String columnNameDatecenter = fieldMappingImportDTO.get(i).getColumnNameDatacenter();
                                for (int j = i+1; j < size_file; j++){
                                    if(columnNameDatecenter.equals(fieldMappingImportDTO.get(j).getColumnNameDatacenter())){
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(j+1, 0, "字段名称(数据中心) "+ columnNameDatecenter +" 文件中重复"));
                                        return combinationResultModel;
                                    }
                                }
                            }

                            List<SalsesColumnMappingExportDTO> salsesColumnMappingExportDTOList = getTemplate(businessType);
                            data.getList().stream().forEach(mappingImportDTO -> {
                                //校验是否在数据模型表中(模板表-ent_datacenter_template)
                                boolean flag = true;
                                Integer rowIndex = mappingImportDTO.getRowIndex();
                                String columnNameDatacenter = mappingImportDTO.getColumnNameDatacenter();
                                if(StringUtil.isNotBlank(columnNameDatacenter)){
                                    if(!salsesColumnMappingExportDTOList.stream().anyMatch(s->columnNameDatacenter.equals(s.getColumnNameDatacenter()))){
                                        MessageUtils.addErrorMessage(combinationResultModel.error,new Message(rowIndex,0,"字段名称(数据中心) "+columnNameDatacenter+" 不存在或被修改"));
                                        flag = false;
                                    }
                                    if(StringUtil.isEmpty(mappingImportDTO.getColumnNameFile())){
                                        MessageUtils.addErrorMessage(combinationResultModel.warn,new Message(rowIndex,0,"字段名称(数据中心) "+columnNameDatacenter+" 未映射"));
                                        flag = false;
                                    }
                                }
                                if (flag) {
                                    list.add(mappingImportDTO);
                                }
                            });
                            return combinationResultModel;
                        })
                        .startWork().waitResult();
                map.put("fileName", originalFilename);
                return map;
            } catch (Exception e) {
                log.error("error [{}]", e);
                throw new BusinessException(new ErrorInfo("ERROR", "解析失败"));
            }
        });
    }

    @Override
    public Boolean commitExcel(String token, ProjectInsExcelQuery excelModelQuery) throws IOException {
        final String userId = SystemContext.getUserId();
        final String userName = SystemContext.getUserName();
        new SimpExcelConsumer<FieldMappingImportDTO>(cacheExcelData,token)
                .objectMapper(objectMapper,FieldMappingImportDTO.class)
                .consumer((list) ->{
                    List<FieldMapping> insertList = new ArrayList<>();
                    for (FieldMappingImportDTO fieldMappingImportDTO : list) {
                        FieldMapping fieldMapping = new FieldMapping();
                        fieldMapping.setBusinessType(excelModelQuery.businessType());
                        fieldMapping.setProjectId(excelModelQuery.projectId());
                        fieldMapping.setCreateTime(new Date());
                        fieldMapping.setCreateBy(userId);
                        fieldMapping.setCreateName(userName);
                        fieldMapping.setTitleName(fieldMappingImportDTO.getColumnNameFile());
                        fieldMapping.setPropertyName(fieldMappingImportDTO.getColumnNameDatacenter());
                        //fieldMapping.setProjectInstitutionId(excelModelQuery.projectInstitutionId());
                        fieldMapping.setProjectInstitutionCode(excelModelQuery.projectInstitutionCode());
                        fieldMapping.setProjectInstitutionName(excelModelQuery.projectInstitutionName());
                        fieldMapping.setSort(fieldMappingImportDTO.getRowIndex());
                        insertList.add(fieldMapping);
                    }
                    if(!insertList.isEmpty()){
                        batchDeleteFieldMapping(excelModelQuery.projectId(),excelModelQuery.businessType(),excelModelQuery.projectInstitutionCode());
                        fieldMappingMapper.batchInsert(insertList);
                        //batchInsert(insertList);
                    }
                }).pullAll(2000);
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchDeleteFieldMapping(String projectId, String businessType, String projectInstitutionCode) {
        fieldMappingMapper.deleteFieldMapping(projectId,businessType,projectInstitutionCode,SystemContext.getTenantId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchDeleteFieldMapping(String projectId, String businessType) {
        fieldMappingMapper.deleteFieldMapping(projectId,businessType,null,SystemContext.getTenantId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteMapping(CommonQuery commonQuery) {
        //删除业务类型映射关系
        /*BusinessTypeMapping businessTypeMapping = new BusinessTypeMapping();
        businessTypeMapping.setId(commonQuery.getId());
        businessTypeMapping.setDeleted(true);
        businessTypeMappingMapper.updateByPrimaryKeySelective(businessTypeMapping);*/
        businessTypeMappingMapper.delete(commonQuery.getId());
        //删除关联列映射关系
        if(StringUtil.isEmpty(commonQuery.getProjectInstitutionCode())){
            batchDeleteFieldMapping(commonQuery.getProjectId(),commonQuery.getBusinessType());
        }else {
            batchDeleteFieldMapping(commonQuery.getProjectId(),commonQuery.getBusinessType(),commonQuery.getProjectInstitutionCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public List<FieldMappingDTO> load(CommonQuery commonQuery) {
        PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        commonQuery.setTenantId(SystemContext.getTenantId());
        return fieldMappingMapper.getList(commonQuery);
    }

    @Override
    public List<FieldDTO> viewFields(CommonQuery commonQuery) {
        //PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        commonQuery.setTenantId(SystemContext.getTenantId());
        return fieldMappingMapper.getFields(commonQuery);
    }

    /**
     * 取Map集合的差集
     *
     * @param bigMap   大集合
     * @param smallMap 小集合
     * @return 两个集合的差集
     */
    public static Map<String, Object> getDifferenceSetByGuava(Map<String, Object> bigMap, Map<String, Object> smallMap) {
        Set<String> bigMapKey = bigMap.keySet();
        Set<String> smallMapKey = smallMap.keySet();
        Set<String> differenceSet = Sets.difference(bigMapKey, smallMapKey);
        Map<String, Object> result = Maps.newHashMap();
        for (String key : differenceSet) {
            result.put(key, bigMap.get(key));
        }
        return result;
    }
}
