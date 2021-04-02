package com.softium.datacenter.paas.web.controller;

import com.softium.datacenter.paas.api.dto.InstitutionDTO;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sofium
 */
@RestController
@RequestMapping("institution")
public class MdmController {

  @Autowired
  private DistributorService distributorService;

    @PostMapping("/add")
    public ActionResult add(@RequestBody InstitutionDTO institutionDTO){
        return new ActionResult<>(distributorService.addInstitution(institutionDTO, "saaspilot"));
    }

  @PostMapping("/list")
  public ActionResult list(@RequestBody InstitutionDTO institutionDTO){
    return new ActionResult<>(distributorService.getInstitutionList(institutionDTO, "saaspilot"));
  }

}
