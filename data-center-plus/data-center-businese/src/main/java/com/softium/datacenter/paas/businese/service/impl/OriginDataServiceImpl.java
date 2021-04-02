package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.InventoryDataDTO;
import com.softium.datacenter.paas.api.dto.PurchaseDataDTO;
import com.softium.datacenter.paas.api.dto.SaleDataDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.entity.InspectSale;
import com.softium.datacenter.paas.api.entity.OriginInventory;
import com.softium.datacenter.paas.api.entity.OriginPurchase;
import com.softium.datacenter.paas.api.entity.OriginSale;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.OriginDataService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.database.Table;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.service.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author huashan.li
 */
@Service
public class OriginDataServiceImpl implements OriginDataService {
    @Autowired
    private OriginSaleMapper originSaleMapper;
    @Autowired
    private OriginPurchaseMapper originPurchaseMapper;
    @Autowired
    private OriginInventoryMapper originInventoryMapper;
    @Autowired
    private InspectSaleMapper inspectSaleMapper;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    ProjectMapper projectMapper;
    @Autowired
    FileParseLevelMapper fileParseLevelMapper;
    @Autowired
    FileParseLogMapper fileParseLogMapper;
    @Override
    public List<SaleDataDTO> saleList(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String tenantId = SystemContext.getTenantId();
        Class<?> saleClass= OriginSale.class;
        Table table= ORMapping.get(saleClass);
        String originSaleName =shardingManager.getShardingTableNameByValue(table,tenantId);
        return originSaleMapper.getOriginSaleList(originSaleName,originDataQuery,tenantId);
    }

    @Override
    public SaleDataDTO getId(String id) {
        String originSaleName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
        return originSaleMapper.getOriginSaleById(originSaleName,id,SystemContext.getTenantId());
    }

    @Override
    public List<PurchaseDataDTO> purchaseList(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String tableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
        return originPurchaseMapper.getList(tableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public PurchaseDataDTO getPurchaseId(String id) {
        String tableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
        return originPurchaseMapper.getId(tableName,id,SystemContext.getTenantId());
    }

    @Override
    public List<InventoryDataDTO> inventoryList(OriginDataQuery originDataQuery) {
        PageHelper.startPage(originDataQuery.getCurrent(),originDataQuery.getPageSize(),true);
        String tableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
        return originInventoryMapper.getList(tableName,originDataQuery,SystemContext.getTenantId());
    }

    @Override
    public InventoryDataDTO getInventoryId(String id) {
        String tableName = shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
        return originInventoryMapper.getId(tableName,id,SystemContext.getTenantId());
    }

    @Override
    public int deleteSale(List<SaleDataDTO> saleDataDTOS) {
        int num =0;
        for(int i=0 ; i< saleDataDTOS.size(); i++){
            //判断是否封板 （封板无法删除）
            if("UnArchive".equalsIgnoreCase(saleDataDTOS.get(i).getIsSeal())){
            /**
             * 原始数据删除
             */
           int count = originSaleMapper.delete(saleDataDTOS.get(i).getId());
            /**
             * 核查数据删除
             */
            Criteria<InspectSale> inspectSaleCriteria = new Criteria<>();
            inspectSaleCriteria.addCriterion(new Condition("originSaleId", Operator.equal,saleDataDTOS.get(i).getId()));
            inspectSaleMapper.deleteByCriteria(inspectSaleCriteria);
            /**
             * 交互数据删除
             */
            num+=count;
            }else {
                throw new BusinessException(new ErrorInfo("ERROR","数据ID为"+saleDataDTOS.get(i).getId()+"的数据已封板,已中断删除"));
            }
        }
        return num;
    }

    @Override
    public ActionResult queryAllData(OriginDataQuery queryDTO) {
        //ActionResult result=new ActionResult();
        /**todo  适应业务需求，现在做法是先根据id查父表，不存在，则为子表查询，存在根据父表id获取子表id，这样避免多表关联
         * 后续需要根据是日销售，月销售查询日表和月源数据表*/
        /*List<FileParseLog> parseLogs=fileParseLogMapper.findByProperty(FileParseLog::getId,queryDTO.getFileId());
        if(parseLogs!=null&&parseLogs.size()!=0){
            List<FileManagementDTO> list=fileParseLevelMapper.queryDataById(queryDTO.getFileId());
            for(FileManagementDTO dto:list){
                queryDTO.setFileId(dto.getId());
                break;
            }
        }*/
        //JSONObject allDataDTOS=new JSONObject();
        String tableNam="";
        if(queryDTO.getBusinessType().equals("SM")){
            tableNam= shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),SystemContext.getTenantId());
            List<SaleDataDTO> objects= originSaleMapper.getOriginSaleList(tableNam, queryDTO, SystemContext.getTenantId());
            PageInfo<List<SaleDataDTO>> pageInfo = new PageInfo(objects);
            pageInfo.setPageSize(queryDTO.getPageSize());
            pageInfo.setPageNum(queryDTO.getCurrent());
            return new ActionResult<>(pageInfo);
        }else if(queryDTO.getBusinessType().equals("PM")){
            tableNam=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),SystemContext.getTenantId());
        }else if(queryDTO.getBusinessType().equals("IM")){
            tableNam= shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),SystemContext.getTenantId());
        }
        return null;
    }

}
