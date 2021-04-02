package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.dto.PeriodDTO;
import com.softium.datacenter.paas.api.entity.Period;
import com.softium.datacenter.paas.api.enums.SealStatus;
import com.softium.datacenter.paas.api.mapper.PeriodMapper;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.common.query.SortProperty;
import com.softium.framework.mapping.Mapper;
import com.softium.framework.service.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
@Service
public class PeriodServiceImpl implements PeriodService {

    @Autowired
    private PeriodMapper periodMapper;
    @Autowired
    private Mapper mapper;

    @Override
    public Boolean saveOrUpdate(PeriodDTO periodDTO) {
        //校验参数合法性
        validate(periodDTO);
        Period period = new Period();
        mapper.map(periodDTO, period);
        Period period1 = periodMapper.get(periodDTO.getId());
        if(null ==  period1){
            return periodMapper.insert(period)>0 ? Boolean.TRUE : Boolean.FALSE;
        }else {
            return periodMapper.updateSelective(period)>0 ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    @Override
    public List<PeriodDTO> getPeriod(PeriodDTO periodDTO) {
        Criteria<Period> criteria = Criteria.from(Period.class);
        criteria.and(Period::getPeriodName, Operator.equal, periodDTO.getPeriodName());
        criteria.and(Period::getPrePeriodId,Operator.equal, periodDTO.getPrePeriodId());

        List<Period> byCriteria = periodMapper.findByCriteria(criteria);
        List<PeriodDTO> periodDTOList = new ArrayList<>();
        if(null != byCriteria){
            byCriteria.forEach(a -> {
                PeriodDTO periodDTO1 = new PeriodDTO();
                mapper.map(a,periodDTO1);
                periodDTOList.add(periodDTO1);
            });
        }
        return periodDTOList;
    }

    @Override
    public PeriodDTO getUntreatedPeriod() {
        Criteria<Period> criteria = Criteria.from(Period.class);
        criteria.and(Period::getIsSeal, Operator.equal, SealStatus.UnArchive);
        criteria.asc(Period::getConfigEffectiveDate);
        List<Period> byCriteria = periodMapper.findByCriteria(criteria);
        if(CollectionUtils.isEmpty(byCriteria)){
           throw new BusinessException(new ErrorInfo("period.unexist", "不存在活动的账期"));
        }
        Period period = byCriteria.get(0);
        PeriodDTO periodDTO = new PeriodDTO();
        mapper.map(period,periodDTO);
        return periodDTO;
    }

    @Override
    public String queryPeriodNameService(String id,String tenantId) {
        return periodMapper.getPeriodNameById(id,tenantId);
    }

    private void validate(PeriodDTO periodDTO){
        if (null==periodDTO) return;
        Period prePeriod = StringUtils.isEmpty(periodDTO.getPrePeriodId())?null:periodMapper.get(periodDTO.getPrePeriodId());
        Period nextPeriod = StringUtils.isEmpty(periodDTO.getId())?null:periodMapper.findOne(Period::getPrePeriodId,periodDTO.getId());
        //发货开始时间 不小于上期发货终止日期and配置生效日，不大于下期发货起始日期（编辑时）
        if(null!=periodDTO.getDeliveryBeginTime()&&null!=prePeriod&&null!=prePeriod.getDeliveryEndTime()){
            if (periodDTO.getDeliveryBeginTime().before(prePeriod.getDeliveryEndTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","发货开始时间不小于上期发货终止日期"));
            }
        }
        if(null!=periodDTO.getDeliveryBeginTime()){
            if (periodDTO.getDeliveryBeginTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","发货开始时间不小于配置生效日"));
            }
        }
        if (null!=nextPeriod&&null!=nextPeriod.getDeliveryBeginTime()&&null!=periodDTO.getDeliveryBeginTime()){
            if (periodDTO.getDeliveryBeginTime().after(nextPeriod.getDeliveryBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","发货开始时间不大于下期发货起始日期"));
            }
        }

        //发货终止日期 不小于发货起始日期and配置生效日，不大于下期发货起始日期（编辑时）
        if (null!=periodDTO.getDeliveryEndTime()){ //不小于发货起始日期-前端过滤
            if (periodDTO.getDeliveryEndTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","发货开始时间不小于配置生效日"));
            }
        }
        if (null!=periodDTO.getDeliveryEndTime()&&null!=nextPeriod&&null!=nextPeriod.getDeliveryBeginTime()){
            if (periodDTO.getDeliveryEndTime().after(nextPeriod.getDeliveryBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","发货终止日期不大于下期发货起始日期"));
            }
        }

        //采购起始日期 不小于上期采购终止日期and配置生效日，不大于下期采购起始日期（编辑时）
        if (null!=periodDTO.getPurchaseBeginTime()&&null!=prePeriod&&null!=prePeriod.getPurchaseEndTime()){
            if (periodDTO.getPurchaseBeginTime().before(prePeriod.getPurchaseEndTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","采购起始日期不小于上期采购终止日期"));
            }
        }
        if (null!=periodDTO.getPurchaseBeginTime()){
            if (periodDTO.getPurchaseBeginTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","采购起始日期不小于配置生效日"));
            }
        }
        if (null!=nextPeriod&&null!=nextPeriod.getPurchaseBeginTime()&&null!=periodDTO.getPurchaseBeginTime()){
            if (nextPeriod.getPurchaseBeginTime().after(nextPeriod.getPurchaseBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","采购起始日期不大于下期采购起始日期"));
            }
        }

        //采购终止日期 不小于采购起始日期and配置生效日，不大于下期采购起始日期（编辑时）
        if (null!=periodDTO.getPurchaseEndTime()){
            if (periodDTO.getPurchaseEndTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","采购终止日期不小于配置生效日"));
            }
        }
        if (null!=periodDTO.getPurchaseEndTime()&&null!=nextPeriod&&null!=nextPeriod.getPurchaseBeginTime()){
            if (periodDTO.getPurchaseEndTime().after(nextPeriod.getPurchaseBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","采购终止日期不大于下期采购起始日期"));
            }
        }

        //销售起始日期 不小于上期销售终止日期and配置生效日，不大于下期销售起始日期（编辑时）
        if (null!=periodDTO.getSaleBeginTime()&&null!=prePeriod&&null!=prePeriod.getSaleEndTime()){
            if (periodDTO.getSaleBeginTime().before(prePeriod.getSaleEndTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","销售起始日期不小于上期销售终止日期"));
            }
        }
        if (null!=periodDTO.getSaleBeginTime()){
            if (periodDTO.getSaleBeginTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","销售起始日期不小于配置生效日"));
            }
        }
        if (null!=periodDTO.getSaleBeginTime()&&null!=nextPeriod&&null!=nextPeriod.getSaleBeginTime()){
            if (periodDTO.getSaleBeginTime().after(nextPeriod.getSaleBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","销售起始日期下期销售起始日期"));
            }
        }

        //销售终止日期 不小于销售起始日期and配置生效日，不大于下期销售起始日期（编辑时）
        if (null!=periodDTO.getSaleEndTime()){
            if (periodDTO.getSaleEndTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","销售终止日期不小于配置生效日"));
            }
        }
        if (null!=periodDTO.getSaleEndTime()&&null!=nextPeriod&&null!=nextPeriod.getSaleBeginTime()){
            if (periodDTO.getSaleEndTime().after(nextPeriod.getSaleBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","销售终止日期不大于下期销售起始日期"));
            }
        }

        //库存日期	不小于上期库存日期and配置生效日，不大于下期库存日期（编辑时）
        if (null!=periodDTO.getInventoryTime()&&null!=prePeriod&&null!=prePeriod.getInventoryTime()){
            if (periodDTO.getInventoryTime().before(prePeriod.getInventoryTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","库存日期不小于上期库存日期"));
            }
        }
        if (null!=periodDTO.getInventoryTime()){
            if (periodDTO.getInventoryTime().before(periodDTO.getConfigEffectiveDate())) {
                throw new BusinessException(new ErrorInfo("ERROR","库存日期不小于配置生效日"));
            }
        }
        if (null!=periodDTO.getInventoryTime()&&null!=nextPeriod&&null!=nextPeriod.getInventoryTime()){
            if (periodDTO.getInventoryTime().after(nextPeriod.getInventoryTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","库存日期不大于下期库存日期"));
            }
        }

        //采集起始时间 不小于上期采集终止日期and配置生效日，不大于下期上传起始日期（编辑时）
        if (null==periodDTO.getUploadBeginTime()){
            throw new BusinessException(new ErrorInfo("ERROR","采集起始时间不能为空"));
        }
        if (null!=prePeriod&&periodDTO.getUploadBeginTime().before(prePeriod.getUploadBeginTime())){
            throw new BusinessException(new ErrorInfo("ERROR","采集起始时间不小于上期采集终止日期"));
        }
        if (null!=prePeriod&&periodDTO.getUploadBeginTime().before(periodDTO.getConfigEffectiveDate())){
            throw new BusinessException(new ErrorInfo("ERROR","采集起始时间不小于配置生效日"));
        }
        if (null!=nextPeriod){
            if (null!=prePeriod&&periodDTO.getUploadBeginTime().after(nextPeriod.getUploadBeginTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","采集起始时间不大于下期采集起始日期"));
            }
        }

        //采集终止时间 不小于上传起始时间and配置生效日，不大于下期上传起始日期（编辑时）
        if (null==periodDTO.getUploadEndTime()){
            throw new BusinessException(new ErrorInfo("ERROR","采集终止时间不能为空"));
        }
        if (periodDTO.getUploadEndTime().before(periodDTO.getConfigEffectiveDate())){
            throw new BusinessException(new ErrorInfo("ERROR","采集终止时间不小于配置生效日"));
        }
        if (null!=nextPeriod){
            if (periodDTO.getUploadEndTime().after(nextPeriod.getUploadEndTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","采集终止时间不大于下期上传起始日期"));
            }
        }

        //补量截止时间 不小于上传终止时间and配置生效日，不大于下期上传终止日期（编辑时）
        if (null==periodDTO.getSupplementEndTime()){
            throw new BusinessException(new ErrorInfo("ERROR","补量截止时间不能为空"));
        }
        if (periodDTO.getSupplementEndTime().before(periodDTO.getUploadEndTime())){
            throw new BusinessException(new ErrorInfo("ERROR","补量截止时间不小于采集终止时间"));
        }
        if (periodDTO.getSupplementEndTime().before(periodDTO.getConfigEffectiveDate())){
            throw new BusinessException(new ErrorInfo("ERROR","补量截止时间不小于配置生效日"));
        }
        if (null!=nextPeriod){
            if (periodDTO.getSupplementEndTime().after(nextPeriod.getUploadEndTime())) {
                throw new BusinessException(new ErrorInfo("ERROR","补量截止时间不大于下期采集终止日期"));
            }
        }
    }

}
