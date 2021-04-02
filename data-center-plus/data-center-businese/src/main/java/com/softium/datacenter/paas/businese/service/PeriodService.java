package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.PeriodDTO;

import java.util.List;

/***
 * 账期服务类
 * @author net
 * @since 2020-11-16 11:42:07
 */
public interface PeriodService {
    /**
     * 保存 更新账期
     * */
    Boolean saveOrUpdate(PeriodDTO periodDTO);

    /**
     * 查询账期
     * */
    List<PeriodDTO> getPeriod(PeriodDTO periodDTO);

    /**
     * 获取为处理的账期
     * */
    PeriodDTO getUntreatedPeriod();
    /**根据账期id获取账期名称*/
    String queryPeriodNameService(String id,String tenantId);
}
