package com.softium.datacenter.paas.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.softium.datacenter.paas.api.dto.FileManagementDTO;
import com.softium.datacenter.paas.api.dto.query.FileParseLogQuery;
import com.softium.datacenter.paas.api.entity.FileParseLevel;
import com.softium.datacenter.paas.api.mapper.FileParseLevelMapper;
import com.softium.datacenter.paas.web.service.FileParseLevelService;
import com.softium.framework.common.SystemContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileParseLevelServiceImpl implements FileParseLevelService {
    @Autowired
    FileParseLevelMapper levelMapper;
    private static final Integer BATCH_SIZE = 1000;
    @Override
    public List<FileManagementDTO> levelList(FileParseLogQuery fileParseLogQuery) {
        PageHelper.startPage(fileParseLogQuery.getCurrent(),fileParseLogQuery.getPageSize(),true);
        return levelMapper.queryAllLevelData(fileParseLogQuery, SystemContext.getTenantId());
    }

    @Override
    public List<FileManagementDTO> queryByIdList(String fileId) {
        //List<FileManagementDTO> list=levelMapper.queryDataById(fileId);
        List<FileManagementDTO> list=levelMapper.queryAllDataByLogId(SystemContext.getTenantId(),fileId);
        return list;
    }
    @Override
    public void parseLevelBatchInsert(List<FileParseLevel> fileParseLevels) {
        if (!CollectionUtils.isEmpty(fileParseLevels)) {
            int size = fileParseLevels.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > fileParseLevels.size()) {
                    end = fileParseLevels.size();
                }
                levelMapper.fileLevelBatchInsert(fileParseLevels.subList(start, end));
            }
        }
    }
}
