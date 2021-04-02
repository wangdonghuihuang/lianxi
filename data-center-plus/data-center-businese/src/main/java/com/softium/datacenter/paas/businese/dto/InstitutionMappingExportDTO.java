package com.softium.datacenter.paas.web.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author huashan.li
 */
@Data
public class InstitutionMappingExportDTO {
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @ExcelProperty(value = "*原始机构名称", index = 2)
    private String originalInstitutionName;
    @ExcelProperty(value = "*标准机构编码", index = 3)
    private String standardInstitutionCode;
    @ExcelProperty(value = "标准机构名称", index = 4)
    private String standardInstitutionName;
}
