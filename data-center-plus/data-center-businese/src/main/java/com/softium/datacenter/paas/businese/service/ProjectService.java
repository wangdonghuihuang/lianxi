package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.DistributorDTO;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.init.TenantInitDTO;
import com.softium.datacenter.paas.api.dto.query.ProjectQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: york
 * @create: 2020-07-31 11:50
 **/
@Repository
public interface ProjectService {

    List<ProjectDTO> load(ProjectQuery projectQuery);

    ProjectDTO getById(String id);

    void projectUpdate(ProjectDTO projectDTO);

    void projectInsert(ProjectDTO projectDTO);

    String generateProjectCode(String maxNum, String numPrefix);

    /***
     * @description: 获取某项目下的经销商映射配置列表
     * @return
     */
    //List<ProjectInstitutionDTO> loadProIns(ProjectQuery projectQuery);

    Object initResource();

    List<DistributorDTO> fuzzyQuery(String keyWords);

    List<DistributorDTO> fuzzyQuery();

    boolean initDateCenter(TenantInitDTO tenantInitDTO);

    /*
    void proInsInsert(ProjectInstitutionDTO model, String userId);

    void proInsUpdate(ProjectInstitutionDTO model, String userId);

    CompletableFuture<Map> upload(InputStream inputStream, String originalFilename, String projectId);

    Boolean commitExcel(String token, ProjectQuery projectQuery) throws IOException;

    *//***
     * @description: 经销商映射逻辑删除
     * @param model
     * @param userId
     *//*
    void proInsDel(ProjectInstitutionDTO model, String userId);



    Object detail(String id);*/
}
