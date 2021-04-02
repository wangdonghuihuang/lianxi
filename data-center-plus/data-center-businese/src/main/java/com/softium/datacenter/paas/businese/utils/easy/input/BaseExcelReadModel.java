package com.softium.datacenter.paas.web.utils.easy.input;

import com.alibaba.excel.annotation.ExcelIgnore;
import org.apache.commons.lang3.StringUtils;

/**
 * 2019/11/11
 *
 * @author paul
 */
public class BaseExcelReadModel {

    @ExcelIgnore
    private Integer rowIndex;
    @ExcelIgnore
    private String message;

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void appendMessage(String message) {

        if (StringUtils.isNotBlank(message)) {
            this.message = (this.message == null ? "" : this.message) + message;
        }
    }
}
