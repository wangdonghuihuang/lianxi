package com.softium.datacenter.paas.web.controller;

import com.softium.datacenter.paas.api.dto.PeriodDTO;
import com.softium.datacenter.paas.web.service.PeriodService;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("period")
public class PeriodController {

    @Autowired
    private PeriodService periodService;

    @PostMapping("/list")
    public ActionResult<List<PeriodDTO>> list(@RequestBody PeriodDTO periodDTO){
        return new ActionResult<>(periodService.getPeriod(periodDTO));
    }

    @PostMapping("/detail")
    public ActionResult<PeriodDTO> detail(@RequestBody PeriodDTO periodDTO){
        List<PeriodDTO> periodDTOList = periodService.getPeriod(periodDTO);
        return new ActionResult<>(CollectionUtils.isEmpty(periodDTOList) ? null:periodDTOList.get(0));
    }

    /**
     * 入参id必填
     * */
    @PostMapping("/saveOrUpdate")
    public ActionResult<Boolean> saveOrUpdate(@RequestBody PeriodDTO periodDTO){
        return new ActionResult<>(periodService.saveOrUpdate(periodDTO));
    }

    /**
     *获取当前显示账期
     * @return
     */
    @PostMapping("/getUntreatedPeriod")
    public ActionResult<?> getUntreatedPeriod(){
        return new ActionResult<>(periodService.getUntreatedPeriod());
    }
}
