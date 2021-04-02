package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.dto.ParseLogDTO;
import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.FileParseStatus;
import com.softium.datacenter.paas.api.enums.FileQualityStatus;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.api.utils.CommonUtil;
import com.softium.datacenter.paas.api.utils.ListMapCommonUtils;
import com.softium.datacenter.paas.web.common.ConstantCacheMap;
import com.softium.datacenter.paas.web.service.FileJudgeTaskService;
import com.softium.datacenter.paas.web.service.InspectDataService;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 源数据质检接口实现层
 */
@Slf4j
@Service
public class FileJudgeTaskServiceImpl implements FileJudgeTaskService {
    @Autowired
    FileParseLogMapper parseLogMapper;
    @Autowired
    PreprocessRuleMapper preprocessRuleMapper;
    @Autowired
    OriginSaleMapper originSaleMapper;
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
    @Resource
    ShardingManager shardingManager;
    private static final Integer BATCH_SIZE = 100;

    @Override
    public void judgeOriginSaleData(JudgeDataQuery dataQuery) throws Exception {

        //最终待修改log表中质检状态成功待入库集合
        List<String> allParseLogList = new ArrayList<>();
        //最终待修改log表中质检状态失败集合
        List<String> failParseLogList = new ArrayList<>();
        //todo  用来测试集合检测数量位置，记得删除
        AtomicInteger testinteger = new AtomicInteger(1);
        StopWatch watch = new StopWatch();
        watch.start();
        //以文件为维度，查询ent_datacenter_file_parse_log表，获取全部文件,加上分表后，需要以租户id为每次迭代依据。
        List<String> tenantAllId = parseLogMapper.queryByTypeOrTanId(FileParseStatus.QUALITY_PENDING.toString(), "SD");
        if (tenantAllId.size() > 0) {
            for (String str : tenantAllId) {
                //根据表名和分表片键值，获取数据所在表名
                String originTable = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class), str);
                String inspectTable = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), str);
                log.info("存在表:" + originTable);
                //最终正确入库数据
                Map<String, List<OriginSale>> mpList = new HashMap<>();
                Criteria<FileParseLog> criteria = new Criteria<>();
                criteria.addCriterion(new Condition("fileStatus", Operator.equal, FileParseStatus.QUALITY_PENDING.toString()));
                criteria.addCriterion(new Condition("businessType", Operator.equal, "SD"));
                criteria.addCriterion(new Condition("disabled", Operator.equal, 0));
                criteria.addCriterion(new Condition("tenantId", Operator.equal, str));
                List<FileParseLog> parseLogList = parseLogMapper.findByCriteria(criteria);
                log.info("销售质检任务开始,待质检文件总数为{}条", parseLogList.size());
                //以parseLogList为外层总遍历开关
                for (FileParseLog fileParseLog : parseLogList) {
                    SystemContext.setTenantId(fileParseLog.getTenantId());
                    AtomicInteger atomicInteger = new AtomicInteger(0);
                    //List<OriginSale> dataList=originSaleMapper.findByProperty(OriginSale::getFileId,fileParseLog.getId());
                    //根据文件id,关联查询ent_datacenter_origin_sale获取此文件下全部数据
                    List<OriginSale> dataList = originSaleMapper.getOriginSaleByFileId(originTable, fileParseLog.getId());
                    if (dataList != null && dataList.size() > 0) {
                        //根据项目id查询预处理表默认规则,项目经销商id为null即可
                        List<PreprocessRule> defaultPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(fileParseLog.getProjectId(), null, fileParseLog.getBusinessType());
                        // 根据项目id查询预处理特殊经销商规则,加上条件项目经销商id参数
                        List<PreprocessRule> specialPreList = preprocessRuleMapper.queryPreProcessRuleByProjectId(fileParseLog.getProjectId(), fileParseLog.getProjectInstitutionCode(), fileParseLog.getBusinessType());
                        //字符转换配置数据，为空做一次查询，不为空则不用再与数据库交互
                        List<FieldFormatRule> zifuformatRules = null;
                        //字段转换配置数据，为空做一次查询，不为空则不用再与数据库交互
                        List<FieldFormatRule> fieldformatRules = null;
                        //读取到全部数据，开始解析，每一行，每一列中的每个字段，是否符合各项校验规则
                        if (defaultPreList.size() > 0 || specialPreList.size() > 0) {
                            for (OriginSale saleData : dataList) {
                                int weizhi = testinteger.incrementAndGet();
                                /**根据项目经销商id查询预处理表，如果数量不为0,代表已经配置特殊经销商，不在走默认规则解析
                                 * 为0则走默认配置解析*/
                                if (specialPreList.size() > 0) {
                                    for (PreprocessRule specialrule : specialPreList) {
                                        if (specialrule.getRuleType() == 4 && zifuformatRules == null) {//双重条件，只查询一次
                                            zifuformatRules = formatRuleMapper.getFormatDataByPrecessId(specialrule.getId(), null, null);
                                        } else if (specialrule.getRuleType() == 5 && fieldformatRules == null) {
                                            fieldformatRules = formatRuleMapper.getFormatDataByPrecessId(specialrule.getId(), null, specialrule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(specialrule, saleData, zifuformatRules, fieldformatRules);
                                    }
                                } else {
                                    for (PreprocessRule rule : defaultPreList) {
                                        if (rule.getRuleType() == 4 && zifuformatRules == null) {
                                            zifuformatRules = formatRuleMapper.getFormatDataByPrecessId(rule.getId(), null, null);
                                        } else if (rule.getRuleType() == 5 && fieldformatRules == null) {
                                            fieldformatRules = formatRuleMapper.getFormatDataByPrecessId(rule.getId(), null, rule.getRuleName());
                                        }
                                        ListMapCommonUtils.parseDataByRule(rule, saleData, zifuformatRules, fieldformatRules);
                                    }
                                }
                            }
                        }

                        //预处理全部结束，开始质检
                        //3质检规则解析
                        /**
                         * 根据项目id查询质检表默认规则,项目经销商id为null即可
                         * 不管任何符合不符合，都需要将全部字段顺序检测一遍
                         * 质检全部解析完毕，完全符合最终入库，并记录日志库
                         * 如任何一个字段检测不符合，将只记录不符合信息入日志库，不入最终库
                         * */
                        //根据项目id查询质检表表默认规则,项目经销商id为null即可
                        List<QualityRule> defaultQualiList = qualityRuleMapper.queryAllRuleByType(fileParseLog.getProjectId(), null, fileParseLog.getBusinessType());
                        // 根据项目id查询质检表特殊经销商规则,加上条件项目经销商id参数
                        List<QualityRule> specialQualiList = qualityRuleMapper.queryAllRuleByType(fileParseLog.getProjectId(), fileParseLog.getProjectInstitutionCode(), fileParseLog.getBusinessType());
                        List<FileHandleRule> fileHandleRuleList = null;
                        //销售源数据表数据每一条做集合操作
                        if (defaultQualiList.size() > 0 || specialQualiList.size() > 0) {
                            if (specialQualiList.size() > 0) {
                                for (QualityRule speRule : specialQualiList) {
                                    if (speRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || speRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {//字段类型,字段必填
                                        if (fileHandleRuleList == null) {
                                            //查询ent_datacenter_filecolumn_rule，ent_datacenter_filehandle_rule表获取所配置字段数据
                                            fileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(fileParseLog.getProjectId(), fileParseLog.getProjectInstitutionCode(), fileParseLog.getBusinessType());
                                        }
                                        //调用方法，筛查全部列是否符合规则
                                        qualityParseData(dataList, fileHandleRuleList, speRule, fileParseLog, null, atomicInteger);
                                    } else if (speRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {//文件重复处理
                                        //获取rule_content值,以配置的规则，判断文件内是否重复
                                        qualityParseData(dataList, fileHandleRuleList, speRule, fileParseLog, speRule.getRuleContent(), atomicInteger);
                                    }
                                }
                            } else {
                                //根据规则名称，对应处理，现为字段类型，字段必填，文件重复
                                for (QualityRule quRule : defaultQualiList) {
                                    if (quRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(1)) || quRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(2))) {//字段类型,字段必填
                                        if (fileHandleRuleList == null) {
                                            //查询ent_datacenter_filecolumn_rule，ent_datacenter_filehandle_rule表获取所配置字段数据
                                            fileHandleRuleList = fileColumnRuleMapper.queryFileHandleRuleById(fileParseLog.getProjectId(), null, fileParseLog.getBusinessType());
                                        }
                                        //aloneSaleData.add(saleData);
                                        //调用方法，筛查全部列是否符合规则
                                        qualityParseData(dataList, fileHandleRuleList, quRule, fileParseLog, null, atomicInteger);
                                    } else if (quRule.getRuleName().equals(CommonUtil.QUALITY_RULE_TYPE.get(3))) {//文件重复处理
                                        qualityParseData(dataList, fileHandleRuleList, quRule, fileParseLog, quRule.getRuleContent(), atomicInteger);
                                    }
                                }
                            }
                        }
                        //此条件处理有任何一个规则存在
                        if (defaultPreList.size() > 0 || specialPreList.size() > 0 || defaultQualiList.size() > 0 || specialQualiList.size() > 0) {
                            //质检全部完毕，根据类变量atomicinteger数量，为0代表全部数据符合，大于0表示有检测失败，整个文件的数据将不入库
                            if (atomicInteger.get() == 0) {//可能会出现只配置质检不配置预处理，利用规则条件多个控制拦截
                                //1.数据放入最终需修改文件状态集合，待最终落地后，修改文件质检状态为待入库
                        /*fileParseLog.setFileStatus(FileParseStatus.STORAGE_PENDING.toString());
                        fileParseLog.setModifier("admin");
                        fileParseLog.setModifiedTime(LocalDateTime.now());*/
                                allParseLogList.add(fileParseLog.getId());
                                //2.数据批量入最终落地库inspect_sale
                                mpList.put(fileParseLog.getId(), dataList);
                            } else {
                                //调用修改方法，修改ent_datacenter_file_parse_log质检状态为失败
                       /* fileParseLog.setFileStatus(FileParseStatus.QUALITY_FAILURE.toString());
                        fileParseLog.setModifier("admin");
                        fileParseLog.setModifiedTime(LocalDateTime.now());
                        parseLogMapper.updateFileStatus(fileParseLog);*/
                                failParseLogList.add(fileParseLog.getId());
                            }
                        } else {
                            //此处处理有数据，但是没有任何规则存在
                            allParseLogList.add(fileParseLog.getId());
                            mpList.put(fileParseLog.getId(), dataList);
                        }
                    } else {
                        //处理存在file_parse_log表中，但源数据为空的日志记录，状态改为待入库
                        allParseLogList.add(fileParseLog.getId());
                    }
                }
                //全部完毕，批量正确数据入库
                for (Map.Entry<String, List<OriginSale>> mp : mpList.entrySet()) {
                    List<OriginSale> orgList = mp.getValue();
                    List<InspectSale> inspectSales = new ArrayList<>(orgList.size());
                    orgList.forEach(sal -> {
                        inspectSales.add(formatToInspectSale(sal));
                    });
                    //批量入库
                    inspectDataService.inspectSalebatchInsert(inspectTable, inspectSales);
                }
            }
        }
        //todo  关于 parseLogMapper.updateFileStatus(fileParseLog);修改文件质检状态，后面改成最后在操作

        if (allParseLogList.size() > 0) {
            parseLogMapper.updateStatus(allParseLogList, "admin", FileParseStatus.QUALITY_SUCCESS.toString());
        }
        if (failParseLogList.size() > 0) {
            parseLogMapper.batchUpdateParseLog(failParseLogList, "admin");
        }
        watch.stop();
        log.info("执行完毕:销售共花费:" + watch.getTotalTimeSeconds() + "秒,总共清洗数据:" + testinteger.get() + "条");

    }

    @Override
    public void parseLogBatchInsert(List<ParseLogDTO> logDTOList) {
        if (!CollectionUtils.isEmpty(logDTOList)) {
            int size = logDTOList.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > logDTOList.size()) {
                    end = logDTOList.size();
                }
                logMapper.parseLogBatchInsert(logDTOList.subList(start, end));
            }
        }
    }

    /**
     * 质检规则名称分类处理
     *
     * @param qualityRule type 1字段类型 2 字段必填 3 文件内数据重复
     */
    public void qualityParseData(List<OriginSale> qurule, List<FileHandleRule> fileHandleRuleList, QualityRule qualityRule, FileParseLog fileParseLog, String contentStr, AtomicInteger atcInteger) throws Exception {
        Map<String, String> fieldMap = ConstantCacheMap.FIELD_MAPPING_MAP;
        /**有任何一个校验不符合，整个sale数据集合(表示一个excel中文件)将全部不落地最终质检后库，只记录log*/
        //boolean isJudgePass=true;
        //记录不符合校验数据
        List<ParseLogDTO> errorQualiData = new CopyOnWriteArrayList<>();
        if (qualityRule.getRuleType() == 1) {
            fileHandleRuleList.parallelStream().forEach(k -> {
                //1配置的字段是否存在于映射map关系中
                if (fieldMap.containsKey(k.getFieldName())) {
                    //存在规则，获取配置类型，校验类型是否符合
                    String typeStr = k.getFieldType();
                    if (typeStr.equals(CommonUtil.FIELD_TYPE_DATE)) {//日期类型
                        qurule.parallelStream().forEach(n -> {
                            try {
                                String getStr = String.valueOf(ListMapCommonUtils.getFiledValue(n, fieldMap.get(k.getFieldName())));
                                //如果为非必填，获取值，在判断是否为空，为空也是符合数据，不参与校验类型，如果为必填，则空也是失败数据
                                if (k.getRequired() == 0 && StringUtils.isEmpty(getStr)) {
                                    return;
                                }
                                //if(!k.getRequired()&&StringUtils.isEmpty(getStr)){
                                if (ListMapCommonUtils.judgeTime(getStr) == null) {//等于null表示不是时间格式，失败
                                    //System.out.println("销售源数据集合中:"+fieldMap.get(k.getFieldName())+"字段不是时间格式");
                                    /**记录日志表
                                     * 暂时采用两种办法，后面比较效率
                                     * 1.将检测结果存为合格和不合格两个集合，最后全部结束后，处理批量入库
                                     * 2.单独处理，检测错误信息单条入库*/
                                    ParseLogDTO logDTO = new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(n.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(k.getFieldName() + "字段类型应为" + CommonUtil.FIELD_TYPE.get(CommonUtil.FIELD_TYPE_DATE));
                                    logDTO.setJudgeType(1);
                                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                                    logDTO.setJudgeMatchingId(qualityRule.getId());
                                    logDTO.setRowNum(n.getRowNum());
                                    logDTO.setFileName(fileParseLog.getFileName());
                                    logDTO.setCreateTime(new Date());
                                    logDTO.setCreateBy(fileParseLog.getUpdateBy());
                                    logDTO.setUpdateTime(new Date());
                                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                                    logDTO.setTenantId(fileParseLog.getTenantId());
                                    logDTO.setVersion(fileParseLog.getVersion());
                                    errorQualiData.add(logDTO);
                                }
                                //}

                            } catch (Exception e) {
                                log.error("质检处理日期错误:" + e.getMessage());
                            }
                        });
                    } else if (typeStr.equals(CommonUtil.FIELD_TYPE_NUM)) {//数字类型
                        /*for(OriginSale p:qurule){
                            try {
                                String getStr=String.valueOf(ListMapCommonUtils.getFiledValue(p,fieldMap.get(k.getFieldName())));
                                if(!k.getRequired()&&StringUtils.isEmpty(getStr)){
                                    return;
                                }
                                if(!ListMapCommonUtils.isNumeric(getStr)){//为true表示为数字,false表示不符合格式，应该为数字
                                    //System.out.println("销售源数据集合中:"+fieldMap.get(k.getFieldName())+"字段不是数字格式");
                                    ParseLogDTO logDTO=new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionId(fileParseLog.getProjectInstitutionId());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(p.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(LocalDateTime.now());
                                    logDTO.setJudgeMsg(k.getFieldName()+"字段类型应为"+CommonUtil.FIELD_TYPE.get(CommonUtil.FIELD_TYPE_NUM));
                                    logDTO.setJudgeType(1);
                                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                                    logDTO.setJudgeMatchingId(qualityRule.getId());
                                    logDTO.setRowNumber(p.getRowNumber());
                                    logDTO.setFileName(fileParseLog.getFileName());
                                    logDTO.setCreatedTime(LocalDateTime.now());
                                    logDTO.setCreator("admin");
                                    logDTO.setModifiedTime(LocalDateTime.now());
                                    logDTO.setModifier("admin");
                                    errorQualiData.add(logDTO);
                                }
                            } catch (Exception e) {
                                log.error("质检处理数字类型错误:"+e.getMessage());
                            }
                        }*/
                        qurule.parallelStream().forEach(p -> {
                            try {
                                String getStr = String.valueOf(ListMapCommonUtils.getFiledValue(p, fieldMap.get(k.getFieldName())));
                                if (k.getRequired() == 0 && StringUtils.isEmpty(getStr)) {
                                    return;
                                }
                                if (!ListMapCommonUtils.isNumeric(getStr)) {//为true表示为数字,false表示不符合格式，应该为数字
                                    //System.out.println("销售源数据集合中:"+fieldMap.get(k.getFieldName())+"字段不是数字格式");
                                    ParseLogDTO logDTO = new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(p.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(k.getFieldName() + "字段类型应为" + CommonUtil.FIELD_TYPE.get(CommonUtil.FIELD_TYPE_NUM));
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
                                log.error("质检处理数字类型错误:" + e.getMessage());
                            }
                        });
                    }
                }
            });
        } else if (qualityRule.getRuleType() == 2) {
            fileHandleRuleList.parallelStream().forEach(f -> {
                //1配置的字段是否存在于映射map关系中
                if (fieldMap.containsKey(f.getFieldName())) {
                    //获取是否必填
                    int isRequired = f.getRequired();
                    qurule.parallelStream().forEach(a -> {
                        try {
                            String numStr = String.valueOf(ListMapCommonUtils.getFiledValue(a, fieldMap.get(f.getFieldName())));
                            if (isRequired == 1) {//必填
                                if (StringUtils.isEmpty(numStr) || numStr.equals("null") || numStr.length() == 0) {
                                    ParseLogDTO logDTO = new ParseLogDTO();
                                    logDTO.setId(UUID.randomUUID().toString());
                                    logDTO.setProjectId(fileParseLog.getProjectId());
                                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                                    logDTO.setOriginId(a.getId());
                                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                                    logDTO.setJudgeTime(new Date());
                                    logDTO.setJudgeMsg(f.getFieldName() + "字段类型应必填");
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
        } else if (qualityRule.getRuleType() == 3) {
            if (contentStr.contains(",")) {
                //将rule_content中文字段转换为数据实体类对应字段
                StringBuffer sta = new StringBuffer();
                String[] bzhi = contentStr.split(",");
                for (int m = 0; m < bzhi.length; m++) {
                    if (fieldMap.containsKey(bzhi[m])) {
                        sta.append(fieldMap.get(bzhi[m])).append(",");
                    }
                }
                contentStr = sta.deleteCharAt(sta.length() - 1).toString();
            } else {
                contentStr = fieldMap.get(contentStr);
            }
            Map<String, OriginSale> map = new HashMap<>();
            Map<String, String> judgeMap = new HashMap<>();
            Set<OriginSale> seList = new HashSet<>();
            for (OriginSale sale : qurule) {
                String keyValue = ListMapCommonUtils.parseListBeanByName(contentStr, sale);
                //重复返回的是第一次put进map中数据的value值
                OriginSale tempValString = map.put(keyValue, sale);
                //不为空表示重复
                if (tempValString != null) {
                    if (StringUtils.isEmpty(judgeMap.get(keyValue)) || judgeMap.get(keyValue) == null) {
                        //1.将第一条数据入set集合
                        seList.add(tempValString);
                        //2.将存储第一条数据map对应key值的value值置为非null
                        judgeMap.put(keyValue, keyValue);
                    }
                    //使用去重性质集合保存第一次存入map中数据
                    ParseLogDTO logDTO = new ParseLogDTO();
                    logDTO.setId(UUID.randomUUID().toString());
                    logDTO.setProjectId(fileParseLog.getProjectId());
                    logDTO.setProjectInstitutionCode(fileParseLog.getProjectInstitutionCode());
                    logDTO.setBusinessType(fileParseLog.getBusinessType());
                    logDTO.setOriginId(sale.getId());
                    logDTO.setJudgeStatus(FileQualityStatus.QUALITY_FAILURE.toString());
                    logDTO.setJudgeTime(new Date());
                    logDTO.setJudgeMsg("文件内重复");
                    logDTO.setJudgeType(3);
                    logDTO.setJudgeStandard(qualityRule.getHandleProcess());
                    logDTO.setJudgeMatchingId(qualityRule.getId());
                    logDTO.setRowNum(sale.getRowNum());
                    logDTO.setFileName(fileParseLog.getFileName());
                    logDTO.setCreateTime(new Date());
                    logDTO.setCreateBy(fileParseLog.getCreateBy());
                    logDTO.setUpdateTime(new Date());
                    logDTO.setUpdateBy(fileParseLog.getUpdateBy());
                    logDTO.setTenantId(fileParseLog.getTenantId());
                    logDTO.setVersion(fileParseLog.getVersion());
                    errorQualiData.add(logDTO);
                } else {
                    /*非重复可以新入一个集合，此集合中所存储数据为绝不重复数据，当然包含可入和不可入日志库全部数据，利用这一性质，上层可以再次判空
                     * 通过获取key值，为Null,表示已经有此key值存在，而且是第一条数据
                     * */
                    judgeMap.put(keyValue, null);
                }
            }
            //全部结束，遍历集合map，将数据放入errorQualiData
            for (OriginSale org : seList) {
                ParseLogDTO logDTO = new ParseLogDTO();
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
        if (errorQualiData.size() > 0) {
            /*List<String> numCount=errorQualiData.stream().collect(Collectors.groupingBy(a ->a.getJudgeStandard(),Collectors.counting()))
                    .entrySet().stream().filter(ent ->ent.getValue()>1)
                    .map(ety ->ety.getKey()).collect(Collectors.toList());*/
            //if(numCount.contains(CommonUtil.QUALITY_PROCESS_BLOCK)){
            List<ParseLog> judgeList = errorQualiData.stream().filter(a -> a.getJudgeStandard().equals(CommonUtil.QUALITY_PROCESS_BLOCK)).collect(Collectors.toList());
            if (judgeList.size() != 0) {
                //System.out.println("错误集合:"+judgeList.size());
                log.info("文件{}中有质检阻断错误,全部不入库" + fileParseLog.getId());
                atcInteger.incrementAndGet();
            }
            //处理错误数据记录批量入库
            //logMapper.batchInsert(errorQualiData);
            parseLogBatchInsert(errorQualiData);
        }
    }

    private InspectSale formatToInspectSale(OriginSale originSale) {
        InspectSale inspectSale = new InspectSale();
        BeanUtils.copyProperties(originSale, inspectSale);
        inspectSale.setId(UUID.randomUUID().toString());
        inspectSale.setOriginSaleId(originSale.getId());
        inspectSale.setFileId(originSale.getFileId());
        inspectSale.setFromInstitutionCode(originSale.getInstitutionCode());
        inspectSale.setTenantId(originSale.getTenantId());
        //inspectSale.setProjectId(originSale.getProjectId());
        return inspectSale;
    }
}
