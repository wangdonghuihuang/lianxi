package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.mongo.entity.CleaningRulesSettingPO;
import com.softium.datacenter.paas.api.mongo.repository.CleaningRulesSettingRepository;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.CleaningRulesSettingService;
import com.softium.framework.common.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huashan.li
 */
@Service
public class CleaningRulesSettingServiceImpl implements CleaningRulesSettingService {
    @Autowired
    private CleaningRulesSettingRepository cleaningRulesSettingRepository;
    @Override
    public List<CleaningRulesSettingPO> list(String tenantId) {
        return cleaningRulesSettingRepository.findByTenantId(tenantId);
    }

    @Override
    public void updateSetting(List<CleaningRulesSettingPO> cleaningRulesSettings) {
        List<CleaningRulesSettingPO> cleaningRulesSettingPOS = new ArrayList<>();
        cleaningRulesSettings.forEach(cleaningRulesSettingPO -> {
            CleaningRulesSettingPO cleaningRulesSettingPO1 = cleaningRulesSettingRepository.findByIdAndTenantId(cleaningRulesSettingPO.getId(),SystemContext.getTenantId());
            cleaningRulesSettingPO1.setIsBillPrint(cleaningRulesSettingPO.getIsBillPrint());
            cleaningRulesSettingPO1.setIsCleaning(cleaningRulesSettingPO.getIsCleaning());
            cleaningRulesSettingPO1.setIsProduct(cleaningRulesSettingPO.getIsProduct());
            cleaningRulesSettingPO1.setIsProductUnit(cleaningRulesSettingPO.getIsProductUnit());
            cleaningRulesSettingPO1.setIsCustomer(cleaningRulesSettingPO.getIsCustomer());
            cleaningRulesSettingPO1.setIsDateRule(cleaningRulesSettingPO.getIsDateRule());
            cleaningRulesSettingPOS.add(cleaningRulesSettingPO1);
        });
        cleaningRulesSettingRepository.saveAll(cleaningRulesSettingPOS);
    }

    @Override
    public List<Map<String, Object>> monthDateRuleAccess() {
        Map map = new HashMap();
        List<CleaningRulesSettingPO> cleaningRulesSettingPOS = cleaningRulesSettingRepository.findByTenantId(SystemContext.getTenantId());
        cleaningRulesSettingPOS.forEach(cleaningRulesSettingPO -> {
            if("SM".equalsIgnoreCase(cleaningRulesSettingPO.getBusinessType())&& 1==cleaningRulesSettingPO.getIsDateRule()){
                map.put("SM","月销售");
            }
            if("PM".equalsIgnoreCase(cleaningRulesSettingPO.getBusinessType())&& 1==cleaningRulesSettingPO.getIsDateRule()){
                map.put("PM","月采购");
            }
            if("IM".equalsIgnoreCase(cleaningRulesSettingPO.getBusinessType())&& 1==cleaningRulesSettingPO.getIsDateRule()){
                map.put("IM","月库存");
            }
            if("DM".equalsIgnoreCase(cleaningRulesSettingPO.getBusinessType())&& 1==cleaningRulesSettingPO.getIsDateRule()){
                map.put("DM","月库存");
            }
        });
        return CommonUtil.pocketConvert(map);
    }
}
