package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.ParseLogDTO;
import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;

import java.util.List;

/**源数据质检接口层*/
public interface FileJudgeTaskService {
    public void judgeOriginSaleData(JudgeDataQuery dataQuery) throws Exception;
    public void parseLogBatchInsert(List<ParseLogDTO> logDTOList);
}
