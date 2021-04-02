package com.softium.datacenter.paas.web.service;

import com.alibaba.fastjson.JSONObject;
import com.softium.datacenter.paas.api.entity.ExcelTemplate;
import com.softium.datacenter.paas.api.entity.FieldMapping;
import com.softium.datacenter.paas.api.entity.Period;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUploadService {
    ActionResult<String> upload(MultipartFile file, String fileTempPath, List<ExcelTemplate> fieldMappingList,String dataType,String periodId,String template,String collectName);
    JSONObject queryAllList();
    List<ExcelTemplate> queryColumnByTitleName();
    void saveTemplateConfig(JSONObject object);
    ActionResult<List<Period>> queryFilePageService();

}
