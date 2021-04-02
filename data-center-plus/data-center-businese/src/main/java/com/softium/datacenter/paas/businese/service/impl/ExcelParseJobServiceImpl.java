package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.mapper.ExcelHandlerMapper;
import com.softium.datacenter.paas.api.mapper.FileParseLevelMapper;
import com.softium.datacenter.paas.api.mapper.FileParseLogMapper;
import com.softium.datacenter.paas.api.mapper.FileParseResultMapper;
import com.softium.datacenter.paas.api.utils.ListMapCommonUtils;
import com.softium.datacenter.paas.web.automap.OriginInventoryAutoMap;
import com.softium.datacenter.paas.web.automap.OriginPurchaseAutoMap;
import com.softium.datacenter.paas.web.automap.OriginSaleAutoMap;
import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.FileParseResultStatus;
import com.softium.datacenter.paas.api.enums.FileParseResultStatusRemark;
import com.softium.datacenter.paas.api.enums.FileParseStatus;
import com.softium.datacenter.paas.web.utils.DataHandleExcelUtils;
import com.softium.datacenter.paas.web.utils.MapCache;
import com.softium.datacenter.paas.web.service.*;
import com.softium.framework.common.SystemContext;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
/**解析excel文件入库执行任务*/
@Slf4j
@Service
public class ExcelParseJobServiceImpl implements ExcelParseJobService {
    @Autowired
    FileParseLevelMapper fileParseLevelMapper;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    OriginPurchaseService originPurchaseService;
    @Autowired
    OriginInventoryService originInventoryService;
    @Autowired
    OriginSaleService originSaleService;
    @Autowired
    FileParseLogMapper fileParseLogMapper;
    @Autowired
    ExcelHandlerMapper excelHandlerMapper;
    @Autowired
    FileParseResultMapper fileParseResultMapper;
    @Autowired
    FileParseLevelService fileParseLevelService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private RinseMessageService rinseMessageService;

    @Override
    public void judgeExcelFileData(ExcelJobDTO jobDTO) {
        AtomicInteger atomicInteger=new AtomicInteger(0);
        String tenantId = jobDTO.getTenantId();
        String userId = jobDTO.getCreateBy();
        SystemContext.setTenantId(tenantId);
        SystemContext.setUserId(userId);
        Map<String, List<ExcelTemplate>> businessMap = new HashMap<>();
        List<String> fileParseLogId=new ArrayList<>();
        Integer allNum=0;
        businessMap = jobDTO.getFieldMappingList().stream().collect(Collectors.groupingBy(ExcelTemplate::getBusinseeDesc));
        try {
            Map<String,List<Map<String, Object>>> allDataMap=new HashMap<>();
            List<Future<Integer>> allStr=new ArrayList<>();
            List<String> allIdList=new CopyOnWriteArrayList<>();
            StopWatch stopWatch=new StopWatch();
            stopWatch.start();


            log.info("解析excel开始");
            allDataMap= DataHandleExcelUtils.readExcel(jobDTO, businessMap, false,jobDTO.getDataTypeMap());
            stopWatch.stop();
            log.info("解析excel结束,耗时:{}秒",stopWatch.getTotalTimeSeconds());
            //根据文件id,从缓存获取sheet名对应关系数据
            Map<String,String>  sheetNameMap= (Map<String, String>) MapCache.get(jobDTO.getParseLogId());
            if(allDataMap.size()>0){
                //遍历map，根据类型获取对应业务数据
                for(Map.Entry<String,List<Map<String, Object>>> mp:allDataMap.entrySet()){
                    String key=mp.getKey();
                    List<Map<String, Object>> dataList=mp.getValue();
                    if(dataList!=null&&dataList.size()>0){
                        if(key.equals("SD")){
                            CompletableFuture<Integer> future1=CompletableFuture.supplyAsync(()->{
                                return insertOriginSaleData(key,dataList,jobDTO,atomicInteger,allIdList,sheetNameMap);
                            });
                            allStr.add(future1);
                        }else if(key.equals("PD")){
                            CompletableFuture<Integer> future2=CompletableFuture.supplyAsync(()->{
                                return insertPurchaseSaleData(key,dataList,jobDTO,atomicInteger,allIdList,sheetNameMap);
                            });
                            allStr.add(future2);
                        }else if(key.equals("ID")){
                            CompletableFuture<Integer> future3=CompletableFuture.supplyAsync(()->{
                                return insertInspectSaleData(key,dataList,jobDTO,atomicInteger,allIdList,sheetNameMap);
                            });
                            allStr.add(future3);
                        }
                    }

                   /* switch (key)
                    {
                        *//*此处判断类型源头，是模板配置表中类型和sheet名的映射，目前只有四个销 采 库 发概念，不对应日，月
                        * 日月分别入不同库，可在此大类型内部再用jobDTO中uploadDataType字段做区分*//*
                        case "PD":
                            insertPurchaseSaleData(key,dataList,jobDTO,atomicInteger);
                            break;
                        case "ID":
                            insertInspectSaleData(key,dataList,jobDTO,atomicInteger);
                            break;
                        case "SD":
                            insertOriginSaleData(key,dataList,jobDTO,atomicInteger);
                            break;
                        case "DD":

                            break;
                    }*/
                }
                for(int i=0;i<allStr.size();i++){
                    allNum=allStr.get(i).get();
                }
                if(allNum>0){
                    //全部执行完毕，回改parse_log表文件信息
                    fileParseLogId.add(jobDTO.getParseLogId());
                    fileParseLogMapper.updateStatusAndRowNum(fileParseLogId,SystemContext.getUserId(),FileParseStatus.QUALITY_PENDING.toString(),allNum,tenantId);
                    //全部结束，获取redis中值，以1递减,最终为0表示全部文件入库结束，调用任务通知质检
                    redisTemplate.boundValueOps(jobDTO.getFileKey()).increment(-1);
                    String fileNum=redisTemplate.opsForValue().get(jobDTO.getFileKey());
                    if(Integer.parseInt(fileNum)==0){
                        //将全部子表id作为参数通知任务调度
                        //System.out.println("共"+allIdList.size()+"个子表集合");
                        //删除redis中key
                        redisTemplate.delete(jobDTO.getFileKey());
                        //清除map中本地缓存
                        MapCache.remove(jobDTO.getParseLogId());
                        //todo  调用任务系统传参
                        rinseMessageService.sendFileHandleMsg(tenantId, userId, jobDTO, allIdList);
                    }else {
                        //递减逐次清除文件表id为key的缓存
                        MapCache.remove(jobDTO.getParseLogId());
                    }
                }else {
                    //全部解析为空，清空map缓存
                    MapCache.remove(jobDTO.getParseLogId());
                    //回改file_parse_log和子表文件状态为质检成功
                    fileParseLogMapper.updateFileStatusByLogId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.QUALITY_SUCCESS.toString(),tenantId);
                    //回改子表状态
                    fileParseLevelMapper.updateStatusByParseId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.QUALITY_SUCCESS.toString(),tenantId);
                }
            }else{
                //全部解析为空，清空map缓存
                MapCache.remove(jobDTO.getParseLogId());
                //回改file_parse_log和子表文件状态为质检成功
                fileParseLogMapper.updateFileStatusByLogId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.QUALITY_SUCCESS.toString(),tenantId);
                //回改子表状态
                fileParseLevelMapper.updateStatusByParseId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.QUALITY_SUCCESS.toString(),tenantId);
            }
        }catch (Exception e){
            log.error("异步解析文件错误:"+e.getMessage());
            //回改file_parse_log和子表文件状态为上传失败
            fileParseLogMapper.updateFileStatusByLogId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.UPLOAD_FAIL.toString(),tenantId);
            //回改子表状态
            fileParseLevelMapper.updateStatusByParseId(jobDTO.getParseLogId(),jobDTO.getCreateBy(),FileParseStatus.UPLOAD_FAIL.toString(),tenantId);
            //记录错误信息入表
            addFileResult(jobDTO);
            //异常清除缓存
            MapCache.remove(jobDTO.getParseLogId());
        }
    }

    /**批量入库销售源数据*/
    public Integer insertOriginSaleData(String busineType,List<Map<String, Object>> orgList,ExcelJobDTO jobDTO,AtomicInteger atomicInteger,List<String> idList,Map<String,String> nameMap){
        List<OriginSale> originSaleList = new ArrayList<>(orgList.size());
        //listmap分组
        Map<String,List<Map<String,Object>>> list=orgList.stream().collect(Collectors.groupingBy(e->e.get("经销商名称").toString()));
        //再次封装待入源数据表数据，此时key为入库后的字表id
        Map<String,List<Map<String,Object>>> dataMap=new HashMap<>();
        List<String> fileParseLogId=new ArrayList<>();
        List<String> fileLevelId=new ArrayList<>();
        List<FileParseLevel> parseLevelList=new ArrayList<>();
        //key为经销商名称，value为每个经销商下的数据
        //入库子表，字表只存当前sheet页名称即为业务类型，经销商名称，每个经销商数据总行数，parse_log表id,采集方式，文件校验状态，上传类型，账期id
        //状态为解析中
        for(Map.Entry<String,List<Map<String, Object>>> entry:list.entrySet()){
            String key=entry.getKey();
            List<Map<String,Object>> oblist=entry.getValue();
            //按照经销商名称分组，需要经销商编码入库
            String insCode="";
            for(int p=0;p<oblist.size();p++){
                boolean isContain=ListMapCommonUtils.macthMapStr(oblist.get(p),"经销商代码");
                if(isContain){
                    insCode=String.valueOf(oblist.get(p).get("经销商代码"));
                    break;
                }
            }
            /*for(int p=0;p<oblist.size();p++){
                if(!insCode.equals("")){
                    break;
                }
                Map<String,Object> mp=oblist.get(p);
               for(Map.Entry<String,Object> nmap:mp.entrySet()){
                    if(nmap.getKey().equals("经销商代码")&&(nmap.getValue()!=null&& StringUtils.isNotEmpty(String.valueOf(nmap.getValue())))){
                        insCode=String.valueOf(nmap.getValue());
                        break;
                    }
               }
            }*/
            /*Map<String,List<Map<String,Object>>> codelist=oblist.stream().collect(Collectors.groupingBy(e->e.get("经销商代码").toString()));
            for(Map.Entry<String,List<Map<String,Object>>> mp:codelist.entrySet()){
                insCode=mp.getKey();
                break;
            }*/
            //System.out.println("key为:"+key+"-----数量为:"+oblist.size());
            //封装ent_datacenter_fileparse_level数据，执行inset入库，拿到入库后的主键id
            FileParseLevel level=new FileParseLevel();
            level.setId(UUIDUtils.getUUID());
            //todo  暂时标记,后续看是否直接将此字段值改为月销售，日销售这样的类型
            level.setBusinessType(busineType);
            if(jobDTO.getUploadDataType().equals("0")){
                //月数据
                level.setBusinessDesc("SM");
            }else{
                //日数据
                level.setBusinessDesc("SD");
            }
            level.setSheetName(nameMap.get(busineType));
            level.setAccessType(jobDTO.getCollectType());
            level.setFileStatus(FileParseStatus.WAIT_CHECK.toString());
            level.setParseLogId(jobDTO.getParseLogId());
            level.setPeriodId(jobDTO.getPeriodId());
            level.setProjectInstitutionName(key);
            level.setRowcount(oblist.size());
            level.setUploadType(jobDTO.getUploadType());
            //level.setCreateBy(SystemContext.getUserId());
            level.setCreateBy(jobDTO.getCreateBy());
            //level.setCreateTime(new Date());
            level.setTenantId(jobDTO.getTenantId());
            level.setIsDeleted(0);
            level.setVersion(0L);
            level.setUpdateBy(jobDTO.getCreateBy());
            level.setFileName(jobDTO.getOriginalFilename());
            level.setUploadPeopleName(jobDTO.getCreateBy());
            level.setProjectInstitutionCode(insCode);
            level.setProjectId(jobDTO.getProjectId());
            //level.setUpdateTime(new Date());
            //todo  抽出外层做批量插入
            parseLevelList.add(level);
            idList.add(level.getId());
            //fileParseLevelMapper.addDataByParseLogId(level);
            //入库子表完毕后，开始进行解析purList集合，入库对应源数据表，注意，此时入库源数据表file_id将是子表的入库后的主键id
            //封装待入源数据表数据
            oblist.forEach(data->originSaleList.add(OriginSaleAutoMap.originSale(data,level.getId(),level.getUpdateBy(),jobDTO.getTenantId(),level.getPeriodId(),jobDTO.getCollectType())));
            //将每次生成的字表id,封装为集合，入源数据表完毕之后，需要回改文件状态字段值，需要用到，parse_log表的主键id直接从ExcelJobDTO获取parselogid字段值
            fileLevelId.add(level.getId());
            atomicInteger.getAndAdd(oblist.size());
        }
        fileParseLevelService.parseLevelBatchInsert(parseLevelList);
        //fileParseLogId.add(jobDTO.getParseLogId());
        //以上数据封装完毕，执行批量入源数据表
        String oeiginSaleTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),jobDTO.getTenantId());
        originSaleService.batchInsertOriginSale(oeiginSaleTableName,originSaleList);
        //入库源数据完毕后，需要再次回改，parse_log和子表中对应的文件状态
        //fileParseLogMapper.updateStatus(fileParseLogId,SystemContext.getUserId(),FileParseStatus.QUALITY_PENDING.toString());
        //回改子表状态
        fileParseLevelMapper.updateLevalStatus(fileLevelId,jobDTO.getCreateBy(),FileParseStatus.QUALITY_PENDING.toString(),jobDTO.getTenantId());
        return atomicInteger.get();
    }
    /**批量入库采购源数据*/
    public Integer insertPurchaseSaleData(String busineType,List<Map<String, Object>> purList,ExcelJobDTO jobDTO,AtomicInteger atomicInteger,List<String> idList,Map<String,String> nameMap){
        List<OriginPurchase> purchaseSaleList = new ArrayList<>(purList.size());
        //listmap分组
        Map<String,List<Map<String,Object>>> list=purList.stream().collect(Collectors.groupingBy(e->e.get("经销商名称").toString()));
        //再次封装待入源数据表数据，此时key为入库后的字表id
        Map<String,List<Map<String,Object>>> dataMap=new HashMap<>();
        List<String> fileParseLogId=new ArrayList<>();
        List<String> fileLevelId=new ArrayList<>();
        List<FileParseLevel> parseLevels=new ArrayList<>();
        //key为经销商名称，value为每个经销商下的数据
        //入库字表，字表只存当前sheet页名称即为业务类型，经销商名称，每个经销商数据总行数，parse_log表id,采集方式，文件校验状态，上传类型，账期id
        //状态为解析中
        for(Map.Entry<String,List<Map<String, Object>>> entry:list.entrySet()){
            String key=entry.getKey();
            List<Map<String,Object>> purSaleList=entry.getValue();
            //按照经销商名称分组，需要经销商编码入库
            String purCode="";
           /* Map<String,List<Map<String,Object>>> inspeclist=purSaleList.stream().collect(Collectors.groupingBy(e->e.get("经销商代码").toString()));
            for(Map.Entry<String,List<Map<String,Object>>> mp:inspeclist.entrySet()){
                purCode=mp.getKey();
                break;
            }*/
            for(int p=0;p<purSaleList.size();p++){
                boolean isContain=ListMapCommonUtils.macthMapStr(purSaleList.get(p),"经销商代码");
                if(isContain){
                    purCode=String.valueOf(purSaleList.get(p).get("经销商代码"));
                    break;
                }
            }
            //System.out.println("key为:"+key+"-----数量为:"+purSaleList.size());
            //封装ent_datacenter_fileparse_level数据，执行inset入库，拿到入库后的主键id
            FileParseLevel level=new FileParseLevel();
            level.setId(UUIDUtils.getUUID());
            level.setBusinessType(busineType);
            if(jobDTO.getUploadDataType().equals("0")){
                //月数据
                level.setBusinessDesc("PM");
            }else{
                //日数据
                level.setBusinessDesc("PD");
            }
            level.setSheetName(nameMap.get(busineType));
            level.setAccessType(jobDTO.getCollectType());
            level.setFileStatus(FileParseStatus.WAIT_CHECK.toString());
            level.setParseLogId(jobDTO.getParseLogId());
            level.setPeriodId(jobDTO.getPeriodId());
            level.setProjectInstitutionName(key);
            level.setRowcount(purSaleList.size());
            level.setUploadType(jobDTO.getUploadType());
            //level.setCreateBy(SystemContext.getUserId());
            level.setCreateBy(jobDTO.getCreateBy());
            //level.setCreateTime(new Date());
            level.setTenantId(jobDTO.getTenantId());
            level.setIsDeleted(0);
            level.setVersion(0L);
            level.setUpdateBy(jobDTO.getCreateBy());
            level.setFileName(jobDTO.getOriginalFilename());
            level.setUploadPeopleName(jobDTO.getCreateBy());
            level.setProjectInstitutionCode(purCode);
            level.setProjectId(jobDTO.getProjectId());
            parseLevels.add(level);
            idList.add(level.getId());
            //level.setUpdateTime(new Date());
            //todo  抽出外层做批量插入
            //fileParseLevelMapper.addDataByParseLogId(level);
            //入库子表完毕后，开始进行解析purList集合，入库对应源数据表，注意，此时入库源数据表file_id将是子表的入库后的主键id
            //封装待入源数据表数据
            purSaleList.forEach(data->purchaseSaleList.add(OriginPurchaseAutoMap.originPurchase(data,level.getId(),level.getUpdateBy(),jobDTO.getTenantId(),level.getPeriodId(),jobDTO.getCollectType())));
            //将每次生成的字表id,封装为集合，入源数据表完毕之后，需要回改文件状态字段值，需要用到，parse_log表的主键id直接从ExcelJobDTO获取parselogid字段值
            fileLevelId.add(level.getId());
            atomicInteger.getAndAdd(purSaleList.size());
        }
        fileParseLevelService.parseLevelBatchInsert(parseLevels);
        //fileParseLogId.add(jobDTO.getParseLogId());
        //以上数据封装完毕，执行批量入源数据表
        String purchaseTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),jobDTO.getTenantId());
        originPurchaseService.batchInsertOriginPurchase(purchaseTableName,purchaseSaleList);
        //入库源数据完毕后，需要再次回改，parse_log和子表中对应的文件状态
        //fileParseLogMapper.updateStatus(fileParseLogId,SystemContext.getUserId(),FileParseStatus.QUALITY_PENDING.toString());
        //回改子表状态
        fileParseLevelMapper.updateLevalStatus(fileLevelId,jobDTO.getCreateBy(),FileParseStatus.QUALITY_PENDING.toString(),jobDTO.getTenantId());
        return atomicInteger.get();
    }
    /**批量入库库存源数据*/
    public Integer insertInspectSaleData(String busineType,List<Map<String, Object>> insList,ExcelJobDTO jobDTO,AtomicInteger atomicInteger,List<String> idList,Map<String,String> nameMap){
        List<OriginInventory> inspectSaleList = new ArrayList<>(insList.size());
        //listmap分组
        Map<String,List<Map<String,Object>>> list=insList.stream().collect(Collectors.groupingBy(e->e.get("经销商名称").toString()));
        //再次封装待入源数据表数据，此时key为入库后的字表id
        Map<String,List<Map<String,Object>>> dataMap=new HashMap<>();
        List<String> fileParseLogId=new ArrayList<>();
        List<String> fileLevelId=new ArrayList<>();
        List<FileParseLevel> list1=new ArrayList<>();
        //key为经销商名称，value为每个经销商下的数据
        //入库字表，字表只存当前sheet页名称即为业务类型，经销商名称，每个经销商数据总行数，parse_log表id,采集方式，文件校验状态，上传类型，账期id
        //状态为解析中
        for(Map.Entry<String,List<Map<String, Object>>> entry:list.entrySet()){
            String key=entry.getKey();
            List<Map<String,Object>> insSalelist=entry.getValue();
            //System.out.println("key为:"+key+"-----数量为:"+insSalelist.size());
            //按照经销商名称分组，需要经销商编码入库
            String inspecCode="";
            for(int p=0;p<insSalelist.size();p++){
                boolean isContain=ListMapCommonUtils.macthMapStr(insSalelist.get(p),"经销商代码");
                if(isContain){
                    inspecCode=String.valueOf(insSalelist.get(p).get("经销商代码"));
                    break;
                }
            }
          /*  Map<String,List<Map<String,Object>>> inspeclist=insSalelist.stream().collect(Collectors.groupingBy(e->e.get("经销商代码").toString()));
            for(Map.Entry<String,List<Map<String,Object>>> mp:inspeclist.entrySet()){
                inspecCode=mp.getKey();
                break;
            }*/
            //封装ent_datacenter_fileparse_level数据，执行inset入库，拿到入库后的主键id
            FileParseLevel level=new FileParseLevel();
            level.setId(UUIDUtils.getUUID());
            level.setBusinessType(busineType);
            if(jobDTO.getUploadDataType().equals("0")){
                //月数据
                //level.setBusinessDesc(TemplateExcelCode.getValue("IM"));
                level.setBusinessDesc("IM");
            }else{
                //日数据
                level.setBusinessDesc("ID");
            }
            level.setSheetName(nameMap.get(busineType));
            level.setAccessType(jobDTO.getCollectType());
            level.setFileStatus(FileParseStatus.WAIT_CHECK.toString());
            level.setParseLogId(jobDTO.getParseLogId());
            level.setPeriodId(jobDTO.getPeriodId());
            level.setProjectInstitutionName(key);
            level.setRowcount(insSalelist.size());
            level.setUploadType(jobDTO.getUploadType());
            //level.setCreateBy(SystemContext.getUserId());
            level.setCreateBy(jobDTO.getCreateBy());
            //level.setCreateTime(new Date());
            level.setTenantId(jobDTO.getTenantId());
            level.setIsDeleted(0);
            level.setVersion(0L);
            level.setUpdateBy(jobDTO.getCreateBy());
            level.setFileName(jobDTO.getOriginalFilename());
            level.setUploadPeopleName(jobDTO.getCreateBy());
            level.setProjectInstitutionCode(inspecCode);
            level.setProjectId(jobDTO.getProjectId());
            //level.setUpdateTime(new Date());
            //todo  抽出外层做批量插入
            //fileParseLevelMapper.addDataByParseLogId(level);
            list1.add(level);
            idList.add(level.getId());
            //入库子表完毕后，开始进行解析purList集合，入库对应源数据表，注意，此时入库源数据表file_id将是子表的入库后的主键id
            //封装待入源数据表数据
            insSalelist.forEach(data->inspectSaleList.add(OriginInventoryAutoMap.originInventory(data,level.getId(),level.getUpdateBy(),jobDTO.getTenantId(),level.getPeriodId(),jobDTO.getCollectType())));
            //将每次生成的字表id,封装为集合，入源数据表完毕之后，需要回改文件状态字段值，需要用到，parse_log表的主键id直接从ExcelJobDTO获取parselogid字段值
            fileLevelId.add(level.getId());
            atomicInteger.getAndAdd(insSalelist.size());
        }
        fileParseLevelService.parseLevelBatchInsert(list1);
        //fileParseLogId.add(jobDTO.getParseLogId());
        //以上数据封装完毕，执行批量入源数据表
        String invenTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),jobDTO.getTenantId());
        originInventoryService.batchInsertOriginInventory(invenTableName,inspectSaleList);
        //入库源数据完毕后，需要再次回改，parse_log和子表中对应的文件状态
        //fileParseLogMapper.updateStatus(fileParseLogId,SystemContext.getUserId(),FileParseStatus.QUALITY_PENDING.toString());
        //回改子表状态
        fileParseLevelMapper.updateLevalStatus(fileLevelId,jobDTO.getCreateBy(),FileParseStatus.QUALITY_PENDING.toString(),jobDTO.getTenantId());
        return atomicInteger.get();
    }
    /**文件内部解析异常，记录入ent_datacenter_file_parse_result日志表*/
    public  void addFileResult(ExcelJobDTO jobDTO){
        FileParseResult fileParseResult = new FileParseResult();
        fileParseResult.setId(UUIDUtils.getUUID());
        fileParseResult.setFileParseLogId(jobDTO.getParseLogId());
        fileParseResult.setStatus(FileParseResultStatus.FAILURE.toString());
        fileParseResult.setStatusRemark(FileParseResultStatusRemark.BUSINESS_TYPE_NOT_FOUND.toString());
        fileParseResult.setCreateBy(SystemContext.getUserId());
        fileParseResult.setTenantId(SystemContext.getTenantId());
        fileParseResult.setCreateTime(new Date());
        fileParseResultMapper.addDataByFileType(fileParseResult);
    }
}
