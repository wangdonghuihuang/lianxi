package com.softium.datacenter.paas.web.dto;

import com.softium.datacenter.paas.api.dto.InspectSaleDTO;
import com.softium.datacenter.paas.api.dto.ProductDTO;
import lombok.Data;

import java.util.List;

/**
 * 产品匹配dto
 * */
@Data
public class ProductMatchDTO {

    private List<InspectSaleDTO> inspectSaleDTOList;

    private ProductDTO productDTO;
}
