package com.softium.datacenter.paas.web.dto;

import com.softium.datacenter.paas.api.dto.InspectSaleDTO;
import com.softium.datacenter.paas.api.dto.InstitutionDTO;
import lombok.Data;

/**
 * 机构匹配dto
 * */
@Data
public class InstitutionMatchDTO {

    private InstitutionDTO institutionDTO;

    private InspectSaleDTO inspectSaleDTO;
}
