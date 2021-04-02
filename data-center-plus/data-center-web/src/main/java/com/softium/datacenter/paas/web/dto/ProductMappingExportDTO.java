package com.softium.datacenter.paas.web.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.softium.framework.orm.common.po.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;

/**
 * @author huashan.li
 */
@Data
public class ProductMappingExportDTO {
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @ExcelProperty(value = "*原始产品名称", index = 2)
    private String originalProductName;
    @ExcelProperty(value = "*原始产品规格", index = 3)
    private String originalProductSpec;
    @ExcelProperty(value = "*标准产品编码", index = 4)
    private String standardProductCode;
    @ExcelProperty(value = "标准产品名称", index = 5)
    private String standardProductName;
    @ExcelProperty(value = "标准产品规格", index = 6)
    private String standardProductSpec;

}
