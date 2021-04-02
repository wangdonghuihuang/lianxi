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
import com.softium.datacenter.paas.web.service.JudgePurchaseService;
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

/**采购数据质检实现类*/
@Slf4j
@Service
public class JudgePurchaseServiceImpl implements JudgePurchaseService {
    @Autowired
    FileParseLogMapper parseLogMapper;
    @Autowired
    PreprocessRuleMapper preprocessRuleMapper;
    @Autowired
    OriginPurchaseMapper originPurchaseMapper;
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
    public void judgePurchaseData(JudgeDataQuery dataQuery) throws Exception {
        Map<String, List<OriginPurchase>> impList=new HashMap<>();
        List<String> pallParseLogList=new ArrayList<>();
        List<String> pfailParseLogList=new ArrayList<>();
        StopWatch watch=new StopWatch();
        watch.start();
        List<String> tenantAllId=parseLogMapper.queryByTypeOrTanId(FileParseStatus.QUALITY_PENDING.toString(),"PD");
        if(tenantAllId.size()>0){
            for (String str:tenantAllId){
                String oriPurseTable=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),str);
                String insPurseTable=shardingManager.getShardingTableNameByValue(ORMapping.get(InspectPurchase.class),str);
                Criteria<FileParseLog> criteria=new Criteria<>();
                criteria.addCriterion(new Condition("fileStatus", Operator.equal, FileParseStatus.QUALITY_PENDING.toString()));
                criteria.addCriterion(new Condition("businessType",Operator.equal,"PD"));
                criteria.addCriterion(new Condition("disabled",Operator.equal,0));
                List<FileParseLog> parseLogList = parseLogMapper.findByCriteria(criteria);
                log.info("库存质检任务开始,待质检文件总数为{}条",parseLogList.size());
                for(FileParseLog pfileParseLog:parseLogList){
                    SystemContext.setTenantId(pfileParseLog.getTenantId());
                    AtomicInteger patomicInter=new AtomicInteger(0);
                    List<OriginPurchase> purchaseList=originPurchaseMapper.getOriginPurchaseByFileId(oriPurseTable,pfileParseLog.getId());
                    if(purchaseList!=null&&purchaseList.size()>0){
                        List<PreprocessRule> pdefaultPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(pfileParseLog.getProjectId(), null, pfileParseLog.getBusinessType());
                        List<PreprocessRule> pspecialPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(pfileParseLog.getProjectId(), pfileParseLog.getProjectInstitutionCode(), pfileParseLog.getBusinessType());
                        List<FieldFormatRule> pzifuformatRules = null;
                        List<FieldFormatRule> pfieldformatRules = null;
                        if(pdefaultPreList.size()>0||pspecialPreList.size()>0){
                            for(OriginPurchase purchaseData:purchaseList){
                                if(pspecialPreList.size()>0){
                                    for (PreprocessRule pspecialrule : pspecialPreList) {
                                        if(pspecialrule.getRuleType()==4&&pzifuformatRules==null){//双重条件，只查询一次
                                            pzifuformatRules = formatRuleMapper.getFormatDataByPrecessId(pspecialrule.getId(), null, null);
                                        }else if(pspecialrule.getRuleType()==5&&pfieldformatRules==null){
                                            pfieldformatRules = formatRuleMapper.getFormatDataByPrecessId(pspecialrule.getId(), null, pspecialrule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(pspecialrule, purchaseData, pzifuformatRules, pfieldformatRules);
                                    }
                                }else{
                                    for (PreprocessRule prule : pdefaultPreList) {
                                        if(prule.getRuleType()==4&&pzifuformatRules==null){
                                            pzifuformatRules = formatRuleMapper.getFormatDataByPrecessId(prule.getId(), null, null);
                                        }else if(prule.getRuleType()==5&&pfieldformatRules==null){
                                            pfieldformatRules = formatRuleMapper.getFormatDataByPrecessId(prule.getId(), null, prule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(prule, purchaseData, pzifuformatRules, pfieldformatRules);
                                    }
                                }
                            }
                        }
                        //根据项目id查询质检表表默认规则,项目经销商id为null即可
                        List<QualityRule> pdefaultQualiList = qualityRuleMapper.queryAllRuleByType(pfileParseLog.getProjectId(), null, pfileParseLog.getBusinessType());
                        // 根据项目id查询质检表特殊经销商规则,加上条件项目经销商id参数
                        List<QualityRule> pspecialQualiList = qualityRuleMapper.queryAllRuleByType(pfileParseLog.getProjectId(), pfileParseLog.getProjectInstitutionCode(), pfileParseLog.getBusinessType());
                        List<FileHandleRule> pfileHandleRuleList = null;
                        if(pdefaultQualiList.size()>0||pspecialQualiList.size()>0){
                            if(pspecialQualiList.size()>0){
                                for(QualityRule ispeRule:pspecialQualiList){
                                    if (ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {
                                        if (pfileHandleRuleList == null) {
                                            //查询ent_datacenter_filecolumn_rule，ent_datacenter_filehandle_rule表获取所配置字段数据
                                            pfileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(pfileParseLog.getProjectId(),pfileParseLog.getProjectInstitutionCode(),pfileParseLog.getBusinessType());
                                        }
                                        //调用方法，筛查全部列是否符合规则
                                        qualityParseData(purchaseList,pfileHandleRuleList,ispeRule,pfileParseLog,null,patomicInter);
                                    } else if (ispeRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {//文件重复处理
                                        //获取rule_content值,以配置的规则，判断文件内是否重复
                                        qualityParseData(purchaseList,pfileHandleRuleList,ispeRule,pfileParseLog,ispeRule.getRuleContent(),patomicInter);
                                    }
                                }
                            }else {
                                for (QualityRule iquRule : pdefaultQualiList) {
                                    if (iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {
                                        if (pfileHandleRuleList == null) {
                                            pfileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(pfileParseLog.getProjectId(),null,pfileParseLog.getBusinessType());
                                        }
                                        qualityParseData(purchaseList,pfileHandleRuleList,iquRule,pfileParseLog,null,patomicInter);
                                    } else if (iquRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {
                                        qualityParseData(purchaseList,pfileHandleRuleList,iquRule,pfileParseLog,iquRule.getRuleContent(),patomicInter);
                                    }
                                }
                            }
                        }
                        if(pdefaultPreList.size()>0||pspecialPreList.size()>0||pdefaultQualiList.size()>0||pspecialQualiList.size()>0){
                            if(patomicInter.get()==0){//可能会出现只配置质检不配置预处理，利用规则条件多个控制拦截
                                pallParseLogList.add(pfileParseLog.getId());
                                //2.数据批量入最终落地库inspect_sale
                                impList.put(pfileParseLog.getId(),purchaseList);
                            }else{
                                pfailParseLogList.add(pfileParseLog.getId());
                            }
                        }else{
                            pallParseLogList.add(pfileParseLog.getId());
                            impList.put(pfileParseLog.getId(),purchaseList);
                        }
                    }else{
                        pallParseLogList.add(pfileParseLog.getId());
                    }
                }
                for(Map.Entry<String,List<OriginPurchase>> mp:impList.entrySet()){
                    List<OriginPurchase> orgList=mp.getValue();
                    List<InspectPurchase> purchases = new ArrayList<>(orgList.size());
                    orgList.forEach(sal ->{
                        purchases.add(formatToInspectSale(sal));
                    });
                    //批量入库
                    inspectDataService.batchInsertPurchase(insPurseTable,purchases);
                }
            }
        }

        if(pallParseLogList.size()>0){
            parseLogMapper.updateStatus(pallParseLogList,"admin", FileParseStatus.QUALITY_SUCCESS.toString());
        }
        if(pfailParseLogList.size()>0){
            parseLogMapper.batchUpdateParseLog(pfailParseLogList,"admin");
        }
        watch.stop();
        log.info("执行完毕:采购数据共花费:"+watch.getTotalTimeSeconds()+"秒");
    }
    public void qualityParseData(List<OriginPurchase> qurule, List<FileHandleRule> fileHandleRuleList, QualityRule qualityRule, FileParseLog fileParseLog, String contentStr, AtomicInteger atcInteger) throws Exception {
        Map<String, String> fieldMap= ConstantCacheMap.ORIGIN_PURCHASE_MAP;
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
            Map<String, OriginPurchase> map = new HashMap<>();
            Map<String, String> judgeMap = new HashMap<>();
            Set<OriginPurchase> seList=new HashSet<>();
            for (OriginPurchase isale : qurule) {
                String keyValue=ListMapCommonUtils.parseListBeanByName(contentStr,isale);
                OriginPurchase tempValString = map.put(keyValue,isale);
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
            for(OriginPurchase org:seList){
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
    private InspectPurchase formatToInspectSale(OriginPurchase originPurchase) {
        InspectPurchase inspectPurchase = new InspectPurchase();
        BeanUtils.copyProperties(originPurchase, inspectPurchase);
        inspectPurchase.setId(UUID.randomUUID().toString());
        inspectPurchase.setOriginPurchaseId(originPurchase.getId());
        inspectPurchase.setFileId(originPurchase.getFileId());
        inspectPurchase.setFromInstitutionCode(originPurchase.getInstitutionCode());
        inspectPurchase.setTenantId(originPurchase.getTenantId());
        return inspectPurchase;
    }
}
