package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.PreprocessRule;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.PreProcessService;
import com.softium.framework.common.dto.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 预处理规则入口
 * @author: york
 * @create: 2020-08-25 15:52
 **/
@RestController
@Slf4j
@RequestMapping("preProcess")
public class PreProcessController{
    @Autowired
    private PreProcessService preProcessService;

    //@SpecialPocket({BusinessTypeValuePocket.class, PreRulePocket.class, RoleStatusPocket.class, DateFormatPocket.class, AssertionPocketBack.class})
    @GetMapping("defaultLoad")
    public ActionResult defaultLoad(@RequestParam(value = "businessType",required=true) String businessType) {
        return fieldDefaultList(businessType);
    }

    @PostMapping("preRuleEdit")
    public ActionResult preRuleEdit(@RequestBody PreProcessCommonDTO preProcessCommonDTO) {
        preProcessService.preRuleEdit(preProcessCommonDTO);
        /*if(StringUtil.isEmpty(preProcessCommonDTO.getProjectInstitutionCode())){
            return fieldDefaultList(preProcessCommonDTO.getProjectId());
        }
        CommonQuery filter = new CommonQuery();
        filter.setProjectId(preProcessCommonDTO.getProjectId());
        return fieldSpecialList(filter);*/
        return new ActionResult(true,"success");
    }

    @PostMapping("specialLoad")
    public ActionResult specialLoad(@RequestBody CommonQuery commonQuery) {
        return fieldSpecialList(commonQuery);
    }

    @PostMapping("searchSpecial")
    public ActionResult searchSpecial(@RequestBody CommonQuery commonQuery) {
        return fieldSpecialList(commonQuery);
    }

    @PostMapping("switchOnOff")
    public ActionResult switchOnOff(@RequestBody PreProcessCommonDTO preProcessCommonDTO) {
        preProcessService.switchOnOFF(preProcessCommonDTO);
        return new ActionResult(true,"success");
    }

    @PostMapping("deletePreRule")
    public ActionResult deletePreRule(@RequestBody PreProcessCommonDTO preProcessCommonDTO) {
        preProcessService.deletePreRule(preProcessCommonDTO);
        return new ActionResult(true,"success");
    }

    @PostMapping("deleteFieldCharConvert")
    public ActionResult deleteFieldCharConvert(@RequestBody PreProcessCommonDTO preProcessCommonDTO) {
        preProcessService.deleteFieldCharConvert(preProcessCommonDTO);
        return new ActionResult(true,"success");
    }

    @GetMapping("detail")
    public ActionResult viewPreRule(@RequestParam(value = "id") String id){
        PreProcessCommonDTO preProcessCommonDTO = preProcessService.viewPreRuleDetail(id);
        return new ActionResult<>(preProcessCommonDTO);
    }

    /***
     * @description 所有的字段下拉框模糊查询
     * @return
     */
    @GetMapping("fuzzyQueryField")
    public ActionResult fuzzyQueryField(
                                       @RequestParam(value = "projectInstitutionCode") String projectInstitutionCode,
                                       @RequestParam(value = "businessType") String businessType,
                                       @RequestParam(value = "fieldName") String fieldName){

        return new ActionResult<>(preProcessService.fuzzyQueryFieldNames(projectInstitutionCode,businessType,fieldName));
    }

    @GetMapping("/preProcessPocket")
    public ActionResult fileHandleRulePocket(){
        return new ActionResult( Map.of("whetherNot", CommonUtil.pocketConvert(CommonUtil.DISABLED_NOT),
                "preRulePocket",CommonUtil.pocketConvert(CommonUtil.PRE_PROCESS_RULE_TYPE),
                "dateFormatPocket",CommonUtil.pocketConvert(CommonUtil.DATE_FORMAT_TYPE)));
    }

    /**
     * @description 加载查询-特殊经销商-预处理
     * @param commonQuery
     * @return
     */
    private ActionResult fieldSpecialList(CommonQuery commonQuery) {
        List<PreProcessCommonDTO> list = preProcessService.loadSpecial(commonQuery);
        PageInfo<List<PreProcessCommonDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(commonQuery.getPageSize());
        pageInfo.setPageNum(commonQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    /***
     * @description 加载默认预处理规则
     * @return
     */
    private ActionResult fieldDefaultList(String businessType) {
        List<PreprocessRule> list = preProcessService.loadDefault(businessType);
        return new ActionResult<>(list);
    }
}
