package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.entity.FileColumnRule;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: york
 * @create: 2020-08-28 11:23
 **/
@Repository
public interface FileColumnRuleService {
    List<FileColumnRule> loadDefault(String projectId);

    List<PreProcessCommonDTO> loadSpecial(CommonQuery pageModel);

    /***
     * @description 删除列规则
     * @param id
     * @param
     */
    void deleteFileColumnRuleById(String id);

    List<SelectionEnumDTO> getBusiType(String projectId);

    /***
     * @description 添加或更新文件列规则详情配置
     * @param fileColumnRuleDTO
     */
    void saveFileColumnRule(FileColumnRuleDTO fileColumnRuleDTO);

    /***
     * @desccription 删除列规则详情
     * @param id
     */
    void deleteFieldRule(String id);

    /***
     * @descriptiton 展示文件列规则 字段详情配置
     * @return
     */
    List<FileHandleRuleDTO> viewFieldList(String ruleId);

    /***
     * @description 获取当前文件列规则配置业务类型可选文件列映射字段
     * @param commonQuery
     * @return
     */
    List<FieldDTO> getFieldListPocket(CommonQuery commonQuery);

    List<FileHandleRuleDTO> loadFileHandleRule(CommonQuery commonQuery);

    void saveAndEdit(FileColumnRuleDTO fileColumnRuleDTO);
}
