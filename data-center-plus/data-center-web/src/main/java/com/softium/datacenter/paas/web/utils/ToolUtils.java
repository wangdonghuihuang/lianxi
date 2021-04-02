package com.softium.datacenter.paas.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**全局常用工具类
 **/
@Slf4j
public class ToolUtils {
  /**
   * @param object  校验对象
   * */
  public static boolean checkBeanIsNull(Object object){
      if (null == object) {
          return true;
      }
      try {
          for (Field f : object.getClass().getDeclaredFields()) {
              f.setAccessible(true);
              if (f.get(object) != null && StringUtils.isNotBlank(f.get(object).toString())) {
                  return false;
              }
          }
      } catch (Exception e) {
          log.error("parse bean{}",e.getMessage());
      }
      return true;
  }
  /**校验集合对象，筛选出重复值
   * */
  public static <E, R> List<R> getDuplicateValue(List<E> list, Function<E, R> function) {

      Map<R, Long> frequencies = list.stream().collect(Collectors.groupingBy(function, Collectors.counting()));
      return frequencies.entrySet().stream()
              .filter(entry -> entry.getValue() > 1).map(entry -> entry.getKey()).collect(Collectors.toList());

  }
  /**获取字符串编码格式*/
  public static String getEncoding(String str) {
      String encode = "GB2312";
      try {
          if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GB2312
              String s = encode;
              return s; //是的话，返回“GB2312“，以下代码同理
          }
          encode = "ISO-8859-1";
          if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是ISO-8859-1
              String s1 = encode;
              return s1;
          }
          encode = "UTF-8";
          if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是UTF-8
              String s2 = encode;
              return s2;
          }
          encode = "GBK";
          if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GBK
              String s3 = encode;
              return s3;
          }
      } catch (UnsupportedEncodingException ex) {
          log.error("encode error{}",ex.getMessage());
      }
      return "";
  }
  /**根据参数编码格式，统一成utf8*/
  public static String encodeProperty(String str){
      try {
         if(str.equals(new String(str.getBytes("ISO-8859-1"),"ISO-8859-1"))){
              str=new String(str.getBytes("ISO-8859-1"), "utf-8");
          }
      }catch (UnsupportedEncodingException ex){
          log.error("getencode error{}",ex.getMessage());
      }
      return str;
  }
}
