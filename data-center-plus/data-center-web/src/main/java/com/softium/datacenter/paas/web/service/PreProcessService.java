package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.PreprocessRule;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: york
 * @create: 2020-08-25 16:09
 **/
@Repository
public interface PreProcessService {

    List<PreprocessRule> loadDefault(String businessType);

    /***
     * @description 预处理规则默认添加
     * @param preProcessCommonDTO
     *
     */
    void preRuleEdit(PreProcessCommonDTO preProcessCommonDTO);

    List<PreProcessCommonDTO> loadSpecial(CommonQuery commonQuery);

    /***
     * @description 启用/不启用 预处理规则
     * @param preProcessCommonDTO
     */
    void switchOnOFF(PreProcessCommonDTO preProcessCommonDTO);

    /***
     * @description 删除预处理规则
     * @param preProcessCommonDTO
     */
    void deletePreRule(PreProcessCommonDTO preProcessCommonDTO);

    void deleteFieldCharConvert(PreProcessCommonDTO preProcessCommonDTO);

    PreProcessCommonDTO viewPreRuleDetail(String id);

    List<Map<String,String>> fuzzyQueryFieldNames( String projectInstitutionCode, String businessType, String fieldName);
}
