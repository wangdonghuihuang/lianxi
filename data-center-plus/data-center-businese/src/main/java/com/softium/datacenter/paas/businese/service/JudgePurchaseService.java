package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.query.JudgeDataQuery;

public interface JudgePurchaseService {
    public void judgePurchaseData(JudgeDataQuery dataQuery) throws Exception;
}
