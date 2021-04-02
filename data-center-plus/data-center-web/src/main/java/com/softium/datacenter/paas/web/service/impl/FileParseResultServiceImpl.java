package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.dto.FileParseResultDTO;
import com.softium.datacenter.paas.api.entity.FileParseResult;
import com.softium.datacenter.paas.api.enums.FileParseResultStatus;
import com.softium.datacenter.paas.api.enums.FileParseResultStatusRemark;
import com.softium.datacenter.paas.api.mapper.FileParseResultMapper;
import com.softium.datacenter.paas.web.service.FileParseResultService;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Service
public class FileParseResultServiceImpl implements FileParseResultService {
    @Autowired
    private FileParseResultMapper fileParseResultMapper;
    private static final Integer BATCH_SIZE = 100;

    @Override
    public void batchInsert(List<FileParseResult> fileParseResultList) {
        if (!CollectionUtils.isEmpty(fileParseResultList)) {
            int size = fileParseResultList.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > fileParseResultList.size()) {
                    end = fileParseResultList.size();
                }
                fileParseResultMapper.batChInsertFileParse(fileParseResultList.subList(start, end));
            }
        }
    }

    @Override
    public List<FileParseResultDTO> getFileId(String fileId) {
        Criteria<FileParseResult> criteria=new Criteria<>();
        criteria.addCriterion(new Condition("fileParseLogId", Operator.equal,fileId));
        List<FileParseResult> fileParseResults = fileParseResultMapper.findByCriteria(criteria);
        List<FileParseResultDTO> fileParseResultDTOS = new ArrayList<>(fileParseResults.size());
        for(FileParseResult fileParseResult: fileParseResults){
            FileParseResultDTO fileParseResultDTO = new FileParseResultDTO();
            BeanUtils.copyProperties(fileParseResult,fileParseResultDTO);
            if(fileParseResultDTO.getStatus().equals(FileParseResultStatus.FAILURE.toString())){
                if(FileParseResultStatusRemark.FILE_REPEAT_AND_PARSE_NEW_FILE.toString().equals(fileParseResultDTO.getStatusRemark())){
                    fileParseResultDTO.setRuleType("文件名重复校验");
                }else if(FileParseResultStatusRemark.BUSINESS_TYPE_NOT_FOUND.toString().equals(fileParseResultDTO.getStatusRemark())){
                    fileParseResultDTO.setRuleType("业务类型校验");
                }else {
                    fileParseResultDTO.setRuleType("文件名规范校验");
                }
            }else if(fileParseResultDTO.getStatus().equals(FileParseResultStatus.SUCCESS.toString())){
                if(FileParseResultStatusRemark.FILE_NAME_PARSE_SUCCESS.toString().equals(fileParseResultDTO.getStatusRemark())) {
                    fileParseResultDTO.setRuleType("文件名规范校验");
                }
                if(FileParseResultStatusRemark.FILE_NAME_NOT_REPEAT.toString().equals(fileParseResultDTO.getStatusRemark())) {
                    fileParseResultDTO.setRuleType("文件名重复校验");
                }
                if(FileParseResultStatusRemark.BUSINESS_TYPE_NOT_FOUND.toString().equals(fileParseResultDTO.getStatusRemark())){
                    fileParseResultDTO.setRuleType("业务类型校验");
                }
            }
            fileParseResultDTOS.add(fileParseResultDTO);
        }
        return fileParseResultDTOS;
    }
}
