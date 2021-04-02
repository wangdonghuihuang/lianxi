package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.FileManagementDTO;
import com.softium.datacenter.paas.api.dto.query.FileParseLogQuery;
import com.softium.datacenter.paas.api.entity.FileParseLevel;

import java.util.List;

/**文件上传记录表查询业务层*/
public interface FileParseLevelService {
    List<FileManagementDTO> levelList(FileParseLogQuery fileParseLogQuery);
    List<FileManagementDTO> queryByIdList(String fileId);
    void parseLevelBatchInsert(List<FileParseLevel> fileParseLevels);
}
