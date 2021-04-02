package com.softium.datacenter.paas.web.dto.query;

import com.softium.datacenter.paas.web.utils.easy.input.EasyExcelService;

import java.util.LinkedHashMap;

/**
 * 2019/11/11
 *
 * @author paul
 * @see EasyExcelService
 */
public class ExcelModelQuery extends LinkedHashMap<String, Object> {


    public String errorToken() {
        return (String) get(EasyExcelService.ERROR_TOKEN_KEY);
    }

    public String dataToken() {
        return (String) get(EasyExcelService.DATA_TOKEN_KEY);
    }

    public String dataConvertToken() {
        return (String) get(EasyExcelService.DATA_CONVERT_TOKEN_KEY);
    }

    public String fileName() {
        return (String) get("fileName");
    }

}
