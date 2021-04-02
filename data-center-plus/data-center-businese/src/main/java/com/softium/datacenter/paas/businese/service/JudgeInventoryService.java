package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;

public interface JudgeInventoryService {
    public void judgeInventData(JudgeDataQuery dataQuery) throws Exception;
}
