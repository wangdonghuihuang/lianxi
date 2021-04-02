package com.softium.datacenter.paas.web.controller;

import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.*;
import com.softium.datacenter.paas.api.dto.excel.MatchDistribDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchMechanismDTO;
import com.softium.datacenter.paas.api.dto.excel.MatchProductDTO;
import com.softium.datacenter.paas.api.dto.query.OriginDataQuery;
import com.softium.datacenter.paas.api.enums.InspectStatus;
import com.softium.datacenter.paas.web.dto.IndustryDisplayDTO;
import com.softium.datacenter.paas.web.dto.InstitutionMatchDTO;
import com.softium.datacenter.paas.web.dto.ProductMatchDTO;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.web.manage.MatchSvcService;
import com.softium.datacenter.paas.web.service.InspectDataService;
import com.softium.datacenter.paas.web.service.MatchService;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.framework.common.SystemContext;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.ErrorInfo;
import com.softium.framework.service.BusinessException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/****
 * 经销商，机构，产品，单位匹配
 * @author net
 * @since 2020-11-13 11:03:52
 */
@RestController
@RequestMapping("match")
@Slf4j
public class MatchController extends BaseController {

    @Autowired
    private InspectDataService inspectDataService;
    @Autowired
    private MatchSvcService matchSvcService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private DistributorService distributorService;
    @Autowired
    private PeriodService periodService;
    @PostMapping("/list")
    public ActionResult list(@RequestBody PeriodDTO periodDTO) {

        return null;
    }

    /**
     * 经销商匹配列表查询
     */
    @PostMapping("getFromInstitutionMatch")
    public ActionResult<PageInfo<List<InspectSaleDTO>>> getFromInstitutionMatch(@RequestBody OriginDataQuery originDataQuery) {
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(inspectDataService.getFromInstitutionMatch(originDataQuery));
        pageInfo.setPageSize(originDataQuery.getPageSize());
        pageInfo.setPageNum(originDataQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    /**
     * 机构匹配列表查询
     */
    @PostMapping("getToInstitutionMatch")
    public ActionResult getToInstitutionMatch(@RequestBody OriginDataQuery originDataQuery) {
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(inspectDataService.getToInstitutionMatch(originDataQuery));
        pageInfo.setPageSize(originDataQuery.getPageSize());
        pageInfo.setPageNum(originDataQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    /**
     * 产品匹配列表查询
     */
    @PostMapping("getProductMatch")
    public ActionResult getProductMatch(@RequestBody OriginDataQuery originDataQuery) {
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(inspectDataService.getProductMatch(originDataQuery));
        pageInfo.setPageSize(originDataQuery.getPageSize());
        pageInfo.setPageNum(originDataQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    /**
     * 经销商匹配列表查询
     */
    @PostMapping("getProductUnitMatch")
    public ActionResult getProductUnitMatch(@RequestBody OriginDataQuery originDataQuery) {
        PageInfo<List<InspectSaleDTO>> pageInfo = new PageInfo(inspectDataService.getProductUnitMatch(originDataQuery));
        pageInfo.setPageSize(originDataQuery.getPageSize());
        pageInfo.setPageNum(originDataQuery.getCurrent());
        return new ActionResult<>(pageInfo);
    }

    /**
     * 企业主数据智能推荐列表
     */
    @PostMapping("enterpriseIntelligentMatchingList")
    public ActionResult<List<IntelligentMatchingDTO>> enterpriseIntelligentMatchingList(@RequestBody IntelligentMatchingRequestDTO source) {
        return matchSvcService.intelligentMatchingList(source, SystemContext.getTenantId(), "enterprise-hco");
    }

    /**
     * 行业主数据智能推荐列表
     */
    @PostMapping("industryIntelligentMatchingList")
    public ActionResult<List<IntelligentMatchingDTO>> industryIntelligentMatchingList(@RequestBody IntelligentMatchingRequestDTO source) {
        return matchSvcService.intelligentMatchingList(source, SystemContext.getTenantId(), "industry-hco");
    }

    /**
     * 经销商匹配
     */
    @PostMapping("/fromInstitutionMatch")
    public ActionResult<?> fromInstitutionMatch(@RequestBody InstitutionMatchDTO institutionMatchDTO) throws IOException {
        matchService.fromInstitutionMatch(institutionMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 取消经销商匹配
     */
    @PostMapping("/cancelFromInstitutionMatch")
    public ActionResult<?> cancelFromInstitutionMatch(@RequestBody InstitutionMatchDTO institutionMatchDTO) throws IOException {
        matchService.cancelFromInstitutionMatch(institutionMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 机构匹配
     */
    @PostMapping("/toInstitutionMatch")
    public ActionResult<?> toInstitutionMatch(@RequestBody InstitutionMatchDTO institutionMatchDTO) throws IOException {
        matchService.toInstitutionMatch(institutionMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 取消机构匹配
     */
    @PostMapping("/cancelToInstitutionMatch")
    public ActionResult<?> cancelToInstitutionMatch(@RequestBody InstitutionMatchDTO institutionMatchDTO) throws IOException {
        matchService.cancelToInstitutionMatch(institutionMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 产品匹配
     */
    @PostMapping("/productMatch")
    public ActionResult<?> productMatch(@RequestBody ProductMatchDTO productMatchDTO) throws IOException {
        matchService.productMatch(productMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 取消产品匹配
     */
    @PostMapping("/cancelProductMatch")
    public ActionResult<?> cancelProductMatch(@RequestBody ProductMatchDTO productMatchDTO) throws IOException {
        matchService.cancelProductMatch(productMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 单位匹配
     */
    @PostMapping("/productUnitMatch")
    public ActionResult<?> productUnitMatch(@RequestBody ProductMatchDTO productMatchDTO) throws IOException {
        matchService.productUnitMatch(productMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 取消单位匹配
     */
    @PostMapping("/cancelProductUnitMatch")
    public ActionResult<?> cancelProductUnitMatch(@RequestBody ProductMatchDTO productMatchDTO) throws IOException {
        matchService.cancelProductUnitMatch(productMatchDTO);
        return new ActionResult<>();
    }

    /**
     * 查询机构,经销商，产品，单位统计数据
     *
     * @return
     */
    @GetMapping("/getSaleCount")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "fromInstitution,toInstitution,product,unit", required = true, dataType = "InspectStatus", paramType = "query")
    })
    public SaleCountDto getSaleCount(String status,String periodId) {
            if(StringUtils.isEmpty(periodId)){
            PeriodDTO periodDTO = periodService.getUntreatedPeriod();
            periodId=periodDTO.getId();
        }
        return matchService.getMatchCount(status,periodId);
    }

    /**
     * 导出待匹配
     */
    @PostMapping("/exportUnMatch")
    public CompletableFuture<ResponseEntity<byte[]>> exportUnMatch(@RequestBody OriginDataQuery pageModel) {
//        List<InspectSaleExportDTO> inspectSaleExportDTOS = inspectDataService.exportInspectSaleList(pageModel);
//        String filename = "InspectSaleTemplate" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
//        return CompletableFuture.supplyAsync(() -> downloadExcel(inspectSaleExportDTOS, InspectSaleExportDTO.class, filename, "sheet"));
        String zhi = pageModel.getStatusList();
        if (pageModel.getStatusList() == null && StringUtils.isEmpty(pageModel.getStatusList())) {
            throw new BusinessException(new ErrorInfo("file_not_exit", "导出文件类型不能为空"));
        }
        if (pageModel.getStatusList().equals(InspectStatus.product.toString())) {
            List<MatchProductDTO> distribDTOS = inspectDataService.queryExportProductService(pageModel.getPeriodId());
            String fileName = "Product" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return CompletableFuture.supplyAsync(() -> downloadExcel(distribDTOS, MatchDistribDTO.class, fileName, "sheet"));
        } else if (pageModel.getStatusList().equals(InspectStatus.fromInstitution.toString())) {
            List<MatchDistribDTO> distribDTOS = inspectDataService.queryExportDistribService(pageModel.getPeriodId());
            String fileName = "Distributor" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return CompletableFuture.supplyAsync(() -> downloadExcel(distribDTOS, MatchDistribDTO.class, fileName, "sheet"));
        } else if (pageModel.getStatusList().equals(InspectStatus.toInstitution.toString())) {
            List<MatchMechanismDTO> distribDTOS = inspectDataService.queryExportMechanisService(pageModel.getPeriodId());
            String fileName = "Mechanism" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return CompletableFuture.supplyAsync(() -> downloadExcel(distribDTOS, MatchDistribDTO.class, fileName, "sheet"));
        }
        return null;
    }

    /**
     * 行业智能匹配
     */
    @PostMapping("/industyMatch")
    public void industyMatch() {

    }

    /**
     * 经销商行业智能匹配
     */
    @PostMapping("/fromIndustyMatch")
    public void fromIndustyMatch() {

    }

    /**
     * 机构行业智能匹配
     */
    @PostMapping("/toIndustyMatch")
    public void toIndustyMatch() {

    }

    /**
     * 经销商新增企业主数据
     */
    @PostMapping("/addFromInstitution")
    public ActionResult<InstitutionMatchDTO> addFromInstitution(@RequestBody InstitutionMatchDTO dto) throws IOException {
        return new ActionResult<>(matchService.addFromInstitution(dto));
    }

    /**
     * 机构新增企业主数据
     */
    @PostMapping("/addToInstitution")
    public ActionResult<InstitutionMatchDTO> addToInstitution(@RequestBody InstitutionMatchDTO institutionDTO) throws IOException {
        return new ActionResult<>(matchService.addToInstitution(institutionDTO));
    }

    /**
     * 获取主数据配置
     */
    @PostMapping("/getMdmConfig")
    public ActionResult<MdmConfigDTO> getMdmConfig() {
        return distributorService.getMdmConfig(SystemContext.getTenantId());
    }


    /**
     * 主数据产品列表查询
     */
    @PostMapping("getMdmProductList")
    public ActionResult<PageInfo<ProductDTO>> getMdmProductList(@RequestBody ProductDTO productDTO) {
        return distributorService.getProductList(productDTO, SystemContext.getTenantId());
    }

    /**
     * 主数据字典查询
     */
    @PostMapping("getDictionary")
    public ActionResult<PageInfo<DictionaryDTO>> getDictionary(@RequestBody DictionaryDTO dictionaryDTO) {
        return distributorService.getDictionary(dictionaryDTO, SystemContext.getTenantId());
    }

    /**
     * 获取行业智能推荐按钮是否显示
     *
     * @param tabName 页面传参  外层初始化页面传 outMatching,点击匹配按钮传 insideMatching
     */
    @GetMapping("getIsDisplay")
    public IndustryDisplayDTO getDisplayController(@RequestParam("tabName") String tabName) {
        return matchService.queryDisplayService(tabName);
    }
}
