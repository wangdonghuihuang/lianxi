package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.FileColumnRuleDTO;
import com.softium.datacenter.paas.api.dto.FileHandleRuleDTO;
import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.FileColumnRule;
import com.softium.datacenter.paas.api.mapper.TemplateMapper;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.FileColumnRuleService;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 文件列规则入口
 * @author: york
 * @create: 2020-08-28 11:16
 **/
@RestController
@Slf4j
@RequestMapping("fileColumnRule")
public class FileColumnRuleController extends BaseController{
    @Autowired
    private FileColumnRuleService fileColumnRuleService;
    @Autowired
    private TemplateMapper templateMapper;

    //@SpecialPocket({BusinessTypeValuePocket.class, AssertionPocket.class, FieldTypePocket.class })
    @GetMapping("defaultLoad")
    public ActionResult defaultLoad(@RequestParam(value = "projectId") String projectId) {
        return fieldDefaultList(projectId);
    }

    @PostMapping("specialLoad")
    public ActionResult specialLoad(@RequestBody CommonQuery commonQuery) {
        return fieldSpecialList(commonQuery);
    }

    @PostMapping("delFileColumnRule")
    public ActionResult delFileColumnRule(@RequestBody CommonQuery commonQuery){
        fileColumnRuleService.deleteFileColumnRuleById(commonQuery.getId());
        /*if(StringUtil.isEmpty(commonQuery.getProjectInstitutionCode())){
            return fieldDefaultList(commonQuery.getProjectId());
        }else {
            CommonQuery filter = new CommonQuery();
            filter.setCurrent(1);
            filter.setPageSize(10);
            filter.setProjectId(commonQuery.getProjectId());
            return fieldSpecialList(filter);
        }*/
        return new ActionResult(true,"success");
    }

    @PostMapping("saveFileColumRule")
    public ActionResult saveFileColumRule(@RequestBody FileColumnRuleDTO fileColumnRuleDTO){
//        fileColumnRuleService.saveFileColumnRule(fileColumnRuleDTO);
        /*if(StringUtil.isEmpty(fileColumnRuleDTO.getProjectInstitutionCode())){
            return defaultLoad(fileColumnRuleDTO.getProjectId());
        }else {
            CommonQuery filter = new CommonQuery();
            filter.setProjectId(fileColumnRuleDTO.getProjectId());
            return specialLoad(filter);
        }*/
        fileColumnRuleService.saveAndEdit(fileColumnRuleDTO);
        return new ActionResult(true,"success");
    }

    @PostMapping("deleteFieldRule")
    public ActionResult deleteFieldRule(@RequestBody CommonQuery commonQuery){
        fileColumnRuleService.deleteFieldRule(commonQuery.getId());
        return new ActionResult(true);
    }

    @PostMapping("viewFieldList")
    public ActionResult viewFieldList(@RequestBody CommonQuery commonQuery){
        List<FileHandleRuleDTO> fileHandleRuleDTOS = fileColumnRuleService.viewFieldList(commonQuery.getId());
        return new ActionResult<>(
                Map.of("fileHandleRuleDTOS",fileHandleRuleDTOS,
                "fieldList",fileColumnRuleService.getFieldListPocket(commonQuery)));
    }

    @GetMapping("/fileHandleRulePocket")
    public ActionResult fileHandleRulePocket(){
        return new ActionResult( Map.of("whetherNot", CommonUtil.pocketConvert(CommonUtil.WHETHER_NOT),
                "fieldTypePocket", CommonUtil.pocketConvert(CommonUtil.FIELD_TYPE)));
    }
    @PostMapping("/loadFileHandleRule")
    public ActionResult loadFileHandleRule(@RequestBody CommonQuery commonQuery){
        if(StringUtils.isEmpty(commonQuery.getBusinessType())){
            throw new BusinessException(new ErrorInfo("ERROR","无业务类型"));
        }
        List<FileHandleRuleDTO> fileHandleRuleDTOList = fileColumnRuleService.loadFileHandleRule(commonQuery);
        return new ActionResult( Map.of("fileHandleRuleDTOS",fileHandleRuleDTOList,
                "templatePocket",templateMapper.getTemplateTypes(commonQuery.getBusinessType())));
    }

    private ActionResult fieldSpecialList(CommonQuery commonQuery) {
        List<PreProcessCommonDTO> preProcessCommonDTOList = fileColumnRuleService.loadSpecial(commonQuery);
        PageInfo<List<PreProcessCommonDTO>> pageInfo = new PageInfo(preProcessCommonDTOList);
        pageInfo.setPageSize(commonQuery.getPageSize());
        pageInfo.setPageNum(commonQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }


    private ActionResult fieldDefaultList(String projectId) {
        List<FileColumnRule> fileColumnRules = fileColumnRuleService.loadDefault(projectId);
        return new ActionResult<>(fileColumnRules);
//        return new ActionResult<>(Map.of("fileColumnRules",fileColumnRules,
//                "businessTypeDynamic",fileColumnRuleService.getBusiType(projectId)));
    }

}
