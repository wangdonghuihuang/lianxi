package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.FileDownLoadDTO;
import com.softium.datacenter.paas.api.dto.FileManagementDTO;
import com.softium.datacenter.paas.api.dto.FileParseDTO;
import com.softium.datacenter.paas.api.dto.query.CommonQuery;
import com.softium.datacenter.paas.api.dto.query.FileParseLogQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Fanfan.Gong
 **/
public interface FileParseLogService {
    List<FileParseDTO> listSuccessFileParseLog(String projectId);
    List<FileParseDTO> listErrorFileParseLog(String projectId);

    void batchInsert(List<FileParseDTO> errorFileParseList);

    void handleRepeatFile(Map<String, String> needDeleteIds, String userId);

    void updateFileStatus(FileParseDTO oldFileParseLog);

    void insert(FileParseDTO fileParse);

    void updateStatus(List<String> pendingFileLogIds, String userId, String toString);

    //List<FileParseDTO> list(FileParseLogQuery fileParseLogQuery);
    List<FileManagementDTO> list(FileParseLogQuery fileParseLogQuery);
    void deleteFile(String id, Integer isDeleted,int isOpen);

    FileDownLoadDTO downloadFileById(CommonQuery commonQuery);

    FileDownLoadDTO downloadReport(CommonQuery commonQuery);
    FileDownLoadDTO downManualReport(CommonQuery commonQuery,HttpServletResponse response) throws IOException;

}
