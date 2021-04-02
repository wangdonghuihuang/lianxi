package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.dto.SaleCountDto;
import com.softium.datacenter.paas.web.dto.IndustryDisplayDTO;
import com.softium.datacenter.paas.web.dto.InstitutionMatchDTO;
import com.softium.datacenter.paas.web.dto.ProductMatchDTO;

import java.io.IOException;

/***
 * 匹配服务
 * @author net
 * @since 2020-11-17 09:52:56
 */
public interface MatchService {
    /**
     * 经销商匹配
     * @return  返回值为中间关系表的id
     */
    void fromInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException;

    /**
     * 取消经销商匹配
     * @return  返回值为中间关系表的id
     */
    void cancelFromInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException;

    /**
     * 机构匹配
     */
    void toInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException;

    /**
     * 取消机构匹配
     */
    void cancelToInstitutionMatch(InstitutionMatchDTO institutionMatchDTO) throws IOException;

    /**
     * 产品匹配
     */
    void productMatch(ProductMatchDTO productMatchDTO) throws IOException;

    /**
     * 取消产品匹配
     */
    void cancelProductMatch(ProductMatchDTO productMatchDTO) throws IOException;

    /**
     * 单位匹配
     */
    void productUnitMatch(ProductMatchDTO productMatchDTO) throws IOException;

    /**
     * 单取消位匹配
     */
    void cancelProductUnitMatch(ProductMatchDTO productMatchDTO) throws IOException;

    /**
     * 新增经销商主数据
     * */
    InstitutionMatchDTO addFromInstitution(InstitutionMatchDTO dto) throws IOException;

    /**
     * 新增机构主数据
     * */
    InstitutionMatchDTO addToInstitution(InstitutionMatchDTO institutionDTO) throws IOException;

    /**
     * 查询机构,经销商，产品，单位统计数据
     */
    SaleCountDto getMatchCount(String status,String periodName);

    /**
     * 经销商行业智能匹配
     * */
    void fromIndustyMatch();

    /**
     * 机构行业智能匹配
     * */
    void toIndustyMatch();
    /**行业库智能匹配，行业库推荐是否显示按钮业务层*/
    IndustryDisplayDTO queryDisplayService(String tabName);
}
