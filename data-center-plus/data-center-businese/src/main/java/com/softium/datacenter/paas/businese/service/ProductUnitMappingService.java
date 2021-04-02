package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.ProductUnitMappingDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;

import java.util.List;

/**
 * @author huashan.li
 */
public interface ProductUnitMappingService {
    List<ProductUnitMappingDTO> list(InstitutionMappingQuery institutionMappingQuery);
}
