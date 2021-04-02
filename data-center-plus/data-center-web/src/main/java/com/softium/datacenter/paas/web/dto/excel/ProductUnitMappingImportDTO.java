package com.softium.datacenter.paas.web.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.softium.datacenter.paas.web.utils.easy.input.ExcelReadModel;
import lombok.Data;

/**
 * @author huashan.li
 */
@Data
public class ProductUnitMappingImportDTO extends ExcelReadModel {
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @ExcelProperty(value = "*产品编码", index = 2)
    private String standardProductCode;
    @ExcelProperty(value = "产品名称", index = 3)
    private String standardProductName;
    @ExcelProperty(value = "产品规格", index = 4)
    private String standardProductSpec;
    @ExcelProperty(value = "标准单位", index = 5)
    private String standardProductUnit;
    @ExcelProperty(value = "*原始单位", index = 6)
    private String originalProductUnit;
    @ExcelProperty(value = "*原始单位系数", index = 7)
    private Integer originalRatio;
    @ExcelProperty(value = "*标准单位系数", index = 8)
    private Integer standardRatio;
    @ExcelProperty(value = "行号", index = 9)
    private Integer rowIndex;
    @ExcelProperty(value = "错误信息", index = 10)
    private String message;
    @ExcelIgnore
    private String institutionId;
    @ExcelIgnore
    private String productSpecId;

}
