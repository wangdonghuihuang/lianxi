package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.QualityRuleDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.QualityRule;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: york
 * @create: 2020-08-27 15:19
 **/
@Repository
public interface QualityRuleService {
    /**
     * @description 加载默认质检配置
     * @return
     */
    List<QualityRule> defaultLoad(String businessType);

    List<PreProcessCommonDTO> loadSpecial(CommonQuery pageModel);

    void editQualityRule(QualityRuleDTO qualityRuleDTO);

    void switchOnOff(QualityRuleDTO qualityRuleDTO);

    void deleteQualityRule(QualityRuleDTO qualityRuleDTO);

    PreProcessCommonDTO viewQualityRule(String id);
}
