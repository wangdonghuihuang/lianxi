package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.ProductMappingDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;
import com.softium.datacenter.paas.api.mapper.ProductMappingMapper;
import com.softium.datacenter.paas.web.service.ProductMappingService;
import com.softium.framework.common.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author huashan.li
 */
@Service
public class ProductMappingServiceImpl implements ProductMappingService {
    @Autowired
    private ProductMappingMapper productMappingMapper;
    @Override
    public List<ProductMappingDTO> list(InstitutionMappingQuery institutionMappingQuery) {
        PageHelper.startPage(institutionMappingQuery.getCurrent(),institutionMappingQuery.getPageSize(),true);
        return productMappingMapper.list(institutionMappingQuery, SystemContext.getTenantId());
    }
}
