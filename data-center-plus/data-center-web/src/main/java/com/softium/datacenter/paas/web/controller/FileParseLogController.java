package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.dto.query.FileParseLogQuery;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.mapper.FileParseLevelMapper;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.service.FileParseLevelService;
import com.softium.datacenter.paas.web.service.FileParseLogService;
import com.softium.datacenter.paas.web.service.FileParseResultService;
import com.softium.datacenter.paas.web.service.OriginDataService;
import com.softium.datacenter.paas.web.utils.ToolUtils;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * @author huashan.li
 */
@RestController
@RequestMapping("fileParseLog")
public class FileParseLogController {

    @Autowired
    private FileParseLogService fileParseLogService;
    @Autowired
    private FileParseResultService fileParseResultService;
    @Autowired
    private OriginDataService originDataService;
    @Autowired
    private FileParseLevelService fileParseLevelService;
    @Autowired
    private FileParseLevelMapper fileParseLevelMapper;
    /**
     * 文件管理界面初始化接口
     * 初始化加载查询fileparselog表
     */
    @GetMapping("load")
    public ActionResult<PageInfo<List<FileManagementDTO>>> load() {
        FileParseLogQuery fileParseLogQuery = new FileParseLogQuery();
        fileParseLogQuery.setCurrent(1);
        fileParseLogQuery.setPageSize(20);
        return levelList(fileParseLogQuery);
    }
    /**
     * 查询主文件记录日志表
     */
    private ActionResult list(FileParseLogQuery pageModel) {
        List<FileManagementDTO> list = fileParseLogService.list(pageModel);
        for(FileManagementDTO dto:list){
            if(CommonUtil.ACCESS_TYPE_MANUAL.equals(dto.getAccessType())){//如果是人工上传
                //0不带加号，1带加号，int值默认是0，所以只需要在需要显示加号，置为1
                dto.setIsOpen(1);
            }
        }
        PageInfo<List<FileManagementDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageNum(pageModel.getCurrent());
        pageInfo.setSize(pageModel.getPageSize());
        return new ActionResult<>(pageInfo);
    }

    /**
     * 查询子文件日志记录表
     */
    private ActionResult levelList(FileParseLogQuery logQuery) {
        List<FileManagementDTO> list = fileParseLevelService.levelList(logQuery);
        for(FileManagementDTO dto:list){
            dto.setIsButton(1);
        }
        PageInfo<List<FileManagementDTO>> pageInfo = new PageInfo(list);
        pageInfo.setPageNum(logQuery.getCurrent());
        pageInfo.setSize(logQuery.getPageSize());
        return new ActionResult<>(pageInfo);
    }
/**初始化加载查询*/
private ActionResult loadList(FileParseLogQuery logQuery) {
    PageHelper.startPage(logQuery.getCurrent(),logQuery.getPageSize(),true);
    List<FileManagementDTO> list = fileParseLevelMapper.queryDataCondition(logQuery, SystemContext.getTenantId());
    for(FileManagementDTO dto:list){
        if(CommonUtil.ACCESS_TYPE_MANUAL.equals(dto.getAccessType())){//如果是人工上传
            //0不带加号，1带加号，int值默认是0，所以只需要在需要显示加号，置为1
            dto.setIsOpen(1);
        }
    }
    PageInfo<List<FileManagementDTO>> pageInfo = new PageInfo(list);
    pageInfo.setPageNum(logQuery.getCurrent());
    pageInfo.setSize(logQuery.getPageSize());
    return new ActionResult<>(pageInfo);
}
    @PostMapping("/search")
    public ActionResult<PageInfo<List<FileManagementDTO>>> search(@RequestBody FileParseLogQuery pageModel) {
        /*todo 文件管理需注意，在没有任何参数表示加载页面，此时需要查询父表，而且数据后面操作按钮只有两个，原始文件下载，删除
        *  当展开二级列表后，操作按钮为四个: 数据查看  删除  质检报告下载  文件校验结果查看
        * 以上则决定了，删除接口需要判断下是父表还是子表id,其他接口不需要，要不只处理父表id,要不只处理子表id
        * 原始文件下载功能，全部是下载整个文件，不存在只下载某个经销商的原始文件*/
       /* if ((pageModel.getProjectInstitutionName() != null && StringUtils.isNotEmpty(pageModel.getProjectInstitutionName()))
                || (pageModel.getBusinessDesc()!= null && StringUtils.isNotEmpty(pageModel.getBusinessDesc()))) {
            //如果经销商不为空 ,业务类型不为空，子表去掉分组，查询全部
            return levelList(pageModel);
        }else{
            //新版，全部查子表，只是根据条件，文件名则分组等等
            return loadList(pageModel);
        }*/
        boolean isAllNull= ToolUtils.checkBeanIsNull(pageModel);
        if(isAllNull){
            //全部为空表示加载初始化页面
            return list(pageModel);
        }else{
            //有任何参数查询都是查询子表
            return levelList(pageModel);
        }
        //旧版查主表，子表
        /*if ((pageModel.getProjectInstitutionName() != null && StringUtils.isNotEmpty(pageModel.getProjectInstitutionName()))
                || (pageModel.getBusinessType() != null && StringUtils.isNotEmpty(pageModel.getBusinessType()))) {
            //如果经销商不为空，查询需要查询子表  ,如果业务类型不为空，也需要查询子表
            return levelList(pageModel);
        } else {
            return list(pageModel);
        }*/

    }

    /**
     * 文件二级下拉展示接口
     */
    @GetMapping("/searchFile")
    public ActionResult<List<FileManagementDTO>> searchFileById(@RequestParam("fileId") String fileId) {
        List<FileManagementDTO> dtos = fileParseLevelService.queryByIdList(fileId);
        return new ActionResult<>(dtos);
    }

  /*  @PostMapping("/delete")
    public ActionResult deleteFile(@RequestBody FileParseDTO fileParseDTO) {
        fileParseLogService.deleteFile(fileParseDTO.getId(), fileParseDTO.getIsDeleted());
        return null;
    }*/
    /**文件管理删除接口*/
    @PostMapping("delete")
    public ActionResult deleteFile(@RequestBody FileParseDTO fileParseDTO){
        //数据删除 未封板的原始数据、核查数据、交付数据
        fileParseLogService.deleteFile(fileParseDTO.getId(),fileParseDTO.getIsDeleted(),fileParseDTO.getIsOpen());
        return this.load();
    }
    /**
     * 文件校验结果查看接口
     */
    @GetMapping("/getFileParseResult")
    public ActionResult getFileParseResult(@RequestParam("fileId") String fileId) {
        //传入参数是子表id,获取父表id
        String filecode=fileParseLevelMapper.queryParseLogId(fileId,SystemContext.getTenantId());
        List<FileParseResultDTO> fileParseResults = fileParseResultService.getFileId(filecode);
        return new ActionResult(fileParseResults);
    }

    @GetMapping("/detail")
    public ActionResult<SaleDataDTO> detail(@RequestParam String fileId) {
        OriginDataQuery originDataQuery = new OriginDataQuery();
        originDataQuery.setFileId(fileId);
        List<SaleDataDTO> list = originDataService.saleList(originDataQuery);
        SaleDataDTO saleDataDTO = list.get(0);
        return new ActionResult<>(saleDataDTO);
    }
    /**原始文件下载接口*/
    @PostMapping("originData/download")
    public Object export(@RequestBody CommonQuery commonQuery) {
        Map<String, Object> error = new HashMap<>(2);
        try {
            FileDownLoadDTO fileDownLoadDTO = fileParseLogService.downloadFileById(commonQuery);
            return CompletableFuture.supplyAsync(() -> downloadExcel(fileDownLoadDTO.getFileByte(), fileDownLoadDTO.getFileName()));
        } catch (Exception e) {
            error.put("message", e.getMessage());
            e.printStackTrace();
        }
        return error;
    }

    /**
     * 质检报告下载
     *
     * @return
     */
    @PostMapping("report/download")
    public Object reportDownload(HttpServletResponse response,@RequestBody CommonQuery commonQuery) {
        Map<String, Object> error = new HashMap<>(2);
        try {
            if(commonQuery.getAccessType().equals(CommonUtil.ACCESS_TYPE_MANUAL)){
                //人工上传
                FileDownLoadDTO fileDownLoadDTO = fileParseLogService.downManualReport(commonQuery,response);
                return CompletableFuture.supplyAsync(() -> downloadExcel(fileDownLoadDTO.getFileByte(), fileDownLoadDTO.getFileName()));
            }else{
                //todo ddi暂时保持原代码不变,后续也需要改变为依据子表id关联查询源数据
                FileDownLoadDTO fileDownLoadDTO = fileParseLogService.downloadReport(commonQuery);
                 CompletableFuture.supplyAsync(() -> downloadExcel(fileDownLoadDTO.getFileByte(), fileDownLoadDTO.getFileName()));
            }

        } catch (Exception e) {
            error.put("message", e.getMessage());
            e.printStackTrace();
        }
        return error;
    }

    protected ResponseEntity<byte[]> downloadExcel(byte[] bytes, String fileName) {
        return responseEntity(bytes, httpHeaders(fileName), HttpStatus.OK);
    }

    /**
     * 生成下载
     *
     * @param body
     * @param httpHeaders
     * @param statusCode
     * @return
     */
    protected ResponseEntity<byte[]> responseEntity(byte[] body, HttpHeaders httpHeaders, HttpStatus statusCode) {
        return new ResponseEntity<>(body, httpHeaders, statusCode);
    }

    /**
     * 配置文件名称
     *
     * @param fileName
     * @return
     */
    protected HttpHeaders httpHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set("filename", fileName);
        // 设置文件名
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return headers;

    }
}