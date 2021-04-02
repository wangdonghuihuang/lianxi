package com.softium.datacenter.paas.web.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.BillPrintDTO;
import com.softium.datacenter.paas.api.dto.BillPrintExportListDTO;
import com.softium.datacenter.paas.api.dto.InstitutionDTO;
import com.softium.datacenter.paas.api.dto.PeriodDTO;
import com.softium.datacenter.paas.web.dto.excel.BillPrintImportDTO;
import com.softium.datacenter.paas.api.dto.query.BillPrintQuery;
import com.softium.datacenter.paas.api.entity.BillPrint;
import com.softium.datacenter.paas.api.entity.Period;
import com.softium.datacenter.paas.api.mapper.BillPrintMapper;
import com.softium.datacenter.paas.api.mapper.PeriodMapper;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.web.service.BillPrintService;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.datacenter.paas.web.utils.easy.cache.CacheExcelData;
import com.softium.datacenter.paas.web.utils.easy.input.CombinationResultModel;
import com.softium.datacenter.paas.web.utils.easy.input.EasyExcelService;
import com.softium.datacenter.paas.web.utils.easy.input.Message;
import com.softium.datacenter.paas.web.utils.easy.input.SimpExcelConsumer;
import com.softium.datacenter.paas.web.utils.easy.validate.MessageUtils;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.dto.PageRequest;
import com.softium.framework.common.dto.Pagination;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.mapping.Mapper;
import com.softium.framework.service.BusinessException;
import com.softium.framework.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/***
 *
 * @author net
 * @since 2020-11-16 19:24:37
 */
@Service
public class BillPrintServiceImpl implements BillPrintService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Mapper mapper;
    @Autowired
    private BillPrintMapper billPrintMapper;
    @Autowired
    private PeriodMapper periodMapper;
    @Autowired
    CacheExcelData cacheExcelData;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private DistributorService distributorService;
    @Autowired
    private PeriodService periodService;
    private static final Integer BATCH_SIZE = 100;
    @Override
    public Boolean saveOrUpdate(BillPrintDTO billPrintDTO) {
        PeriodDTO periodDTO = periodService.getUntreatedPeriod();
        List<BillPrint> billPrints = isBillPrintList(billPrintDTO.getInstitutionCode(),periodDTO.getId());
        BillPrint billPrint = new BillPrint();
        mapper.map(billPrintDTO, billPrint);
        if(StringUtils.isEmpty(billPrintDTO.getId())){
            if(!CollectionUtils.isEmpty(billPrints)){
                throw new BusinessException(new ErrorInfo("ERROR","打单名单重复！请勿添加"));
            }
            billPrint.setPrintStatus("ACTIVE");
            return billPrintMapper.insert(billPrint)>0 ? Boolean.TRUE : Boolean.FALSE;
        }else {
            if(CollectionUtils.isEmpty(billPrints)){
                return billPrintMapper.updateSelective(billPrint)>0 ? Boolean.TRUE : Boolean.FALSE;
            }else {
                BillPrint billPrint1 = billPrintMapper.getById(billPrintDTO.getId());
                if(!StringUtils.isEmpty(billPrintDTO.getInstitutionCode())){
                    if (!billPrintDTO.getInstitutionCode().equals(billPrint1.getInstitutionCode())) {
                        throw new BusinessException(new ErrorInfo("ERROR","打单名单重复！请勿添加"));
                    }
                }
                return billPrintMapper.updateSelective(billPrint)>0 ? Boolean.TRUE : Boolean.FALSE;
            }
        }
    }

    @Override
    public PageInfo<List<BillPrintDTO>> getBillPrintList(BillPrintQuery billPrintQuery) {
        PageRequest<BillPrint> pageRequest=new PageRequest<>();
        pageRequest.setNeedCount(true);
        pageRequest.setNeedPaging(true);
        pageRequest.setPageSize(billPrintQuery.getPageSize());
        pageRequest.setPageNo(billPrintQuery.getCurrent());
        Criteria<BillPrint> criteria = Criteria.from(BillPrint.class);
        if (!StringUtils.isEmpty(billPrintQuery.getId())){
            criteria.and(BillPrint::getId, Operator.equal, billPrintQuery.getId());
        }
        if(!StringUtils.isEmpty(billPrintQuery.getCollectType())) {
            criteria.and(BillPrint::getCollectType, Operator.equal, billPrintQuery.getCollectType());
        }
        if (!StringUtils.isEmpty(billPrintQuery.getInstitutionName())){
            criteria.and(BillPrint::getInstitutionCode, Operator.contains, billPrintQuery.getInstitutionName());
            criteria.or(BillPrint::getInstitutionName, Operator.contains, billPrintQuery.getInstitutionName());
        }
        if(!StringUtils.isEmpty(billPrintQuery.getProvince())){
            criteria.and(BillPrint::getProvince,Operator.contains,billPrintQuery.getProvince());
        }
        if(!StringUtils.isEmpty(billPrintQuery.getPeriodId())){
            criteria.and(BillPrint::getPeriodId,Operator.equal,billPrintQuery.getPeriodId());
        }
        if(!StringUtils.isEmpty(billPrintQuery.getPrintStatus())){
            criteria.and(BillPrint::getPrintStatus,Operator.equal,billPrintQuery.getPrintStatus());
        }
        pageRequest.setCriteria(criteria);
        Pagination<BillPrint> billPrintPagination = billPrintMapper.findPage(pageRequest);
        List<BillPrint> billPrintList = billPrintPagination.getRows();
        List<BillPrintDTO> billPrintDTOList = new ArrayList<>();
        billPrintList.forEach( a->{
            Period period = periodMapper.getById(a.getPeriodId());
            BillPrintDTO billPrintDTO1 = new BillPrintDTO();
            mapper.map(a,billPrintDTO1);
            billPrintDTO1.setPeriodName(period.getPeriodName());
            billPrintDTO1.setPeriodStatus(period.getPeriodStatus());
            billPrintDTOList.add(billPrintDTO1);
        });
        PageInfo pageInfo = new PageInfo(billPrintDTOList);
        pageInfo.setPageSize(billPrintQuery.getPageSize());
        pageInfo.setPageNum(billPrintQuery.getCurrent());
        pageInfo.setTotal(billPrintPagination.getTotal());
        return pageInfo;
    }

    @Override
    public CompletableFuture<Map> upload(InputStream inputStream, String name) {
        PeriodDTO periodDTO = periodService.getUntreatedPeriod();
        return CompletableFuture.supplyAsync(()->{
            try {
                Map<String, Object> map = new EasyExcelService<>(BillPrintImportDTO.class, cacheExcelData)
                        .producer(inputStream)
                        .batchNumber(1000)
                        .convertStorage(cacheExcelData)
                        .filterAndConvert((data) -> {
                            CombinationResultModel combinationResultModel = new CombinationResultModel();
                            List<BillPrintImportDTO> list = new ArrayList<>();
                            combinationResultModel.list = list;
                            List<BillPrintImportDTO> billPrintImportDTOList = data.getList();
                            //文件内 机构判重
                            for (int i = 0; i < billPrintImportDTOList.size() - 1; i++) {
                                String institutionCode = billPrintImportDTOList.get(i).getInstitutionCode();
                                for (int j = i + 1; j < billPrintImportDTOList.size(); j++) {
                                    String institutionCodeN = billPrintImportDTOList.get(j).getInstitutionCode();
                                    if (institutionCode.equals(institutionCodeN)) {
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(j + 1, "经销商编码重复(文件)"));
                                        return combinationResultModel;
                                    }
                                }
                            }
                            billPrintImportDTOList.stream().forEach(billPrintImportDTO -> {
                                boolean flg = true;
                                Integer rowIndex = billPrintImportDTO.getRowIndex();
                                String institutionCode = billPrintImportDTO.getInstitutionCode();
                                //调佣主数据接口查询 经销商是否存在
                                InstitutionDTO institutionDto = new InstitutionDTO();
                                institutionDto.setCode(institutionCode);
                                ActionResult<PageInfo<InstitutionDTO>> infoActionResult = distributorService.getInstitutionList(institutionDto,SystemContext.getTenantId());
                                if (!infoActionResult.isSuccess()) {
                                    throw new BusinessException(new ErrorInfo("error", "微服务接口调用失败"));
                                }
                                List<InstitutionDTO> institutionDTOS = infoActionResult.getData().getList();
                                if (CollectionUtils.isEmpty(institutionDTOS)) {
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 1, "经销商主数据中不存在"));
                                        flg = false;
                                } else {
                                    InstitutionDTO institutionDTO = institutionDTOS.get(0);
                                    List<BillPrint> billPrints = isBillPrintList(institutionCode,periodDTO.getId());
                                    if(CollectionUtils.isEmpty(billPrints)){
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 1, "此经销商本期打单已存在"));
                                        flg = false;
                                    }
                                    if("网查".equals(billPrintImportDTO.getDataSource()) || "邮件".equals(billPrintImportDTO.getDataSource())){
                                        billPrintImportDTO.setDataSource("网查".equals(billPrintImportDTO.getDataSource())? "WEB" : "EMAIL");
                                    }
                                    if("人工上传".equals(billPrintImportDTO.getCollectType()) || "DDI".equals(billPrintImportDTO.getCollectType())){
                                        billPrintImportDTO.setCollectType("人工上传".equals(billPrintImportDTO.getCollectType())? "MANUAL" : "DDI");
                                    }else {
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 3, "采集方式输入错误"));
                                        flg = false;
                                    }
                                    if("需打单".equals(billPrintImportDTO.getPrintStatus())|| "无需打单".equals(billPrintImportDTO.getPrintStatus())){
                                        billPrintImportDTO.setPrintStatus("需打单".equals(billPrintImportDTO.getPrintStatus())? "ACTIVE" : "INACTIVE");
                                    }else {
                                        MessageUtils.addErrorMessage(combinationResultModel.error, new Message(rowIndex, 5, "打单状态输入错误"));
                                        flg = false;
                                    }
                                    billPrintImportDTO.setProvince(institutionDTO.getProvince());
                                    billPrintImportDTO.setCity(institutionDTO.getCity());
                                    billPrintImportDTO.setCategory(institutionDTO.getCategory());
                                    billPrintImportDTO.setInstitutionId(institutionDTO.getCode());
                                }
                                if (flg) {
                                    list.add(billPrintImportDTO);
                                }
                            });
                            return combinationResultModel;
                        }).startWork().waitResult();
                map.put("fileName", name);
                map.put("requestUrl", "/billPrint/download/error?errorToken="+map.get(EasyExcelService.ERROR_TOKEN_KEY));
                return map;
            }catch (Exception e){
                logger.error("error [{}]", e);
                throw new BusinessException(new ErrorInfo("", "解析失败"));
            }
        });
    }

    @Override
    public Boolean commitExcel(String token, String fileName) throws IOException {
        PeriodDTO periodDTO = periodService.getUntreatedPeriod();
        new SimpExcelConsumer<BillPrintImportDTO>(cacheExcelData,token)
                .objectMapper(objectMapper,BillPrintImportDTO.class)
                .consumer(billPrintImportDTOS -> {
                    List<BillPrint> billPrints = new ArrayList<>();
                    billPrintImportDTOS.stream().forEachOrdered(billPrintImportDTO -> {
                        BillPrint billPrint = new BillPrint();
                        mapper.map(billPrintImportDTO,billPrint);
                        //todo
                        billPrint.setPeriodId(periodDTO.getId());
                        billPrints.add(billPrint);
                    });
                    if(!CollectionUtils.isEmpty(billPrints)){
                        batchInsert(billPrints);
                    }
                }).pullAll(2000);
        return Boolean.TRUE;
    }

    @Override
    public List<BillPrintExportListDTO> exportList(BillPrintQuery billPrintQuery) {
        return billPrintMapper.exportList(billPrintQuery, SystemContext.getTenantId());
    }

    @Override
    public BillPrintDTO getId(String id) {
        BillPrint billPrint = billPrintMapper.getById(id);
        Period period = periodMapper.getById(billPrint.getPeriodId());
        BillPrintDTO billPrintDTO = new BillPrintDTO();
        mapper.map(billPrint,billPrintDTO);
        billPrintDTO.setPeriodName(period.getPeriodName());
        return billPrintDTO;
    }

    @Override
    public Boolean copyBillPrint(String periodId) {
        Period period = periodMapper.getById(periodId);
        Criteria<BillPrint> criteria = Criteria.from(BillPrint.class);
        criteria.and(BillPrint::getPeriodId,Operator.equal,period.getPrePeriodId());
        List<BillPrint> billPrints = billPrintMapper.findByCriteria(criteria);
        List<BillPrint> billPrintList = new ArrayList<>();
        billPrints.forEach(b -> {
            BillPrint billPrint1 = new BillPrint();
            mapper.map(b,billPrint1);
            billPrint1.setId(UUIDUtils.getUUID());
            billPrint1.setPeriodId(periodId);
            billPrintList.add(billPrint1);
        });

        return billPrintMapper.batchInsert(billPrintList) >0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * 判断添加打单名单是否存在
     * @param institutionCode
     * @param periodId
     * @return
     */
    private List<BillPrint> isBillPrintList(String institutionCode,String periodId){
        Criteria<BillPrint> billPrintCriteria = Criteria.from(BillPrint.class);
        billPrintCriteria.and(BillPrint::getInstitutionCode,Operator.equal,institutionCode);
        billPrintCriteria.and(BillPrint::getPeriodId,Operator.equal,periodId);
        List<BillPrint> billPrints = billPrintMapper.findByCriteria(billPrintCriteria);
        return billPrints;
    }
    private void batchInsert(List<BillPrint> billPrints){
        /**
         * 默认一次100个 批量插入
         */
        if (!CollectionUtils.isEmpty(billPrints)) {
            int size = billPrints.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > billPrints.size()) {
                    end = billPrints.size();
                }
                billPrintMapper.batchInsert(billPrints.subList(start,end));
            }
        }
    }
}
