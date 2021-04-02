package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.mongo.entity.CleaningRulesSettingPO;

import java.util.List;
import java.util.Map;

/**
 * @author huashan.li
 */
public interface CleaningRulesSettingService {
    List<CleaningRulesSettingPO> list(String tenantId);

    void updateSetting(List<CleaningRulesSettingPO> cleaningRulesSettings);

    List<Map<String,Object>> monthDateRuleAccess();
}
