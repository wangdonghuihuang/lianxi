package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.PreProcessCommonDTO;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.QualityRuleDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.QualityRule;
import com.softium.datacenter.paas.api.mapper.ProjectMapper;
import com.softium.datacenter.paas.api.mapper.QualityRuleMapper;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.QualityRuleService;
import com.softium.datacenter.paas.web.utils.UUIDUtil;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.*;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: york
 * @create: 2020-08-27 15:19
 **/
@Service
public class QualityRuleServiceImpl implements QualityRuleService {
    @Autowired
    private QualityRuleMapper qualityRuleMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<QualityRule> defaultLoad(String businessType) {
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        String projectId=projectDTOS.get(0).getId();
        Criteria<QualityRule> criteria = Criteria.from(QualityRule.class);
        criteria.and(QualityRule::getProjectId, Operator.equal,projectId);
        criteria.and(QualityRule::getBusinessType,Operator.equal,businessType);
        criteria.and(QualityRule::getProjectInstitutionCode,Operator.notEqual,"")
                .or(QualityRule::getProjectInstitutionCode,Operator.notEqual,null);
        criteria.sort(new SortProperty("createTime",Sort.DESC));
        return qualityRuleMapper.findByCriteria(criteria);
    }

    @Override
    public List<PreProcessCommonDTO> loadSpecial(CommonQuery commonQuery) {
        PageHelper.startPage(commonQuery.getCurrent(), commonQuery.getPageSize(), true);
        return qualityRuleMapper.getSpecialQualityRuleList(commonQuery.getProjectId(),SystemContext.getTenantId());
    }

    @Override
    public void editQualityRule(QualityRuleDTO qualityRuleDTO) {
        List<ProjectDTO> projectDTOS = projectMapper.getProjectListBytenandId(SystemContext.getTenantId());
        if(projectDTOS.isEmpty()){
            throw new BusinessException(new ErrorInfo("ERROR", "请初始化项目配置"));
        }
        String projectId=projectDTOS.get(0).getId();
        qualityRuleDTO.setProjectId(projectId);
        this.checkQalityRule(qualityRuleDTO);
        String uuid = StringUtil.isEmpty(qualityRuleDTO.getId())? UUIDUtil.uuid():qualityRuleDTO.getId();
        QualityRule qualityRule = new QualityRule();
        qualityRule.setBusinessType(qualityRuleDTO.getBusinessType());
        qualityRule.setHandleProcess(qualityRuleDTO.getHandleProcess());
        qualityRule.setProjectId(qualityRuleDTO.getProjectId());
        //qualityRule.setProjectInstitutionId(qualityRuleDTO.getProjectInstitutionId());
        qualityRule.setProjectInstitutionCode(qualityRuleDTO.getProjectInstitutionCode());
        qualityRule.setProjectInstitutionName(qualityRuleDTO.getProjectInstitutionName());
        qualityRule.setRuleContent(qualityRuleDTO.getRuleContent());
        qualityRule.setRuleType(qualityRuleDTO.getRuleType());
        qualityRule.setRuleName(CommonUtil.QUALITY_RULE_TYPE.get(qualityRuleDTO.getRuleType()));
        qualityRule.setDisabled(qualityRuleDTO.getDisabled());
        qualityRule.setId(uuid);
        if(StringUtil.isEmpty(qualityRuleDTO.getId())) {
            qualityRule.setCreateBy(SystemContext.getUserId());
            qualityRule.setCreateName(SystemContext.getUserName());
            qualityRule.setCreateTime(new Date());
            qualityRule.setIsDetail(false);
            qualityRuleMapper.insert(qualityRule);
        } else{
            qualityRule.setUpdateBy(SystemContext.getUserId());
            qualityRule.setUpdateName(SystemContext.getUserName());
            qualityRule.setUpdateTime(new Date());
            qualityRuleMapper.updateSelective(qualityRule);
        }
    }

    @Override
    public void switchOnOff(QualityRuleDTO qualityRuleDTO) {
        this.checkQalityRule(qualityRuleDTO);
        if(StringUtil.isEmpty(qualityRuleDTO.getId())) throw new BusinessException(new ErrorInfo("ERROR","ID参数必传"));
        if(null==qualityRuleDTO.getDisabled()) throw new BusinessException(new ErrorInfo("ERROR","是否启用参数为空"));
        QualityRule qualityRule = new QualityRule();
        qualityRule.setId(qualityRuleDTO.getId());
        qualityRule.setDisabled(qualityRuleDTO.getDisabled());
        qualityRule.setUpdateTime(new Date());
        qualityRule.setUpdateBy(SystemContext.getUserId());
        qualityRule.setUpdateName(SystemContext.getUserName());
        qualityRuleMapper.updateSelective(qualityRule);
    }

    @Override
    public void deleteQualityRule(QualityRuleDTO qualityRuleDTO) {
        if(StringUtil.isEmpty(qualityRuleDTO.getId())) throw new BusinessException(new ErrorInfo("ERROR","ID参数必传"));
        qualityRuleMapper.delete(qualityRuleDTO.getId()); //标识位删除
    }

    @Override
    public PreProcessCommonDTO viewQualityRule(String id) {
        return qualityRuleMapper.viewQualityRuleById(id);
    }

    private void checkQalityRule(QualityRuleDTO qualityRuleDTO) {
        Criteria<QualityRule> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("projectId",Operator.equal,qualityRuleDTO.getProjectId()))
                .addCriterion(new Condition("projectInstitutionCode",Operator.equal,qualityRuleDTO.getProjectInstitutionCode()))
                .addCriterion(new Condition("businessType",Operator.equal,qualityRuleDTO.getBusinessType()))
                .addCriterion(new Condition("ruleType",Operator.equal,qualityRuleDTO.getRuleType()))
                .addCriterion(new Condition("id",Operator.notEqual,qualityRuleDTO.getId()));

        if(StringUtil.isEmpty(qualityRuleDTO.getProjectInstitutionCode())) { //默认规则->排除特殊经销商
            criteria.and(QualityRule::getProjectInstitutionCode,Operator.equal,null)
            .or(QualityRule::getProjectInstitutionCode,Operator.equal,"");
        }
        long cnt = qualityRuleMapper.countByCriteria(criteria);
        if(cnt>0) throw new BusinessException(new ErrorInfo("ERROR","当前处理规则已存在！"));
    }
}
