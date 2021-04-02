package com.softium.datacenter.paas.web.service;

import com.softium.datacenter.paas.api.entity.Test;

/**
 * @author Fanfan.Gong
 **/
public interface TestService {
    void add(Test test);
    void update(Test test);
    void delete(String id);
    Test getById(String id);
}
