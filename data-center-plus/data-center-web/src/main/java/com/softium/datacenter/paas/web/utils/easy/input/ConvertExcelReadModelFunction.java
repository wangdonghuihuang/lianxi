package com.softium.datacenter.paas.web.utils.easy.input;

import java.util.List;
import java.util.function.Function;

/**
 * 2019/11/8
 *
 * @author paul
 */
@FunctionalInterface
public interface ConvertExcelReadModelFunction<T extends BaseExcelReadModel> extends Function<List<T>, List<Object>> {


    @Override
    List<Object> apply(List<T> list);
}
