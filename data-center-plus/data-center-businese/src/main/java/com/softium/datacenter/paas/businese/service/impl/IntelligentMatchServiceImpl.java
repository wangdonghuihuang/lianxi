package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.IntelligentMatch;
import com.softium.datacenter.paas.api.mapper.IntelligentMatchMapper;
import com.softium.datacenter.paas.web.service.IntelligentMatchService;
import com.softium.framework.common.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author huashan.li
 */
@Service
public class IntelligentMatchServiceImpl implements IntelligentMatchService {
    @Autowired
    private IntelligentMatchMapper intelligentMatchMapper;
    @Override
    public List<IntelligentMatch> list() {
        List<IntelligentMatch> intelligentMatches = intelligentMatchMapper.list(SystemContext.getTenantId());
        if(CollectionUtils.isEmpty(intelligentMatches)){
            //初始化数据
            IntelligentMatch intelligentMatch = new IntelligentMatch();
            intelligentMatch.setRemark("行业库推荐");
            intelligentMatch.setMatchRule("INDUSTRY_MATCH");
            intelligentMatch.setDisabled(0);
            intelligentMatch.setAutoMatchPass(1);
            intelligentMatch.setStartConfidence(100);
            intelligentMatch.setEndConfidence(100);
            IntelligentMatch intelligentMatch2 = new IntelligentMatch();
            intelligentMatch2.setRemark("企业库推荐");
            intelligentMatch2.setMatchRule("ENTERPRISE_MATCH");
            intelligentMatch2.setDisabled(1);
            intelligentMatch2.setAutoMatchPass(1);
            intelligentMatch2.setStartConfidence(100);
            intelligentMatch2.setEndConfidence(100);
            intelligentMatches.add(intelligentMatch2);
            intelligentMatches.add(intelligentMatch);
            intelligentMatchMapper.batchInsert(intelligentMatches);
        }
        return intelligentMatchMapper.list(SystemContext.getTenantId());
    }

    @Override
    public List<IntelligentMatch> updateSetting(List<IntelligentMatch> intelligentMatches) {
        intelligentMatchMapper.batchUpdateSelective(intelligentMatches);
        return intelligentMatchMapper.list(SystemContext.getTenantId());
    }
}
