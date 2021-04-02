package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;

/**处理上传zip,excel文件业务层*/
public interface ExcelParseJobService {
    public void judgeExcelFileData(ExcelJobDTO jobDTO);
}
