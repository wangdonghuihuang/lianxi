package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.FileParseResultDTO;
import com.softium.datacenter.paas.api.entity.FileParseResult;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
public interface FileParseResultService {
    void batchInsert(List<FileParseResult> fileParseResultList);

    List<FileParseResultDTO> getFileId(String fileId);
}
