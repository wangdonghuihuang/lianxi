package com.softium.datacenter.paas.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.entity.ExcelTemplate;
import com.softium.datacenter.paas.api.entity.FieldMapping;
import com.softium.datacenter.paas.api.entity.FileHandleRule;
import com.softium.datacenter.paas.api.entity.Period;
import com.softium.datacenter.paas.api.enums.FileParseStatus;
import com.softium.datacenter.paas.api.enums.SealStatus;
import com.softium.datacenter.paas.api.mapper.ExcelHandlerMapper;
import com.softium.datacenter.paas.api.mapper.FieldMappingMapper;
import com.softium.datacenter.paas.api.mapper.FileHandleRuleMapper;
import com.softium.datacenter.paas.api.mapper.PeriodMapper;
import com.softium.datacenter.paas.web.service.FileUploadService;
import com.softium.datacenter.paas.web.utils.DateUtils;
import com.softium.datacenter.paas.web.utils.ToolUtils;
import com.softium.datacenter.paas.web.utils.poi.ExeclDownloadUtil;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传处理接口层*
 *
 * @author: wyb
 * @create: 2020-11-03
 */
@RestController
@RequestMapping("fileHandleProcess")
@Slf4j
public class FileHandleController {
    @Value("${xsk.filePathDir}")
    private String fileDirPath;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    FieldMappingMapper fieldMappingMapper;
    @Autowired
    PeriodMapper periodMapper;
    @Autowired
    FileHandleRuleMapper handleRuleMapper;
    @Autowired
    ExcelHandlerMapper excelHandlerMapper;
    /**
     * 文件上传接口
     * @param file 上传文件
     * @param dataType 日数据，月数据 对应(1和0)
     * @param periodId 账期名称(暂时是模拟的几个日期字段)
     * @param template 上传模板 ("0","默认模板"  "1","经销商模板")
     */
    @ResponseBody
    @RequestMapping(value = "fileUpload", consumes = {"multipart/form-data"})
    public ActionResult<String> fileUploadController(@ModelAttribute MultipartFile file,@ModelAttribute ("dataType") String dataType,
                                                     @ModelAttribute("periodId") String periodId,@ModelAttribute("template") String template) throws UnsupportedEncodingException {
        if (file.isEmpty() || file == null) {
            throw new BusinessException(new ErrorInfo("ERROR", "请选择上传文件"));
        }
        //List<FieldMapping> fieldMappingList = fieldMappingMapper.findByProperty(FieldMapping::getTenantId, SystemContext.getTenantId());
        List<ExcelTemplate> fieldMappingList= excelHandlerMapper.findByProperty(ExcelTemplate::getTenantId,SystemContext.getTenantId());
        if (null == fieldMappingList || fieldMappingList.size() == 0) {
            throw new BusinessException(new ErrorInfo("field_mapping_not_exist", "字段匹配关系不存在"));
        }
        //根据归属账期名称查询period表
        log.info("periodId: {},encode:{}", periodId, ToolUtils.getEncoding(periodId));
        String convertPeriodId = null;
        if(ToolUtils.getEncoding(periodId).equals("ISO-8859-1")) {
            convertPeriodId = new String(periodId.getBytes("iso-8859-1"), "utf-8");
            log.info("convert to: {}", convertPeriodId);
        } else {
            convertPeriodId = periodId;
        }
        List<Period> periods=periodMapper.findByProperty(Period::getPeriodName,convertPeriodId);
        Period period=new Period();
        for(Period iod:periods){
            period=iod;
            break;
        }
        //正常采集时间段为上传开始时间与上传结束时间
        boolean normalDateUtils=DateUtils.compareStrDate(
                DateUtils.getDate(period.getUploadBeginTime()),DateUtils.getDate(period.getUploadEndTime()),DateUtils.getDate(new Date()));
        //时间加一天
        String objSuppTime=DateUtils.dateToAdd(period.getUploadEndTime(),1,Calendar.DATE);
        Date uplodEndTim=DateUtils.parseDateFromString(objSuppTime);
        //补量采集时间段为上传结束时间+1与补量截止时间
        boolean suppleTime=DateUtils.compareStrDate(
                DateUtils.getDate(uplodEndTim),DateUtils.getDate(period.getSupplementEndTime()),DateUtils.getDate(new Date()));
        String collectName="";
        //如果正常，补量任何一个满足，并且账期等于未封板，数据可以上传，否则不可以
        if(SealStatus.UnArchive.toString().equals(period.getIsSeal())){
            if(!normalDateUtils&&!suppleTime){
                throw new BusinessException(new ErrorInfo("period_not_exit", "账期已封板，不可上传"));
            }else{
                if(normalDateUtils){
                    collectName= FileParseStatus.NORMAL_COLLECTION.toString();//正常采集
                }else if(suppleTime){
                    collectName=FileParseStatus.SUPPLEMENT_CONNECTION.toString();//补量采集
                }
            }
        }else{
            throw new BusinessException(new ErrorInfo("period_not_exit", "账期已封板，不可上传"));
        }
       /* if((normalDateUtils||suppleTime)&&period.getIsSeal().equals(0)){
            if(normalDateUtils){
                collectName= FileParseStatus.NORMAL_COLLECTION.toString();//正常采集
            }else if(suppleTime){
                collectName=FileParseStatus.SUPPLEMENT_CONNECTION.toString();//补量采集
            }
        }else {
            throw new BusinessException(new ErrorInfo("period_not_exit", "账期已封板，不可上传"));
        }*/
//查询模板表，获取当前配置的所有类型的模板配置，后面excel解析，需要判断sheet名是否存在于配置表
        return fileUploadService.upload(file, fileDirPath, fieldMappingList,dataType,period.getId(),template,collectName);
    }

    /**
     * 上传文件初始加载接口
     */
    @GetMapping("filepageload")
    public ActionResult<List<Period>> loadFilePage() {
        return fileUploadService.queryFilePageService();
    }

    /**
     * 上传模板配置接口
     */
    @PostMapping("uploadTemplate")
    public ActionResult uploadExcelTemplate(@RequestBody JSONObject templateDTO) {
        JSONObject object = templateDTO;
        fileUploadService.saveTemplateConfig(object);
        return new ActionResult(true, "success");
    }

    /**
     * 上传模板页面加载接口
     */
    @GetMapping("templateLoad")
    public ActionResult<JSONObject> uploadTemplateLoad() {
        JSONObject list = fileUploadService.queryAllList();
        return new ActionResult<>(list);
    }

    /**
     * 下载模板接口
     */
    @GetMapping("download")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        List<ExcelTemplate> templates = fileUploadService.queryColumnByTitleName();
        if(templates==null||templates.size()==0){
            throw new BusinessException(new ErrorInfo("down_template_not_exist", "模板不存在，请于列配置优先配置匹配映射"));
        }
        String fileName = "dataTemplate" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Map<String,String> matchTypeMap=new HashMap<>();
        for (ExcelTemplate template : templates) {
            list.add(template.getDataType());
            map.put(template.getDataType(), template.getColumnExcelName());
            matchTypeMap.put(template.getDataType(),template.getBusinseeDesc());
        }
        //查询获取列规则配置必填字段
        List<FileHandleRule> fileNameList = handleRuleMapper.queryFileNameByRequired(SystemContext.getTenantId());
        Map<String,List<FileHandleRule>> typeMap=fileNameList.stream().collect(Collectors.groupingBy((itm) -> groupingBybusType(itm)));
        XSSFWorkbook hssfWorkbook = new XSSFWorkbook();
        OutputStream out = response.getOutputStream();
        export(hssfWorkbook, out, response, list, fileName, map,typeMap,matchTypeMap);
    }

    public void export(XSSFWorkbook workbook, OutputStream out, HttpServletResponse resp, List<String> list, String fileName, Map<String, String> map,Map<String,List<FileHandleRule>> typeMap,Map<String,String> matchTypeMap) {
        try {
            String[] headers = new String[50];
            String typeKey="";
            //map是循环条件  循环一次一个sheet
            for (int i = 0; list.size() > i; i++) {//循环输出多个 sheet,单个 去掉循环即可
                for (Map.Entry<String, String> mp : map.entrySet()) {
                    if (mp.getKey().equals(list.get(i))) {
                        headers = map.get(list.get(i)).split(",");
                        typeKey=mp.getKey();
                    }
                }
                //获取当前业务类型所配置的全部必填字段
                List<ExcelTemplate> nameList=excelHandlerMapper.queryAllColumnName(typeKey);
                Map<String,String> nameMap=new HashMap<>();
                for(ExcelTemplate template:nameList){
                    nameMap.put(template.getColumnExcelName(),template.getColumnTitleName());
                }
                List<FileHandleRule> fileNameList =typeMap.get(matchTypeMap.get(typeKey));
                Map<String,List<FileHandleRule>> requireMap=fileNameList.stream().collect(Collectors.groupingBy((itm) -> groupingByFiledName(itm)));
               //在此处根据类型查询数据库，获取对应类型的数据中心字段，文件配置的字段，作为后续模板标红条件
                ExeclDownloadUtil.exportExcel(workbook, i, list.get(i), null, out, headers, null,requireMap,nameMap);
              /*  resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                // 解决导出文件名中文乱码
                resp.setCharacterEncoding("UTF-8");
                //resp.setHeader("Content-Disposition","attachment;filename="+new String(fileName.getBytes("UTF-8"),"ISO-8859-1")+".xlsx");
                resp.setHeader("filename", new String((fileName + ".xlsx").getBytes(),"UTF-8"));
                resp.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(fileName+".xlsx","UTF-8"));
                resp.setStatus(200);*/
                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
                resp.setHeader("filename", new String((fileName + ".xlsx").getBytes(),"UTF-8"));
                resp.setStatus(200);
                resp.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=" + URLEncoder.encode(fileName+".xlsx","UTF-8"));
            }
            workbook.write(out);
            out.flush();
            workbook.close();
        } catch (Exception e) {
            log.error("上传模板下载出错:{}", e.getMessage());
            throw new BusinessException(new ErrorInfo("ERROR", "下载失败"));
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Exception e) {
                log.error("下载模板流关闭异常:" + e.getMessage());
            }

        }
    }
    private String groupingByFiledName(FileHandleRule handleRule){
        return handleRule.getFieldName();
    }
    private String groupingBybusType(FileHandleRule handleRule){
        return handleRule.getBusinessType();
    }
}
