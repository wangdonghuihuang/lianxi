package com.softium.datacenter.paas.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.excel.MatchDistribDTO;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.Source;
import com.softium.datacenter.paas.api.enums.TodoType;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.dto.IndustryDisplayDTO;
import com.softium.datacenter.paas.web.dto.InstitutionMatchDTO;
import com.softium.datacenter.paas.web.dto.ProductMatchDTO;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.web.manage.MatchSvcService;
import com.softium.datacenter.paas.web.service.InspectDataService;
import com.softium.datacenter.paas.web.service.InstitutionMappingService;
import com.softium.datacenter.paas.web.service.MatchService;
import com.softium.datacenter.paas.web.service.RinseMessageService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.mapping.Mapper;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.service.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * 匹配服务
 * @author net
 * @since 2020-11-17 14:25:13
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private InstitutionMapper institutionMapper;
    @Autowired
    private Mapper mapper;
    @Autowired
    private InspectSaleMapper inspectSaleMapper;
    @Autowired
    private InstitutionMappingService institutionMappingService;
    @Autowired
    private DistributorService distributorService;
    @Autowired
    private InstitutionMappingMapper institutionMappingMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductMappingMapper productMappingMapper;
    @Autowired
    private ProductUnitMappingMapper productUnitMappingMapper;
    @Autowired
    private RinseMessageService rinseMessageService;
    @Autowired
    private InspectDataService inspectDataService;
    @Autowired
    private MatchSvcService matchSvcService;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    IntelligentMatchMapper intelligentMatchMapper;

    @Override
    public void fromInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException {
        //先校验institution_mapping是否存在，不存在插入  -》 mq通知清洗
        String mappingId = dealInstitution(institutionMatchDTO, "fromInstitution");
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(institutionMatchDTO.getInspectSaleDTO());
        stringObjectMap.put("mappingId", mappingId);
        stringObjectMapList.add(stringObjectMap);
        //mq通知经销商 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.FROM_INSTITUTION);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(institutionMatchDTO.getInspectSaleDTO().getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void cancelFromInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException {
        //  经销商失效  经销商回退 (数量,单位,产品,客户)  上下游机构id 品规id 单位 数量 置为null
        InstitutionMapping institutionMapping = institutionMappingMapper.get(institutionMatchDTO.getInspectSaleDTO().getFromInstitutionIdFormat());
        institutionMapping.setDisabled(1);
        institutionMappingMapper.updateSelective(institutionMapping);
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), SystemContext.getTenantId());
        inspectSaleMapper.fromInstitutionBack(inspectTableName, institutionMatchDTO.getInspectSaleDTO().getFromInstitutionIdFormat(),
                institutionMatchDTO.getInspectSaleDTO().getPeriodId(), SystemContext.getTenantId());
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(institutionMatchDTO.getInspectSaleDTO());
        stringObjectMapList.add(stringObjectMap);
        //mq通知经销商 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.FROM_INSTITUTION);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(institutionMatchDTO.getInspectSaleDTO().getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void toInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException {
        //先校验institution_mapping是否存在，不存在插入  -》 插入institutionMapping  -》 mq通知继续清洗
        String mappingId = dealInstitution(institutionMatchDTO, "toInstitution");
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(institutionMatchDTO.getInspectSaleDTO());
        stringObjectMap.put("mappingId", mappingId);
        stringObjectMapList.add(stringObjectMap);
        // mq通知机构 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.TO_INSTITUTION);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(institutionMatchDTO.getInspectSaleDTO().getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void cancelToInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException {
        InstitutionMapping institutionMapping = institutionMappingMapper.get(institutionMatchDTO.getInspectSaleDTO().getToInstitutionIdFormat());
        institutionMapping.setDisabled(1);
        institutionMappingMapper.updateSelective(institutionMapping);
        //机构失效   下游机构id置为null
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), SystemContext.getTenantId());
        inspectSaleMapper.toInstitutionBack(inspectTableName, institutionMatchDTO.getInspectSaleDTO().getToInstitutionIdFormat(),
                institutionMatchDTO.getInspectSaleDTO().getPeriodId(), SystemContext.getTenantId());
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(institutionMatchDTO.getInspectSaleDTO());
        stringObjectMapList.add(stringObjectMap);
        // mq通知机构 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.TO_INSTITUTION);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(institutionMatchDTO.getInspectSaleDTO().getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void productMatch(ProductMatchDTO productMatchDTO) throws IOException {
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        productMatchDTO.getInspectSaleDTOList().forEach(getInspectSaleDTO -> {
            ProductMapping sproductMapping = new ProductMapping();
            sproductMapping.setInstitutionId(getInspectSaleDTO.getFromInstitutionIdFormat());
            sproductMapping.setStandardProductCode(productMatchDTO.getProductDTO().getCode());
            sproductMapping.setStandardProductId(productMatchDTO.getProductDTO().getCode());
            sproductMapping.setStandardProductName(productMatchDTO.getProductDTO().getName());
            sproductMapping.setStandardProductSpec(productMatchDTO.getProductDTO().getSpecification());
            sproductMapping.setStandardProductParentCode(productMatchDTO.getProductDTO().getParentId());
            sproductMapping.setStandardProductUnit(productMatchDTO.getProductDTO().getUnit());
            sproductMapping.setDisabled(0);
            sproductMapping.setOriginalProductCode(getInspectSaleDTO.getProductCode());
            sproductMapping.setOriginalProductName(getInspectSaleDTO.getProductName());
            sproductMapping.setOriginalProductSpec(getInspectSaleDTO.getProductSpec());
            sproductMapping.setSource(Source.MANUAL.name());
            productMappingMapper.insert(sproductMapping);
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(getInspectSaleDTO);
            stringObjectMap.put("mappingId", sproductMapping.getId());
            stringObjectMapList.add(stringObjectMap);
        });

        // mq通知产品清洗成功 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.PRODUCT);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(productMatchDTO.getInspectSaleDTOList().get(0).getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void cancelProductMatch(ProductMatchDTO productMatchDTO) throws IOException {
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        productMatchDTO.getInspectSaleDTOList().forEach(inspectSaleDTO -> {
            ProductMapping productMapping = productMappingMapper.get(inspectSaleDTO.getProductIdFormat());
            productMapping.setDisabled(1);
            productMappingMapper.updateSelective(productMapping);
            // 产品  品规id置为null
            String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), SystemContext.getTenantId());
            inspectSaleMapper.productBack(inspectTableName, inspectSaleDTO.getProductIdFormat(),
                    inspectSaleDTO.getPeriodId(), SystemContext.getTenantId());
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(inspectSaleDTO);
            stringObjectMapList.add(stringObjectMap);
        });

        // mq通知产品清洗成功 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.PRODUCT);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(productMatchDTO.getInspectSaleDTOList().get(0).getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public void productUnitMatch(ProductMatchDTO productMatchDTO) throws IOException {
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        productMatchDTO.getInspectSaleDTOList().forEach(inspectSaleDTO -> {
            //校验单位
            ProductUnitMapping productUnitMapping = new ProductUnitMapping();
            productUnitMapping.setDisabled(0);
            productUnitMapping.setInstitutionId(inspectSaleDTO.getFromInstitutionIdFormat());
            productUnitMapping.setProductSpecId(inspectSaleDTO.getProductIdFormat());
            productUnitMapping.setOriginalUnit(inspectSaleDTO.getProductUnit());
            productUnitMapping.setStandardRatio(productMatchDTO.getProductDTO().getStandardRatio());
            productUnitMapping.setOriginalRatio(productMatchDTO.getProductDTO().getOriginalRatio());
            productUnitMappingMapper.insert(productUnitMapping);
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(inspectSaleDTO);
            stringObjectMap.put("mappingId", productUnitMapping.getId());
            stringObjectMapList.add(stringObjectMap);
        });

        // 清洗完单位 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.PRODUCT_UNIT);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(productMatchDTO.getInspectSaleDTOList().get(0).getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);

    }

    @Override
    public void cancelProductUnitMatch(ProductMatchDTO productMatchDTO) throws IOException {
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        productMatchDTO.getInspectSaleDTOList().forEach(inspectSaleDTO -> {
            com.softium.framework.common.query.Criteria<ProductUnitMapping> productUnitMappingCriteria = new Criteria<>();
            productUnitMappingCriteria.and(ProductUnitMapping::getOriginalUnit, Operator.equal, inspectSaleDTO.getProductUnit());
            productUnitMappingCriteria.and(ProductUnitMapping::getProductSpecId, Operator.equal, inspectSaleDTO.getProductIdFormat());

            List<ProductUnitMapping> productUnitMappings = productUnitMappingMapper.findByCriteria(productUnitMappingCriteria);
            productUnitMappings.forEach(s -> {
                s.setDisabled(1);
                productUnitMappingMapper.updateSelective(s);
            });
            // 产品  品规id置为null
            String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), SystemContext.getTenantId());
            inspectSaleMapper.productUnitBack(inspectTableName, inspectSaleDTO.getProductIdFormat(),
                    inspectSaleDTO.getPeriodId(), SystemContext.getTenantId());
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(inspectSaleDTO);
            stringObjectMapList.add(stringObjectMap);
        });

        // 清洗完单位 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.PRODUCT_UNIT);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(productMatchDTO.getInspectSaleDTOList().get(0).getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }

    @Override
    public InstitutionMatchDTO addFromInstitution(InstitutionMatchDTO dto) throws IOException {
        ActionResult<InstitutionDTO> institutionDTOActionResult = distributorService.addInstitution(dto.getInstitutionDTO(), SystemContext.getTenantId());
        fromInstitutionMatch(dto);
        InstitutionMatchDTO matchDTO=new InstitutionMatchDTO();
        matchDTO.setInstitutionDTO(institutionDTOActionResult.getData());
        matchDTO.setInspectSaleDTO(dto.getInspectSaleDTO());
        return dto;
    }

    @Override
    public InstitutionMatchDTO addToInstitution(InstitutionMatchDTO dto) throws IOException {
        ActionResult<InstitutionDTO> actionResult = distributorService.addInstitution(dto.getInstitutionDTO(), SystemContext.getTenantId());
        toInstitutionMatch(dto);
        InstitutionMatchDTO matchDTO=new InstitutionMatchDTO();
        matchDTO.setInstitutionDTO(actionResult.getData());
        matchDTO.setInspectSaleDTO(dto.getInspectSaleDTO());
        return dto;
    }

    @Override
    public SaleCountDto getMatchCount(String status, String periodId) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class), SystemContext.getTenantId());
        //InspectStatus
        switch (status) {
            case "fromInstitution":
                return inspectSaleMapper.getFromInstitutionCount(inspectTableName, SystemContext.getTenantId(), periodId);
            case "toInstitution":
                return inspectSaleMapper.getToInstitutionCount(inspectTableName, SystemContext.getTenantId(), periodId);
            case "product":
                return inspectSaleMapper.getProductCount(inspectTableName, SystemContext.getTenantId(), periodId);
            case "unit":
                return inspectSaleMapper.getProductUntiCount(inspectTableName, SystemContext.getTenantId(), periodId);
            default:
                throw new BusinessException(new ErrorInfo("param.error", "参数错误"));
        }
    }

    @Override
    public void fromIndustyMatch() {
        //获取所有未匹配的数据 ->  调用主数据行业库智能匹配 -> 匹配上的调用清洗
        List<MatchDistribDTO> distribDTOS = inspectDataService.queryExportDistribService(null);
        List<IntelligentMatchingDTO> source = new ArrayList<>();
        distribDTOS.forEach(a -> {
            IntelligentMatchingDTO intelligentMatchingDTO = new IntelligentMatchingDTO();
            intelligentMatchingDTO.setName(a.getFromInstitutionName());
            intelligentMatchingDTO.setUniqueId(a.getFromInstitutionName());
            source.add(intelligentMatchingDTO);
        });
        IntelligentMatchingRequestDTO intelligentMatchingRequestDTO = new IntelligentMatchingRequestDTO();
        intelligentMatchingRequestDTO.setSource(source);
        try {
            ActionResult<List<IntelligentMatchingDTO>> listActionResult = matchSvcService.intelligentMatchingList(intelligentMatchingRequestDTO, SystemContext.getTenantId(), "enterprise-hco");
            if (!listActionResult.isSuccess()) {
                throw new BusinessException(new ErrorInfo("response error", "远程调用失败"));
            }
            List<IntelligentMatchingDTO> result = listActionResult.getData();
            List<InstitutionMatchDTO> institutionMatchDTOList = new ArrayList<>();
            //todo 拼装对象，待自测
            //主数据返回集合对象result，与我方数据distribDTOS进行匹配比较，匹配字段--uniqueId
            for (IntelligentMatchingDTO matchingDTO : result) {
                for (MatchDistribDTO distribDTO : distribDTOS) {
                    if (matchingDTO.getUniqueId().equals(distribDTO.getFromInstitutionName())) {
                        InstitutionMatchDTO institutionMatchDTO = new InstitutionMatchDTO();
                        InstitutionDTO institutionDTO = new InstitutionDTO();//封装主数据返回数据
                        BeanUtils.copyProperties(matchingDTO, institutionDTO);
                        InspectSaleDTO saleDTO = new InspectSaleDTO();//封装本系统数据
                        BeanUtils.copyProperties(distribDTO, saleDTO);
                        institutionMatchDTO.setInstitutionDTO(institutionDTO);
                        institutionMatchDTO.setInspectSaleDTO(saleDTO);
                        institutionMatchDTOList.add(institutionMatchDTO);
                    }
                }
            }
            fromInstitutionMatchBatch(institutionMatchDTOList);
        } catch (Exception e) {
            throw new BusinessException(new ErrorInfo("request error", "远程调用失败"));
        }


    }

    private void fromInstitutionMatchBatch(List<InstitutionMatchDTO> institutionMatchDTOList) throws IOException {
        List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
        institutionMatchDTOList.forEach(institutionMatchDTO -> {
            //先校验institution_mapping是否存在，不存在插入  -》 mq通知清洗
            String mappingId = dealInstitution(institutionMatchDTO, "fromInstitution");
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(institutionMatchDTO.getInspectSaleDTO());
            stringObjectMap.put("mappingId", mappingId);
            stringObjectMapList.add(stringObjectMap);
        });

        //mq通知经销商 继续清洗
        RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
        rinseMessageDTO.setTenantId(SystemContext.getTenantId());
        rinseMessageDTO.setUserId(SystemContext.getUserId());
        rinseMessageDTO.setTodoType(TodoType.FROM_INSTITUTION);
        rinseMessageDTO.setTodoParams(stringObjectMapList);
        rinseMessageDTO.setPeriodId(institutionMatchDTOList.get(0).getInspectSaleDTO().getPeriodId());
        rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
    }


    @Override
    public void toIndustyMatch() {

    }

    private String dealInstitution(InstitutionMatchDTO institutionMatchDTO, String type) {
        if (null == institutionMatchDTO || null == institutionMatchDTO.getInstitutionDTO() || null == institutionMatchDTO.getInspectSaleDTO()) {
            throw new BusinessException(new ErrorInfo("param error", "传参错误"));
        }
        //保存上下游关系mapping
        //TODO 校验mapping的重复
        InstitutionMapping fromInstitutionMapping = new InstitutionMapping();
        fromInstitutionMapping.setStandardInstitutionId(institutionMatchDTO.getInstitutionDTO().getCode());
        fromInstitutionMapping.setStandardInstitutionCode(institutionMatchDTO.getInstitutionDTO().getCode());
        fromInstitutionMapping.setStandardInstitutionName(institutionMatchDTO.getInstitutionDTO().getName());
        fromInstitutionMapping.setStandardInstitutionProvinceName(institutionMatchDTO.getInstitutionDTO().getProvince());
        fromInstitutionMapping.setStandardInstitutionCityName(institutionMatchDTO.getInstitutionDTO().getCity());
        fromInstitutionMapping.setSource(Source.MANUAL.toString());
        fromInstitutionMapping.setDisabled(0);
        String existMappingId;
        switch (type) {
            case "toInstitution":
                fromInstitutionMapping.setInstitutionId(institutionMatchDTO.getInspectSaleDTO().getFromInstitutionIdFormat());
                fromInstitutionMapping.setInstitutionCode(institutionMatchDTO.getInspectSaleDTO().getStandardInstitutionCode());
                fromInstitutionMapping.setInstitutionName(institutionMatchDTO.getInspectSaleDTO().getStandardInstitutionName());
                fromInstitutionMapping.setInstitutionProvinceName(institutionMatchDTO.getInspectSaleDTO().getStandardInstitutionProvince());
                fromInstitutionMapping.setInstitutionCityName(institutionMatchDTO.getInspectSaleDTO().getStandardInstitutionCity());
                fromInstitutionMapping.setOriginalInstitutionCode(institutionMatchDTO.getInspectSaleDTO().getToInstitutionCode());
                fromInstitutionMapping.setOriginalInstitutionName(institutionMatchDTO.getInspectSaleDTO().getToInstitutionName());
                break;
            case "fromInstitution":
                fromInstitutionMapping.setOriginalInstitutionCode(institutionMatchDTO.getInspectSaleDTO().getFromInstitutionCode());
                fromInstitutionMapping.setOriginalInstitutionName(institutionMatchDTO.getInspectSaleDTO().getFromInstitutionName());
                break;
            default:
                break;
        }
        institutionMappingMapper.insert(fromInstitutionMapping);
        return fromInstitutionMapping.getId();
    }

    @Override
    public IndustryDisplayDTO queryDisplayService(String tabName) {
        IndustryDisplayDTO displayDTO = new IndustryDisplayDTO();
        //行业库推荐按钮显示
        Criteria<IntelligentMatch> criteria = Criteria.from(IntelligentMatch.class);
        criteria.and(IntelligentMatch::getMatchRule, Operator.equal, "INDUSTRY_MATCH");
        List<IntelligentMatch> matches = intelligentMatchMapper.findByCriteria(criteria);
        int isintelligence = 0;
        int isrecommend = 0;
        for (IntelligentMatch match : matches) {
            isintelligence = match.getDisabled();
            isrecommend = match.getAutoMatchPass();
        }
        if (tabName.equals("insideMatching")) {//行业库推荐按钮
            if (isintelligence == 1) {
                displayDTO.setRecommend(1);
            }
        } else {
            //调用主数据接口
            long stTime = (System.currentTimeMillis() / 1000) % 60;
            String tenantId = SystemContext.getTenantId();
            ActionResult<MdmConfigDTO> configDTO = distributorService.getMdmConfig(tenantId);
            long edTime = (System.currentTimeMillis() / 1000) % 60;
            log.info("get-setting-config-time-{}second", edTime - stTime);
            if (configDTO.getData() != null) {
                log.info("获取主数据返回:" + configDTO.getData().getIsInstitutionAutomaticCode());
                if (configDTO.getData().getIsInstitutionAutomaticCode() != null) {
                    boolean isInstitutionAutomaticCode = configDTO.getData().getIsInstitutionAutomaticCode();
                    //为true表示开启，在结合表中配置两个条件，都满足及展示按钮
                    if (isInstitutionAutomaticCode && isintelligence == 1 && isrecommend == 1) {
                        displayDTO.setIntelligence(1);
                    }
                }
            }
        }
        return displayDTO;
    }
}
