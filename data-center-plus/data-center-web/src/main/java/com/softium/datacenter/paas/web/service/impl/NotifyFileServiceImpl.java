package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.api.enums.*;
import com.softium.datacenter.paas.web.automap.OriginInventoryAutoMap;
import com.softium.datacenter.paas.web.automap.OriginPurchaseAutoMap;
import com.softium.datacenter.paas.web.automap.OriginSaleAutoMap;
import com.softium.datacenter.paas.api.entity.*;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.api.mapper.*;
import com.softium.datacenter.paas.web.utils.FilePathFormatter;
import com.softium.datacenter.paas.web.utils.FtpProxy;
import com.softium.datacenter.paas.web.utils.ToolExcelUtils;
import com.softium.datacenter.paas.web.service.*;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import com.softium.framework.service.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Fanfan.Gong
 **/
@Service
public class NotifyFileServiceImpl implements NotifyFileService {
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private FilePathRuleMapper filePathRuleMapper;
    @Autowired
    private FileNameRuleMapper fileNameRuleMapper;
//    @Autowired
//    private ProjectInstitutionMapper projectInstitutionMapper;
    @Autowired
    private BusinessTypeMappingMapper businessTypeMappingMapper;
    @Autowired
    private FileParseLogService fileParseLogService;
    @Autowired
    private FileParseResultService fileParseResultService;
    @Autowired
    private FieldMappingMapper fieldMappingMapper;
    @Autowired
    private OriginSaleService originSaleService;
    @Autowired
    private OriginPurchaseService originPurchaseService;
    @Autowired
    private OriginInventoryService originInventoryService;
    @Autowired
    private DistributorService distributorService;
    @Resource
    ShardingManager shardingManager;

    private static final String FILE_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(NotifyFileServiceImpl.class);

    @Override
    public void handleByConfigId(String notifyConfigId, String userId) {
        NotifyConfig notifyConfig = notifyConfigMapper.getById(notifyConfigId);
        if (null == notifyConfig) {
            throw new BusinessException(new ErrorInfo("notify_config_not_exist", "?????????????????????"));
        }
        // ????????????ID??????
        String tenantId = notifyConfig.getTenantId();
        SystemContext.setTenantId(tenantId);

        ProjectFtpDTO projectFtpDTO = projectMapper.getProjectFtpDTOById(notifyConfig.getProjectId(), tenantId);
        if (null == projectFtpDTO) {
            throw new BusinessException(new ErrorInfo("project_or_ftpinfo_not_exist","??????????????????FTP?????????????????????"));
        }
        /**
         * ?????????????????????
         */
        FilePathRuleDTO filePathRule = filePathRuleMapper.getByProject(notifyConfig.getProjectId(), notifyConfig.getProjectInstitutionCode(), SystemContext.getTenantId());
        if (null == filePathRule) {
            throw new BusinessException(new ErrorInfo("file_path_rule_not_exist", "???????????????????????????"));
        }

        FileNameRule fileNameRule = fileNameRuleMapper.getByProject(notifyConfig.getProjectId(), notifyConfig.getProjectInstitutionCode(), SystemContext.getTenantId());
        if (null == fileNameRule) {
            throw new BusinessException(new ErrorInfo("file_name_rule_not_exist", "????????????????????????"));
        }

        List<FieldMapping> fieldMappingList = fieldMappingMapper.getFieldMappingByProject(notifyConfig.getProjectId(), SystemContext.getTenantId());
        if (null == fieldMappingList || fieldMappingList.size() == 0) {
            throw new BusinessException(new ErrorInfo("field_mapping_not_exist", "???????????????????????????"));
        }

        List<BusinessTypeMappingDTO> businessTypeMappingList = businessTypeMappingMapper.list(notifyConfig.getProjectId(), SystemContext.getTenantId());
        if (null == businessTypeMappingList || businessTypeMappingList.size() == 0) {
            throw new BusinessException(new ErrorInfo("business_type_mapping_not_exist", "?????????????????????????????????"));
        }

        LocalDateTime dateTime = LocalDateTime.now().minusDays(notifyConfig.getBeforeDayNum());
        String filePath = FilePathFormatter.format(filePathRule.getRule(), filePathRule.getProjectName(), filePathRule.getInstitutionName(), dateTime);
        logger.info("????????????????????????: {}, {}", filePathRule.getRule(), filePath);

        /**
         * ????????????
         */
        collectAndStorage(userId, filePath, projectFtpDTO, notifyConfig, fileNameRule, businessTypeMappingList, fieldMappingList);
    }

    /**
     * ???????????????????????????????????????log??????
     *
     * @param filePath
     * @param projectFtp
     * @param notifyConfig
     * @return
     */
    private void collectAndStorage(String userId, String filePath, ProjectFtpDTO projectFtp, NotifyConfig notifyConfig, FileNameRule fileNameRule,                                   List<BusinessTypeMappingDTO> businessTypeMappingList, List<FieldMapping> fieldMappingList) {
        FtpProxy ftpProxy = new FtpProxy(projectFtp.getFtpHost(), projectFtp.getFtpPort(), projectFtp.getProtocol());
        boolean flag = ftpProxy.login(notifyConfig.getFtpUsername(), notifyConfig.getFtpPassword());
        if (!flag) {
            throw new BusinessException(new ErrorInfo("ftp_login_failure", "??????ftp??????"));
        }

        // ????????? rootPath
        String rootPath = notifyConfig.getRootPath();
        boolean changeFlag = false;
        if (StringUtils.isNotEmpty(rootPath)) {
            changeFlag = ftpProxy.changeDir(rootPath);
        }
        if (!changeFlag) {
            throw new BusinessException(new ErrorInfo("change_root_path_failure", "??????RootPath??????"));
        }

        /**
         * ????????????
         */
        List<String> paths = findDirList(ftpProxy, filePath);

        List<DistributorDTO> distributorList = null;

        try {
            ActionResult<List<DistributorDTO>> distributorResult = distributorService.listDistributor(notifyConfig.getTenantId());
            if (distributorResult.isSuccess()) {
                distributorList = distributorResult.getData();
            } else {
                logger.error("???????????????-?????????????????????????????????, success = false: {}", distributorResult);
            }
        }catch (Exception e) {
            logger.error("???????????????-?????????????????????????????????????????????: {}", e.getMessage());
            throw new BusinessException(new ErrorInfo("get_distributor_error", "???????????????????????????????????????"));
        }
        if (null == distributorList || distributorList.size() == 0) {
            throw new BusinessException(new ErrorInfo("distributor_is_empty", "????????????????????????????????????"));
        }
//        List<ProjectInstitution> projectInstitutionList = projectInstitutionMapper.list(notifyConfig.getProjectId(), notifyConfig.getProjectInstitutionCode(), SystemContext.getTenantId());
        List<FileParseDTO> successParseLogList = fileParseLogService.listSuccessFileParseLog(notifyConfig.getProjectId());
        List<FileParseDTO> errorParseLogList = fileParseLogService.listErrorFileParseLog(notifyConfig.getProjectId());
        /**
         * ????????????
         */
        handleFile(userId, ftpProxy, paths, notifyConfig, fileNameRule, distributorList, businessTypeMappingList,
                fieldMappingList, successParseLogList, errorParseLogList);
    }

    /**
     * ???????????????????????????
     *
     * @param ftpProxy
     * @param paths
     * @param notifyConfig
     */
    private void handleFile(String userId, FtpProxy ftpProxy, List<String> paths, NotifyConfig notifyConfig,
                            FileNameRule fileNameRule, List<DistributorDTO> distributorList,
                            List<BusinessTypeMappingDTO> businessTypeMappingList, List<FieldMapping> fieldMappingList,
                            List<FileParseDTO> oldSuccessParseLogList, List<FileParseDTO> oldErrorParseLogList) {

        Map<String, FileParseDTO> parseSuccessFileLogMap = new HashMap<>(oldSuccessParseLogList.size());
        Map<String, FileParseDTO> unionFileParseLogMap = new HashMap<>(oldErrorParseLogList.size());

        oldErrorParseLogList.forEach(fileParseLog -> {
            unionFileParseLogMap.put(fileParseLog.getFileName(), fileParseLog);
        });

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        oldSuccessParseLogList.forEach(fileParseLog -> {
            if (!fileParseLog.getIsDeleted().equals(1)) {
                String key = fileParseLog.getProjectId() + fileParseLog.getProjectInstitutionCode() +
                        fileParseLog.getBusinessType() + fileParseLog.getFileType().toUpperCase() + fileParseLog.getFileTime().format(dateFormatter);
                parseSuccessFileLogMap.put(key, fileParseLog);
            }
            unionFileParseLogMap.put(fileParseLog.getFileName(), fileParseLog);
        });


        List<FileParseDTO> fileParseList = new ArrayList<>(4);
        String projectId = notifyConfig.getProjectId();
        for (String path : paths) {
            FTPFile[] files = ftpProxy.list(path);
            for (FTPFile file : files) {
                FileParseDTO fileParse = new FileParseDTO();
                fileParse.setId(UUID.randomUUID().toString());
                fileParse.setNotifyId(notifyConfig.getId());
                fileParse.setProjectId(projectId);
                fileParse.setRootPath(notifyConfig.getRootPath());
                fileParse.setFilePath(path);
                fileParse.setAccessType(AccessType.DDI.toString());
                String fileName = file.getName();
                String modifyTime = ftpProxy.getTimestamp(path + FILE_SEPARATOR + fileName);
                LocalDateTime fileLastModifyTime;
                if (StringUtils.isEmpty(modifyTime)) {
                    long fileLastModifyMillions = file.getTimestamp().getTimeInMillis() + file.getTimestamp().getTimeZone().getOffset(0);
                    fileLastModifyTime = Instant.ofEpochMilli(fileLastModifyMillions).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
                } else {
                    long timeMillions = LocalDateTime.parse(modifyTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
                    fileLastModifyTime = Instant.ofEpochMilli(timeMillions).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
                }
                fileParse.setFileLastModifyTime(fileLastModifyTime);
                fileParse.setFileName(fileName);
                fileParse.setFileNameRule(fileNameRule.getRule());
                fileParse.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
                fileParse.setCreateBy(userId);
                fileParse.setCreateTime(new Date());
                fileParse.setUpdateBy(userId);
                fileParse.setUpdateTime(new Date());
                fileParse.setRowcount(0);
                fileParse.setIsDeleted(0);
                fileParse.setTenantId(SystemContext.getTenantId());
                fileParseList.add(fileParse);
            }
        }
        if (fileParseList.size() == 0) {
            return;
        }
        Map<String, DistributorDTO> institutionCodeMap = new HashMap<>(distributorList.size());
        for (DistributorDTO distributorDTO : distributorList) {
            institutionCodeMap.put(distributorDTO.getCode(), distributorDTO);
        }
        Map<String, BusinessTypeMappingDTO> businessTypeMappingMap = new HashMap<>(businessTypeMappingList.size());
        for (BusinessTypeMappingDTO businessTypeMapping : businessTypeMappingList) {
            if (!StringUtils.isEmpty(businessTypeMapping.getProjectInstitutionCode())) {
                String key = businessTypeMapping.getProjectId() + businessTypeMapping.getProjectInstitutionCode() + businessTypeMapping.getDdiBusinessType();
                businessTypeMappingMap.put(key, businessTypeMapping);
            } else {
                businessTypeMappingMap.put(businessTypeMapping.getProjectId() + businessTypeMapping.getDdiBusinessType(), businessTypeMapping);
            }
        }
        Map<String, List<FieldMapping>> fieldMappingMap = new HashMap<>(fieldMappingList.size());
        for (FieldMapping fieldMapping : fieldMappingList) {
            String key = fieldMapping.getProjectId() + fieldMapping.getBusinessType();
            if (!StringUtils.isEmpty(fieldMapping.getProjectInstitutionCode())) {
                key = fieldMapping.getProjectId() + fieldMapping.getProjectInstitutionCode() + fieldMapping.getBusinessType();
            }
            List<FieldMapping> value = fieldMappingMap.get(key);
            if (null == value) {
                value = new ArrayList<>(2);
                value.add(fieldMapping);
                fieldMappingMap.put(key, value);
            } else {
                value.add(fieldMapping);
            }
        }
        // ???????????????????????????
        List<FileParseDTO> errorFileParseList = new ArrayList<>(4);
        // ??????????????????
        List<FileParseResult> fileParseResultList = new ArrayList<>(4);
        // ????????????????????????
        List<FileParseDTO> storageFileParseList = new ArrayList<>(4);
        for (FileParseDTO fileParse : fileParseList) {
            boolean flag = parseFileName(fileParse);
            /**
             * ??????????????????????????? ??????????????? + ?????????????????????
             */
            boolean exists = false;
            FileParseDTO oldFileParseLog = unionFileParseLogMap.get(fileParse.getFileName());
            if (null != oldFileParseLog && oldFileParseLog.getFileLastModifyTime().equals(fileParse.getFileLastModifyTime())) {
                exists = true;
            }
            if (!flag) {
                if (!exists) {
                    LocalDateTime fileTime = fileParse.getFileTime();
                    if (null == fileTime) {
                        fileTime = LocalDateTime.parse("1900-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                    fileParse.setFileTime(fileTime);
                    fileParse.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
                    fileParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(), FileParseResultStatusRemark.FILE_NAME_PARSE_ERROR.toString(), userId));
                    errorFileParseList.add(fileParse);
                }
                continue;
            }
            DistributorDTO distributorDTO = institutionCodeMap.get(fileParse.getInstitutionCode().trim());
            if (null == distributorDTO) {
                if (!exists) {
                    fileParse.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
                    fileParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(), FileParseResultStatusRemark.INSTITUTION_CODE_NOT_FOUND.toString(), userId));
                    errorFileParseList.add(fileParse);
                }
                continue;
            } else {
                //fileParse.setProjectInstitutionCode(projectInstitution.getId());
                fileParse.setProjectInstitutionCode(distributorDTO.getCode());
                fileParse.setProjectInstitutionName(distributorDTO.getName());
            }
            String fileType = fileParse.getFileType();
            if (!"csv".equalsIgnoreCase(fileType) && !"xls".equalsIgnoreCase(fileType) && !"xlsx".equalsIgnoreCase(fileType)) {
                if (!exists) {
                    fileParse.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
                    fileParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(), FileParseResultStatusRemark.FILE_TYPE_ERROR.toString(), userId));
                    errorFileParseList.add(fileParse);
                }
                continue;
            }
            BusinessTypeMappingDTO businessTypeMapping = null;

            businessTypeMapping = businessTypeMappingMap.get(fileParse.getProjectId() + fileParse.getProjectInstitutionCode() + fileParse.getFileBusinessType());
            if (null == businessTypeMapping) {
                businessTypeMapping = businessTypeMappingMap.get(fileParse.getProjectId() + fileParse.getFileBusinessType());
            }
            if (null == businessTypeMapping) {
                if (!exists) {
                    fileParse.setFileStatus(FileParseStatus.FILE_PARSE_ERROR.toString());
                    fileParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(), FileParseResultStatusRemark.BUSINESS_TYPE_NOT_FOUND.toString(), userId));
                    errorFileParseList.add(fileParse);
                }
                continue;
            }
            fileParse.setBusinessTypeMapping(businessTypeMapping);
            fileParse.setBusinessType(businessTypeMapping.getDdiBusinessType());
            //????????????(?????????????????????)
//            if (DataType.SALE.toString().equalsIgnoreCase(businessTypeMapping.getType()) || DataType.PURCHASE.toString().equalsIgnoreCase(businessTypeMapping.getType())) {
//                continue;
//            }
            // ???????????????????????????????????????????????????
            if (!exists) {
                storageFileParseList.add(fileParse);
            } else if (oldFileParseLog.getFileStatus().equals(FileParseStatus.FILE_PARSE_ERROR.toString()) ||
                    oldFileParseLog.getFileStatus().equals(FileParseStatus.FILE_TRANSFER_ERROR.toString())) {
                storageFileParseList.add(fileParse);
            }
        }


        fileParseLogService.batchInsert(errorFileParseList);
        fileParseResultService.batchInsert(fileParseResultList);


        if (storageFileParseList.size() > 0) {
            /**
             * ??????????????????
             */
            List<FileParseDTO> preStorageList = handleRepeatFile(storageFileParseList, parseSuccessFileLogMap, userId);
            /**
             * ??????????????????
             */
            parseFileStorage(ftpProxy, preStorageList, fieldMappingMap, unionFileParseLogMap, userId);
        }
    }

    /**
     * ??????????????????
     *
     * @param storageFileParseList
     * @param successParseLogMap
     * @param userId
     * @return
     */
    private List<FileParseDTO> handleRepeatFile(List<FileParseDTO> storageFileParseList, Map<String, FileParseDTO> successParseLogMap, String userId) {
        List<FileParseDTO> preStorageList = new ArrayList<>(storageFileParseList.size());
        List<FileParseDTO> repeatFileList = new ArrayList<>(4);
        List<FileParseResult> repeatParseResultList = new ArrayList<>(4);
        Map<String, String> needDeleteIds = new HashMap<>(4);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        Map<String, FileParseDTO> storageFileMap = new HashMap<>();
        storageFileParseList.forEach(fileParse -> {
            String key = fileParse.getProjectId() + fileParse.getProjectInstitutionCode() +
                    fileParse.getBusinessType() + fileParse.getFileType().toUpperCase() + fileParse.getFileTime().format(dateFormatter);
            FileParseDTO oldFileParse = storageFileMap.get(key);
            if (null == oldFileParse) {
                storageFileMap.put(key, fileParse);
            } else {
                String fileRepeat = fileParse.getBusinessTypeMapping().getFileNameRepeat();
                if (FileNameRepeat.NEW.toString().equals(fileRepeat)) {
                    if (oldFileParse.getFileLastModifyTime().isBefore(fileParse.getFileLastModifyTime())) {
                        storageFileMap.put(key, fileParse);
                        oldFileParse.setNewDataId(fileParse.getId());
                        repeatFileList.add(oldFileParse);
                    } else {
                        fileParse.setNewDataId(oldFileParse.getId());
                        repeatFileList.add(fileParse);
                    }
                } else {
                    if (fileParse.getFileTime().isBefore(oldFileParse.getFileTime())) {
                        storageFileMap.put(key, fileParse);
                        oldFileParse.setNewDataId(fileParse.getId());
                        repeatFileList.add(oldFileParse);
                    } else {
                        fileParse.setNewDataId(oldFileParse.getId());
                        repeatFileList.add(fileParse);
                    }
                }
            }
        });
        /**
         * ftp???????????????????????????????????????
         */
        repeatFileList.forEach(fileParse -> {
            fileParse.setIsDeleted(1);
            repeatParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(),
                    FileParseResultStatusRemark.FILE_REPEAT_AND_PARSE_NEW_FILE.toString(), userId));
        });

        /**
         *
         */
        storageFileMap.forEach((k, fileParse) -> {
            String key = fileParse.getProjectId() + fileParse.getProjectInstitutionCode() +
                    fileParse.getBusinessType() + fileParse.getFileType().toUpperCase() + fileParse.getFileTime().format(dateFormatter);
            FileParseLog repeatFileLog = successParseLogMap.get(key);
            if (null == repeatFileLog) {
                preStorageList.add(fileParse);
            } else {
                String fileRepeat = fileParse.getBusinessTypeMapping().getFileNameRepeat();
//                boolean before = repeatFileLog.getFileLastModifyTime().isBefore(fileParse.getFileLastModifyTime());
//                boolean fileTimeBefore = repeatFileLog.getFileTime().isBefore(fileParse.getFileTime());
                if (FileNameRepeat.NEW.toString().equals(fileRepeat)) {
                    needDeleteIds.put(repeatFileLog.getId(), fileParse.getId());
                    preStorageList.add(fileParse);
                }
                if (FileNameRepeat.OLD.toString().equals(fileRepeat)) {
                    fileParse.setIsDeleted(1);
                    fileParse.setNewDataId(repeatFileLog.getId());
                    repeatParseResultList.add(generateFileResult(fileParse.getId(), FileParseResultStatus.FAILURE.toString(),
                            FileParseResultStatusRemark.FILE_REPEAT_AND_PARSE_NEW_FILE.toString(), userId));
                    repeatFileList.add(fileParse);
                }
            }
        });
        fileParseLogService.batchInsert(repeatFileList);
        fileParseResultService.batchInsert(repeatParseResultList);
        fileParseLogService.handleRepeatFile(needDeleteIds, userId);
        return preStorageList;
    }


    /**
     * ??????????????????
     *
     * @param ftpProxy
     * @param fileParseList
     * @param fieldMappingMap
     */
    private void parseFileStorage(FtpProxy ftpProxy, List<FileParseDTO> fileParseList, Map<String, List<FieldMapping>> fieldMappingMap,
                                  Map<String, FileParseDTO> parseErrorFileLogMap, String userId) {
        List<FileParseResult> fileParseResults = new ArrayList<>(4);
        List<String> pendingFileLogIds = new ArrayList<>();

        FileParseDTO oldFileParseLog = null;
        for (FileParseDTO fileParse : fileParseList) {
            String fileName = fileParse.getRootPath() + fileParse.getFilePath() + FILE_SEPARATOR + fileParse.getFileName();
            InputStream in = ftpProxy.loadFile(fileName);
            if (null == in) {
//                oldFileParseLog = parseErrorFileLogMap.get(fileParse.getFileName());
//                fileParseLogService.insert(fileParse);
//                return;
            }
            List<FieldMapping> projectFieldMapping = fieldMappingMap.get(fileParse.getProjectId() + fileParse.getProjectInstitutionCode() + fileParse.getBusinessType());
            if (null == projectFieldMapping || projectFieldMapping.size() == 0) {
                projectFieldMapping = fieldMappingMap.get(fileParse.getProjectId() + fileParse.getBusinessType());
            }
            List<OriginSale> originSaleList = new ArrayList<>(2);
            List<OriginPurchase> originPurchaseList = new ArrayList<>(2);
            List<OriginInventory> originInventoryList = new ArrayList<>(2);
            /**
             * true = ??????????????????
             * false = ????????????????????????
             */
            boolean existsErrorFileLog = false;
            String fileType = "";
            String fileParseLogId = "";
            String projectId="";
            Integer row = 0;
            try {
                oldFileParseLog = parseErrorFileLogMap.get(fileParse.getFileName());
                /**
                 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????log??????
                 */
                if (null != oldFileParseLog && fileParse.getFileLastModifyTime().equals(oldFileParseLog.getFileLastModifyTime())) {
                    existsErrorFileLog = true;
                    fileType = oldFileParseLog.getFileType();
                    fileParseLogId = oldFileParseLog.getId();
                    projectId=oldFileParseLog.getProjectId();
                } else {
                    fileType = fileParse.getFileType();
                    fileParseLogId = fileParse.getId();
                    projectId=fileParse.getProjectId();
                }
                if (DataType.SALE.toString().equals(fileParse.getBusinessTypeMapping().getType())) {
                    originSaleList = loadSaleDataByFile(in, fileType, fileParseLogId, projectFieldMapping, userId,projectId);
                    row = originSaleList.size();
                }
                if (DataType.PURCHASE.toString().equals(fileParse.getBusinessTypeMapping().getType())) {
                    originPurchaseList = loadPurchaseDataByFile(in, fileType, fileParseLogId, projectFieldMapping, userId,projectId);
                    row = originPurchaseList.size();
                }
                if (DataType.INVENTORY.toString().equals(fileParse.getBusinessTypeMapping().getType())) {
                    originInventoryList = loadOriginInventoryDataByFile(in, fileType, fileParseLogId, projectFieldMapping, userId,projectId);
                    row = originInventoryList.size();
                }

            } catch (Exception e) {
                fileParse.setFileStatus(FileParseStatus.FILE_TRANSFER_ERROR.toString());
                e.printStackTrace();
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    ftpProxy.completePendingCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String emptyDataFileId = "";
            try {
                if (existsErrorFileLog) {
                    oldFileParseLog.setProjectInstitutionCode(fileParse.getProjectInstitutionCode());
                    oldFileParseLog.setProjectInstitutionCode(fileParse.getProjectInstitutionCode());
                    oldFileParseLog.setProjectInstitutionName(fileParse.getProjectInstitutionName());
                    oldFileParseLog.setFileStatus(FileParseStatus.QUALITY_PENDING.toString());
                    oldFileParseLog.setRowcount(row);
                    oldFileParseLog.setUpdateBy(userId);
                    oldFileParseLog.setUpdateTime(new Date());
                    oldFileParseLog.setBusinessType(fileParse.getBusinessType());
                    oldFileParseLog.setFileTime(fileParse.getFileTime());
                    emptyDataFileId = oldFileParseLog.getId();
                    fileParseLogService.updateFileStatus(oldFileParseLog);
                    fileParseResults.add(generateFileResult(oldFileParseLog.getId(), FileParseResultStatus.SUCCESS.toString(), FileParseResultStatusRemark.FILE_NAME_PARSE_SUCCESS.toString(), userId));
                } else {
                    fileParse.setRowcount(row);
                    fileParse.setFileStatus(FileParseStatus.QUALITY_PENDING.toString());
                    emptyDataFileId = fileParse.getId();
                    fileParseLogService.insert(fileParse);
//                    fileParseResults.add(generateFileResult(fileParse.getId(), FileParseResultStatus.SUCCESS.toString(), FileParseResultStatusRemark.FILE_NAME_NOT_REPEAT.toString(), userId));
//                    fileParseResults.add(generateFileResult(fileParse.getId(), FileParseResultStatus.SUCCESS.toString(), FileParseResultStatusRemark.FILE_NAME_PARSE_SUCCESS.toString(), userId));
              }
                String originTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),fileParse.getTenantId());
                String purchaseTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginPurchase.class),fileParse.getTenantId());
                String inventoryTableName=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginInventory.class),fileParse.getTenantId());
                originSaleService.batchInsertOriginSale(originTableName,originSaleList);
                originPurchaseService.batchInsertOriginPurchase(purchaseTableName,originPurchaseList);
                originInventoryService.batchInsertOriginInventory(inventoryTableName,originInventoryList);
                if (DataType.SALE.toString().equals(fileParse.getBusinessTypeMapping().getType()) && originSaleList.size() == 0) {
                    pendingFileLogIds.add(emptyDataFileId);
                }
                if (DataType.PURCHASE.toString().equals(fileParse.getBusinessTypeMapping().getType()) && originPurchaseList.size() == 0) {
                    pendingFileLogIds.add(emptyDataFileId);
                }
                if (DataType.INVENTORY.toString().equals(fileParse.getBusinessTypeMapping().getType()) && originInventoryList.size() == 0) {
                    pendingFileLogIds.add(emptyDataFileId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            List<InspectSale> inspectSales = new ArrayList<>(originSaleList.size());
//            originSaleList.forEach(originSale -> {
//                inspectSales.add(formatToInspectSale(originSale));
//            });
//            List<InspectPurchase> inspectPurchases = new ArrayList<>(originPurchaseList.size());
//            originPurchaseList.forEach(originPurchase -> {
//                inspectPurchases.add(formatToInspectPurchase(originPurchase));
//            });
//            List<InspectInventory> inspectInventories = new ArrayList<>(originInventoryList.size());
//            originInventoryList.forEach(originInventory -> {
//                inspectInventories.add(formatToInspectInventory(originInventory));
//            });
//            try {
//                inspectDataService.batchInsertSales(inspectSales);
//                if (inspectSales.size() > 0) {
//                    pendingFileLogIds.add(inspectSales.get(0).getFileId());
//                }
//                inspectDataService.batchInsertPurchase(inspectPurchases);
//                if (inspectPurchases.size() > 0) {
//                    pendingFileLogIds.add(inspectPurchases.get(0).getFileId());
//                }
//                inspectDataService.batchInsertInventory(inspectInventories);
//                if (inspectInventories.size() > 0) {
//                    pendingFileLogIds.add(inspectInventories.get(0).getFileId());
//                }
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        fileParseResultService.batchInsert(fileParseResults);
        fileParseLogService.updateStatus(pendingFileLogIds, userId, FileParseStatus.QUALITY_SUCCESS.toString());
    }

    private InspectInventory formatToInspectInventory(OriginInventory originInventory) {
        InspectInventory inspectInventory = new InspectInventory();
        BeanUtils.copyProperties(originInventory, inspectInventory);
        inspectInventory.setId(UUID.randomUUID().toString());
        inspectInventory.setOriginInventoryId(originInventory.getId());
        inspectInventory.setFileId(originInventory.getFileId());
        inspectInventory.setFromInstitutionCode(originInventory.getInstitutionCode());
        inspectInventory.setCreateTime(new Date());
        return inspectInventory;
    }

    private InspectPurchase formatToInspectPurchase(OriginPurchase originPurchase) {
        InspectPurchase inspectPurchase = new InspectPurchase();
        BeanUtils.copyProperties(originPurchase, inspectPurchase);
        inspectPurchase.setId(UUID.randomUUID().toString());
        inspectPurchase.setOriginPurchaseId(originPurchase.getId());
        inspectPurchase.setFileId(originPurchase.getFileId());
        inspectPurchase.setFromInstitutionCode(originPurchase.getInstitutionCode());
        inspectPurchase.setCreateTime(new Date());
        return inspectPurchase;
    }

    private InspectSale formatToInspectSale(OriginSale originSale) {
        InspectSale inspectSale = new InspectSale();
        BeanUtils.copyProperties(originSale, inspectSale);
        inspectSale.setId(UUID.randomUUID().toString());
        inspectSale.setOriginSaleId(originSale.getId());
        inspectSale.setFileId(originSale.getFileId());
        inspectSale.setFromInstitutionCode(originSale.getInstitutionCode());
        inspectSale.setCreateTime(new Date());
        return inspectSale;
    }

    /**
     * ??????????????????????????????
     *
     * @param in
     * @param projectFieldMapping
     * @return
     */
    private List<OriginSale> loadSaleDataByFile(InputStream in, String fileType, String fileParseLogId, List<FieldMapping> projectFieldMapping, String userId,String projectId) throws IOException {
        Map<String, Object> fieldMap = projectFieldMapping.stream()
                .collect(Collectors.toMap(FieldMapping::getPropertyName, FieldMapping::getTitleName));
        List<Map<String, Object>> dataList = ToolExcelUtils.readExcel(in, fileType, fieldMap, false);
        List<OriginSale> originSaleList = new ArrayList<>(dataList.size());
        dataList.forEach(data -> originSaleList.add(OriginSaleAutoMap.originSale(data, fileParseLogId, userId,projectId,"zhangqi",null)));

        return originSaleList;
    }

    /**
     * ??????????????????????????????
     * @param in
     * @param projectFieldMapping
     * @return
     */
    private List<OriginPurchase> loadPurchaseDataByFile(InputStream in, String fileType, String fileParseLogId, List<FieldMapping> projectFieldMapping, String userId,String projectId) throws IOException {
        Map<String, Object> fieldMap = projectFieldMapping.stream()
                .collect(Collectors.toMap(FieldMapping::getPropertyName, FieldMapping::getTitleName));
        List<Map<String, Object>> dataList = ToolExcelUtils.readExcel(in, fileType, fieldMap, false);
        List<OriginPurchase> originPurchaseList = new ArrayList<>(dataList.size());
        dataList.forEach(data -> originPurchaseList.add(OriginPurchaseAutoMap.originPurchase(data, fileParseLogId, userId,projectId,"zhangqi",null)));

        return originPurchaseList;
    }
    /**
     * ??????????????????????????????
     * @param in
     * @param projectFieldMapping
     * @return
     */
    private List<OriginInventory> loadOriginInventoryDataByFile(InputStream in, String fileType, String fileParseLogId, List<FieldMapping> projectFieldMapping, String userId,String projectId) throws IOException {
        Map<String, Object> fieldMap = projectFieldMapping.stream()
                .collect(Collectors.toMap(FieldMapping::getPropertyName, FieldMapping::getTitleName));
        List<Map<String, Object>> dataList = ToolExcelUtils.readExcel(in, fileType, fieldMap, false);
        List<OriginInventory> originInventoryList = new ArrayList<>(dataList.size());
        dataList.forEach(data -> originInventoryList.add(OriginInventoryAutoMap.originInventory(data, fileParseLogId, userId,projectId,"zhangqi",null)));

        return originInventoryList;
    }
    /**
     * ???????????????????????????????????????????????????
     *
     * @param fileParseLogId
     * @param fileParseFailureReason
     * @return
     */
    private FileParseResult generateFileResult(String fileParseLogId, String status, String
            fileParseFailureReason, String userId) {
        FileParseResult fileParseResult = new FileParseResult();
        fileParseResult.setId(UUID.randomUUID().toString());
        fileParseResult.setFileParseLogId(fileParseLogId);
        fileParseResult.setStatus(status);
        fileParseResult.setStatusRemark(fileParseFailureReason);
        fileParseResult.setTenantId(SystemContext.getTenantId());
        fileParseResult.setCreateBy(userId);
        fileParseResult.setCreateTime(new Date());
        return fileParseResult;
    }


    /**
     * ???????????????
     *
     * @param fileParse
     */
    private boolean parseFileName(FileParseDTO fileParse) {
        boolean flag = true;
        String fileName = fileParse.getFileName();
        String rule = fileParse.getFileNameRule();
        Pattern pattern = Pattern.compile(rule);
        Matcher matcher = pattern.matcher(fileName);
        try {
            while (matcher.find()) {
                fileParse.setFileBusinessType(matcher.group("businessType"));
                fileParse.setInstitutionCode(matcher.group("institutionCode"));
                fileParse.setInstitutionName(matcher.group("institutionName"));
                String date = matcher.group("date");
                if (StringUtils.isNumeric(date)) {
                    if (date.length() == 8) {
                        fileParse.setFileTime(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")));
                    } else if (date.length() == 14) {
                        fileParse.setFileTime(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                    } else if (date.length() > 14) {
                        fileParse.setFileTime(LocalDateTime.parse(date.substring(0, 14), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                    }
                } else {
                    return false;
                }
                fileParse.setFileType(matcher.group("fileType"));
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        if (flag) {
            if (StringUtils.isEmpty(fileParse.getInstitutionCode()) || StringUtils.isEmpty(fileParse.getFileBusinessType()) ||
                    null == fileParse.getFileTime() || StringUtils.isEmpty(fileParse.getFileType())) {
                return false;
            }
        }
        return flag;
    }

    /**
     * ????????????????????????????????????
     *
     * @param ftpProxy
     * @param filePath
     * @return
     */
    private List<String> findDirList(FtpProxy ftpProxy, String filePath) {
        List<String> paths = new ArrayList<>(4);
        if (StringUtils.isNotEmpty(filePath)) {
            if (filePath.contains("*")) {
                parsePath(ftpProxy, filePath, paths);
            } else {
                paths.add(filePath);
            }
        }
        return paths;
    }

    private void parsePath(FtpProxy ftpProxy, String filePath, List<String> paths) {
        int index = filePath.indexOf("*");
        String dir = filePath.substring(0, index);
        FTPFile[] files = ftpProxy.list(dir);
        for (FTPFile file : files) {
            if (file.isDirectory()) {
                String parseDir = dir + file.getName() + filePath.substring(index + 1);
                if (parseDir.contains("*")) {
                    parsePath(ftpProxy, parseDir, paths);
                } else {
                    paths.add(parseDir);
                }
            }
        }
    }
}
