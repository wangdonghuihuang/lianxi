package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.entity.IntelligentMatch;
import com.softium.datacenter.paas.web.service.IntelligentMatchService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author huashan.li
 */
@RestController
@RequestMapping("intelligentMatch")
public class IntelligentMatchController {
    @Autowired
    private IntelligentMatchService intelligentMatchService;
    @PostMapping("/load")
    public ActionResult load(){
        List<IntelligentMatch> intelligentMatchesList = intelligentMatchService.list();
        PageInfo pageInfo = new PageInfo(intelligentMatchesList);
        return new ActionResult<>(pageInfo);
    }
    @PostMapping("/update")
    public ActionResult update(@RequestBody List<IntelligentMatch> intelligentMatches){
        List<IntelligentMatch> intelligentMatchesList = intelligentMatchService.updateSetting(intelligentMatches);
        PageInfo pageInfo = new PageInfo(intelligentMatchesList);
        return new ActionResult<>(pageInfo);
    }
}
