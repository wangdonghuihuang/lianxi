package com.softium.datacenter.paas.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.enums.*;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.common.Constats;
import com.softium.datacenter.paas.api.dto.JobTaskDTO;
import com.softium.datacenter.paas.api.dto.excel.*;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.AsyncTaskService;
import com.softium.datacenter.paas.web.service.FileUploadService;
import com.softium.datacenter.paas.web.utils.DateUtils;
import com.softium.datacenter.paas.web.utils.ToolUtils;
import com.softium.datacenter.paas.web.utils.fileCommon.MyFileUtils;
import com.softium.datacenter.paas.web.utils.fileCommon.MyStringUtil;
import com.softium.datacenter.paas.web.utils.fileCommon.MyZipUtil;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    ExcelHandlerMapper excelHandlerMapper;
    @Autowired
    FieldMappingMapper fieldMappingMapper;
    @Autowired
    FileHandleRuleMapper fileHandleRuleMapper;
    @Autowired
    FileParseLogMapper fileParseLogMapper;
    @Autowired
    AsyncTaskService taskService;
    @Autowired
    PeriodMapper periodMapper;
    @Autowired
    ProjectMapper projectMapper;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    FileParseResultMapper resultMapper;
    @Override
    public ActionResult<String> upload(MultipartFile file, String fileTempPath, List<ExcelTemplate> fieldMappingList,String dataType,String periodId,String template,String collectName) {
            try {
                Map<String, String> analysisParamsByMultipartFileResult = analysisParamsByMultipartFile(file, fileTempPath);
                String fileKey=String.valueOf(System.currentTimeMillis());
                file.transferTo(new File(analysisParamsByMultipartFileResult.get("localFileFull")));
                List<FileList> resultModel = new ArrayList<>();
                if (MyStringUtil.containsIgnoreCase(analysisParamsByMultipartFileResult.get("fileType"), Constats.FILE_TYPE_ZIP)) {
                    //开始解压
                    File path = MyZipUtil.unzip(analysisParamsByMultipartFileResult.get("localFileFull"), analysisParamsByMultipartFileResult.get("localFilePathAndName"));
                    List<File> files = MyFileUtils.loopFiles(path);
                    if(files==null||files.size()==0){
                        throw new BusinessException(new ErrorInfo("FILE_NOT_EXIST","zip包为空"));
                    }
                  /*  files.forEach(tem ->{
                        Map<String, String> fileFormat = analysisParamsByFullName(tem);
                        String fileTypeName=fileFormat.get("fileType").toString();
                        if(!MyStringUtil.strtoLowerCase(fileTypeName).equals(Constats.FILE_TYPE_XLS)&&!MyStringUtil.strtoLowerCase(fileTypeName).equals(Constats.FILE_TYPE_XLSX)){
                            //记录文件表
                            String parseLogId=addParseLogDao(fileFormat.get("fileType"),fileFormat.get("originalFilename"),fileFormat.get("localFilePath"),periodId,collectName,template);
                            //todo  此处增加入库操作，ent_datacenter_file_parse_result记录错误信息
                            addFileResultDao(parseLogId);
                            throw new BusinessException(new ErrorInfo("FILE_FORMAT_ERROR","文件只支持excel格式"));
                        }
                    });*/
                    Iterator<File> stringIterator=files.iterator();
                    while (stringIterator.hasNext()){
                        Map<String, String> fileFormat = analysisParamsByFullName(stringIterator.next());
                        String fileTypeName=fileFormat.get("fileType").toString();
                        if(!MyStringUtil.strtoLowerCase(fileTypeName).equals(Constats.FILE_TYPE_XLS)&&!MyStringUtil.strtoLowerCase(fileTypeName).equals(Constats.FILE_TYPE_XLSX)){
                            //记录文件表
                            String parseLogId=addParseLogDao(fileFormat.get("fileType"),fileFormat.get("originalFilename"),fileFormat.get("localFilePath"),periodId,collectName,template);
                            //todo  此处增加入库操作，ent_datacenter_file_parse_result记录错误信息
                            addFileResultDao(parseLogId);
                            stringIterator.remove();
                        }
                    }
                    //TODO 文件数量放入redis,作为后续递减结束的依据，全部完毕，调用质检,注意在设置同时设置过期时间，以防最终程序错误导致无删除，遗留
                    redisTemplate.opsForValue().set(fileKey, String.valueOf(files.size()),60*10,TimeUnit.SECONDS);
                    files.forEach(item -> {
                        Map<String, String> analysisParamsByFullNameResult = analysisParamsByFullName(item);
                        this.saveFileList(fieldMappingList, analysisParamsByFullNameResult.get("localFileFull"), analysisParamsByFullNameResult.get("localFileName"), analysisParamsByFullNameResult.get("fileType"), analysisParamsByFullNameResult.get("originalFilename"),analysisParamsByFullNameResult.get("localFilePath"),dataType,periodId,template,collectName,fileKey);
                    });
                } else if (MyStringUtil.containsIgnoreCase(analysisParamsByMultipartFileResult.get("fileType"),
                        Constats.FILE_TYPE_XLS) || MyStringUtil.containsIgnoreCase(
                        analysisParamsByMultipartFileResult.get("fileType"), Constats.FILE_TYPE_XLSX)) {
                    redisTemplate.opsForValue().set(fileKey, String.valueOf(1),60*10,TimeUnit.SECONDS);
                    //调用excel处理工具
                    this.saveFileList(fieldMappingList, analysisParamsByMultipartFileResult.get("localFileFull"), analysisParamsByMultipartFileResult.get("localFileName"), analysisParamsByMultipartFileResult.get("fileType"), analysisParamsByMultipartFileResult.get("fullFileName"),analysisParamsByMultipartFileResult.get("localFilePath"),dataType,periodId,template,collectName,fileKey);
                } else {
                    //todo  此处增加入库操作，ent_datacenter_file_parse_result记录错误信息
                    throw new BusinessException(new ErrorInfo("FILE_FORMAT_ERROR","文件只支持excel格式"));
                }
                return new ActionResult<>("上传完成");
            } catch (Exception e) {
                log.info("上传错误:" + e.getMessage());
               throw new BusinessException(new ErrorInfo("ERROR","上传失败"));
            }

    }

    @Override
    public JSONObject queryAllList() {
        List<ExcelTemplate> list = excelHandlerMapper.findByProperty(ExcelTemplate::getTenantId, SystemContext.getTenantId());
        //查询模板表后，若有数据，则继续。无数据，需要查询ent_datacenter_field_mapping表，获取对应配置字段，入库
        if (list == null || list.size() == 0) {
            List<ExcelTemplate> addAllList = new ArrayList<>();
            //查询field_mapping表
            List<FieldMapping> mappingList = fieldMappingMapper.queryAllDataByType(SystemContext.getTenantId());
            if(mappingList==null||mappingList.size()==0){
                throw new BusinessException(new ErrorInfo("field_mapping_not_exist", "字段匹配关系不存在"));
            }
            for (FieldMapping fieldMapping : mappingList) {
                List<String> stringList = Arrays.asList(fieldMapping.getPropertyName().split(","));//数据中心实体表属性名称
                for (int i = 0; i < stringList.size(); i++) {
                    ExcelTemplate excelTemplate = new ExcelTemplate();
                    excelTemplate.setId(UUIDUtils.getUUID());
                    excelTemplate.setHeaderRow(1);
                    excelTemplate.setBusinessValue("sheet");
                    excelTemplate.setBusinseeType(TemplateExcelCode.getType(fieldMapping.getBusinessType()));
                    excelTemplate.setDataType(fieldMapping.getBusinessType());
                    excelTemplate.setColumnExcelName(stringList.get(i));
                    excelTemplate.setColumnTitleName(stringList.get(i));
                    excelTemplate.setCreateBy(SystemContext.getUserId());
                    excelTemplate.setIsDeleted(0);
                    excelTemplate.setTenantId(SystemContext.getTenantId());
                    excelTemplate.setUpdateBy(SystemContext.getUserId());
                    excelTemplate.setVersion(Long.valueOf("1"));
                    excelTemplate.setBusinseeDesc(fieldMapping.getBusinessType());
                    excelTemplate.setId(UUIDUtils.getUUID());
                    excelTemplate.setTemplateType(0);
                    excelTemplate.setSort(i);
                    addAllList.add(excelTemplate);
                }
            }
            //数据封装完毕，入默认模板库
            excelHandlerMapper.insertExcelTemplateList(addAllList);
            //只有为空，封装完数据，需要再次查询结果
            list = excelHandlerMapper.findByProperty(ExcelTemplate::getTenantId, SystemContext.getTenantId());
        }
        Map<String, List<ExcelTemplate>> map = list.stream().collect(Collectors.groupingBy(ExcelTemplate::getBusinseeType));
        JSONObject jsonObject=new JSONObject();
        Map<String,Object> objectMap=new HashMap<>();
        boolean allTitle=true;
        for(Map.Entry<String,List<ExcelTemplate>> listEntry:map.entrySet()){
            List<TemplateEntityDTO> dtos=new ArrayList<>();
            List<TemplateListDto> listDtos=new ArrayList<>();
            TemplateEntityDTO entityDTO=new TemplateEntityDTO();
            List<ExcelTemplate> templateList=listEntry.getValue();
            boolean isAdd=true;
            for(int p=0;p<templateList.size();p++){
                TemplateListDto listDto=new TemplateListDto();
                ExcelTemplate excelTemplate=templateList.get(p);
                if(allTitle){
                    objectMap.put("businessValue",String.valueOf(excelTemplate.getBusinessValue().equals("sheet") ? 0 : 1));
                    objectMap.put("headerRow",excelTemplate.getHeaderRow());
                    allTitle=false;
                }
                if(isAdd){
                    entityDTO.setBusinseeType(TemplateExcelCode.getType(excelTemplate.getBusinseeType()));
                    entityDTO.setDataType(excelTemplate.getDataType());
                    isAdd=false;
                }
                listDto.setColumnExcelName(excelTemplate.getColumnExcelName());
                listDto.setColumnTitleName(excelTemplate.getColumnTitleName());
                listDtos.add(listDto);
                //dtos.add(entityDTO);
                //objectMap.put(excelTemplate.getDataType(),)
            }
            entityDTO.setDataList(listDtos);
            objectMap.put(TemplateExcelCode.getValue(listEntry.getKey()),entityDTO);
        }
        /*ExcelTemplateDTO dto = new ExcelTemplateDTO();

        List<ExcelTemplateDTO.ExcelTemplateTypeDTO> list1 = new ArrayList<>();
        for (Map.Entry<String, List<ExcelTemplate>> mp : map.entrySet()) {
            String key = mp.getKey();
            boolean zhi = true;
            boolean isUpdate = true;
            ExcelTemplateDTO.ExcelTemplateTypeDTO dto1 = dto.new ExcelTemplateTypeDTO();
            List<ExcelTemplateDTO.ExcelTitleNameDTO> templateTypeDTOS = new ArrayList<>();
            List<ExcelTemplate> templatesa = mp.getValue();
            for (ExcelTemplate plate : templatesa) {
                if (isUpdate) {
                    dto.setBusinessValue(String.valueOf(plate.getBusinessValue().equals("sheet") ? 0 : 1));
                    dto.setHeaderRow(plate.getHeaderRow());
                    dto1.setBusinseeType(TemplateExcelCode.getType(plate.getBusinseeType()));
                    dto1.setDataType(plate.getDataType());
                    isUpdate = false;
                }
                ExcelTemplateDTO.ExcelTitleNameDTO dto2 = dto.new ExcelTitleNameDTO();
                dto2.setColumnExcelName(plate.getColumnExcelName());
                dto2.setColumnTitleName(plate.getColumnTitleName());
                templateTypeDTOS.add(dto2);
            }
            dto1.setDataList(templateTypeDTOS);
            list1.add(dto1);
        }
        dto.setTemplateTypeDTOS(list1);
        return dto;*/
        JSONObject itemJsonObj=JSONObject.parseObject(JSON.toJSONString(objectMap));
        return itemJsonObj;
    }

    @Override
    public List<ExcelTemplate> queryColumnByTitleName() {
        return excelHandlerMapper.queryByTitleName(SystemContext.getTenantId());
    }

    /**
     * 人工上传默认模板配置信息
     */
    @Override
    public void saveTemplateConfig(JSONObject object) {
        //解析json串
        List<ExcelTemplate> excelTemplatesList = new ArrayList<>();
        List<ExcelTemplate> allList = excelHandlerMapper.findByProperty(ExcelTemplate::getTenantId, SystemContext.getTenantId());
        Map<String, List<ExcelTemplate>> collect = allList.stream().collect(Collectors.groupingBy((o) -> fetchGroupKey(o)));
        List<ExcelTemplateDTO.ExcelTemplateTypeDTO> list = new ArrayList<>();
        list = (List<ExcelTemplateDTO.ExcelTemplateTypeDTO>) object.get("templateTypeDTOS");
        for (int i = 0; i < list.size(); i++) {
            JSONObject object1 = JSON.parseObject(JSONObject.toJSONString(list.get(i)));
            List<ExcelTemplateDTO.ExcelTitleNameDTO> list1 = (List<ExcelTemplateDTO.ExcelTitleNameDTO>) object1.get("dataList");
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(list1));
            for (int m = 0; m < jsonArray.size(); m++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(m);
                ExcelTemplate template = new ExcelTemplate();
                template.setHeaderRow(Integer.valueOf(object.get("headerRow").toString()));
                String busValue = (Integer.valueOf(object.get("businessValue").toString()) == 0 ? "sheet" : "byDataType");
                template.setBusinessValue(busValue);
                template.setBusinseeType(TemplateExcelCode.getValue(object1.get("businseeType").toString()));
                template.setDataType(object1.get("dataType").toString());
                template.setColumnExcelName(jsonObject2.get("columnExcelName").toString());
                template.setColumnTitleName(jsonObject2.get("columnTitleName").toString());
                template.setCreateBy(SystemContext.getUserId());
                template.setIsDeleted(0);
                template.setTenantId(SystemContext.getTenantId());
                template.setUpdateBy(SystemContext.getUserId());
                template.setVersion(Long.valueOf("1"));
                template.setId(UUIDUtils.getUUID());
                template.setTemplateType(0);
                excelTemplatesList.add(template);
            }
        }
        //校验data_type是否有重复，(对应excel中sheet名)
       Map<String,List<ExcelTemplate>> excelNum = excelTemplatesList.stream().collect(Collectors.groupingBy(e ->e.getDataType()));
        if(excelNum.size()!=list.size()){
            throw new BusinessException(new ErrorInfo("sheet_name_error","sheet名重复"));
        }
        //校验excel字段名称是否有重复
        Map<String,List<ExcelTemplate>> mpList=excelTemplatesList.stream().collect(Collectors.groupingBy(ExcelTemplate::getBusinseeType));
        for(Map.Entry<String,List<ExcelTemplate>> mp:mpList.entrySet()){
            List<ExcelTemplate> templates=mp.getValue();
            List<String> disList= ToolUtils.getDuplicateValue(templates,lst->lst.getColumnExcelName());
            if(disList.size()>0){
                //提示错误信息
                StringBuilder builder=new StringBuilder();
                for(String str:disList){
                    builder.append(str).append(",");
                }
                builder.delete(builder.length()-1,builder.length());
                throw new BusinessException(new ErrorInfo("field_distinct","字段值唯一,"+builder.toString()+"已存在"));
            }
    }
        //查询ent_datacenter_filehandle_rule表，获取配置字段是否必填等信息，校验上传模板中数据是否满足
        List<FileHandleRule> fileNameList = fileHandleRuleMapper.queryFileNameByRequired(SystemContext.getTenantId());
        if(fileNameList==null||fileNameList.size()==0){
            throw new BusinessException(new ErrorInfo("field_requierd","字段默认必填项不存在，请配置"));
        }
        Map<String, List<FileHandleRule>> fileNameMap = fileNameList.stream().collect(Collectors.groupingBy((a) -> fetchGroupByFileHandleName(a)));
        //校验必填是否为空
        for (ExcelTemplate templates : excelTemplatesList) {
            //如果配置表中必填存在，判断column_excel_name是否为空
            if(fileNameMap.containsKey(templates.getColumnTitleName()+TemplateExcelCode.getValue(templates.getBusinseeType()))){
                if(templates.getColumnExcelName()==null|| StringUtils.isEmpty(templates.getColumnExcelName())){
                    throw new BusinessException(new ErrorInfo("field_not_exist",templates.getColumnTitleName()+"字段不能为空"));
                }
            }
        }
        //遍历集合，根据businsee_type，data_type，column_title_name，column_excel_name判断如果存在，则修改，不存在则添加
        for (ExcelTemplate template : excelTemplatesList) {
            //System.out.println(template.getColumnTitleName()+TemplateExcelCode.getValue(template.getBusinseeType()));
            String mapKey = template.getBusinseeType()+template.getColumnTitleName();
            if (collect.containsKey(mapKey)) {//记录存在，修改
                excelHandlerMapper.updateByExcelName(template,SystemContext.getTenantId());
            } else {
                //记录不存在，添加
                excelHandlerMapper.addExcelTemplate(template);
            }
        }
        //excelHandlerMapper.insertExcelTemplateList(excelTemplatesList);
    }

    @Override
    public ActionResult<List<Period>> queryFilePageService() {
        //查询账期表，获取全部账期值
        //List<String> list=periodMapper.queryByPeriodName();
        //TODO  后续需要改为，后端查询全部状态为未封板的数据返回
        List<Period> allList=new ArrayList<>();
        Criteria<Period> periodCriteria=Criteria.from(Period.class);
        periodCriteria.and(Period::getIsSeal, Operator.equal, SealStatus.UnArchive.toString());
        List<Period> list=periodMapper.findByCriteria(periodCriteria);
        for(Period period:list){
            boolean normalDateUtils= DateUtils.compareStrDate(
                    DateUtils.getDate(period.getUploadBeginTime()),DateUtils.getDate(period.getUploadEndTime()),DateUtils.getDate(new Date()));
            //时间加一天
            String objSuppTime=DateUtils.dateToAdd(period.getUploadEndTime(),1,Calendar.DATE);
            Date uplodEndTim=DateUtils.parseDateFromString(objSuppTime);
            //补量采集时间段为上传结束时间+1与补量截止时间
            boolean suppleTime=DateUtils.compareStrDate(
                    DateUtils.getDate(uplodEndTim),DateUtils.getDate(period.getSupplementEndTime()),DateUtils.getDate(new Date()));
            if(period.getIsSeal().equals(SealStatus.UnArchive.toString())){
                if(!normalDateUtils&&!suppleTime){//同时不满足正常采集和补量采集则为不可上传
                    period.setIsSeal("1");
                }else{//符合任一一个则可以上传
                    period.setIsSeal("0");
                }
            }
            allList.add(period);
        }
        //正常采集时间段为上传开始时间与上传结束时间

        return new ActionResult<>(allList);
    }

    private Map<String, String> analysisParamsByMultipartFile(MultipartFile file, String fileDirPath) {
        Long now = System.currentTimeMillis();
        String originalFilename = file.getOriginalFilename();
        String rawFileName = MyStringUtil.subStringBeforeLast(originalFilename, ".");
        String fileType = MyStringUtil.substringAfterLast(originalFilename, ".");
        String localFilePath = MyStringUtil.appendIfMissing(fileDirPath, MyFileUtils.getSeparator());
        String localFileName = rawFileName;
        String localFilePathAndName = localFilePath + localFileName + now;
        String localFileFull = localFilePath + localFileName + now + "." + fileType;
        String fullFileName=localFileName+now+"."+fileType;
        Map<String, String> analysisParamsByMultipartFileResult = new HashMap<>();
        analysisParamsByMultipartFileResult.put("originalFilename", originalFilename);
        analysisParamsByMultipartFileResult.put("rawFileName", rawFileName);
        analysisParamsByMultipartFileResult.put("fileType", fileType);
        analysisParamsByMultipartFileResult.put("localFilePath", localFilePath);
        analysisParamsByMultipartFileResult.put("localFileName", localFileName);
        analysisParamsByMultipartFileResult.put("localFilePathAndName", localFilePathAndName);
        analysisParamsByMultipartFileResult.put("localFileFull", localFileFull);
        analysisParamsByMultipartFileResult.put("fullFileName",fullFileName);
        return analysisParamsByMultipartFileResult;
    }

    private Map<String, String> analysisParamsByFullName(File file) {
        String originalFilename = MyStringUtil.substringAfterLast(file.getPath(), MyFileUtils.getSeparator());
        String rawFileName = MyStringUtil.substringBeforeLast(originalFilename, ".");
        String fileType = MyStringUtil.substringAfterLast(originalFilename, ".");
        String localFilePath = MyStringUtil.appendIfMissing(MyStringUtil.substringBeforeLast(file.getPath(), MyFileUtils.getSeparator()), MyFileUtils.getSeparator());
        String localFileName = rawFileName;
        String localFilePathAndName = localFilePath + localFileName;
        String localFileFull = localFilePathAndName + "." + fileType;
        Map<String, String> analysisFileResult = new HashMap<>();
        analysisFileResult.put("originalFilename", originalFilename);
        analysisFileResult.put("rawFileName", rawFileName);
        analysisFileResult.put("fileType", fileType);
        analysisFileResult.put("localFilePath", localFilePath);
        analysisFileResult.put("localFileName", localFileName);
        analysisFileResult.put("localFilePathAndName", localFilePathAndName);
        analysisFileResult.put("localFileFull", localFileFull);
        return analysisFileResult;
    }

    private String saveFileList(List<ExcelTemplate> fieldMappingList, String localFileFull, String localFileName, String fileType, String originalFilename,String localFilePath,String dataType,String periodId,String template,String collectName,String fileKey) {
        //根据租户id查询项目表获取项目id
        Project projectDTO=projectMapper.findOne(Project::getTenantId,SystemContext.getTenantId());
        //查询模板表，获取业务类型和数据类型
        List<ExcelTemplate> excelTemplates=excelHandlerMapper.queryAllType(SystemContext.getTenantId());
        Map<String,String> typeMap=excelTemplates.stream().collect(Collectors.toMap(ExcelTemplate::getDataType,ExcelTemplate::getBusinseeType));
        //数据组装完毕入库
        //将解析文件封装为parse_log实体对象
        FileParseLog parseLog=new FileParseLog();
        parseLog.setId(UUIDUtils.getUUID());
        parseLog.setFileName(originalFilename);
        parseLog.setProjectInstitutionCode(null);
        parseLog.setProjectInstitutionName(null);
        parseLog.setBusinessType(null);
        parseLog.setAccessType(CommonUtil.ACCESS_TYPE_MANUAL);
        parseLog.setFileStatus(FileParseStatus.WAIT_CHECK.toString());
        parseLog.setFilePath(localFilePath);
        parseLog.setRootPath("/");
        parseLog.setFileLastModifyTime(LocalDateTime.now());
        parseLog.setProjectId(projectDTO.getId());
        //todo 任务id暂时先写死
        parseLog.setNotifyId("12345");
        parseLog.setRowcount(0);
        parseLog.setFileTime(LocalDateTime.now());
        parseLog.setNewDataId(null);
        parseLog.setFileType(fileType);
        parseLog.setDisabled(0);
        parseLog.setTenantId(SystemContext.getTenantId());
        parseLog.setIsDeleted(0);
        parseLog.setVersion(0L);
        parseLog.setCreateBy(SystemContext.getUserId());
        parseLog.setUploadType(collectName);
        parseLog.setPeriodId(periodId);
        parseLog.setTemplateType(TemplateExcelCode.getType(template));
        parseLog.setUploadPeopleName(SystemContext.getUserName());
        fileParseLogMapper.singleAddParseLogData(parseLog);
        /*todo 0.1版本采用异步内处理文件入子表库，但是不满足，当上传完成后。
           文件管理查看，这时候子表没有内容，这是为了统一文件管理界面，保证id为子表id,
           为了其他功能按钮能不在关联父表子表id再去关联数据表，可以在这里直接将文件入库，只是类型和数量什么都没有，状态为待校验，
           后面线程中去修改文件字段值*/
        //入库后，异步执行文件解析
        ExcelJobDTO dto=new ExcelJobDTO();
        dto.setParseLogId(parseLog.getId());
        dto.setFieldMappingList(fieldMappingList);
        dto.setLocalFileFull(localFileFull);
        dto.setLocalFileName(localFileName);
        dto.setFileType(fileType);
        dto.setOriginalFilename(originalFilename);
        dto.setLocalFilePath(localFilePath);
        dto.setBusinessType(parseLog.getBusinessType());
        dto.setCollectType(parseLog.getAccessType());
        dto.setUploadType(parseLog.getUploadType());
        dto.setProjectInstitutionName(parseLog.getProjectInstitutionName());
        dto.setProjectInstitutionCode(parseLog.getProjectInstitutionCode());
        dto.setPeriodId(periodId);
        dto.setTenantId(parseLog.getTenantId());
        dto.setVersion(parseLog.getVersion());
        dto.setCreateBy(parseLog.getCreateBy());
        dto.setUploadDataType(dataType);
        dto.setDataTypeMap(typeMap);
        dto.setProjectId(projectDTO.getId());
        dto.setFileKey(fileKey);
        /*ExecutorService executorService= Executors.newFixedThreadPool(10);
        executorService.execute(new ExcelFileParseJob(dto));*/
        JobTaskDTO jobTaskDTO=new JobTaskDTO();
        jobTaskDTO.setJobContext("excelParse");
        taskService.asyncFtpProcess(jobTaskDTO,dto);
        //taskService.asyncExcelFile(dto);
       /* Map<String, List<FieldMapping>> businessMap = new HashMap<>();
        //Map<Integer, List<User>> map = list.stream().collect(Collectors.groupingBy(User::getId));
        businessMap = fieldMappingList.stream().collect(Collectors.groupingBy(FieldMapping::getBusinessType));*/
        //DataHandleExcelUtils.readExcel(file.getInputStream(), fileType, businessMap, false);
        return "上传成功";
    }

    private String fetchGroupKey(ExcelTemplate template) {
        /**todo 默认模板配置表中，具有不可修改唯一性的两个值  ，用来做是否存在，修改还是添加操作*/
        return template.getBusinseeType()  + template.getColumnTitleName();
    }
    private String fetchGroupByFileHandleName(FileHandleRule handleRule){
        return handleRule.getFieldName()+handleRule.getBusinessType();
    }
    /**封装入文件表*/
    public String addParseLogDao(String fileType,String originalFilename,String localFilePath,String periodId,String collectName,String template){
        //根据租户id查询项目表获取项目id
        Project projectDTO=projectMapper.findOne(Project::getTenantId,SystemContext.getTenantId());
        FileParseLog parseLog=new FileParseLog();
        parseLog.setId(UUIDUtils.getUUID());
        parseLog.setFileName(originalFilename);
        parseLog.setProjectInstitutionCode(null);
        parseLog.setProjectInstitutionName(null);
        parseLog.setBusinessType(null);
        parseLog.setAccessType(CommonUtil.ACCESS_TYPE_MANUAL);
        parseLog.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
        parseLog.setFilePath(localFilePath);
        parseLog.setRootPath("/");
        parseLog.setFileLastModifyTime(LocalDateTime.now());
        parseLog.setProjectId(projectDTO.getId());
        parseLog.setNotifyId("12345");
        parseLog.setRowcount(0);
        parseLog.setFileTime(LocalDateTime.now());
        parseLog.setNewDataId(null);
        parseLog.setFileType(fileType);
        parseLog.setDisabled(0);
        parseLog.setTenantId(SystemContext.getTenantId());
        parseLog.setIsDeleted(0);
        parseLog.setVersion(0L);
        parseLog.setCreateBy(SystemContext.getUserId());
        parseLog.setUploadType(collectName);
        parseLog.setPeriodId(periodId);
        parseLog.setTemplateType(TemplateExcelCode.getType(template));
        parseLog.setUploadPeopleName(SystemContext.getUserName());
        fileParseLogMapper.singleAddParseLogData(parseLog);
        return parseLog.getId();
    }
    /**封装入文件校验日志表*/
    public  void addFileResultDao(String logId){
        FileParseResult fileParseResult = new FileParseResult();
        fileParseResult.setId(UUIDUtils.getUUID());
        fileParseResult.setFileParseLogId(logId);
        fileParseResult.setStatus(FileParseResultStatus.FAILURE.toString());
        fileParseResult.setStatusRemark(FileParseResultStatusRemark.FILE_TYPE_ERROR.toString());
        fileParseResult.setCreateBy(SystemContext.getUserId());
        fileParseResult.setTenantId(SystemContext.getTenantId());
        fileParseResult.setCreateTime(new Date());
        resultMapper.addDataByFileType(fileParseResult);
    }
}
