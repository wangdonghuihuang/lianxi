package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.SealStatus;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.web.automap.DataParseLogAutoMap;
import com.softium.datacenter.paas.web.automap.methodEnum;
import com.softium.datacenter.paas.web.common.ConstantCacheMap;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.dto.query.FileParseLogQuery;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.FileParseResultStatus;
import com.softium.datacenter.paas.api.enums.FileParseResultStatusRemark;
import com.softium.datacenter.paas.api.enums.FileParseStatus;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.FileParseLogService;
import com.softium.datacenter.paas.web.utils.GenerateFileUtil;
import com.softium.datacenter.paas.web.utils.poi.ExportExcelUtils;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Fanfan.Gong
 **/
@Slf4j
@Service
public class FileParseLogServiceImpl implements FileParseLogService {
    @Autowired
    private FileParseLogMapper fileParseLogMapper;
    @Autowired
    private FileParseResultMapper fileParseResultMapper;
    @Autowired
    private ParseLogMapper parseLogMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private OriginInventoryMapper originInventoryMapper;
    @Autowired
    private OriginPurchaseMapper originPurchaseMapper;
    @Autowired
    private OriginSaleMapper originSaleMapper;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    FileParseLevelMapper fileParseLevelMapper;
    @Autowired
    ExcelHandlerMapper excelHandlerMapper;
    @Autowired
    InspectSaleMapper inspectSaleMapper;
    @Autowired
    PeriodMapper periodMapper;
    @Autowired
    private InspectInventoryMapper inspectInventoryMapper;
    @Autowired
    private InspectPurchaseMapper inspectPurchaseMapper;
    private static final Integer BATCH_SIZE = 100;

    @Override
    public List<FileParseDTO> listSuccessFileParseLog(String projectId) {
        return fileParseLogMapper.listSuccessFileParseLog(projectId, SystemContext.getTenantId());
    }

    @Override
    public List<FileParseDTO> listErrorFileParseLog(String projectId) {
        return fileParseLogMapper.listErrorFileParseLog(projectId, SystemContext.getTenantId());
    }

    @Override
    public void batchInsert(List<FileParseDTO> fileParseList) {
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(fileParseList)) {
            int size = fileParseList.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > fileParseList.size()) {
                    end = fileParseList.size();
                }
                fileParseLogMapper.batchInsertFileParseLog(fileParseList.subList(start, end));
            }
        }
    }

    @Override
    public void handleRepeatFile(Map<String, String> needDeleteIds, String userId) {
        List<FileParseResult> fileParseResults = new ArrayList<>(needDeleteIds.size());
        needDeleteIds.forEach((k, v) -> {
            fileParseLogMapper.updateDelete(k, v, userId,SystemContext.getTenantId());
            FileParseResult fileParseResult = new FileParseResult();
            fileParseResult.setId(UUID.randomUUID().toString());
            fileParseResult.setFileParseLogId(k);
            fileParseResult.setStatus(FileParseResultStatus.FAILURE.toString());
            fileParseResult.setStatusRemark(FileParseResultStatusRemark.FILE_REPEAT_AND_PARSE_NEW_FILE.toString());
            fileParseResult.setCreateBy(userId);
            fileParseResult.setTenantId(SystemContext.getTenantId());
            fileParseResult.setCreateTime(new Date());
            fileParseResults.add(fileParseResult);
        });
        if (fileParseResults.size() > 0) {
            fileParseResultMapper.batChInsertFileParse(fileParseResults);
        }
    }

    @Override
    public void updateFileStatus(FileParseDTO fileParseLog) {
        fileParseLogMapper.updateFileStatus(fileParseLog,SystemContext.getTenantId());
    }

    @Override
    public void insert(FileParseDTO fileParse) {
        List<FileParseDTO> fileParseLogs = new ArrayList<>(1);
        fileParseLogs.add(fileParse);
        fileParseLogMapper.batchInsertFileParseLog(fileParseLogs);
    }

    @Override
    public void updateStatus(List<String> pendingFileLogIds, String userId, String status) {
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(pendingFileLogIds)) {
            int size = pendingFileLogIds.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > size) {
                    end = size;
                }
                fileParseLogMapper.updateStatus(pendingFileLogIds.subList(start, end), userId, status);
            }
        }
    }

    @Override
    public List<FileManagementDTO> list(FileParseLogQuery fileParseLogQuery) {
        PageHelper.startPage(fileParseLogQuery.getCurrent(),fileParseLogQuery.getPageSize(),true);
        return fileParseLogMapper.getList(fileParseLogQuery,SystemContext.getTenantId());
    }

    @Override
    public void deleteFile(String id, Integer isDeleted,int isOpen) {
        if(isDeleted == 1){
            throw new BusinessException(new ErrorInfo("ERROR","该文件及数据已被删除"));
        }
        //todo 删除需要判断是父表id还是子表id
        FileParseLog parseLog=fileParseLogMapper.findOne(FileParseLog::getId,id);
        if(parseLog!=null){
            if(parseLog.getFileStatus().equals(FileParseStatus.FILE_PARSE_ERROR.toString())){
                fileParseLogMapper.delete(id);
                throw new BusinessException(new ErrorInfo("ERROR","此文件为非规定格式，已删除"));
            }
            List<FileParseLevel> levels=fileParseLevelMapper.findByProperty(FileParseLevel::getParseLogId,parseLog.getId());
            List<String> stringList=fileParseLevelMapper.queryIdByParseId(parseLog.getId(),SystemContext.getTenantId());
            Period period=periodMapper.findOne(Period::getId,parseLog.getPeriodId());
            if(period.getIsSeal().equals(SealStatus.UnArchive.toString())){
                //执行删除语句，父表状态
                fileParseLogMapper.delete(id);
                if(stringList!=null&&stringList.size()>0){
 /*todo 由父表id删除，不需再根据业务类型判断删除某个表，而是对应文件，文件中可能会有多个类型的数据，全部需要删除
                父表id,数据删除-未封板的原始数据、核查数据、交付数据*/
                    Criteria<FileParseLevel> criteria = Criteria.from(FileParseLevel.class);
                    criteria.and(FileParseLevel::getId, Operator.in,stringList);
                    fileParseLevelMapper.deleteByCriteria(criteria);
                    //删除销售源数据表
                    Criteria<OriginSale> saleCriteria = Criteria.from(OriginSale.class);
                    saleCriteria.and(OriginSale::getFileId,Operator.in,stringList);
                    originSaleMapper.deleteByCriteria(saleCriteria);
                    //删除库存源数据表
                    Criteria<OriginInventory> invenCriteria = Criteria.from(OriginInventory.class);
                    invenCriteria.and(OriginInventory::getFileId,Operator.in,stringList);
                    originInventoryMapper.deleteByCriteria(invenCriteria);
                    //删除采购源数据表
                    Criteria<OriginPurchase> purCriteria = Criteria.from(OriginPurchase.class);
                    purCriteria.and(OriginPurchase::getFileId,Operator.in,stringList);
                    originPurchaseMapper.deleteByCriteria(purCriteria);
                    //删除销售落地表
                    Criteria<InspectSale> insaleCriteriaCriteria = Criteria.from(InspectSale.class);
                    insaleCriteriaCriteria.and(InspectSale::getFileId,Operator.in,stringList);
                    inspectSaleMapper.deleteByCriteria(insaleCriteriaCriteria);
                    //删除库存落地表
                    Criteria<InspectInventory> invenCriteriaCriteria = Criteria.from(InspectInventory.class);
                    invenCriteriaCriteria.and(InspectInventory::getFileId,Operator.in,stringList);
                    inspectInventoryMapper.deleteByCriteria(invenCriteriaCriteria);
                    //删除采购落地表
                    Criteria<InspectPurchase> inpurCriteriaCriteria = Criteria.from(InspectPurchase.class);
                    inpurCriteriaCriteria.and(InspectPurchase::getFileId,Operator.in,stringList);
                    inspectPurchaseMapper.deleteByCriteria(inpurCriteriaCriteria);
                }
                /*for(int i=0;i<levels.size();i++){
                    //是否封版 0 未封版 1已封板
                        if(levels.get(i).getBusinessDesc().equals("SM")){
                            //删除子表
                            Criteria<FileParseLevel> criteria = Criteria.from(FileParseLevel.class);
                            criteria.and(FileParseLevel::getId, Operator.in,stringList);
                            fileParseLevelMapper.deleteByCriteria(criteria);
                            //删除源数据表
                            Criteria<OriginSale> saleCriteria = Criteria.from(OriginSale.class);
                            saleCriteria.and(OriginSale::getFileId,Operator.in,stringList);
                            originSaleMapper.deleteByCriteria(saleCriteria);
                            //删除核查表
                            Criteria<InspectSale> inspectCriteriaCriteria = Criteria.from(InspectSale.class);
                            inspectCriteriaCriteria.and(InspectSale::getFileId,Operator.in,stringList);
                            inspectSaleMapper.deleteByCriteria(inspectCriteriaCriteria);
                        }
                }*/
            }
        }else{
            //根据id获取类型，根据类型查询删除对应数据表
            FileParseLevel levelId=fileParseLevelMapper.findOne(FileParseLevel::getId,id);
            Period period=periodMapper.findOne(Period::getId,levelId.getPeriodId());
            if(period.getIsSeal().equals(SealStatus.UnArchive.toString())){
                /*if(levelId.getBusinessDesc().equals("SM")){
                    //子表删除，只删除此id对应的经销商的数据
                    //执行删除语句，父表状态,子表只删除这个经销商的，父表文件不修改状态
                    //fileParseLogMapper.delete(levelId.getParseLogId());
                    //子表状态
                    fileParseLevelMapper.delete(id);
                    Criteria<OriginSale> criteria = Criteria.from(OriginSale.class);
                    criteria.and(OriginSale::getFileId,Operator.equal,id);
                    originSaleMapper.deleteByCriteria(criteria);
                    //核查落地表状态
                    Criteria<InspectSale> inspectSaleCriteria=new Criteria<>();
                    inspectSaleCriteria.and(InspectSale::getFileId,Operator.equal,id);
                    inspectSaleMapper.deleteByCriteria(inspectSaleCriteria);
                }*/
                //todo  此处使用适配枚举类执行对应方法
                switch (levelId.getBusinessDesc()){
                    case  "SM":
                        methodEnum.SM.handleMethod(id);
                        break;
                    case  "SD":

                        break;
                    case  "PM":
                        methodEnum.PM.handleMethod(id);
                        break;
                    case  "PD":

                        break;
                    case  "IM":
                        methodEnum.IM.handleMethod(id);
                        break;
                    case  "ID":

                        break;
                }
            }
        }
        /*//根据id获取类型，根据类型查询删除对应数据表
        FileParseLevel levelId=fileParseLevelMapper.findOne(FileParseLevel::getId,id);
        Period period=periodMapper.findOne(Period::getId,levelId.getPeriodId());
        //是否封版 0 未封版 1已封板
        if(period.getIsSeal().equals("0")){
            //todo  后续需要增加八个类型对应
            if(levelId.getBusinessDesc().equals("SM")){
                //0不带加号，只需要根据id查询出对应条数据关联数据表
                if(isOpen==0){
                    //执行删除语句，父表状态
                    fileParseLogMapper.delete(levelId.getParseLogId());
                    //子表状态
                    fileParseLevelMapper.delete(id);
                    Criteria<OriginSale> criteria = Criteria.from(OriginSale.class);
                    criteria.and(OriginSale::getFileId,Operator.equal,id);
                    originSaleMapper.deleteByCriteria(criteria);
                    //核查落地表状态
                    Criteria<InspectSale> inspectSaleCriteria=new Criteria<>();
                    inspectSaleCriteria.addCriterion(new Condition("file_id",Operator.equal,id));
                    inspectSaleMapper.deleteByCriteria(inspectSaleCriteria);

                }else {
                    //1带加号，外层数据删除，则需要拿到此id对应的父id，再次拿到全部此父id的全部子表数据，多个id关联数据表，删除多个数据
                    List<String> stringList=fileParseLevelMapper.queryIdByParseId(levelId.getParseLogId());
                    //执行删除语句，父表状态
                    fileParseLogMapper.delete(levelId.getParseLogId());
                    //删除子表
                    Criteria<FileParseLevel> criteria = Criteria.from(FileParseLevel.class);
                    criteria.and(FileParseLevel::getId, Operator.in,stringList);
                    fileParseLevelMapper.deleteByCriteria(criteria);
                    //删除元数据表
                    Criteria<OriginSale> saleCriteria = Criteria.from(OriginSale.class);
                    saleCriteria.and(OriginSale::getFileId,Operator.in,stringList);
                    originSaleMapper.deleteByCriteria(saleCriteria);
                    //删除核查表
                    Criteria<InspectSale> inspectCriteriaCriteria = Criteria.from(InspectSale.class);
                    inspectCriteriaCriteria.and(InspectSale::getFileId,Operator.in,stringList);
                    inspectSaleMapper.deleteByCriteria(inspectCriteriaCriteria);
                }
            }
        }*/
    }

    @Override
    public FileDownLoadDTO downloadFileById(CommonQuery commonQuery) {
        FileParseLog parseLog=fileParseLogMapper.findOne(FileParseLog::getId,commonQuery.getFileId());
        if(parseLog==null){
            String filecode=fileParseLevelMapper.queryParseLogId(commonQuery.getFileId(),SystemContext.getTenantId());
            parseLog=fileParseLogMapper.getById(filecode);
        }
        //String filecode=fileParseLevelMapper.queryParseLogId(commonQuery.getFileId());
        //FileParseLog parseLog=fileParseLogMapper.getById(commonQuery.getFileId());
        String fileName=parseLog.getFileName();
        String filePath=parseLog.getFilePath()+fileName;
        FileDownLoadDTO fileDownLoadDTO = new FileDownLoadDTO();
        //截取文件名，上传有时间戳命名，以最后一个.结束位置开始，往前截取13位
        //fileName=fileName.substring(0,fileName.lastIndexOf(".")-13)+fileName.substring(fileName.lastIndexOf("."),fileName.length());
        try {
            File file=new File(filePath);
            InputStream inputStream=new FileInputStream(file);
            fileDownLoadDTO.setFileByte(CommonUtil.toByteArray(inputStream));
            fileName=URLEncoder.encode(fileName,"UTF-8");
        }catch (IOException e){
            log.error("下载文件异常:"+e.getMessage());
        }
        fileDownLoadDTO.setFileName(fileName);
        return fileDownLoadDTO;
    }

    @Override
    public FileDownLoadDTO downloadReport(CommonQuery commonQuery) {
        FileDownLoadDTO fileDownLoadDTO = new FileDownLoadDTO();
        FileParseLog fileParseLog = fileParseLogMapper.getById(commonQuery.getFileId());
        if(StringUtil.isBlank(fileParseLog.getBusinessType())){
            throw new BusinessException(new ErrorInfo("ERROR","业务类型为空无法下载质检结果"));
        }
        //错误信息原始表头
        List<Map<String,Object>> templateList = templateMapper.getTemplateType(fileParseLog.getBusinessType());
        Map<String,String> templateMap = new LinkedHashMap<>();
        for(Map map : templateList){
            templateMap.put(map.get("ckey").toString(),map.get("cvalue").toString());
        }
        //判断有没有特殊经销商字段映射配置配置
        int n=0;
        if(StringUtil.isNotBlank(fileParseLog.getProjectInstitutionCode())){
            n=fieldMappingMapper.countInstitution(fileParseLog.getProjectId(),fileParseLog.getProjectInstitutionCode(),fileParseLog.getBusinessType(),SystemContext.getTenantId());
        }
        CommonQuery common = new CommonQuery();
        if(n>0){
            common.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
        }
        common.setProjectId(fileParseLog.getProjectId());
        common.setBusinessType(fileParseLog.getBusinessType());
        List<FieldDTO> fieldDTOS = fieldMappingMapper.getFields(common);
        List heads = new ArrayList();//原表头处理
        for (String key : templateMap.keySet()){
            for(FieldDTO fieldDTO : fieldDTOS){
                if(key.equals(fieldDTO.getPropertyName())){
                    templateMap.put(key,fieldDTO.getTitleName());
                }
            }
            heads.add(templateMap.get(key));
        }
        heads.add("错误描述");
        //文件中数据质检错误信息
        List<DataParseLogDTO> dataParseLogDTOS = null;
        List<List> dataLogs;
        if("SD".equals(fileParseLog.getBusinessType())){
            String originSaleName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
            dataParseLogDTOS = parseLogMapper.getFileSaleDataLog(originSaleName,commonQuery.getFileId());
            //销售内容
            List<SaleDataDTO> saleDataDTOS = originSaleMapper.getOriginSaleFileId(originSaleName,fileParseLog.getId(),SystemContext.getTenantId());
            for (DataParseLogDTO dataParseLogDTO : dataParseLogDTOS) {
                for (SaleDataDTO saleDataDTO : saleDataDTOS) {
                    if(saleDataDTO.getRowNum().equals(dataParseLogDTO.getRowNum())){
                        if(StringUtil.isBlank(saleDataDTO.getFailCause())){
                            saleDataDTO.setFailCause(dataParseLogDTO.getJudgeMsg());
                        }else {
                            saleDataDTO.setFailCause(saleDataDTO.getFailCause() + ";" + dataParseLogDTO.getJudgeMsg());
                        }
                    }
                }
            }
            //原始数据及错误日志
            dataLogs = DataParseLogAutoMap.saleDataLog(saleDataDTOS);
        }
        //库存内容
        else if("ID".equals(fileParseLog.getBusinessType())){
            String tableNameByValue = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
            dataParseLogDTOS = parseLogMapper.getFileInventoryDataLog(tableNameByValue,commonQuery.getFileId());
            List<InventoryDataDTO> inventoryDataDTOS = originInventoryMapper.getFileId(tableNameByValue,fileParseLog.getId(),SystemContext.getTenantId());
            for (DataParseLogDTO dataParseLogDTO : dataParseLogDTOS) {
                for (InventoryDataDTO inventoryDataDTO : inventoryDataDTOS) {
                    if(inventoryDataDTO.getRowNum().equals(dataParseLogDTO.getRowNum())){
                        if(StringUtil.isBlank(inventoryDataDTO.getFailCause())){
                            inventoryDataDTO.setFailCause(dataParseLogDTO.getJudgeMsg());
                        }else {
                            inventoryDataDTO.setFailCause(inventoryDataDTO.getFailCause() + ";" + dataParseLogDTO.getJudgeMsg());
                        }
                    }
                }
            }
            //原始数据及错误日志
            dataLogs = DataParseLogAutoMap.inventoryDataLog(inventoryDataDTOS);
        }
        //采购内容
        else if("PD".equals(fileParseLog.getBusinessType())){
            String tableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
            dataParseLogDTOS = parseLogMapper.getFilePurchaseDataLog(tableName,commonQuery.getFileId());
            List<PurchaseDataDTO> purchaseDataDTOS = originPurchaseMapper.getFileId(tableName,fileParseLog.getId(),SystemContext.getTenantId());
            for (DataParseLogDTO dataParseLogDTO : dataParseLogDTOS) {
                for (PurchaseDataDTO purchaseDataDTO : purchaseDataDTOS) {
                    if(purchaseDataDTO.getRowNum().equals(dataParseLogDTO.getRowNum())){
                        if(StringUtil.isBlank(purchaseDataDTO.getFailCause())){
                            purchaseDataDTO.setFailCause(dataParseLogDTO.getJudgeMsg());
                        }else {
                            purchaseDataDTO.setFailCause(purchaseDataDTO.getFailCause() + ";" + dataParseLogDTO.getJudgeMsg());
                        }
                    }
                }
            }
            //原始数据及错误日志
            dataLogs = DataParseLogAutoMap.purchaseDataLog(purchaseDataDTOS);
        }else {
            throw new BusinessException(new ErrorInfo("ERROR","未识别到此业务类型"));
        }
        String fileName = "[质检报告]"+fileParseLog.getFileName().replace(".csv",".xls");
        try {
            File file = GenerateFileUtil.makeTempCSV("datalogDownload",fileName,heads,dataLogs);
            FileInputStream fis = new FileInputStream(file);
            fileDownLoadDTO.setFileByte(CommonUtil.toByteArray(fis));
            fileName = URLEncoder.encode(fileName,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileDownLoadDTO.setFileName(fileName);
        return fileDownLoadDTO;
    }

    @Override
    public FileDownLoadDTO downManualReport(CommonQuery commonQuery,HttpServletResponse response) throws IOException {
        FileDownLoadDTO fileDownLoadDTO = new FileDownLoadDTO();
        FileParseLevel fileParseLevel = fileParseLevelMapper.findOne(FileParseLevel::getId, commonQuery.getFileId());
        if (fileParseLevel == null||
                (!fileParseLevel.getFileStatus().equals(FileParseStatus.QUALITY_FAILURE.toString())
                        &&!fileParseLevel.getFileStatus().equals(FileParseStatus.QUALITY_SUCCESS.toString())))
                 {
            throw new BusinessException(new ErrorInfo("ERROR", "质检报告不存在"));
        }
        if(StringUtil.isBlank(fileParseLevel.getBusinessDesc())){
            throw new BusinessException(new ErrorInfo("ERROR","业务类型为空无法下载质检结果"));
        }
        //String busType= TemplateExcelCode.getType(fileParseLevel.getBusinessType());
        String fileName="质检报告"+fileParseLevel.getFileName();
        String originName ="";
        if(fileParseLevel.getBusinessDesc().equals("SD")){

        }else if(fileParseLevel.getBusinessDesc().equals("SM")){
            originName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
            List<Map<String, Object>> dataList=originSaleMapper.queryQualityReport(originName,commonQuery.getFileId(),SystemContext.getTenantId());
            ArrayList<String> titleKeyList=ConstantCacheMap.saleMonthtitleKeyList;
            Map<String,String> map=ConstantCacheMap.saleMonthcolumnMap;
            File file=ExportExcelUtils.expoerDataExcel(titleKeyList,map,dataList,fileName);
            FileInputStream fis = new FileInputStream(file);
            fileDownLoadDTO.setFileByte(CommonUtil.toByteArray(fis));
            fileName = URLEncoder.encode(fileName,"UTF-8");
            fileDownLoadDTO.setFileName(fileName);
        }else if(fileParseLevel.getBusinessDesc().equals("PD")){

        }else if(fileParseLevel.getBusinessDesc().equals("PM")){
            originName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
            List<Map<String, Object>> dataList=originPurchaseMapper.queryPurseReport(originName,commonQuery.getFileId(),SystemContext.getTenantId());
            ArrayList<String> titleKeyList=ConstantCacheMap.purMonthtitleKeyList;
            Map<String,String> map=ConstantCacheMap.purseMonthcolumnMap;
            File file=ExportExcelUtils.expoerDataExcel(titleKeyList,map,dataList,fileName);
            FileInputStream fis = new FileInputStream(file);
            fileDownLoadDTO.setFileByte(CommonUtil.toByteArray(fis));
            fileName = URLEncoder.encode(fileName,"UTF-8");
            fileDownLoadDTO.setFileName(fileName);
        }else if(fileParseLevel.getBusinessDesc().equals("ID")){

        }else if(fileParseLevel.getBusinessDesc().equals("IM")){

        }else if(fileParseLevel.getBusinessDesc().equals("DD")){

        }else if(fileParseLevel.getBusinessDesc().equals("DM")){

        }
        return fileDownLoadDTO;
    }
}
