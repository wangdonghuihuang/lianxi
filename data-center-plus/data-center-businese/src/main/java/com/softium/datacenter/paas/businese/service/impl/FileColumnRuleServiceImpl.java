package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.FileColumnRule;
import com.softium.datacenter.paas.api.entity.FileHandleRule;
import com.softium.datacenter.paas.api.mapper.FieldMappingMapper;
import com.softium.datacenter.paas.api.mapper.FileColumnRuleMapper;
import com.softium.datacenter.paas.api.mapper.FileHandleRuleMapper;
import com.softium.datacenter.paas.api.mapper.ProjectMapper;
import com.softium.datacenter.paas.web.service.FileColumnRuleService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.*;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: york
 * @create: 2020-08-28 11:23
 **/
@Slf4j
@Service
public class FileColumnRuleServiceImpl implements FileColumnRuleService {
    @Autowired
    private FileColumnRuleMapper fileColumnRuleMapper;
    @Autowired
    private FileHandleRuleMapper fileHandleRuleMapper;
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<FileColumnRule> loadDefault(String projectId) {
        Criteria<FileColumnRule> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("projectId", Operator.equal,projectId));
        criteria.and(FileColumnRule::getProjectInstitutionCode,Operator.notEqual,"")
                .and(FileColumnRule::getProjectInstitutionCode,Operator.notEqual,null);
        criteria.sort(new SortProperty("createTime", Sort.DESC));
        return fileColumnRuleMapper.findByCriteria(criteria);
    }

    @Override
    public List<PreProcessCommonDTO> loadSpecial(CommonQuery commonQuery) {
        PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        return fileColumnRuleMapper.getFileColumnRuleList(commonQuery.getProjectId(),SystemContext.getTenantId());
    }

    @Override
    public void deleteFileColumnRuleById(String id) {
        if(StringUtil.isEmpty(id)) throw new BusinessException(new ErrorInfo("ERROR","文件列规则不存在"));
        FileColumnRule fileColumnRule = new FileColumnRule();
        fileColumnRule.setId(id);
        fileColumnRule.setIsDeleted(1);
        fileColumnRule.setUpdateBy(SystemContext.getUserId());
        fileColumnRule.setUpdateName(SystemContext.getUserName());
        fileColumnRule.setUpdateTime(new Date());
        fileColumnRuleMapper.updateSelective(fileColumnRule);
    }

    @Override
    public List<SelectionEnumDTO> getBusiType(String projectId) {
        return fileColumnRuleMapper.getBusiType(projectId);
    }

    @Override
    public void saveFileColumnRule(FileColumnRuleDTO fileColumnRuleDTO) {
        Criteria<FileColumnRule> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("projectId",Operator.equal,fileColumnRuleDTO.getProjectId()));
        criteria.and(FileColumnRule::getProjectInstitutionCode,Operator.equal,fileColumnRuleDTO.getProjectInstitutionCode())
                .and(FileColumnRule::getBusinessType,Operator.equal,fileColumnRuleDTO.getBusinessType())
                .and(FileColumnRule::getId,Operator.notEqual,fileColumnRuleDTO.getId());
        if(StringUtil.isEmpty(fileColumnRuleDTO.getProjectInstitutionCode())) { //默认规则->排除特殊经销商
            criteria.and(FileColumnRule::getProjectInstitutionCode,Operator.notEqual,"")
                    .or(FileColumnRule::getProjectInstitutionCode,Operator.notEqual,null);
        }

        long cnt = fileColumnRuleMapper.countByCriteria(criteria);
        if(cnt>0) throw new BusinessException(new ErrorInfo("ERROR","当前处理规则已存在！"));

        FileColumnRule fileColumnRule = new FileColumnRule();
        if(StringUtil.isEmpty(fileColumnRuleDTO.getId())){
            fileColumnRule.setCreateTime(new Date());
            fileColumnRule.setCreateBy(SystemContext.getUserId());
            fileColumnRule.setCreateName(SystemContext.getUserName());
            fileColumnRule.setBusinessType(fileColumnRuleDTO.getBusinessType());
            fileColumnRule.setProjectId(fileColumnRuleDTO.getProjectId());
            //fileColumnRule.setProjectInstitutionCode(fileColumnRuleDTO.getProjectInstitutionId());
            fileColumnRule.setProjectInstitutionCode(fileColumnRuleDTO.getProjectInstitutionCode());
            fileColumnRule.setProjectInstitutionName(fileColumnRuleDTO.getProjectInstitutionName());
            fileColumnRuleMapper.insert(fileColumnRule);
        }else {
            fileColumnRule.setId(fileColumnRuleDTO.getId());
            fileColumnRule.setUpdateTime(new Date());
            fileColumnRule.setUpdateBy(SystemContext.getUserId());
            fileColumnRule.setUpdateName(SystemContext.getUserName());
            //fileColumnRule.setProjectInstitutionId(fileColumnRuleDTO.getProjectInstitutionId());
            fileColumnRule.setProjectInstitutionCode(fileColumnRuleDTO.getProjectInstitutionCode());
            fileColumnRule.setProjectInstitutionName(fileColumnRuleDTO.getProjectInstitutionName());
            fileColumnRuleMapper.updateSelective(fileColumnRule);
        }

        if(checkFileClumnRuleRepeat(fileColumnRuleDTO.getHandleRuleDTOList())){
            for (int i = 0; i < fileColumnRuleDTO.getHandleRuleDTOList().size(); i++){
                if(StringUtil.isEmpty(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getId())){ //id为空 insert\
                    FileHandleRule fileHandleRule = new FileHandleRule();
                    fileHandleRule.setBusinessType(fileColumnRuleDTO.getBusinessType());
                    fileHandleRule.setFieldName(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldName());
                    fileHandleRule.setFieldType(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldType());
                    fileHandleRule.setFilecolumnRuleId(fileColumnRuleDTO.getId());
                    fileHandleRule.setRequired(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getRequired());
                    fileHandleRule.setCreateName(SystemContext.getUserName());
                    fileHandleRuleMapper.insert(fileHandleRule); //PreInsertListener 会自动插入当前用户ID及创建时间
                }else { //update
                    FileHandleRule fileHandleRule = new FileHandleRule();
                    fileHandleRule.setId(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getId());
                    fileHandleRule.setFieldName(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldName());
                    fileHandleRule.setFieldType(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldType());
                    fileHandleRule.setRequired(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getRequired());
                    fileHandleRule.setUpdateName(SystemContext.getUserName());
                    fileHandleRuleMapper.updateSelective(fileHandleRule);
                }
            }
        }
    }

    @Override
    public void deleteFieldRule(String id) {
        if(StringUtil.isEmpty(id)) throw new BusinessException(new ErrorInfo("ERRR","ID为空"));
        FileHandleRule fileHandleRule = new FileHandleRule();
        fileHandleRule.setId(id);
        fileHandleRule.setIsDeleted(1);
        fileHandleRule.setUpdateName(SystemContext.getUserName());
        fileHandleRuleMapper.updateSelective(fileHandleRule);
    }

    @Override
    public List<FileHandleRuleDTO> viewFieldList(String ruleId) {
        return fileHandleRuleMapper.viewFieldList(ruleId);
    }

    @Override
    public List<FieldDTO> getFieldListPocket(CommonQuery commonQuery) {
        // 1、默认配置仅取默认配置下列映射字段 2、特殊经销商优先取特殊经销商下列映射，如果可选列为空则取默认
        commonQuery.setTenantId(SystemContext.getTenantId());
        List<FieldDTO> fields = fieldMappingMapper.getFields(commonQuery);
        if(CollectionUtils.isEmpty(fields)){
            commonQuery.setProjectInstitutionCode(null);
            fields = fieldMappingMapper.getFields(commonQuery);
        }
        return fields;
    }

    @Override
    public List<FileHandleRuleDTO> loadFileHandleRule(CommonQuery commonQuery) {
        return fileHandleRuleMapper.getHandleRuleList(commonQuery,SystemContext.getTenantId());
    }

    @Override
    public void saveAndEdit(FileColumnRuleDTO fileColumnRuleDTO) {
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        if(checkFileClumnRuleRepeat(fileColumnRuleDTO.getHandleRuleDTOList())){
            for (int i = 0; i < fileColumnRuleDTO.getHandleRuleDTOList().size(); i++){
                if(StringUtil.isEmpty(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getId())){ //id为空 insert\
                    FileHandleRule fileHandleRule = new FileHandleRule();
                    fileHandleRule.setBusinessType(fileColumnRuleDTO.getBusinessType());
                    fileHandleRule.setFieldName(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldName());
                    fileHandleRule.setFieldType(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldType());
                    fileHandleRule.setFilecolumnRuleId(null);
                    fileHandleRule.setProjectId(projectDTOS.get(0).getId());
                    fileHandleRule.setRequired(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getRequired());
                    fileHandleRule.setCreateName(SystemContext.getUserName());
                    fileHandleRule.setIsDetail(false);
                    fileHandleRuleMapper.insert(fileHandleRule); //PreInsertListener 会自动插入当前用户ID及创建时间
                }else { //update
                    FileHandleRule fileHandleRule = new FileHandleRule();
                    fileHandleRule.setId(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getId());
                    fileHandleRule.setFieldName(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldName());
                    fileHandleRule.setFieldType(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getFieldType());
                    fileHandleRule.setRequired(fileColumnRuleDTO.getHandleRuleDTOList().get(i).getRequired());
                    fileHandleRule.setUpdateName(SystemContext.getUserName());
                    fileHandleRuleMapper.updateSelective(fileHandleRule);
                }
            }
        }
    }

    /***
     * @descrptioon 校验文件列规则重复
     * @param handleRuleDTOList
     * @return
     */
    private boolean checkFileClumnRuleRepeat(List<FileHandleRuleDTO> handleRuleDTOList) {
        if (CollectionUtils.isEmpty(handleRuleDTOList)) return false;
        Map<String,String> duplicateRemveMap = new HashMap<>(handleRuleDTOList.size());
        for (FileHandleRuleDTO ruleDTO:handleRuleDTOList) {
            if(StringUtil.isEmpty(ruleDTO.getFieldName()))
                throw new BusinessException(new ErrorInfo("ERROR","字段名称必填"));
            if(StringUtil.isEmpty(ruleDTO.getFieldType()))
                throw new BusinessException(new ErrorInfo("ERROR","字段类型必填"));
            if(null==ruleDTO.getRequired())
                throw new BusinessException(new ErrorInfo("ERROR","请选择是否必填"));
            if(StringUtil.isNotEmpty(duplicateRemveMap.put(ruleDTO.getFieldName(),ruleDTO.getFieldName()))){
                throw new BusinessException(new ErrorInfo("ERRR",ruleDTO.getFieldName()+" 该列规则重复!"));
            }
        }
        return true;
    }
}
