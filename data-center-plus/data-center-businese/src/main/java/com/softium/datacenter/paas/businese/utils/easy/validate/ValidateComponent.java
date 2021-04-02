package com.softium.datacenter.paas.web.utils.easy.validate;

import com.softium.datacenter.paas.web.utils.easy.input.BaseExcelReadModel;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 校验组件
 * 2019/11/14
 *
 * @author paul
 */
public class ValidateComponent {

    final Map<Integer, Map<Integer, String>> mapMap;


    public ValidateComponent(Map<Integer, Map<Integer, String>> mapMap) {
        this.mapMap = mapMap;
    }


    public boolean validate(BaseExcelReadModel baseExcelReadModel) {

        if (baseExcelReadModel == null) {
            return false;
        }
        Map<Integer, String> integerSetMap = mapMap.get(baseExcelReadModel.getRowIndex());
        if (integerSetMap == null) {
            return true;
        }
        return false;
    }

    /**
     * 验证
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public boolean tryValidate(Integer rowIndex, Integer columnIndex) {
        Map<Integer, String> integerSetMap = mapMap.get(rowIndex);
        if (integerSetMap == null) {
            return true;
        }
        if (StringUtils.isBlank(integerSetMap.get(columnIndex))) {
            return true;
        }
        return false;

    }

}
