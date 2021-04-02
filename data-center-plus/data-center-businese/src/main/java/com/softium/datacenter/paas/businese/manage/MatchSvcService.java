package com.softium.datacenter.paas.web.manage;

import com.softium.datacenter.paas.api.dto.IntelligentMatchingDTO;
import com.softium.datacenter.paas.api.dto.IntelligentMatchingRequestDTO;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.rpc.service.ServiceProxy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * 调用主数据智能推荐
 * @author net
 * @since 2020-11-17 15:48:27
 */
@Service
@ServiceProxy(protocol = "${mdm.protocol}", serviceName = "${mdm.host}", restTemplateBeanName = "mdmRestTemplate")
public interface MatchSvcService {

    /***
     * @param source
     * @author net
     * @since 2020-11-17 16:10:44
     * industry-hco-equal-match  行业批量匹配code
     * enterprise-hco  获取企业智能匹配列表（默认20条）
     * industry-hco  获取行业智能匹配列表（默认20条）
     */
    @PostMapping("api/match/sync")
    ActionResult<List<IntelligentMatchingDTO>> intelligentMatchingList(@RequestBody IntelligentMatchingRequestDTO source, @RequestHeader("tenantId") String tenantId, @RequestHeader("taskCode") String taskCode );

}
