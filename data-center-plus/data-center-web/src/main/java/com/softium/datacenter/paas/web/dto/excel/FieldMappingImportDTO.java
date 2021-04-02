package com.softium.datacenter.paas.web.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.softium.datacenter.paas.web.utils.easy.input.ExcelReadModel;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateFilterGroup;
import com.softium.datacenter.paas.web.utils.easy.validate.ValidateServer;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description:列映射模板-销售
 * @author: york
 * @create: 2020-08-04 18:10
 **/
@Data
public class FieldMappingImportDTO extends ExcelReadModel {
    /**经销商编号(DDI平台)*/
    //@NotNull(message = ValidateServer.ERROR_KEY + "字段名称(文件)不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "字段名称(文件)", index = 0)
    private String columnNameFile;
    /**经销商名称(DDI平台)*/
    @NotNull(message = ValidateServer.ERROR_KEY + "字段名称(数据中心)不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*字段名称(数据中心)", index = 1)
    private String columnNameDatacenter;

    @ExcelProperty(value = "行号", index = 2)
    private Integer rowIndex;
    @ExcelProperty(value = "错误信息", index = 3)
    private String message;
}
