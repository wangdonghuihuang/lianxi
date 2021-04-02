package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.ProductUnitMappingDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.api.mapper.ProductUnitMappingMapper;
import com.softium.datacenter.paas.web.service.ProductUnitMappingService;
import com.softium.framework.common.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author huashan.li
 */
@Service
public class ProductUnitMappingServiceImpl implements ProductUnitMappingService{
    @Autowired
    private ProductUnitMappingMapper productUnitMappingMapper;
    @Override
    public List<ProductUnitMappingDTO> list(InstitutionMappingQuery institutionMappingQuery) {
        PageHelper.startPage(institutionMappingQuery.getCurrent(),institutionMappingQuery.getPageSize(),true);
        return productUnitMappingMapper.list(institutionMappingQuery, SystemContext.getTenantId());
    }
}
