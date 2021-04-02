package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.api.utils.ListMapCommonUtils;
import com.softium.datacenter.paas.web.common.ConstantCacheMap;
import com.softium.datacenter.paas.api.dto.ParseLogDTO;
import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.FileParseStatus;
import com.softium.datacenter.paas.api.enums.FileQualityStatus;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.FileJudgeTaskService;
import com.softium.datacenter.paas.web.service.InspectDataService;
import com.softium.datacenter.paas.web.service.JudgeInventoryService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**库存质检数据实现类*/
@Slf4j
@Service
public class JudgeInventoryServiceImpl implements JudgeInventoryService {
    @Autowired
    FileParseLogMapper parseLogMapper;
    @Autowired
    PreprocessRuleMapper preprocessRuleMapper;
    @Autowired
    OriginInventoryMapper originInventoryMapper;
    @Autowired
    FieldFormatRuleMapper formatRuleMapper;
    @Autowired
    QualityRuleMapper qualityRuleMapper;
    @Autowired
    FileColumnRuleMapper fileColumnRuleMapper;
    @Autowired
    ParseLogMapper logMapper;
    @Autowired
    private InspectDataService inspectDataService;
    @Autowired
    private FileJudgeTaskService judgeTaskService;
    @Resource
    ShardingManager shardingManager;
    @Override
    public void judgeInventData(JudgeDataQuery dataQuery) throws Exception {

        List<String> iallParseLogList=new ArrayList<>();
        List<String> ifailParseLogList=new ArrayList<>();
        StopWatch watch=new StopWatch();
        watch.start();
        List<String> tenantAllId=parseLogMapper.queryByTypeOrTanId(FileParseStatus.QUALITY_PENDING.toString(),"ID");
        if(tenantAllId.size()>0){
            for (String str:tenantAllId){
                String oriInvenTable=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),str);
                String invenTable=shardingManager.getShardingTableNameByValue(ORMapping.get(InspectInventory.class),str);
                Map<String, List<OriginInventory>> impList=new HashMap<>();
                Criteria<FileParseLog> criteria=new Criteria<>();
                criteria.addCriterion(new Condition("fileStatus", Operator.equal,FileParseStatus.QUALITY_PENDING.toString()));
                criteria.addCriterion(new Condition("businessType",Operator.equal,"ID"));
                criteria.addCriterion(new Condition("disabled",Operator.equal,0));
                criteria.addCriterion(new Condition("tenantId",Operator.equal,str));
                List<FileParseLog> parseLogList = parseLogMapper.findByCriteria(criteria);
                log.info("库存质检任务开始,待质检文件总数为{}条",parseLogList.size());
                for(FileParseLog ifileParseLog:parseLogList){
                    SystemContext.setTenantId(ifileParseLog.getTenantId());
                    AtomicInteger iatomicInter=new AtomicInteger(0);
                    List<OriginInventory> inventoryList=originInventoryMapper.getOriginInventoryByFileId(oriInvenTable,ifileParseLog.getId());
                    if(inventoryList!=null&&inventoryList.size()>0){
                        List<PreprocessRule> idefaultPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(ifileParseLog.getProjectId(), null, ifileParseLog.getBusinessType());
                        List<PreprocessRule> ispecialPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(ifileParseLog.getProjectId(), ifileParseLog.getProjectInstitutionCode(), ifileParseLog.getBusinessType());
                        List<FieldFormatRule> izifuformatRules = null;
                        List<FieldFormatRule> ifieldformatRules = null;
                        if(idefaultPreList.size()>0||ispecialPreList.size()>0){
                            for(OriginInventory inventoryData:inventoryList){
                                if(ispecialPreList.size()>0){
                                    for (PreprocessRule ispecialrule : ispecialPreList) {
                                        if(ispecialrule.getRuleType()==4&&izifuformatRules==null){//双重条件，只查询一次
                                            izifuformatRules = formatRuleMapper.getFormatDataByPrecessId(ispecialrule.getId(), null, null);
                                        }else if(ispecialrule.getRuleType()==5&&ifieldformatRules==null){
                                            ifieldformatRules = formatRuleMapper.getFormatDataByPrecessId(ispecialrule.getId(), null, ispecialrule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(ispecialrule, inventoryData, izifuformatRules, ifieldformatRules);
                                    }
                                }else{
                                    for (PreprocessRule irule : idefaultPreList) {
                                        if(irule.getRuleType()==4&&izifuformatRules==null){
                                            izifuformatRules = formatRuleMapper.getFormatDataByPrecessId(irule.getId(), null, null);
                                        }else if(irule.getRuleType()==5&&ifieldformatRules==null){
                                            ifieldformatRules = formatRuleMapper.getFormatDataByPrecessId(irule.getId(), null, irule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(irule, inventoryData, izifuformatRules, ifieldformatRules);
                                    }
                                }
                            }
                        }
                        //根据项目id查询质检表表默认规则,项目经销商id为null即可
                        List<QualityRule> idefaultQualiList = qualityRuleMapper.queryAllRuleByType(ifileParseLog.getProjectId(), null, ifileParseLog.getBusinessType());
                        // 根据项目id查询质检表特殊经销商规则,加上条件项目经销商id参数
                        List<QualityRule> ispecialQualiList = qualityRuleMapper.queryAllRuleByType(ifileParseLog.getProjectId(), ifileParseLog.getProjectInstitutionCode(), ifileParseLog.getBusinessType());
                        List<FileHandleRule> ifileHandleRuleList = null;
                        if(idefaultQualiList.size()>0||ispecialQualiList.size()>0){
                            if(ispecialQualiList.size()>0){
                                for(QualityRule ispeRule:ispecialQualiList){
                                    if (ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {
                                        if (ifileHandleRuleList == null) {
                                            //查询ent_datacenter_filecolumn_rule，ent_datacenter_filehandle_rule表获取所配置字段数据
                                            ifileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(ifileParseLog.getProjectId(),ifileParseLog.getProjectInstitutionCode(),ifileParseLog.getBusinessType());
                                        }
                                        //调用方法，筛查全部列是否符合规则
                                        qualityParseData(inventoryList,ifileHandleRuleList,ispeRule,ifileParseLog,null,iatomicInter);
                                    } else if (ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {//文件重复处理
                                        //获取rule_content值,以配置的规则，判断文件内是否重复
                                        qualityParseData(inventoryList,ifileHandleRuleList,ispeRule,ifileParseLog,ispeRule.getRuleContent(),iatomicInter);
                                    }
                                }
                            }else {
                                for (QualityRule iquRule : idefaultQualiList) {
                                    if (iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {
                                        if (ifileHandleRuleList == null) {
                                            ifileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(ifileParseLog.getProjectId(),null,ifileParseLog.getBusinessType());
                                        }
                                        qualityParseData(inventoryList,ifileHandleRuleList,iquRule,ifileParseLog,null,iatomicInter);
                                    } else if (iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {
                                        qualityParseData(inventoryList,ifileHandleRuleList,iquRule,ifileParseLog,iquRule.getRuleContent(),iatomicInter);
                                    }
                                }
                            }
                        }
                        if(idefaultPreList.size()>0||ispecialPreList.size()>0||idefaultQualiList.size()>0||ispecialQualiList.size()>0){
                            if(iatomicInter.get()==0){//可能会出现只配置质检不配置预处理，利用规则条件多个控制拦截
                                iallParseLogList.add(ifileParseLog.getId());
                                //2.数据批量入最终落地库inspect_sale
                                impList.put(ifileParseLog.getId(),inventoryList);
                            }else{
                                ifailParseLogList.add(ifileParseLog.getId());
                            }
                        }else{
                            iallParseLogList.add(ifileParseLog.getId());
                            impList.put(ifileParseLog.getId(),inventoryList);
                        }
                    }else{
                        iallParseLogList.add(ifileParseLog.getId());
                    }
                }
                for(Map.Entry<String,List<OriginInventory>> mp:impList.entrySet()){
                    List<OriginInventory> orgList=mp.getValue();
                    List<InspectInventory> inventories = new ArrayList<>(orgList.size());
                    orgList.forEach(sal ->{
                        inventories.add(formatToInspectSale(sal));
                    });
                    //批量入库
                    inspectDataService.batchInsertInventory(invenTable,inventories);
                }
            }
        }

        if(iallParseLogList.size()>0){
            parseLogMapper.updateStatus(iallParseLogList,"admin", FileParseStatus.QUALITY_SUCCESS.toString());
        }
        if(ifailParseLogList.size()>0){
            parseLogMapper.batchUpdateParseLog(ifailParseLogList,"admin");
        }
        watch.stop();
        log.info("执行完毕:库存数据共花费:"+watch.getTotalTimeSeconds()+"秒");
    }
    public void qualityParseData(List<OriginInventory> qurule, List<FileHandleRule> fileHandleRuleList, QualityRule qualityRule, FileParseLog fileParseLog, String contentStr, AtomicInteger atcInteger) throws Exception {
        Map<String, String> fieldMap= ConstantCacheMap.ORIGIN_INVENTORY_MAP;
        List<ParseLogDTO> errorQualiData=new CopyOnWriteArrayList<>();
        if(qualityRule.getRuleType()==1){
            fileHandleRuleList.parallelStream().forEach(k ->{
                if(fieldMap.containsKey(k.getFieldName())){
                    String typeStr=k.getFieldType();
                    if(typeStr.equals(CommonUtil.FIELD_TYPE_DATE)){//日期类型
                        qurule.parallelStream().forEach(n ->{
                            try {
                                String getStr=String.valueOf(ListMapCommonUtils.getFiledValue(n,fieldMap.get(k.getFieldName())));
                                if(k.getRequired()==0&& StringUtils.isEmpty(getStr)){
                                    return;
                                }
                                if(ListMapCommonUtils.judgeTime(getStr)==null){//等于null表示不是时间格式，失败
                                    ParseLogDTO logDTO=new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(n.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(k.getFieldName()+"字段类型应为"+CommonUtil.FIELD_TYPE.get(CommonUtil.FIELD_TYPE_DATE));
                                    logDTO.setJudgeType(1);
                                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                                    logDTO.setJudgeMatchingId(qualityRule.getId());
                                    logDTO.setRowNum(n.getRowNum());
                                    logDTO.setFileName(fileParseLog.getFileName());
                                    logDTO.setCreateTime(new Date());
                                    logDTO.setCreateBy(fileParseLog.getCreateBy());
                                    logDTO.setUpdateTime(new Date());
                                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                                    logDTO.setTenantId(fileParseLog.getTenantId());
                                    logDTO.setVersion(fileParseLog.getVersion());
                                    errorQualiData.add(logDTO);
                                }
                            } catch (Exception e) {
                                log.error("质检处理日期错误:"+e.getMessage());
                            }
                        });
                    }else if(typeStr.equals(CommonUtil.FIELD_TYPE_NUM)){//数字类型
                        qurule.parallelStream().forEach(p ->{
                            try {
                                String getStr=String.valueOf(ListMapCommonUtils.getFiledValue(p,fieldMap.get(k.getFieldName())));
                                if(k.getRequired()==0&&StringUtils.isEmpty(getStr)){
                                    return;
                                }
                                if(!ListMapCommonUtils.isNumeric(getStr)){//为true表示为数字,false表示不符合格式，应该为数字
                                    ParseLogDTO logDTO=new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(p.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(k.getFieldName()+"字段类型应为"+CommonUtil.FIELD_TYPE.get(CommonUtil.FIELD_TYPE_NUM));
                                    logDTO.setJudgeType(1);
                                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                                    logDTO.setJudgeMatchingId(qualityRule.getId());
                                    logDTO.setRowNum(p.getRowNum());
                                    logDTO.setFileName(fileParseLog.getFileName());
                                    logDTO.setCreateTime(new Date());
                                    logDTO.setCreateBy(fileParseLog.getCreateBy());
                                    logDTO.setUpdateTime(new Date());
                                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                                    logDTO.setTenantId(fileParseLog.getTenantId());
                                    logDTO.setVersion(fileParseLog.getVersion());
                                    errorQualiData.add(logDTO);
                                }
                            } catch (Exception e) {
                                log.error("质检处理数字类型错误:"+e.getMessage());
                            }
                        });
                    }
                }
            });
        }else if(qualityRule.getRuleType()==2){
            fileHandleRuleList.parallelStream().forEach(f ->{
                if(fieldMap.containsKey(f.getFieldName())){
                    int isRequired=f.getRequired();
                    qurule.parallelStream().forEach(a ->{
                        try {
                            String numStr=String.valueOf(ListMapCommonUtils.getFiledValue(a,fieldMap.get(f.getFieldName())));
                            if(isRequired==1){//必填
                                if(StringUtils.isEmpty(numStr)||numStr.equals("null")||numStr.length()==0){
                                    ParseLogDTO logDTO=new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(a.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(f.getFieldName()+"字段类型应必填");
                                    logDTO.setJudgeType(2);
                                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                                    logDTO.setJudgeMatchingId(qualityRule.getId());
                                    logDTO.setRowNum(a.getRowNum());
                                    logDTO.setFileName(fileParseLog.getFileName());
                                    logDTO.setCreateTime(new Date());
                                    logDTO.setCreateBy(fileParseLog.getCreateBy());
                                    logDTO.setUpdateTime(new Date());
                                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                                    logDTO.setTenantId(fileParseLog.getTenantId());
                                    logDTO.setVersion(fileParseLog.getVersion());
                                    errorQualiData.add(logDTO);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        }else if(qualityRule.getRuleType()==3){
            if(contentStr.contains(",")){
                StringBuffer sta=new StringBuffer();
                String[] bzhi=contentStr.split(",");
                for(int m=0;m<bzhi.length;m++){
                    if(fieldMap.containsKey(bzhi[m])){
                        sta.append(fieldMap.get(bzhi[m])).append(",");
                    }
                }
                contentStr= sta.deleteCharAt(sta.length()-1).toString();
            }else{
                contentStr=fieldMap.get(contentStr);
            }
            Map<String, OriginInventory> map = new HashMap<>();
            Map<String, String> judgeMap = new HashMap<>();
            Set<OriginInventory> seList=new HashSet<>();
            for (OriginInventory isale : qurule) {
                String keyValue=ListMapCommonUtils.parseListBeanByName(contentStr,isale);
                OriginInventory tempValString = map.put(keyValue,isale);
                if(tempValString!=null){
                    if(StringUtils.isEmpty(judgeMap.get(keyValue))||judgeMap.get(keyValue)==null){
                        seList.add(tempValString);
                        judgeMap.put(keyValue,keyValue);
                    }
                    ParseLogDTO logDTO=new ParseLogDTO();
                    logDTO.setId(UUID.randomUUID().toString());
                    logDTO.setProjectId(fileParseLog.getProjectId());
                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                    logDTO.setOriginId(isale.getId());
                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                    logDTO.setJudgeTime(new Date());
                    logDTO.setJudgeMsg("文件内重复");
                    logDTO.setJudgeType(3);
                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                    logDTO.setJudgeMatchingId(qualityRule.getId());
                    logDTO.setRowNum(isale.getRowNum());
                    logDTO.setFileName(fileParseLog.getFileName());
                    logDTO.setCreateTime(new Date());
                    logDTO.setCreateBy(fileParseLog.getCreateBy());
                    logDTO.setUpdateTime(new Date());
                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                    logDTO.setTenantId(fileParseLog.getTenantId());
                    logDTO.setVersion(fileParseLog.getVersion());
                    errorQualiData.add(logDTO);
                }else{
                    judgeMap.put(keyValue,null);
                }
            }
            for(OriginInventory org:seList){
                ParseLogDTO logDTO=new ParseLogDTO();
                logDTO.setId(UUID.randomUUID().toString());
                logDTO.setProjectId(fileParseLog.getProjectId());
                logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                logDTO.setBusinessType(fileParseLog.getBusinessType());
                logDTO.setOriginId(org.getId());
                logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                logDTO.setJudgeTime(new Date());
                logDTO.setJudgeMsg("文件内重复");
                logDTO.setJudgeType(3);
                logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                logDTO.setJudgeMatchingId(qualityRule.getId());
                logDTO.setRowNum(org.getRowNum());
                logDTO.setFileName(fileParseLog.getFileName());
                logDTO.setCreateTime(new Date());
                logDTO.setCreateBy(fileParseLog.getCreateBy());
                logDTO.setUpdateTime(new Date());
                logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                logDTO.setTenantId(fileParseLog.getTenantId());
                logDTO.setVersion(fileParseLog.getVersion());
                errorQualiData.add(logDTO);
            }
        }
        if(errorQualiData.size()>0){
            List<ParseLogDTO> judgeList=errorQualiData.stream().filter(a->a.getJudgeStandard().equals(CommonUtil.QUALITY_PROCESS_BLOCK)).collect(Collectors.toList());
            if(judgeList.size()!=0){
                log.info("文件{}中有质检阻断错误,全部不入库"+fileParseLog.getId());
                atcInteger.incrementAndGet();
            }
            //处理错误数据记录批量入库
            judgeTaskService.parseLogBatchInsert(errorQualiData);
        }
    }
    private InspectInventory formatToInspectSale(OriginInventory originInventory) {
        InspectInventory inspectInventory = new InspectInventory();
        BeanUtils.copyProperties(originInventory, inspectInventory);
        inspectInventory.setId(UUID.randomUUID().toString());
        inspectInventory.setOriginInventoryId(originInventory.getId());
        inspectInventory.setFileId(originInventory.getFileId());
        inspectInventory.setFromInstitutionCode(originInventory.getInstitutionCode());
        inspectInventory.setTenantId(originInventory.getTenantId());
        return inspectInventory;
    }
}
