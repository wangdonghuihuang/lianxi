package com.softium.datacenter.paas.web.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.softium.datacenter.paas.web.utils.easy.input.ExcelReadModel;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateFilterGroup;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateServer;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author huashan.li
 */
@Data
public class ProductMappingImportDTO extends ExcelReadModel {
    @NotNull(message = ValidateServer.ERROR_KEY + "经销商编码不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @NotNull(message = ValidateServer.ERROR_KEY + "原始产品名称不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*原始产品名称", index = 2)
    private String originalProductName;
    @NotNull(message = ValidateServer.ERROR_KEY + "原始产品规格不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*原始产品规格", index = 3)
    private String originalProductSpec;
    @NotNull(message = ValidateServer.ERROR_KEY + "标准产品编码不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*标准产品编码", index = 4)
    private String standardProductCode;
    @ExcelProperty(value = "标准产品名称", index = 5)
    private String standardProductName;
    @ExcelProperty(value = "标准产品规格", index = 6)
    private String standardProductSpec;
    @ExcelProperty(value = "行号", index = 7)
    private Integer rowIndex;
    @ExcelProperty(value = "错误信息", index = 8)
    private String message;
    @ExcelIgnore
    private String institutionId;
    @ExcelIgnore
    private String standardProductId;
}
