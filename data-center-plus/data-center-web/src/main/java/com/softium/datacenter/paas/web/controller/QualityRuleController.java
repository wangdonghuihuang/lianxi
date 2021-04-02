package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.QualityRuleDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.QualityRule;
import com.softium.datacenter.paas.web.service.QualityRuleService;
import com.softium.framework.common.dto.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @description: 质检规则入口
 * @author: york
 * @create: 2020-08-27 15:17
 **/
@RestController
@Slf4j
@RequestMapping("qualityRule")
public class QualityRuleController{
    @Autowired
    private QualityRuleService qualityRuleService;

    //@SpecialPocket({BusinessTypePocket.class, QualityRulePocket.class, QualityProcessPocket.class, AssertionPocketBack.class})
    @GetMapping("defaultLoad")
    public ActionResult<List<QualityRule>> defaultLoad(@RequestParam String businessType) {
        return fieldDefaultList(businessType);
    }

    @PostMapping("specialLoad")
    public ActionResult<PageInfo<List<ProjectDTO>>> specialLoad(@RequestBody CommonQuery commonQuery) {
        return fieldSpecialList(commonQuery);
    }

    @PostMapping("editRule")
    public ActionResult add(@RequestBody QualityRuleDTO qualityRuleDTO){
        qualityRuleService.editQualityRule(qualityRuleDTO);
        /*if(StringUtil.isEmpty(qualityRuleDTO.getProjectInstitutionCode())){
            return defaultLoad(qualityRuleDTO.getProjectId());
        }else {
            CommonQuery commonQuery = new CommonQuery();
            commonQuery.setPageSize(10);
            commonQuery.setCurrent(1);
            commonQuery.setProjectId(qualityRuleDTO.getProjectId());
            return specialLoad(commonQuery);
        }*/
        return new ActionResult(true,"success");
    }

    @PostMapping("switchOnOff")
    public ActionResult switchOnOff(@RequestBody QualityRuleDTO qualityRuleDTO){
        qualityRuleService.switchOnOff(qualityRuleDTO);
        /*if(StringUtil.isEmpty(qualityRuleDTO.getProjectInstitutionCode())){
            return defaultLoad(qualityRuleDTO.getProjectId());
        }else {
            CommonQuery commonQuery = new CommonQuery();
            commonQuery.setPageSize(10);
            commonQuery.setCurrent(1);
            commonQuery.setProjectId(qualityRuleDTO.getProjectId());
            return specialLoad(commonQuery);
        }*/
        return new ActionResult(true,"success");
    }

    @PostMapping("deleteQualityRule")
    public ActionResult deleteQualityRule(@RequestBody QualityRuleDTO qualityRuleDTO){
        qualityRuleService.deleteQualityRule(qualityRuleDTO);
        /*if(StringUtil.isEmpty(qualityRuleDTO.getProjectInstitutionCode())){
            return defaultLoad(qualityRuleDTO.getProjectId());
        }else {
            CommonQuery commonQuery = new CommonQuery();
            commonQuery.setPageSize(10);
            commonQuery.setCurrent(1);
            commonQuery.setProjectId(qualityRuleDTO.getProjectId());
            return specialLoad(commonQuery);
        }*/
        return new ActionResult(true,"success");
    }

    @GetMapping("detail")
    public ActionResult viewQualityRule(@RequestParam(value = "id") String id){
        PreProcessCommonDTO qualityRule = qualityRuleService.viewQualityRule(id);
        return new ActionResult<>(qualityRule);
    }



    private ActionResult<PageInfo<List<ProjectDTO>>> fieldSpecialList(CommonQuery commonQuery) {
        List<PreProcessCommonDTO> preProcessCommonDTOList = qualityRuleService.loadSpecial(commonQuery);
        PageInfo<List<ProjectDTO>> pageInfo = new PageInfo(preProcessCommonDTOList);
        pageInfo.setPageSize(commonQuery.getPageSize());
        pageInfo.setPageNum(commonQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }


    private ActionResult<List<QualityRule>> fieldDefaultList(String businessType) {
        List<QualityRule> list = qualityRuleService.defaultLoad(businessType);
        return new ActionResult<>(list);
    }
}
