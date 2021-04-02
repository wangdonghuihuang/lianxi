package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.mongo.entity.CleaningRulesSettingPO;
import com.softium.datacenter.paas.web.service.CleaningRulesSettingService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author huashan.li
 */
@RestController
@RequestMapping("cleaningRulesSetting")
public class CleaningRulesSettingController {
    @Autowired
    private CleaningRulesSettingService cleaningRulesSettingService;

    @PostMapping("/load")
    public ActionResult<?> load() {
        List<CleaningRulesSettingPO> cleaningRulesSettings = cleaningRulesSettingService.list(SystemContext.getTenantId());
        PageInfo<?> pageInfo = new PageInfo<>(cleaningRulesSettings);
        return new ActionResult<>(pageInfo);
    }

    @PostMapping("/update")
    public ActionResult<?> update(@RequestBody List<CleaningRulesSettingPO> cleaningRulesSettings) {
        cleaningRulesSettingService.updateSetting(cleaningRulesSettings);
        return load();
    }

    @GetMapping("/monthDateRuleAccess")
    public ActionResult<?> queryCleaningRules(){
        return new ActionResult<>(cleaningRulesSettingService.monthDateRuleAccess());
    }
}
