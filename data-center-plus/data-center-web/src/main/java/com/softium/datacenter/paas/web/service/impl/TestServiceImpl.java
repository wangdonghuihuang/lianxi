package com.softium.datacenter.paas.web.service.impl;

import com.softium.datacenter.paas.api.entity.Test;
import com.softium.datacenter.paas.api.mapper.TestMapper;
import com.softium.datacenter.paas.web.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fanfan.Gong
 **/
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private TestMapper testMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(Test test) {
        testMapper.insert(test);
    }

    @Override
    public void update(Test test) {
        testMapper.update(test);
    }

    @Override
    public void delete(String id) {
        testMapper.delete(id);
    }

    @Override
    public Test getById(String id) {
        return testMapper.getById(id);
    }
}
