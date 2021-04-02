package com.softium.datacenter.paas.web.utils;

import java.util.UUID;

/**
 * @author John
 **/
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
