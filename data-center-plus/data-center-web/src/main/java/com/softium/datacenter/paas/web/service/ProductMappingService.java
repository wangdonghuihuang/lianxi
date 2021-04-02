package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.ProductMappingDTO;
import com.softium.datacenter.paas.api.dto.query.InstitutionMappingQuery;

import java.util.List;

/**
 * @author huashan.li
 */
public interface ProductMappingService {
    List<ProductMappingDTO> list(InstitutionMappingQuery institutionMappingQuery);
}
