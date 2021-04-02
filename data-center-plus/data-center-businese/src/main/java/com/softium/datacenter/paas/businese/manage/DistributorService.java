package com.softium.datacenter.paas.web.manage;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.DictionaryDTO;
import com.softium.datacenter.paas.api.dto.MdmConfigDTO;
import com.softium.framework.common.SystemConstant;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.rpc.service.ServiceProxy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Fanfan.Gong
 **/
@Service
@ServiceProxy(protocol = "${paas.protocol}", serviceName = "${paas.host}", restTemplateBeanName = "restTemplate")
public interface DistributorService {
    /**
     * 获取租户下的经销商信息
     * @param tenantId
     * @return
     */
    @GetMapping("distributor/{tenantId}")
    ActionResult<List<DistributorDTO>> listDistributor(@PathVariable("tenantId") String tenantId);

    @PostMapping("distributor/search")
    ActionResult<List<DistributorDTO>> searchDistributor(@RequestBody DistributorDTO distributor);

    /***
     * 新增企业主数据
     * @param institutionDto
     * @author net
     * @since 2020-11-14 16:17:45
     */
    @PostMapping("institution/insert")
    ActionResult<InstitutionDTO> addInstitution(@RequestBody InstitutionDTO institutionDto, @RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

    /***
     * 获取企业主数据列表
     * @param institutionDto
     * @author net
     * @since 2020-11-14 16:18:47
     */
    @PostMapping("institution/list")
    ActionResult<PageInfo<InstitutionDTO>> getInstitutionList(@RequestBody InstitutionDTO institutionDto, @RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

    /***
     * 行业匹配
     * @param institutionDto
     * @param tenantId
     * @author net
     * @since 2020-11-17 17:17:41
     */
    @PostMapping("institution/cloneInstitution")
    ActionResult<InstitutionDTO> cloneInstitution(@RequestBody InstitutionDTO institutionDto, @RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

    /***
     * 获取主数据配置表
     * @param tenantId
     * @author net
     * @since 2020-11-18 17:39:11
     *
     */
    @PostMapping("setting/config")
    ActionResult<MdmConfigDTO> getMdmConfig(@RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

    /***
     * 获取产品列表
     * @param productDTO
     * @param tenantId
     * @author net
     * @since 2020-11-21 14:30:43
     */
    @PostMapping("product/list")
    ActionResult<PageInfo<ProductDTO>> getProductList(@RequestBody ProductDTO productDTO, @RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

    @PostMapping("dictionary/search")
    ActionResult<PageInfo<DictionaryDTO>> getDictionary(@RequestBody DictionaryDTO dictionaryDTO, @RequestHeader(SystemConstant.HEADER_TENANT_ID)String tenantId);

}
