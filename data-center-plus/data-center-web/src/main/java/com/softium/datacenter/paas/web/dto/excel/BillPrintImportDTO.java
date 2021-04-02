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
public class BillPrintImportDTO extends ExcelReadModel {
    @NotNull(message = ValidateServer.ERROR_KEY + "经销商编码不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @NotNull(message = ValidateServer.ERROR_KEY + "采集方式不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*采集方式(MANUAL：人工上传 DDI：DDI)", index = 2)
    private String collectType;
    @ExcelProperty(value = "数据来源(WEB：网查、EMAIL：邮件)", index = 3)
    private String dataSource;
    @NotNull(message = ValidateServer.ERROR_KEY + "打单状态不能为空", groups = {ValidateFilterGroup.class})
    @ExcelProperty(value = "*打单状态(需打单,无需打单)", index = 4)
    private String printStatus;
    @ExcelProperty(value = "备注", index = 5)
    private String remark;
    @ExcelProperty(value = "网址", index = 6)
    private String url;
    @ExcelProperty(value = "用户名", index = 7)
    private String username;
    @ExcelProperty(value = "密码", index = 8)
    private String password;
    @ExcelProperty(value = "行号", index = 9)
    private Integer rowIndex;
    @ExcelProperty(value = "错误信息", index = 10)
    private String message;
    @ExcelIgnore
    private String province;
    @ExcelIgnore
    private String city;
    @ExcelIgnore
    private String InstitutionId;
    @ExcelIgnore
    private String category;
}
