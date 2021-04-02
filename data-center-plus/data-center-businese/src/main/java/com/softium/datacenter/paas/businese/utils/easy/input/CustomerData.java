package com.softium.datacenter.paas.web.utils.easy.input;

import com.softium.datacenter.paas.web.utils.easy.validate.ValidateComponent;
import lombok.Data;

import java.util.List;

/**
 * 2019/11/11
 *
 * @author paul
 */
@Data
public class CustomerData<T extends BaseExcelReadModel> {

    private List<T> list;

    public ValidateComponent component;

    public CustomerData(List<T> list) {
        this.list = list;
    }

    public CustomerData(List<T> list, ValidateComponent component) {
        this.list = list;
        this.component = component;
    }

    @Override
    public String toString() {
        return "CustomerData{" +
                "list=" + list +
                '}';
    }
}
