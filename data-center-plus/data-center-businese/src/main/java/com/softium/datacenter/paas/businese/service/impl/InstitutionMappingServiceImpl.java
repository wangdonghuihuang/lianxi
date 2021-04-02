package com.softium.datacenter.paas.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.InstitutionDTO;
import com.softium.datacenter.paas.api.dto.InstitutionMappingDTO;
import com.softium.datacenter.paas.api.dto.InstitutionPocketDTO;
import com.softium.datacenter.paas.web.dto.excel.InstitutionMappingImportDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.api.entity.Institution;
import com.softium.datacenter.paas.api.entity.InstitutionMapping;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.api.mapper.InstitutionMapper;
import com.softium.datacenter.paas.api.mapper.InstitutionMappingMapper;
import com.softium.datacenter.paas.web.service.InstitutionMappingService;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.CombinationResultModel;
import com.softium.datacenter.paas.web.utils.easy.input.EasyExcelService;
import com.softium.datacenter.paas.web.utils.easy.input.Message;
import com.softium.datacenter.paas.web.utils.easy.input.SimpExcelConsumer;
import com.softium.datacenter.paas.web.utils.easy.validate.MessageUtils;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author huashan.li
 */
@Service
public class InstitutionMappingServiceImpl implements InstitutionMappingService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private InstitutionMappingMapper institutionMappingMapper;
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private InstitutionMapper institutionMapper;
    @Autowired
    private DistributorService distributorService;
    private static final Integer BATCH_SIZE = 100;
    @Override
    public List<InstitutionMappingDTO> list(InstitutionMappingQuery institutionMappingQuery) {
        PageHelper.startPage(institutionMappingQuery.getCurrent(),institutionMappingQuery.getPageSize(),true);
        return institutionMappingMapper.list(institutionMappingQuery, SystemContext.getTenantId());
    }

    @Override
    public CompletableFuture<Map> upload(InputStream inputStream, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try{
            Map<String,Object> map = new EasyExcelService<>(InstitutionMappingImportDTO.class,cacheExcelData)
                .producer(inputStream)
                .batchNumber(1000)
                .convertStorage(cacheExcelData)
                .filterAndConvert((data) -> {
                    CombinationResultModel combinationResultModel = new CombinationResultModel();
                    List<InstitutionMappingImportDTO> list = new ArrayList<>();
                    combinationResultModel.list = list;
                    List<InstitutionMappingImportDTO> institutionMappingImportDTOS = data.getList();
                    //文件内判重 经销商编码＋原始机构名称
                    for (int i=0; i<institutionMappingImportDTOS.size()-1; i++){
                        String institutionCode = institutionMappingImportDTOS.get(i).getInstitutionCode();
                        String originalInstitution = institutionMappingImportDTOS.get(i).getOriginalInstitutionName();
                        if(StringUtils.isEmpty(institutionCode)&&StringUtils.isEmpty(originalInstitution)){continue;}
                        for(int j = i+1; j < institutionMappingImportDTOS.size(); j++){
                            String institutionCodeN = institutionMappingImportDTOS.get(j).getInstitutionCode();
                            String originalInstitutionN = institutionMappingImportDTOS.get(j).getOriginalInstitutionName();
                            if(institutionCode.equals(institutionCodeN)&& originalInstitution.equals(originalInstitutionN)){
                                MessageUtils.addErrorMessage(combinationResultModel.error, new Message(j+1,  "数据重复(文件)"));
                                return combinationResultModel;
                            }
                        }
                    }
                    institutionMappingImportDTOS.stream().forEach(institutionMappingImportDTO -> {
                        boolean flg = true;
                        Integer rowIndex = institutionMappingImportDTO.getRowIndex();
                        String institutionId = "";
                        String standardInstitutionId = "";
                        String institutionCode = institutionMappingImportDTO.getInstitutionCode().trim();
                        String originalInstitution = institutionMappingImportDTO.getOriginalInstitutionName().trim();
                        String standardInstitutionCode = institutionMappingImportDTO.getStandardInstitutionCode().trim();
                        //institutionId = institutionCode,standardInstitutionId = standardInstitutionCode (2.0v)
                            //通过code(上游经销商编码) 先查institution表
                            Criteria<Institution> institutionCriteria = new Criteria<>();
                            institutionCriteria.addCriterion(new Condition("code", Operator.equal, institutionCode));
                            List<Institution> institutions = institutionMapper.findByCriteria(institutionCriteria);
                            if(!CollectionUtils.isEmpty(institutions)){
                                institutionId = institutions.get(0).getId();
                            }else {
                                //通过code 再查经销商主数据局id
                                InstitutionDTO institutionDto = new InstitutionDTO();
                                institutionDto.setCode(institutionCode);
                                ActionResult<PageInfo<InstitutionDTO>> infoActionResult = distributorService.getInstitutionList(institutionDto, SystemContext.getTenantId());
                                if (!infoActionResult.isSuccess()) {
                                    throw new BusinessException(new ErrorInfo("error", "微服务接口调用失败"));
                                }
                                List<InstitutionDTO> institutionDTOS = infoActionResult.getData().getList();
                                if(!CollectionUtils.isEmpty(institutionDTOS)){
                                    InstitutionDTO institutionDTO = institutionDTOS.get(0);
                                    Institution institution = new Institution();
                                    institution.setId(institutionDTO.getCode());
                                    institution.setCode(institutionDTO.getCode());
                                    institution.setName(institutionDTO.getName());
                                    institution.setProvinceName(institutionDTO.getProvince());
                                    institution.setCityName(institutionDTO.getCity());
                                    institution.setDistrictName(institutionDTO.getCounty());
                                    institution.setCategory(institutionDTO.getInstitutionCategory());
                                    institution.setSource("接口主数据");
                                    //记录一条 institution表
                                    institutionMapper.insert(institution);
                                    institutionId = institution.getId();
                                }else {
                                    MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 1, "企业主数据系统中不存在"));
                                    flg = false;
                                }
                            }

                            //通过code(下游经销商编码) 先查institution表
                            Criteria<Institution> standardInstitutionCriteria = new Criteria<>();
                            standardInstitutionCriteria.addCriterion(new Condition("code", Operator.equal, standardInstitutionCode));
                            List<Institution> standardInstitutions = institutionMapper.findByCriteria(standardInstitutionCriteria);
                            if(!CollectionUtils.isEmpty(standardInstitutions)){
                                standardInstitutionId = standardInstitutions.get(0).getId();
                            }else {
                                //通过code 再查经销商主数据局id
                                //主数据接口
                                InstitutionDTO institutionDto = new InstitutionDTO();
                                institutionDto.setCode(standardInstitutionCode);
                                ActionResult<PageInfo<InstitutionDTO>> infoActionResult = distributorService.getInstitutionList(institutionDto, SystemContext.getTenantId());
                                if (!infoActionResult.isSuccess()) {
                                    throw new BusinessException(new ErrorInfo("error", "微服务接口调用失败"));
                                }
                                List<InstitutionDTO> institutionDTOS = infoActionResult.getData().getList();
                                if(!CollectionUtils.isEmpty(institutionDTOS)){
                                    InstitutionDTO institutionDTO = institutionDTOS.get(0);
                                    Institution institution = new Institution();
                                    institution.setId(institutionDTO.getCode());
                                    institution.setCode(institutionDTO.getCode());
                                    institution.setName(institutionDTO.getName());
                                    institution.setProvinceName(institutionDTO.getProvince());
                                    institution.setCityName(institutionDTO.getCity());
                                    institution.setDistrictName(institutionDTO.getCounty());
                                    institution.setCategory(institutionDTO.getInstitutionCategory());
                                    institution.setSource("接口主数据");
                                    //记录一条 institution表
                                    institutionMapper.insert(institution);
                                    standardInstitutionId = institutionDTO.getCode();
                                }else {
                                    MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 4, "企业主数据系统中不存在"));
                                    flg = false;
                                }
                            }

                        //判断是否存在系统表中
                        Criteria<InstitutionMapping> criteria = new Criteria<>();
                        criteria.addCriterion(new Condition("original_institution_name", Operator.equal,originalInstitution));
                        criteria.addCriterion(new Condition("institution_id", Operator.equal,institutionId));
                        criteria.addCriterion(new Condition("disabled", Operator.equal,0));
                        List<InstitutionMapping> institutionMappings = institutionMappingMapper.findByCriteria(criteria);
                        if(institutionMappings.size()>0){
                            MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, "数据重复(系统)"));
                            flg = false;
                        }
                        if (flg) {
                            institutionMappingImportDTO.setInstitutionId(institutionId);
                            institutionMappingImportDTO.setStandardInstitutionId(standardInstitutionId);
                            list.add(institutionMappingImportDTO);
                        }
                    });
                    return combinationResultModel;
                }).startWork().waitResult();

            map.put("fileName",name);
            return map;
        } catch (Exception e) {
            logger.error("error [{}]", e);
            throw new BusinessException(new ErrorInfo("", "解析失败"));
        } finally {
        }
        });
    }

    @Transactional
    @Override
    public Boolean commitExcel(String token, String fileName) throws IOException {
        new SimpExcelConsumer<InstitutionMappingImportDTO>(cacheExcelData,token)
                .objectMapper(objectMapper,InstitutionMappingImportDTO.class)
                .consumer((list)->{
                    List<InstitutionMapping> institutionMappingList = new ArrayList<>();
                    list.stream().forEachOrdered(institutionMappingImportDTO -> {
                        InstitutionMapping institutionMapping = new InstitutionMapping();
                        institutionMapping.setId(UUIDUtils.getUUID());
                        institutionMapping.setDisabled(0);
                        institutionMapping.setInstitutionId(institutionMappingImportDTO.getInstitutionId());
                        institutionMapping.setOriginalInstitutionName(institutionMappingImportDTO.getOriginalInstitutionName());
                        institutionMapping.setOriginalInstitutionCode(null);
                        institutionMapping.setStandardInstitutionId(institutionMappingImportDTO.getStandardInstitutionId());
                        institutionMappingList.add(institutionMapping);
                    });
                    if(!institutionMappingList.isEmpty()){
                        batchInsert(institutionMappingList);
                    }
                }).pullAll(2000);
        return Boolean.TRUE;
    }

    @Override
    public List<InstitutionPocketDTO> institutionsPocket() {
        return institutionMappingMapper.institutionsPocket(SystemContext.getTenantId());
    }

    private void batchInsert(List<InstitutionMapping> institutionMappings){
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(institutionMappings)) {
            int size = institutionMappings.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > institutionMappings.size()) {
                    end = institutionMappings.size();
                }
                institutionMappingMapper.batchInsert(institutionMappings.subList(start,end));
            }
        }
    }
}
