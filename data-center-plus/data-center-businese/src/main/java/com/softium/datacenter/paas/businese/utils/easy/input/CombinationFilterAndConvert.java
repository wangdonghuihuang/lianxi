package com.softium.datacenter.paas.web.utils.easy.input;

import java.util.function.Function;

@FunctionalInterface
public interface CombinationFilterAndConvert<T extends BaseExcelReadModel> extends Function<CustomerData<T>, CombinationResultModel> {


    @Override
    CombinationResultModel apply(CustomerData<T> customerData);
}
