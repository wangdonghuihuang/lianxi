package com.softium.datacenter.paas.web.utils;

public class GenerateCode {
     public static  String generate(String prefix, Integer maxCode, Integer digit) {
       return prefix+String.format("%0"+digit.toString()+"d",maxCode+1);
    }
}
