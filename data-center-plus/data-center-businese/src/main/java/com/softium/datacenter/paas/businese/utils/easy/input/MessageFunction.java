package com.softium.datacenter.paas.web.utils.easy.input;

import java.util.function.Function;

@FunctionalInterface
public interface MessageFunction<T extends BaseExcelReadModel> extends Function<CustomerData<T>, MessageModel> {

    @Override
    MessageModel apply(CustomerData<T> customerData);
}
