package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.FieldCharConvert;
import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.FieldFormatRule;
import com.softium.datacenter.paas.api.entity.PreprocessRule;
import com.softium.datacenter.paas.api.mapper.FieldFormatRuleMapper;
import com.softium.datacenter.paas.api.mapper.FieldMappingMapper;
import com.softium.datacenter.paas.api.mapper.PreprocessRuleMapper;
import com.softium.datacenter.paas.api.mapper.ProjectMapper;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.PreProcessService;
import com.softium.datacenter.paas.web.utils.UUIDUtil;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.*;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.StringUtil;
import com.softium.framework.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: york
 * @create: 2020-08-25 16:09
 **/
@Transactional
@Service
@Slf4j
public class PreProcessServiceImpl implements PreProcessService {

    @Autowired
    private PreprocessRuleMapper preprocessRuleMapper;
    @Autowired
    private FieldFormatRuleMapper fieldFormatRuleMapper;
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<PreprocessRule> loadDefault(String businessType) {
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        Criteria<PreprocessRule> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("businessType", Operator.equal,businessType))
                .addCriterion(new Condition("projectId", Operator.equal,projectDTOS.get(0).getId()));
        criteria.and(PreprocessRule::getProjectInstitutionCode,Operator.equal,"")
                .or(PreprocessRule::getProjectInstitutionCode,Operator.equal,null);
        criteria.sort(new SortProperty("createTime", Sort.DESC));
        return preprocessRuleMapper.findByCriteria(criteria);
    }

    /**
     * @param preProcessCommonDTO
     */
    @Override
    public void preRuleEdit(PreProcessCommonDTO preProcessCommonDTO) {
        String projectId = preProcessCommonDTO.getProjectId();
        if(StringUtils.isEmpty(projectId)){
            List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
            if(projectDTOS.isEmpty()){
                throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
            }
            projectId=projectDTOS.get(0).getId();
            preProcessCommonDTO.setProjectId(projectId);
        }
        this.checkPreRuleInsert(preProcessCommonDTO);
        boolean isSucess = true;
        String uuid = StringUtil.isEmpty(preProcessCommonDTO.getId())? UUIDUtils.getUUID():preProcessCommonDTO.getId();
        PreprocessRule preprocessRule = new PreprocessRule();
        preprocessRule.setId(uuid);
        preprocessRule.setCreateBy(SystemContext.getUserId());
        preprocessRule.setCreateName(SystemContext.getUserName());
        preprocessRule.setCreateTime(new Date());
        preprocessRule.setBusinessType(preProcessCommonDTO.getBusinessType());
        preprocessRule.setProjectId(projectId);
        preprocessRule.setProjectInstitutionCode(preProcessCommonDTO.getProjectInstitutionCode());
        preprocessRule.setProjectInstitutionName(preProcessCommonDTO.getProjectInstitutionName());
        preprocessRule.setRuleType(preProcessCommonDTO.getRuleType());
        preprocessRule.setRuleName(CommonUtil.PRE_PROCESS_RULE_TYPE.get(preProcessCommonDTO.getRuleType()));
        preprocessRule.setRuleOrder(CommonUtil.PRE_PROCESS_RULE_ORDER.get(preProcessCommonDTO.getRuleType()));
        preprocessRule.setDisabled(preProcessCommonDTO.getDisabled());
        switch (preProcessCommonDTO.getRuleType()){
            case 1: //去除字段首尾空格
            case 2: //特殊字符过滤
            case 3: //日期格式统一
                preprocessRule.setRuleContent(preProcessCommonDTO.getRuleContent());
                if(StringUtil.isEmpty(preProcessCommonDTO.getId())){
                    preprocessRuleMapper.insert(preprocessRule);
                }else{
                    preprocessRuleMapper.updateSelective(preprocessRule);
                }
                break;
            case 4: //字符转换
                //插入预处理规则
                if(StringUtil.isEmpty(preProcessCommonDTO.getId())){
                    preprocessRuleMapper.insert(preprocessRule);
                }else{
                    preprocessRuleMapper.updateSelective(preprocessRule);
                }
                //字符转换insert
                if(CollectionUtils.isNotEmpty(preProcessCommonDTO.getCharConvert())){
                    List<FieldCharConvert> fieldCharConverts = preProcessCommonDTO.getCharConvert();
                    this.checkReplyChar(fieldCharConverts);
                    for(int i = 0; i< fieldCharConverts.size(); i++){
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getPreConvertContent()))
                            throw new BusinessException(new ErrorInfo("ERROR","转换前字段值必填"));
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getAfterConvertContent()))
                            throw new BusinessException(new ErrorInfo("ERROR","转换后字段值必填"));
                        FieldFormatRule fieldFormatRule = new FieldFormatRule();
                        fieldFormatRule.setId(StringUtil.isEmpty(fieldCharConverts.get(i).getId())?UUIDUtil.uuid():fieldCharConverts.get(i).getId());
                        fieldFormatRule.setFirstFieldName(fieldCharConverts.get(i).getPreConvertContent());
                        fieldFormatRule.setLastFieldName(fieldCharConverts.get(i).getAfterConvertContent());
                        fieldFormatRule.setPreprocessRuleId(preprocessRule.getId());
                        fieldFormatRule.setRuleType(1); //1-预处理 2-质检
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getId())){
                            fieldFormatRule.setCreateTime(new Date());
                            fieldFormatRule.setCreateBy(SystemContext.getUserId());
                            fieldFormatRule.setCreateName(SystemContext.getUserName());
                            fieldFormatRuleMapper.insert(fieldFormatRule);
                        }else {
                            fieldFormatRule.setUpdateTime(new Date());
                            fieldFormatRule.setUpdateBy(SystemContext.getUserId());
                            fieldFormatRule.setUpdateName(SystemContext.getUserName());
                            fieldFormatRuleMapper.updateSelective(fieldFormatRule);
                        }
                    }
                }
                break;
            case 5: //字段内容转换
                if(StringUtil.isEmpty(preProcessCommonDTO.getId())){
                    preprocessRuleMapper.insert(preprocessRule);
                }else{
                    preprocessRuleMapper.updateSelective(preprocessRule);
                }
                //字段内容转换insert
                if(CollectionUtils.isNotEmpty(preProcessCommonDTO.getCharConvert())){
                    List<FieldCharConvert> fieldCharConverts = preProcessCommonDTO.getCharConvert();
                    this.checkReplyCharWithField(fieldCharConverts);
                    for(int i = 0; i< fieldCharConverts.size(); i++){
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getFieldName()))
                            throw new BusinessException(new ErrorInfo("ERROR","字段名称必填"));
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getAfterConvertContent()))
                            throw new BusinessException(new ErrorInfo("ERROR","转换后字段值必填"));
                        FieldFormatRule fieldFormatRule = new FieldFormatRule();
                        fieldFormatRule.setId(StringUtil.isEmpty(fieldCharConverts.get(i).getId())?UUIDUtil.uuid():fieldCharConverts.get(i).getId());
                        fieldFormatRule.setFieldName(fieldCharConverts.get(i).getFieldName());
                        fieldFormatRule.setFirstFieldName(fieldCharConverts.get(i).getPreConvertContent());
                        fieldFormatRule.setLastFieldName(fieldCharConverts.get(i).getAfterConvertContent());
                        fieldFormatRule.setPreprocessRuleId(preprocessRule.getId());
                        fieldFormatRule.setRuleType(1); //1-预处理 2-质检
                        if(StringUtil.isEmpty(fieldCharConverts.get(i).getId())){
                            fieldFormatRule.setCreateTime(new Date());
                            fieldFormatRule.setCreateBy(SystemContext.getUserId());
                            fieldFormatRule.setCreateName(SystemContext.getUserName());
                            fieldFormatRuleMapper.insert(fieldFormatRule);
                        }else {
                            fieldFormatRule.setUpdateTime(new Date());
                            fieldFormatRule.setUpdateBy(SystemContext.getUserId());
                            fieldFormatRule.setUpdateName(SystemContext.getUserName());
                            fieldFormatRuleMapper.updateSelective(fieldFormatRule);
                        }
                    }
                }
                break;
            default:
                isSucess = false;
                log.error("不识别该规则类型:{}",preProcessCommonDTO.getRuleType());
                break;
        }
        if(!isSucess) throw new BusinessException(new ErrorInfo("ERRR","不识别该规则类型:"+preProcessCommonDTO.getRuleType()));

    }

    /***
     * @description 判断提交转换字符串是否有重复 preContent
     * @param fieldCharConverts
     */
    private void checkReplyChar(List<FieldCharConvert> fieldCharConverts) {
        Map<String, String> fieldCharMap = new HashMap<>(fieldCharConverts.size());
        boolean flag = false;
        for (FieldCharConvert fieldCharConvert : fieldCharConverts) {
            String oldValue = fieldCharMap.put(fieldCharConvert.getPreConvertContent(), "1");
            if (!StringUtils.isEmpty(oldValue)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            throw new BusinessException(new ErrorInfo("ERROR","当前转换字符已存在！"));
        }
    }

    /***
     * @description 字段内容转换校验 fieldName + preContent
     * @param fieldCharConverts
     */
    private void checkReplyCharWithField(List<FieldCharConvert> fieldCharConverts) {
        Map<String, String> fieldCharMap = new HashMap<>(fieldCharConverts.size());
        boolean flag = false;
        for (FieldCharConvert fieldCharConvert : fieldCharConverts) {
            String oldValue = fieldCharMap.put((StringUtil.isEmpty(fieldCharConvert.getPreConvertContent())?"":fieldCharConvert.getPreConvertContent())
                    + fieldCharConvert.getFieldName(), "1");
            if (!StringUtils.isEmpty(oldValue)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            throw new BusinessException(new ErrorInfo("ERROR","当前替换字符已存在！"));
        }
    }

    /***
     * @description 检查是否可添加规则
     * @param preProcessCommonDTO
     */
    private void checkPreRuleInsert(PreProcessCommonDTO preProcessCommonDTO) {
        Criteria<PreprocessRule> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("projectId",Operator.equal,preProcessCommonDTO.getProjectId()));
        criteria.and(PreprocessRule::getProjectInstitutionCode,Operator.equal,preProcessCommonDTO.getProjectInstitutionCode())
                .and(PreprocessRule::getBusinessType,Operator.equal,preProcessCommonDTO.getBusinessType())
                .and(PreprocessRule::getRuleType,Operator.equal,preProcessCommonDTO.getRuleType())
                .and(PreprocessRule::getId,Operator.notEqual,preProcessCommonDTO.getId())
                .and(PreprocessRule::getDisabled,Operator.equal,preProcessCommonDTO.getDisabled());
        if(StringUtil.isEmpty(preProcessCommonDTO.getProjectInstitutionCode())) { //默认规则->排除特殊经销商
            criteria.and(PreprocessRule::getProjectInstitutionCode,Operator.notEqual,"")
                    .or(PreprocessRule::getProjectInstitutionCode,Operator.notEqual,null);
        }
        long cnt = preprocessRuleMapper.countByCriteria(criteria);
        if(cnt>0) throw new BusinessException(new ErrorInfo("ERROR","当前处理规则已存在！"));
    }

    @Override
    public List<PreProcessCommonDTO> loadSpecial(CommonQuery commonQuery) {
        PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        return preprocessRuleMapper.getSpecialPreRuleList(commonQuery.getProjectId(),SystemContext.getTenantId());
    }

    @Override
    public void switchOnOFF(PreProcessCommonDTO preProcessCommonDTO) {
        this.checkPreRuleInsert(preProcessCommonDTO);
        PreprocessRule preprocessRule = new PreprocessRule();
        preprocessRule.setId(preProcessCommonDTO.getId());
        preprocessRule.setDisabled(preProcessCommonDTO.getDisabled());
        preprocessRule.setUpdateTime(new Date());
        preprocessRule.setUpdateBy(SystemContext.getUserId());
        preprocessRule.setUpdateName(SystemContext.getUserName());
        preprocessRuleMapper.updateSelective(preprocessRule);
    }

    @Override
    public void deletePreRule(PreProcessCommonDTO preProcessCommonDTO) {
        PreprocessRule preprocessRule = new PreprocessRule();
        preprocessRule.setId(preProcessCommonDTO.getId());
        preprocessRule.setIsDeleted(1);
        preprocessRule.setUpdateName(SystemContext.getUserName());
        preprocessRule.setUpdateBy(SystemContext.getUserId());
        preprocessRule.setUpdateTime(new Date());
        preprocessRuleMapper.updateSelective(preprocessRule);
    }

    @Override
    public void deleteFieldCharConvert(PreProcessCommonDTO preProcessCommonDTO) {
        FieldFormatRule fieldFormatRule = new FieldFormatRule();
        fieldFormatRule.setId(preProcessCommonDTO.getId());
        fieldFormatRule.setId(preProcessCommonDTO.getId());
        fieldFormatRule.setIsDeleted(1);
        fieldFormatRule.setUpdateName(SystemContext.getUserName());
        fieldFormatRule.setUpdateBy(SystemContext.getUserId());
        fieldFormatRule.setUpdateTime(new Date());
        fieldFormatRuleMapper.updateSelective(fieldFormatRule);
    }

    @Override
    public PreProcessCommonDTO viewPreRuleDetail(String id) {
        PreProcessCommonDTO preProcessCommonDTO = preprocessRuleMapper.getPreRuleDetail(id);
        if(null==preProcessCommonDTO) throw new BusinessException(new ErrorInfo("ERROR","预处理规则不存在"));
        if(CommonUtil.CHAR_CONVERT.equals(preProcessCommonDTO.getRuleType())
                ||CommonUtil.FIELD_COONTENT_CONVERT.equals(preProcessCommonDTO.getRuleType())){ //特殊处理两个字符转换规则
            List<FieldCharConvert> convertList = preprocessRuleMapper.getCharContertListByRuleId(id);
            preProcessCommonDTO.setCharConvert(convertList);
        }
        return preProcessCommonDTO;
    }

    @Override
    public List<Map<String,String>> fuzzyQueryFieldNames(String projectInstitutionCode, String businessType, String fieldName) {
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        String projectId=projectDTOS.get(0).getId();
        List<Map<String,String>> mapList = fieldMappingMapper.fuzzyQuery(projectId,projectInstitutionCode,businessType,fieldName);
        if(CollectionUtils.isEmpty(mapList)){
            mapList = fieldMappingMapper.fuzzyQuery(projectId,null,businessType,fieldName);
        }
        return mapList;
    }
}
