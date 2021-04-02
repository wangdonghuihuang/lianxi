package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.entity.IntelligentMatch;

import java.util.List;

/**
 * @author huashan.li
 */
public interface IntelligentMatchService {
    List<IntelligentMatch> list();

    List<IntelligentMatch> updateSetting(List<IntelligentMatch> intelligentMatches);
}
