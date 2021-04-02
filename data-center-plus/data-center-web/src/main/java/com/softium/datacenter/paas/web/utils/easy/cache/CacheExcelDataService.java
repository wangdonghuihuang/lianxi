package com.softium.datacenter.paas.web.utils.easy.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheExcelDataService implements CacheExcelData {


    private StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper;
    /**
     * 30分钟
     */
    private final long expire = 1800000;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 管道存储
    @Override
    public void lPushAll(String token, List list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final List convert = convert(list);
        redisTemplate.opsForList().rightPushAll(token, convert);
        redisTemplate.expire(token, expire, TimeUnit.MILLISECONDS);
    }


    public List convert(List list) {
        List<String> objects = new ArrayList<>(list.size());

        list.forEach(a -> {
            try {
                String s = objectMapper.writeValueAsString(a);
                objects.add(s);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        return objects;
    }

    @Override
    public Long totalNumber(String token) {
        return redisTemplate.opsForList().size(token);
    }

    // lua
    @Override
    public List lPopList(final String token, long start, long end) {

        if (start < 0 || end == 0) {
            List.of();
        }
        synchronized (this) {
            final Long aLong = totalNumber(token);

            if (aLong == null || aLong == Long.valueOf(0) || start > end) {
                return null;
            }

            List list = new ArrayList((int) (end - start));
            boolean b = aLong > end;
            end = b ? end : aLong;
            list.addAll(redisTemplate.opsForList().range(token, start, end));
            return list;
        }
    }
}
