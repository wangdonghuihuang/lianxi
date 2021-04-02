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
public class InstitutionMappingImportDTO extends ExcelReadModel {
    @NotNull(message = ValidateServer.ERROR_KEY + "经销商编码不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @NotNull(message = ValidateServer.ERROR_KEY + "原始机构名称不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*原始机构名称", index = 2)
    private String originalInstitutionName;
    @NotNull(message = ValidateServer.ERROR_KEY + "标准机构编码不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*标准机构编码", index = 3)
    private String standardInstitutionCode;
    @ExcelProperty(value = "标准机构名称", index = 4)
    private String standardInstitutionName;
    @ExcelProperty(value = "行号", index = 5)
    private Integer rowIndex;
    @ExcelProperty(value = "错误信息", index = 6)
    private String message;
    @ExcelIgnore
    private String institutionId;
    @ExcelIgnore
    private String standardInstitutionId;
}
