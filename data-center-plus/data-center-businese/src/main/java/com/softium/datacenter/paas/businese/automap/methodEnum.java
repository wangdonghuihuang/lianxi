package com.softium.datacenter.paas.web.automap;

import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.service.EnumMethodService;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import io.lettuce.core.ScriptOutputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;

public enum methodEnum implements EnumMethodService {
    PM(){
        private FileParseLevelMapper levelMapper;
        private OriginPurchaseMapper originPurchaseMapper;
        private InspectPurchaseMapper inspectPurchaseMapper;
        @Override
        public void handleMethod(String id) {
            levelMapper.delete(id);
            Criteria<OriginPurchase> criteria = Criteria.from(OriginPurchase.class);
            criteria.and(OriginPurchase::getFileId, Operator.equal,id);
            originPurchaseMapper.deleteByCriteria(criteria);
            //核查落地表状态
            Criteria<InspectPurchase> inspectSaleCriteria=new Criteria<>();
            inspectSaleCriteria.and(InspectPurchase::getFileId,Operator.equal,id);
            inspectPurchaseMapper.deleteByCriteria(inspectSaleCriteria);
        }
        @Override
        public void mappingBean(FileParseLevelMapper levelMapper,
                                OriginInventoryMapper originInventoryMapper,OriginPurchaseMapper originPurchaseMapper,
                                OriginSaleMapper originSaleMapper,InspectInventoryMapper inspectInventoryMapper,
                                InspectPurchaseMapper inspectPurchaseMapper,InspectSaleMapper inspectSaleMapper) {
            this.levelMapper=levelMapper;
            this.inspectPurchaseMapper=inspectPurchaseMapper;
            this.originPurchaseMapper=originPurchaseMapper;
        }
    },
    SM(){
        private FileParseLevelMapper levelMapper;
        private OriginSaleMapper originSaleMapper;
        private InspectSaleMapper inspectSaleMapper;
        @Override
        public void handleMethod(String id) {
            levelMapper.delete(id);
            Criteria<OriginSale> criteria = Criteria.from(OriginSale.class);
            criteria.and(OriginSale::getFileId, Operator.equal,id);
            originSaleMapper.deleteByCriteria(criteria);
            //核查落地表状态
            Criteria<InspectSale> inspectSaleCriteria=new Criteria<>();
            inspectSaleCriteria.and(InspectSale::getFileId,Operator.equal,id);
            inspectSaleMapper.deleteByCriteria(inspectSaleCriteria);
        }
        @Override
        public void mappingBean(FileParseLevelMapper levelMapper,
                OriginInventoryMapper originInventoryMapper,OriginPurchaseMapper originPurchaseMapper,
                OriginSaleMapper originSaleMapper,InspectInventoryMapper inspectInventoryMapper,
                InspectPurchaseMapper inspectPurchaseMapper,InspectSaleMapper inspectSaleMapper) {
            this.levelMapper=levelMapper;
            this.inspectSaleMapper=inspectSaleMapper;
            this.originSaleMapper=originSaleMapper;
        }
    },
    IM(){
        private FileParseLevelMapper levelMapper;
        private OriginInventoryMapper originInventoryMapper;
        private InspectInventoryMapper inspectInventoryMapper;
        @Override
        public void handleMethod(String id) {
            levelMapper.delete(id);
            Criteria<OriginInventory> criteria = Criteria.from(OriginInventory.class);
            criteria.and(OriginInventory::getFileId, Operator.equal,id);
            originInventoryMapper.deleteByCriteria(criteria);
            //核查落地表状态
            Criteria<InspectInventory> inspectSaleCriteria=new Criteria<>();
            inspectSaleCriteria.and(InspectInventory::getFileId,Operator.equal,id);
            inspectInventoryMapper.deleteByCriteria(inspectSaleCriteria);
        }
        @Override
        public void mappingBean(FileParseLevelMapper levelMapper,
                                OriginInventoryMapper originInventoryMapper,OriginPurchaseMapper originPurchaseMapper,
                                OriginSaleMapper originSaleMapper,InspectInventoryMapper inspectInventoryMapper,
                                InspectPurchaseMapper inspectPurchaseMapper,InspectSaleMapper inspectSaleMapper) {
            this.levelMapper=levelMapper;
            this.inspectInventoryMapper=inspectInventoryMapper;
            this.originInventoryMapper=originInventoryMapper;
        }
    };
    public abstract void mappingBean(FileParseLevelMapper levelMapper,
                                     OriginInventoryMapper originInventoryMapper,OriginPurchaseMapper originPurchaseMapper,
                                     OriginSaleMapper originSaleMapper,InspectInventoryMapper inspectInventoryMapper,
                                     InspectPurchaseMapper inspectPurchaseMapper,InspectSaleMapper inspectSaleMapper);
    @Component
    public static class EnumMethodInjection{
        @Autowired
        private FileParseLevelMapper levelMapper;
        @Autowired
        private OriginInventoryMapper originInventoryMapper;
        @Autowired
        private OriginPurchaseMapper originPurchaseMapper;
        @Autowired
        private OriginSaleMapper originSaleMapper;
        @Autowired
        private InspectInventoryMapper inspectInventoryMapper;
        @Autowired
        private InspectPurchaseMapper inspectPurchaseMapper;
        @Autowired
        private InspectSaleMapper inspectSaleMapper;
        @PostConstruct
        public void postConstruct(){
            for(methodEnum en: EnumSet.allOf(methodEnum.class)){
                en.mappingBean(levelMapper,originInventoryMapper,originPurchaseMapper,
                        originSaleMapper,inspectInventoryMapper,inspectPurchaseMapper,inspectSaleMapper);
            }
        }
    }
}
