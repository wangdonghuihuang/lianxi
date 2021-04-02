package com.softium.datacenter.paas.web.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author huashan.li
 */
@Data
public class BillPrintExportDTO {
    @ExcelProperty(value = "*经销商编码", index = 0)
    private String institutionCode;
    @ExcelProperty(value = "经销商名称", index = 1)
    private String institutionName;
    @ExcelProperty(value = "*采集方式(MANUAL：人工上传 DDI：DDI)", index = 2)
    private String collectType;
    @ExcelProperty(value = "数据来源(WEB：网查、EMAIL：邮件)", index = 3)
    private String dataSource;
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


}
