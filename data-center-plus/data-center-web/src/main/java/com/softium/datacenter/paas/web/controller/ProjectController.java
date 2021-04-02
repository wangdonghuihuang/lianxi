package com.softium.datacenter.paas.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.ProjectDTO;
import com.softium.datacenter.paas.api.dto.init.TenantInitDTO;
import com.softium.datacenter.paas.api.dto.query.ProjectQuery;
import com.softium.datacenter.paas.web.service.ProjectService;
import com.softium.framework.common.dto.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: york
 * @create: 2020-07-31 13:30
 **/
@RestController
@RequestMapping("projectManagement")
@Slf4j
public class ProjectController {

    @Autowired
    private ProjectService projectService;
/*    @Autowired
    CacheExcelData cacheExcelData;*/
    @Autowired
    ObjectMapper objectMapper;

    //load && search 合并接口为search
    //@SpecialPocket({SpecialRegionProvince.class, RoleStatusPocket.class, BusinessTypeValuePocket.class})
/*    @GetMapping("load")
    public ActionResult<PageInfo<List<ProjectDTO>>> load() {
        return list(null);
    }*/
    @GetMapping("initResource")
    public ActionResult initResource(){
        return new ActionResult(projectService.initResource());
    }

    @PostMapping("initDateCenter")
    public ActionResult initDateCenter(@RequestBody TenantInitDTO tenantInitDTO){
        return new ActionResult(projectService.initDateCenter(tenantInitDTO));
    }

    @PostMapping("search")
    public ActionResult<PageInfo<List<ProjectDTO>>> search(@RequestBody ProjectQuery projectQuery) {
        return list(projectQuery);
    }

    private ActionResult<PageInfo<List<ProjectDTO>>> list(ProjectQuery projectQuery) {
        List<ProjectDTO> projectDTOList = projectService.load(projectQuery);
        PageInfo<List<ProjectDTO>> pageInfo = new PageInfo(projectDTOList);
        pageInfo.setPageSize(projectQuery.getPageSize());
        pageInfo.setPageNum(projectQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    @GetMapping("detail")
    public ActionResult<ProjectDTO> detail(@RequestParam String id) {
        return new ActionResult<>(projectService.getById(id));
    }


    /**
     * @description 项目信息更新接口
     * @param projectDTO
     * @return
     */
    @PostMapping("projectUpdate")
    public ActionResult<PageInfo<List<ProjectDTO>>> projectUpdate(@RequestBody ProjectDTO projectDTO) {
        projectService.projectUpdate(projectDTO);
        ProjectQuery projectQuery = new ProjectQuery();
        projectQuery.setCurrent(1);
        projectQuery.setPageSize(6);
        return this.list(projectQuery);
    }

    /**
     * @description 项目添加接口
     * @param projectDTO
     * @return
     */
    @PostMapping("projectInsert")
    public ActionResult<PageInfo<List<ProjectDTO>>> projectInsert(@RequestBody ProjectDTO projectDTO) {
        projectService.projectInsert(projectDTO);
        ProjectQuery projectQuery = new ProjectQuery();
        projectQuery.setCurrent(1);
        projectQuery.setPageSize(6);
        return this.list(projectQuery);
    }

   /* @GetMapping("proInsList/load")
    public ActionResult<PageInfo<List<ProjectInstitutionDTO>>> proInsLoad(@RequestParam(value = "id") String id){
        ProjectQuery projectQuery = new ProjectQuery();
        projectQuery.setId(id);
        projectQuery.setCurrent(1);
        projectQuery.setPageSize(10);
        return listProIns(projectQuery);
    }*/

   /* private ActionResult<PageInfo<List<ProjectInstitutionDTO>>> listProIns(ProjectQuery projectQuery) {
        List<ProjectInstitutionDTO> list = projectService.loadProIns(projectQuery);
        PageInfo<List<ProjectInstitutionDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageSize(projectQuery.getPageSize());
        pageInfo.setPageNum(projectQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }*/

   /* @PostMapping("proInsList/search")
    public ActionResult<PageInfo<List<ProjectInstitutionDTO>>> proInsSearch(@RequestBody ProjectQuery projectQuery){
        return listProIns(projectQuery);
    }*/

    @GetMapping("fuzzyQuery")
    public ActionResult fuzzyQuery(@RequestParam(value = "keyWords") String keyWords){
        return new ActionResult(projectService.fuzzyQuery(keyWords));
    }

    @GetMapping("fuzzyQueryAll")
    public ActionResult fuzzyQueryAll(){
        return new ActionResult(projectService.fuzzyQuery());
    }

/*    @PostMapping("proIns/insert")
    public ActionResult<PageInfo<List<ProjectInstitutionDTO>>> proInsInsert(@RequestBody ProjectInstitutionDTO projectInstitutionDTO) {
        ProjectQuery projectQuery = new ProjectQuery();
        projectQuery.setId(projectInstitutionDTO.getProjectId());
        projectQuery.setCurrent(1);
        projectQuery.setPageSize(10);
        projectService.proInsInsert(projectInstitutionDTO);
        return listProIns(projectQuery);
    }*/

    /*private Pagination<Project> list(PageRequest<Project> pageRequest) {
         return projectService.load(pageRequest);
    }

    @GetMapping("proIns/detail")
    public ResultModel proInsDetail(@RequestParam(value = "id") String id){
        return projectService.detail(id);
    }

    @PostMapping("proIns/update")
    public ResultModel proInsUpdate(@RequestBody EditAndAddCriteriaModel<ProjectInstitutionDTO, Object> request, Principal principal) {
        projectService.proInsUpdate(request.getModel(),getCurrentUser(principal).getId());
        return this.proInsLoad(request.getModel().getProjectId());
    }

    @PostMapping("proIns/delete")
    public ResultModel proInsDel(@RequestBody EditAndAddCriteriaModel<ProjectInstitutionDTO, ProjectQuery> request, Principal principal) {
        projectService.proInsDel(request.getModel(),getCurrentUser(principal).getId());
        return this.proInsLoad(request.getModel().getProjectId());
    }

    @PostMapping("template/download")
    public CompletableFuture<ResponseEntity<byte[]>> export() {
        List<ProjectInsExportDTO> list = new ArrayList<>();
        String filename = "DistributorMappingTemplate" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        return CompletableFuture.supplyAsync(() -> downloadExcel(list, ProjectInsExportDTO.class, filename, "sheet"));
    }

    @PostMapping("import")
    public CompletableFuture<Map> upload(MultipartFile fileName, String projectId) throws IOException {
        if (fileName == null || fileName.getInputStream() == null) {
            throw new BaseBusinessException("", "请选择上传文件");
        }
        InputStream inputStream = fileName.getInputStream();
        return projectService.upload(inputStream,fileName.getOriginalFilename(),projectId);
    }

    *//**
     * 下载错误信息
     * @param response
     * @param excelModelQuery
     * @throws IOException
     *//*
    @PostMapping("download/error")
    public void downloadUploadExcelData(HttpServletResponse response, @RequestBody ExcelModelQuery excelModelQuery) throws IOException{
        //创建
        DownLoadFlow downLoadFlow = DownLoadFlow.init(response.getOutputStream()).clazz(ProjectInsImportDTO.class);
        // 设置token 和token 存储
        downLoadFlow.cacheExcel(excelModelQuery.errorToken(), cacheExcelData, 10000);
        // 设置请求头事件
        downLoadFlow.event(() -> setExcelContentType(response, "DistributorMappingTemplateError"));
        // ObjectMapper
        downLoadFlow.objectMapper(objectMapper);
        //执行
        downLoadFlow.customizeDealWith().downLoad();
        // 刷新流
        downLoadFlow.finish();
    }

    @PostMapping("import/commit")
    public CompletableFuture commit(@RequestBody ProjectInsExcelQuery excelModelQuery, Principal principal) {
        UserPO user = getCurrentUser(principal);
        if (excelModelQuery == null) {
            throw new BaseBusinessException("ERROR","上传参数出错,请重新上传！");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String token = excelModelQuery.dataConvertToken();
                return projectService.commitExcel(token, excelModelQuery, user);
            } catch (Exception e) {
                log.error("error",e);
                throw new RuntimeException();
            }
        });

    }
*/
}
