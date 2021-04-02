package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.DistributorDTO;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.init.TenantInitDTO;
import com.softium.datacenter.paas.api.dto.query.ProjectQuery;
import com.softium.datacenter.paas.api.entity.BusinessType;
import com.softium.datacenter.paas.api.entity.PreprocessRule;
import com.softium.datacenter.paas.api.entity.Project;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.ProjectService;
import com.softium.datacenter.paas.web.utils.DateUtils;
import com.softium.datacenter.paas.web.utils.GenerateCode;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.dto.PageRequest;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @description:
 * @author: york
 * @create: 2020-07-31 13:52
 **/
@Service
public class ProjectServiceImpl implements ProjectService {

    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private PreprocessRuleMapper preprocessRuleMapper;
    @Autowired
    private ProjectInstitutionMapper projectInstitutionMapper;
    @Autowired
    private OriginSaleMapper originSaleMapper;
    /*@Value("${enterprise_base_url}")
    private String enterpriseBaseUrl;
    @Value("${datacenter.appid}")
    private String datacenterAppId;
    private String fuzzyQueryUrl = "/api/distributor/search/";
    private String queryAllUrl = "/api/distributor/";*/
    @Autowired
    private DistributorService distributorService;
    /*
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    private InstitutionMapper institutionMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private FileParseLogMapper fileParseLogMapper;
    */

    @Override
    public List<ProjectDTO> load(ProjectQuery projectQuery) {
        //兼容前端框架传参 时间参数特殊处理
        ProjectQuery resetQuery = projectQuery;
        resetQuery.setCreatedTimeSection(null);
        resetQuery.setModifiedTimeSection(null);
        if(null!=resetQuery.getCreatedTime()&&resetQuery.getCreatedTime().length==2){
            LocalDateTime[] createdTimes = {DateUtils.timestampToLocalDateTime(resetQuery.getCreatedTime()[0]),
                    DateUtils.timestampToLocalDateTime(resetQuery.getCreatedTime()[1])};
            resetQuery.setCreatedTimeSection(createdTimes);
        }
        if(null!=resetQuery.getModifiedTime()&&resetQuery.getModifiedTime().length==2){
            LocalDateTime[] modifyTimes = {DateUtils.timestampToLocalDateTime(resetQuery.getModifiedTime()[0]),
                    DateUtils.timestampToLocalDateTime(resetQuery.getModifiedTime()[1])};
            resetQuery.setModifiedTimeSection(modifyTimes);
        }
        PageRequest<Project> pageRequest = new PageRequest<>();
        pageRequest.setCriteria(new Criteria<Project>());
        PageHelper.startPage(projectQuery.getCurrent(), projectQuery.getPageSize(), true);
        return projectMapper.getProjectList(resetQuery);
    }

    @Override
    public ProjectDTO getById(String id) {
        return projectMapper.getProjectDTOById(id);
    }

    @Override
    public void projectUpdate(ProjectDTO projectDTO) {
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO,project);
        project.setUpdateName(SystemContext.getUserName());
        project.setUpdateBy(SystemContext.getAccountId());
        projectMapper.updateSelective(project);
    }

    @Override
    public void projectInsert(ProjectDTO projectDTO) {
        //构建查询器
        Criteria<Project> criteria = new Criteria();
        criteria.addCriterion(new Condition("projectName", Operator.equal,projectDTO.getProjectName()));
        //按查询器查询数据条数
        long cnt =projectMapper.countByCriteria(criteria);
        //已存在则抛业务异常，由Interceper捕获返回
        if(cnt>0) throw new BusinessException(new ErrorInfo("ERROR","项目名称已存在"));
        //否则转换参数添加
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO,project);
        //generateProjectCode() 按业务规则生成编码
        project.setProjectCode(generateProjectCode("","SJZXXM"));
        project.setCreateTime(new Date());
        project.setCreateBy(SystemContext.getUserId());
        project.setCreateName(SystemContext.getUserName());
        project.setDisabled(0);
        projectMapper.insert(project);
        //添加项目后-默认在该项目下添加默认的规则
        this.doAfterInsert(project.getId());
    }

    @Override
    public String generateProjectCode(String maxNum, String numPrefix) {
        if(StringUtils.isBlank(maxNum)) {
            maxNum = projectMapper.getMaxProjectCode(numPrefix);
        }
        String projectCode;
        if (ObjectUtils.isEmpty(maxNum)) {
            projectCode = numPrefix+String.format("%06d", 1);
        } else {
            int number=0;
            if(maxNum.length()>numPrefix.length()) {
                number = Integer.parseInt(maxNum.substring(numPrefix.length()));
            }
            projectCode = GenerateCode.generate(numPrefix, number, 6);
        }
        //判断是否存在
        Project query=new Project();
        query.setProjectCode(projectCode);
        Criteria<Project> criteria = new Criteria<>();
        criteria.addCriterion(new Condition("projectCode",Operator.equal,projectCode));
        long count = projectMapper.countByCriteria(criteria);
        if(count>0){
            projectCode=generateProjectCode(projectCode, numPrefix);
        }
        return projectCode;
    }

    private void doAfterInsert(String projectId) {
        List<BusinessType> businessTypeList = fieldMappingMapper.getBusiTypeAll();
        for (BusinessType businessType:businessTypeList) {
            PreprocessRule preprocessRule = new PreprocessRule();
            preprocessRule.setCreateBy(SystemContext.getUserId());
            preprocessRule.setCreateName(SystemContext.getUserName());
            preprocessRule.setCreateTime(new Date());
            preprocessRule.setBusinessType(businessType.getId());
            preprocessRule.setProjectId(projectId);
            preprocessRule.setDisabled(0);
            preprocessRule.setRuleType(CommonUtil.REMOVE_SPACES);
            preprocessRule.setRuleName(CommonUtil.PRE_PROCESS_RULE_TYPE.get(CommonUtil.REMOVE_SPACES));
            preprocessRule.setRuleOrder(CommonUtil.PRE_PROCESS_RULE_ORDER.get(CommonUtil.REMOVE_SPACES));
            preprocessRuleMapper.insert(preprocessRule);
        }
    }

   /* @Override
    public List<ProjectInstitutionDTO> loadProIns(ProjectQuery projectQuery) {
        PageHelper.startPage(projectQuery.getCurrent(), projectQuery.getPageSize(), true);
        List<ProjectInstitutionDTO> institutionDTOList = projectInstitutionMapper.getListByProject(projectQuery);
        return institutionDTOList;
    }*/

    @Override
    public Object initResource() {
        Map<Object,Object> result = new HashMap<>();
        result.put("disabledPocket",CommonUtil.pocketConvert(CommonUtil.DISABLED_ENUM)); //是否生效
        result.put("businessTypeValuePocket",originSaleMapper.findBusinessTypeForVal());
        result.put("businessTypePocket",originSaleMapper.findBusinessType());
        result.put("qualityRulePocket",CommonUtil.pocketConvert(CommonUtil.QUALITY_RULE_TYPE));
        result.put("qualityProcessPocket",CommonUtil.pocketConvert(CommonUtil.QULITY_PROCESS));
        result.put("preRulePocket",CommonUtil.pocketConvert(CommonUtil.PRE_PROCESS_RULE_TYPE));
        result.put("dateFormatPocket",CommonUtil.pocketConvert(CommonUtil.DATE_FORMAT_TYPE));
//        result.put("fieldTypePocket",CommonUtil.FIELD_TYPE);
        result.put("fieldTypePocket",CommonUtil.pocketConvert(CommonUtil.FIELD_TYPE));
        result.put("projectInfo",checkProjectIsCreat());
        result.put("whetherNot",CommonUtil.pocketConvert(CommonUtil.WHETHER_NOT));
        result.put("fileStatusPocket",CommonUtil.pocketConvert(CommonUtil.FILE_PARSE_STATUS));
        result.put("accessTypePocket",CommonUtil.pocketConvert(CommonUtil.ACCESS_TYPE));
        result.put("checkoutStatus",CommonUtil.pocketConvert(CommonUtil.CHECKOUT_STATUS));
        result.put("failureReasonPocket",CommonUtil.pocketConvert(CommonUtil.FILE_PARSE_FAILURE_REASON));
        result.put("businessTypeValue",CommonUtil.pocketConvert(CommonUtil.BUSINESS_DATA_VALUE));
        result.put("periodNamePocket",originSaleMapper.findPeriodName(SystemContext.getTenantId()));
        result.put("uploadTemplateValue",CommonUtil.pocketConvert(CommonUtil.UPLOAD_TEMPLATE_VALUE));
        result.put("baseInspectStatus",CommonUtil.pocketConvert(CommonUtil.BASE_INSPECT_STATUS));
        result.put("uploadTypePocket",CommonUtil.pocketConvert(CommonUtil.UPLOAD_TYPE));
        result.put("disabledNot",CommonUtil.pocketConvert(CommonUtil.DISABLED_NOT));
        result.put("dataSourcePocket",CommonUtil.pocketConvert(CommonUtil.DATA_SOURCE));
        result.put("printStatusPocket",CommonUtil.pocketConvert(CommonUtil.PRINT_STATUS));
        result.put("ptintInterceptionStatus",CommonUtil.pocketConvert(CommonUtil.PTINT_INTERCEPTION_STATUS));
        result.put("mdmStatus",CommonUtil.pocketConvert(CommonUtil.MDM_STATUS));
        result.put("businessAllTypeName",CommonUtil.pocketConvert(CommonUtil.BUSINESS_TYPE));
        result.put("billPrintSearch",CommonUtil.pocketConvert(CommonUtil.BILL_PRINT_SEARCH));
        result.put("matchSearchType",CommonUtil.pocketConvert(CommonUtil.MATCH_SEARCH_TYPE));
        return result;
    }


    @Override
    public List<DistributorDTO> fuzzyQuery(String keyWords) {
        /*Map<String,String> param = new HashMap<>();
        param.put("name",keyWords);
        param.put("tenantId",SystemContext.getTenantId());
        logger.info("请求接口【"+(enterpriseBaseUrl+fuzzyQueryUrl)+"】，参数【"+keyWords+"】");
        String res = "";
        res = HttpUtilsCommon.post(enterpriseBaseUrl+fuzzyQueryUrl,this.buildHeader(), JSONObject.toJSONString(param), ContentType.APPLICATION_JSON);
        logger.info("接口【"+(enterpriseBaseUrl+fuzzyQueryUrl)+"】返回结果【"+res+"】");
        return JSONObject.parseObject(res);*/
        DistributorDTO distributorDTO = new DistributorDTO();
        distributorDTO.setTenantId(SystemContext.getTenantId());
        distributorDTO.setName(keyWords);
        ActionResult<List<DistributorDTO>> result = distributorService.searchDistributor(distributorDTO);
        if (!result.isSuccess())
            throw new BusinessException(new ErrorInfo("error","微服务接口调用失败"));
        return result.getData();


    }

    @Override
    public List<DistributorDTO> fuzzyQuery() {
        /*logger.info("请求接口【"+(enterpriseBaseUrl+queryAllUrl)+"】");
        String res = "";
        try {
            res = HttpUtilsCommon.get(enterpriseBaseUrl+queryAllUrl+SystemContext.getTenantId(),this.buildHeader(),null);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("请求接口异常"+e.getMessage());
            throw new BusinessException(new ErrorInfo("ERROR","外部接口异常"));
        }
        logger.info("接口【"+(enterpriseBaseUrl+queryAllUrl)+"】返回结果【"+res+"】");
        return JSONObject.parseObject(res);*/
        ActionResult<List<DistributorDTO>> result = distributorService.listDistributor(SystemContext.getTenantId());
        if (!result.isSuccess())
            throw new BusinessException(new ErrorInfo("error","微服务接口调用失败"));
        return result.getData();
    }

    @Override
    public boolean initDateCenter(TenantInitDTO tenantInitDTO) {
        List<ProjectDTO> projectList = projectMapper.getProjectListBytenandId(tenantInitDTO.getTenant().getId());
        if(CollectionUtils.isEmpty(projectList)){
            Project project = new Project();
            project.setCreateName("york");
            project.setCreateBy("york");
            project.setCreateTime(new Date());
            project.setEnterpriseName(tenantInitDTO.getTenant().getCode());
            project.setProjectCode(tenantInitDTO.getTenant().getCode());
            project.setProjectName(tenantInitDTO.getTenant().getCode());
            project.setTenantId(tenantInitDTO.getTenant().getId());
            project.setDisabled(0);
            project.setVersion(0L);
            project.setIsDeleted(0);
            project.setId(UUIDUtils.getUUID());
            projectMapper._insert(project);
        }
        return true;
    }

/*    private Map<String, String> buildHeader() {
        Map<String,String> header = new HashMap<>();
        return Map.of("RS-Header-AccountId",SystemContext.getAccountId(),
                "RS-Header-UserId",SystemContext.getUserId(),
                "RS-Header-UserName",SystemContext.getUserName(),
                "RS-Header-TenantId",SystemContext.getTenantId(),
                "RS-Header-Locale","zh_CN",
                "RS-Header-AppId",datacenterAppId);
    }*/

    public Project checkProjectIsCreat(){
        Criteria<Project> criteria = new Criteria<>();
        List<Project> projectList = projectMapper.findByCriteria(criteria);
        if(CollectionUtils.isEmpty(projectList)){
            Project project = new Project();
            project.setCreateName(SystemContext.getUserName());
            project.setCreateBy(SystemContext.getUserId());
            project.setCreateTime(new Date());
            project.setEnterpriseName(SystemContext.getTenantId());
            project.setProjectCode(SystemContext.getTenantId());
            project.setProjectName(SystemContext.getTenantId());
            project.setDisabled(0);
            projectMapper.insert(project);
            this.doAfterInsert(project.getId());
            return project;
        }
        return projectList.get(0);
    }

    public void initProject(TenantInitDTO tenantInitDTO){
        Criteria<Project> criteria = new Criteria<>();
        List<Project> projectList = projectMapper.findByCriteria(criteria);
        if(CollectionUtils.isEmpty(projectList)){
            Project project = new Project();
            project.setCreateName(SystemContext.getUserName());
            project.setCreateBy(SystemContext.getUserId());
            project.setCreateTime(new Date());
            project.setEnterpriseName(SystemContext.getTenantId());
            project.setProjectCode(SystemContext.getTenantId());
            project.setProjectName(SystemContext.getTenantId());
            project.setDisabled(0);
            projectMapper.insert(project);
            this.doAfterInsert(project.getId());
        }
    }

/*


    @Override
    public void proInsInsert(ProjectInstitutionDTO model, String userId) {

        if(StringUtil.isBlank(model.getProjectInstitutionCode())){
            throw new BaseBusinessException("ERROR","经销商编码(DDI平台)不能为空");
        }
        if(StringUtil.isBlank(model.getProjectInstitutionName())){
            throw new BaseBusinessException("ERROR","经销商名称(DDI平台)不能为空");
        }
        if(StringUtil.isBlank(model.getInstitutionCode())|| StringUtil.isBlank(model.getInstitutionId())){
            throw new BaseBusinessException("ERROR","经销商名称(数据中心)不能为空");
        }
        Example proInsExample = new Example(ProjectInstitution.class);
        proInsExample.createCriteria().andEqualTo("projectInstitutionCode",model.getProjectInstitutionCode())
                .andEqualTo("projectId",model.getProjectId())
                .andEqualTo("deleted",false)
                .andEqualTo("disabled",false);
        List<ProjectInstitution> listProIns = projectInstitutionMapper.selectByExample(proInsExample);
        if(CollectionUtil.isNotEmpty(listProIns))
            throw new BaseBusinessException("ERROR","该经销商编码已存在");
        Example proInsExample_name = new Example(ProjectInstitution.class);
        proInsExample_name.createCriteria().andEqualTo("projectInstitutionName",model.getProjectInstitutionName())
                .andEqualTo("projectId",model.getProjectId())
                .andEqualTo("deleted",false)
                .andEqualTo("disabled",false);
        List<ProjectInstitution> listProIns_name = projectInstitutionMapper.selectByExample(proInsExample_name);
        if(CollectionUtil.isNotEmpty(listProIns_name))
            throw new BaseBusinessException("ERROR","该经销商名称已存在");

        ProjectInstitution projectInstitution = new ProjectInstitution();
        projectInstitution.setId(UUIDUtil.uuid());
        projectInstitution.setProjectId(model.getProjectId());
        projectInstitution.setInstitutionId(model.getInstitutionId());
        projectInstitution.setProjectInstitutionName(model.getProjectInstitutionName());
        projectInstitution.setProjectInstitutionCode(model.getProjectInstitutionCode());
        projectInstitution.setCreatedTime(LocalDateTime.now());
        projectInstitution.setCreator(userId);
        projectInstitutionMapper.insertSelective(projectInstitution);
    }

    @Override
    public void proInsUpdate(ProjectInstitutionDTO model, String userId) {
        Example proInsExample = new Example(ProjectInstitution.class);
        proInsExample.createCriteria().andEqualTo("projectInstitutionCode",model.getProjectInstitutionCode())
                .andEqualTo("projectId",model.getProjectId())
                .andEqualTo("deleted",false)
                .andEqualTo("disabled",false)
                .andNotEqualTo("id",model.getId());
        List<ProjectInstitution> listProIns = projectInstitutionMapper.selectByExample(proInsExample);
        if(CollectionUtil.isNotEmpty(listProIns))
            throw new BaseBusinessException("ERROR","该经销商编码已存在");
        Example proInsExample_name = new Example(ProjectInstitution.class);
        proInsExample_name.createCriteria().andEqualTo("projectInstitutionName",model.getProjectInstitutionName())
                .andEqualTo("projectId",model.getProjectId())
                .andEqualTo("deleted",false)
                .andEqualTo("disabled",false)
                .andNotEqualTo("id",model.getId());
        List<ProjectInstitution> listProIns_name = projectInstitutionMapper.selectByExample(proInsExample_name);
        if(CollectionUtil.isNotEmpty(listProIns_name))
            throw new BaseBusinessException("ERROR","该经销商名称已存在");

        ProjectInstitution projectInstitution = new ProjectInstitution();
        projectInstitution.setId(model.getId());
        projectInstitution.setModifiedTime(LocalDateTime.now());
        projectInstitution.setModifier(userId);
        projectInstitution.setProjectInstitutionCode(model.getProjectInstitutionCode());
        projectInstitution.setProjectInstitutionName(model.getProjectInstitutionName());
        projectInstitution.setInstitutionId(model.getInstitutionId());
        projectInstitutionMapper.updateByPrimaryKeySelective(projectInstitution);
    }

    @Override
    public CompletableFuture<Map> upload(InputStream inputStream, String originalFilename, String projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> map = new EasyExcelService<>(ProjectInsImportDTO.class, cacheExcelData)
                        .producer(inputStream)
                        .batchNumber(1000)
                        .convertStorage(cacheExcelData)
                        .filterAndConvert((data) -> {
                            CombinationResultModel combinationResultModel = new CombinationResultModel();
                            List<ProjectInsImportDTO> list = new ArrayList<>();
                            combinationResultModel.list = list;

                            //经销商名称（DDI平台）文件内判重
                            List<ProjectInsImportDTO> projectInsImportDTOS = data.getList();
                            int size = CollectionUtil.isEmpty(projectInsImportDTOS)?0:projectInsImportDTOS.size()-1;
                            boolean isReply = false;
                            for (int i = 0; i < size; i++){
                                String projectInstitutionCode = projectInsImportDTOS.get(i).getProjectInstitutionCode();
                                if(StringUtil.isBlank(projectInstitutionCode)){
                                    return combinationResultModel;
                                }
                                for (int j = i+1; j < size+1; j++){
                                    if(projectInstitutionCode.equals(projectInsImportDTOS.get(j).getProjectInstitutionCode())){
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(j+1, 0, "【经销商编码（DDI平台）】 "+ projectInstitutionCode +" 文件中重复"));
                                        //return combinationResultModel;
                                        isReply = true;
                                        break;
                                    }
                                }
                            }
                            if(isReply){
                                return combinationResultModel;
                            }
                            data.getList().stream().forEach(projectInsImportDTO -> {
                                boolean flag = true;
                                Integer rowIndex = projectInsImportDTO.getRowIndex();
                                //经销商编码（DDI平台）重复校验 提示校验
                                String projectInstitutionCode = projectInsImportDTO.getProjectInstitutionCode();
                                if(StringUtil.isNotBlank(projectInstitutionCode)){
                                    Example example = new Example(ProjectInstitution.class);
                                    example.createCriteria().andEqualTo("projectInstitutionCode",projectInstitutionCode)
                                    .andEqualTo("projectId",projectId)
                                    .andEqualTo("deleted",false)
                                    .andEqualTo("disabled",false);
                                    int cnt = projectInstitutionMapper.selectCountByExample(example);
                                    if(cnt>0){
                                        MessageUtils.addErrorMessage(combinationResultModel.warn,new Message(rowIndex,0,"当前项目下【经销商编码（DDI平台）】 "+projectInstitutionCode+" 系统中已存在"));
                                        flag = true;
                                    }
                                }
                                String projectInstitutionName = projectInsImportDTO.getProjectInstitutionName();
                                if(StringUtil.isNotBlank(projectInstitutionName)){
                                    Example example = new Example(ProjectInstitution.class);
                                    example.createCriteria().andEqualTo("projectInstitutionName",projectInstitutionName)
                                            .andEqualTo("projectId",projectId)
                                            .andEqualTo("deleted",false)
                                            .andEqualTo("disabled",false);
                                    int cnt = projectInstitutionMapper.selectCountByExample(example);
                                    if(cnt>0){
                                        MessageUtils.addErrorMessage(combinationResultModel.warn,new Message(rowIndex,1,"当前项目下【经销商名称（DDI平台）】 "+projectInstitutionName+" 系统中已存在"));
                                        flag = true;
                                    }
                                }
                                //经销商编码(数据中心) 是否存在，阻断校验
                                String institutionCode = projectInsImportDTO.getInstitutionCode();
                                if(StringUtil.isNotBlank(institutionCode)){
                                    Example example = new Example(Institution.class);
                                    example.createCriteria().andEqualTo("institutionCode",institutionCode)
                                            .andEqualTo("deleted",false)
                                            .andEqualTo("disabled",false);
                                    int cnt = institutionMapper.selectCountByExample(example);
                                    if(cnt == 0){
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 2, "【经销商编码（数据中心）】"+institutionCode+"系统内不存在"));
                                        flag = false;
                                    }
                                }
                                if (flag) {
                                    list.add(projectInsImportDTO);
                                }
                            });
                            return combinationResultModel;
                        })
                        .startWork().waitResult();
                map.put("fileName", originalFilename);

                return map;
            } catch (Exception e) {
                if (e instanceof CodedException) {
                    throw new ExcelBusinessException(((CodedException) e).getCode(), e.getMessage());
                }
                logger.error("error [{}]", e);
                throw new ExcelBusinessException("", "解析失败");
            }
        });
    }

    @Override
    public Boolean commitExcel(String token, ProjectInsExcelQuery excelModelQuery, UserPO user) throws IOException {
        if(null==excelModelQuery){
            throw new BaseBusinessException("ERROR","上传参数出错！");
        }
        if(StringUtils.isEmpty(excelModelQuery.projectId())){
            throw new BaseBusinessException("ERROR","上传参数出错！");
        }
        final String userId = user.getId();
        final LocalDateTime now = LocalDateTime.now();
        new SimpExcelConsumer<ProjectInsImportDTO>(cacheExcelData,token)
                .objectMapper(objectMapper,ProjectInsImportDTO.class)
                .consumer((list) ->{
                    list.stream().forEachOrdered(projectInsImportDTO -> {
                        ProjectInstitution projectInstitution = new ProjectInstitution();
                        projectInstitution.setProjectId(excelModelQuery.projectId());
                        projectInstitution.setDeleted(false);
                        projectInstitution.setDisabled(false);
                        projectInstitution.setProjectInstitutionCode(projectInsImportDTO.getProjectInstitutionCode());
                        int cnt_projectInsCode = projectInstitutionMapper.selectCount(projectInstitution);

                        ProjectInstitution projectInstitution_Name = new ProjectInstitution();
                        projectInstitution_Name.setProjectId(excelModelQuery.projectId());
                        projectInstitution_Name.setDeleted(false);
                        projectInstitution_Name.setDisabled(false);
                        projectInstitution_Name.setProjectInstitutionName(projectInsImportDTO.getProjectInstitutionName());
                        int cnt_projectInsName = projectInstitutionMapper.selectCount(projectInstitution_Name);

                        if(cnt_projectInsCode>0||cnt_projectInsName>0){
                            if(cnt_projectInsCode>0){
                                ProjectInstitution projectInstitutionUpdate = new ProjectInstitution();
                                projectInstitutionUpdate.setProjectInstitutionCode(projectInsImportDTO.getProjectInstitutionCode());
                                projectInstitutionUpdate.setProjectInstitutionName(projectInsImportDTO.getProjectInstitutionName());
                                projectInstitutionUpdate.setInstitutionId(institutionMapper.selectIdByCode(projectInsImportDTO.getInstitutionCode()));
                                projectInstitutionUpdate.setModifiedTime(now);
                                projectInstitutionUpdate.setModifier(userId);
                                Example exampleUpdate = new Example(ProjectInstitution.class);
                                exampleUpdate.createCriteria().andEqualTo("projectInstitutionCode",projectInsImportDTO.getProjectInstitutionCode())
                                        .andEqualTo("projectId",excelModelQuery.projectId())
                                        .andEqualTo("deleted",false)
                                        .andEqualTo("disabled",false);
                                projectInstitutionMapper.updateByExampleSelective(projectInstitutionUpdate,exampleUpdate);
                            }else{
                                ProjectInstitution projectInstitutionUpdate = new ProjectInstitution();
                                projectInstitutionUpdate.setProjectInstitutionCode(projectInsImportDTO.getProjectInstitutionCode());
                                projectInstitutionUpdate.setProjectInstitutionName(projectInsImportDTO.getProjectInstitutionName());
                                projectInstitutionUpdate.setInstitutionId(institutionMapper.selectIdByCode(projectInsImportDTO.getInstitutionCode()));
                                projectInstitutionUpdate.setModifiedTime(now);
                                projectInstitutionUpdate.setModifier(userId);
                                Example exampleUpdate = new Example(ProjectInstitution.class);
                                exampleUpdate.createCriteria().andEqualTo("projectInstitutionName",projectInsImportDTO.getProjectInstitutionName())
                                        .andEqualTo("projectId",excelModelQuery.projectId())
                                        .andEqualTo("deleted",false)
                                        .andEqualTo("disabled",false);
                                projectInstitutionMapper.updateByExampleSelective(projectInstitutionUpdate,exampleUpdate);
                            }
                        }else {
                            ProjectInstitution projectInstitutionInsert = new ProjectInstitution();
                            projectInstitutionInsert.setId(UUIDUtil.uuid());
                            projectInstitutionInsert.setCreator(userId);
                            projectInstitutionInsert.setCreatedTime(now);
                            projectInstitutionInsert.setProjectInstitutionCode(projectInsImportDTO.getProjectInstitutionCode());
                            projectInstitutionInsert.setProjectInstitutionName(projectInsImportDTO.getProjectInstitutionName());
                            projectInstitutionInsert.setProjectId(excelModelQuery.projectId());
                            projectInstitutionInsert.setInstitutionId(institutionMapper.selectIdByCode(projectInsImportDTO.getInstitutionCode()));
                            projectInstitutionMapper.insertSelective(projectInstitutionInsert);
                        }
                    });
                }).pullAll(2000);
            return Boolean.TRUE;
    }

    @Override
    public void proInsDel(ProjectInstitutionDTO model, String userId) {
        this.checkRelation(model.getId());
        ProjectInstitution projectInstitution = new ProjectInstitution();
        projectInstitution.setId(model.getId());
        projectInstitution.setDeleted(true);
        projectInstitutionMapper.updateByPrimaryKeySelective(projectInstitution);
    }

    private void checkRelation(String projectInstitutionId) {
        Example example = new Example(FileParseLog.class);
        example.createCriteria().andEqualTo("projectInstitutionId",projectInstitutionId)
                .andEqualTo("deleted",false);
        int cnt = fileParseLogMapper.selectCountByExample(example);
        if(cnt>0) throw new BaseBusinessException("ERROR","当前经销商已被引用，不能删除。");
    }


    @Override
    public ResultModel detail(String id) {
        ProjectInstitutionDTO projectInstitutionDTO = projectInstitutionMapper.getById(id);
        ResultModel resultModel = ResultModelBuild.data(projectInstitutionDTO).build();
        resultModel.setPocket(Map.of("currentInstitutionOptions",List.of(
                Map.of("label",projectInstitutionDTO.getInstitutionName(),
                        "value",projectInstitutionDTO.getInstitutionId(),
                        "extra",Map.of("institutionCode",Map.of(
                                "isField",true,"value",projectInstitutionDTO.getInstitutionCode()))))));
        return resultModel;
    }*/
}
