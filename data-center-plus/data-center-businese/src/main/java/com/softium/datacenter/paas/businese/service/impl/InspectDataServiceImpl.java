package com.softium.datacenter.paas.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.excel.InterceptBillDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchDistribDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchMechanismDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchProductDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.InspectStatus;
import com.softium.datacenter.paas.api.enums.TodoType;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.InspectDataService;
import com.softium.datacenter.paas.web.service.RinseMessageService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.service.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author huashan.li
 */
@Service
public class InspectDataServiceImpl implements InspectDataService {
    @Autowired
    private InspectSaleMapper inspectSaleMapper;
    @Autowired
    private InspectPurchaseMapper inspectPurchaseMapper;
    @Autowired
    private InspectInventoryMapper inspectInventoryMapper;
    private static final Integer BATCH_SIZE = 50;
    @Autowired
    ProjectMapper projectMapper;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    private OriginSaleMapper originSaleMapper;
    @Autowired
    private RinseMessageService rinseMessageService;
    @Override
    public List<InspectSaleDTO> saleList(OriginDataQuery pageModel) {
        PageHelper.startPage(pageModel.getCurrent(),pageModel.getPageSize(),true);
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return  inspectSaleMapper.getInspectSaleList(inspectTableName,originTableName,pageModel, SystemContext.getTenantId());
    }

    @Override
    public InspectSaleDTO getInspectId(String id) {
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return inspectSaleMapper.getInspectSaleById(inspectTableName,originTableName,id,SystemContext.getTenantId());
    }

    @Override
    public void inspectSalebatchInsert(String tableName,List<InspectSale> inspectSales) {
        if (!CollectionUtils.isEmpty(inspectSales)) {
            int size = inspectSales.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > inspectSales.size()) {
                    end = inspectSales.size();
                }
                inspectSaleMapper.inspectSalebatchInsert(tableName,inspectSales.subList(start, end));
            }
        }
        //inspectSaleMapper.batchInsert(inspectSales);
    }

    @Override
    public List<PurchaseDataDTO> purchaseList(OriginDataQuery pageModel) {
        PageHelper.startPage(pageModel.getCurrent(),pageModel.getPageSize(),true);
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectPurchase.class),SystemContext.getTenantId());
        return inspectPurchaseMapper.getInspectPurchaseList(inspectTableName,originTableName,pageModel,SystemContext.getTenantId());
    }

    @Override
    public PurchaseDataDTO getPurchaseId(String id) {
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectPurchase.class),SystemContext.getTenantId());
        return inspectPurchaseMapper.getId(inspectTableName,originTableName,id,SystemContext.getTenantId());
    }

    @Override
    public List<InventoryDataDTO> inventoryList(OriginDataQuery pageModel) {
        PageHelper.startPage(pageModel.getCurrent(),pageModel.getPageSize(),true);
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectInventory.class),SystemContext.getTenantId());
        return inspectInventoryMapper.getInspectInventoryList(inspectTableName,originTableName,pageModel,SystemContext.getTenantId());
    }

    @Override
    public InventoryDataDTO getInventoryId(String id) {
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectInventory.class),SystemContext.getTenantId());
        return inspectInventoryMapper.getId(inspectTableName,originTableName,id,SystemContext.getTenantId());
    }

    @Override
    public void batchInsertPurchase(String tableName,List<InspectPurchase> inspectPurchases) {
        if (!CollectionUtils.isEmpty(inspectPurchases)) {
            int size = inspectPurchases.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > inspectPurchases.size()) {
                    end = inspectPurchases.size();
                }
                inspectPurchaseMapper.inspectPurbatchInsert(tableName,inspectPurchases.subList(start, end));
            }
        }
        //inspectPurchaseMapper.batchInsert(inspectPurchases);
    }

    @Override
    public void batchInsertInventory(String tableName,List<InspectInventory> inspectInventories) {
        if (!CollectionUtils.isEmpty(inspectInventories)) {
            int size = inspectInventories.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > inspectInventories.size()) {
                    end = inspectInventories.size();
                }
                inspectInventoryMapper.inspectInvenbatchInsert(tableName,inspectInventories.subList(start, end));
            }
        }
       //inspectInventoryMapper.batchInsert(inspectInventories);
    }

    @Override
    public void batchDeleteSale(Set<String> ids) {
        //批量删除，同步删除ori表的数据
        for(String id : ids){
            InspectSale inspectSale = inspectSaleMapper.get(id);
            if(null == inspectSale){
                throw new BusinessException(new ErrorInfo("ERROR","数据错误"));
            }
            originSaleMapper.delete(inspectSale.getOriginSaleId());
            inspectSaleMapper.delete(id);
        }
    }

    @Override
    public void batchCancelBlocking(Set<String> ids, String status) throws IOException {
        if(!CollectionUtils.isEmpty(ids)){
            String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
            inspectSaleMapper.inspectSaleBatchUpdate(inspectTableName, ids, status, SystemContext.getUserId());
            List<String> stringList=new ArrayList<>(ids);
            String listId=String.join(",",stringList);
            //TODO 取消拦截后继续清洗，调用清洗接口
            // 清洗完单位 继续清洗
            //根据id获取数据对象集合
            List<Map<String, Object>> stringObjectMapList = new ArrayList<>();
            Criteria<InspectSale> criteria=Criteria.from(InspectSale.class);
            criteria.and(InspectSale::getId, Operator.in,listId);
            //List<InspectSale> byIdList = inspectSaleMapper.getByIdList(new ArrayList<>(ids));
            List<InspectSale> byIdList =inspectSaleMapper.findByCriteria(criteria);
            if(byIdList.size()>0){
                //查询对象集合
                byIdList.forEach(a-> {
                    Map<String, Object> stringObjectMap = BeanUtil.beanToMap(a);
                    stringObjectMapList.add(stringObjectMap);
                });
                RinseMessageDTO rinseMessageDTO = new RinseMessageDTO();
                rinseMessageDTO.setTenantId(SystemContext.getTenantId());
                rinseMessageDTO.setUserId(SystemContext.getUserId());
                rinseMessageDTO.setTodoParams(stringObjectMapList);
                rinseMessageDTO.setPeriodId(byIdList.get(0).getPeriodId());
                if(InspectStatus.bill.toString().equals(status)){
                    rinseMessageDTO.setTodoType(TodoType.TO_INSTITUTION);
                }else if(InspectStatus.period.toString().equals(status)){
                    rinseMessageDTO.setTodoType(TodoType.FROM_INSTITUTION);
                }
                rinseMessageService.sendManualHandleMsg(rinseMessageDTO);
            }
        }
    }

    @Override
    public SaleCountDto getSaleCount(String status,String periodId,String businessType) {
        if(StringUtils.isEmpty(status)){
            throw new BusinessException(new ErrorInfo("ERROR","status不能为空"));
        }
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        String columnStatus = "";
        if(InspectStatus.bill.toString().equals(status)){
            columnStatus = "conform_bill_print_status";
        }else if(InspectStatus.period.toString().equals(status)){
            columnStatus = "conform_period_status";
        }
        return inspectSaleMapper.getSaleCount(inspectTableName,columnStatus, SystemContext.getTenantId(),periodId,businessType);
    }

    @Override
    public List<CleaningStatusDTO> getCleaningStatus(String id) {
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        InspectSaleDTO inspectSaleDTO = inspectSaleMapper.getInspectSaleById(inspectTableName,originTableName,id,SystemContext.getTenantId());
        List<CleaningStatusDTO> cleaningStatusDTOS = new ArrayList<>();
        cleaningStatusDTOS.add(new CleaningStatusDTO("日期规则",inspectSaleDTO.getConformPeriodStatus()));
        cleaningStatusDTOS.add(new CleaningStatusDTO("打单规则",inspectSaleDTO.getConformBillPrintStatus()));
        cleaningStatusDTOS.add(new CleaningStatusDTO("经销商",inspectSaleDTO.getFromInstitutionRinseStatus()));
        cleaningStatusDTOS.add(new CleaningStatusDTO("客户",inspectSaleDTO.getToInstitutionRinseStatus()));
        cleaningStatusDTOS.add(new CleaningStatusDTO("产品",inspectSaleDTO.getProductRinseStatus()));
        cleaningStatusDTOS.add(new CleaningStatusDTO("单位",inspectSaleDTO.getProductUnitRinseStatus()));
        return cleaningStatusDTOS;
    }
    /**文件管理核查数据查看业务层*/
    @Override
    public ActionResult queryAllInsData(OriginDataQuery originDataQuery) {
        String tabName="";
        if(originDataQuery.getBusinessType().equals("SM")){
            tabName= shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
            List<InspectSaleDTO> list=inspectSaleMapper.getInspectSaleList(tabName,null,originDataQuery, SystemContext.getTenantId());
            PageInfo<List<InspectSaleDTO>> listPageInfo=new PageInfo(list);
            listPageInfo.setPageSize(originDataQuery.getPageSize());
            listPageInfo.setPageNum(originDataQuery.getCurrent());
            return new ActionResult<>(listPageInfo);
        }else if (originDataQuery.getBusinessType().equals("PM")){

        }
        return null;
    }

    @Override
    public ActionResult queryAllActiceData(OriginDataQuery originDataQuery) {
        String tabName="";
        if(originDataQuery.getBusinessType().equals("SM")){
            tabName= shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
            List<InspectSaleDTO> list=inspectSaleMapper.getInspectSaleList(tabName,null,originDataQuery, SystemContext.getTenantId());
            PageInfo<List<InspectSaleDTO>> listPageInfo=new PageInfo(list);
            listPageInfo.setPageSize(originDataQuery.getPageSize());
            listPageInfo.setPageNum(originDataQuery.getCurrent());
            return new ActionResult<>(listPageInfo);
        }else if (originDataQuery.getBusinessType().equals("PM")){

        }
        return null;
    }

    @Override
    public List<InspectSaleExportDTO> exportInspectSaleList(OriginDataQuery originDataQuery) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<InspectSaleExportDTO> inspectSaleExportDTOS = inspectSaleMapper.getExportInspectSaleList(inspectTableName,originDataQuery,SystemContext.getTenantId());
        return inspectSaleExportDTOS;
    }

    @Override
    public List<FormatSaleExportDTO> exportFormatSaleList(OriginDataQuery originDataQuery) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<FormatSaleExportDTO> inspectSaleExportDTOS = inspectSaleMapper.getExportFormatSaleList(inspectTableName,originDataQuery,SystemContext.getTenantId());
        return inspectSaleExportDTOS;
    }

    @Override
    public List<InspectSaleDTO> getFromInstitutionMatch(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
            String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
            return inspectSaleMapper.getFromInstitutionMatch(inspectTableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public List<InspectSaleDTO> getToInstitutionMatch(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return inspectSaleMapper.getToInstitutionMatch(inspectTableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public List<InspectSaleDTO> getProductMatch(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return inspectSaleMapper.getProductMatch(inspectTableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public List<InspectSaleDTO> getProductUnitMatch(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return inspectSaleMapper.getProductUnitMatch(inspectTableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public List<MatchDistribDTO> queryExportDistribService(String periodId) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<MatchDistribDTO> distribDTOS= inspectSaleMapper.queryAllMatchDistrib(periodId,inspectTableName,SystemContext.getTenantId());
        return distribDTOS;
    }

    @Override
    public List<MatchMechanismDTO> queryExportMechanisService(String periodId) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<MatchMechanismDTO> distribDTOS= inspectSaleMapper.queryAllMechanism(periodId,inspectTableName,SystemContext.getTenantId());
        return distribDTOS;
    }

    @Override
    public List<MatchProductDTO> queryExportProductService(String periodId) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<MatchProductDTO> distribDTOS= inspectSaleMapper.queryAllMatchProduct(periodId,inspectTableName,SystemContext.getTenantId());
        return distribDTOS;
    }

    @Override
    public List<InterceptBillDTO> queryExportBillService(String periodId) {
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        List<InterceptBillDTO> interceptBillDTOS= inspectSaleMapper.queryAllBillExport(inspectTableName,SystemContext.getTenantId(),periodId);
        return interceptBillDTOS;
    }

    @Override
    public List<InspectSaleDTO> billDateSearchService(OriginDataQuery pageModel) {
        PageHelper.startPage(pageModel.getCurrent(),pageModel.getPageSize(),true);
        String originTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
        String inspectTableName = shardingManager.getShardingTableNameByValue(ORMapping.get(InspectSale.class),SystemContext.getTenantId());
        return  inspectSaleMapper.getBillDateSearch(inspectTableName,originTableName,pageModel, SystemContext.getTenantId());
    }
}
