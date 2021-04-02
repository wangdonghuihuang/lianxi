package com.softium.datacenter.paas.web.utils.easy.cache;

import java.util.List;

/**
 * 2019/11/8
 *
 * @author paul
 */
public interface CacheExcelData {

    /**
     * 批量追加 不能存在token 创建 保证有序
     *
     * @param token
     * @param tList
     */
    void lPushAll(String token, List tList);

    /**
     * 总数量
     *
     * @param token
     * @return
     */
    Long totalNumber(String token);

    /**
     * 获取指定token 的 数据 并删除 保证有序
     *
     * @param token
     * @param end
     * @return 如果 查询不到则返回
     */
    List lPopList(String token, long start, long end);


}
