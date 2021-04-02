package com.softium.datacenter.paas.web.utils.fileCommon;
/**常用string处理类*/
public class MyStringUtil {
    /**判断字符串是否为空*/
    public static boolean isEmpty(String str){
        return str==null||str.length()==0;
    }
    /**从字符串最后一个之前截取*/
    public static String subStringBeforeLast(String str,String separator){
        if(!isEmpty(str)&&!isEmpty(separator)){
            int pos=str.lastIndexOf(separator);
            return pos==-1?str : str.substring(0,pos);
        }else {
            return str;
        }
    }
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (isEmpty(separator)) {
            return "";
        } else {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
        }
    }
    public static String appendIfMissing(String str, String suffix, String... suffixes) {
        return appendIfMissing(str, suffix, false, suffixes);
    }
    public static String appendIfMissing(String str, String suffix, boolean ignoreCase, String... suffixes) {
        if (str != null && !isEmpty(suffix) && !endWith(str, suffix, ignoreCase)) {
            if (suffixes != null && suffixes.length > 0) {
                String[] var4 = suffixes;
                int var5 = suffixes.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String s = var4[var6];
                    if (endWith(str, s, ignoreCase)) {
                        return str.toString();
                    }
                }
            }

            return str.toString().concat(suffix.toString());
        } else {
            return str.toString();
        }
    }
    public static boolean endWith(String str, char c) {
        return c == str.charAt(str.length() - 1);
    }

    public static boolean endWith(String str, String suffix, boolean isIgnoreCase) {
        if (!isBlank(str) && !isBlank(suffix)) {
            return isIgnoreCase ? str.toString().toLowerCase().endsWith(suffix.toString().toLowerCase()) : str.toString().endsWith(suffix.toString());
        } else {
            return false;
        }
    }

    public static boolean endWith(String str, String suffix) {
        return endWith(str, suffix, false);
    }

    public static boolean endWithIgnoreCase(String str, String suffix) {
        return endWith(str, suffix, true);
    }
    public static boolean isBlank(String str) {
        int length;
        if (str != null && (length = str.length()) != 0) {
            for(int i = 0; i < length; ++i) {
                if (!MyNumberUtils.isBlankChar(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
    public static boolean containsIgnoreCase(String str, String testStr) {
        if (null == str) {
            return null == testStr;
        } else {
            return str.toString().toLowerCase().contains(testStr.toString().toLowerCase());
        }
    }
    public static String substringBeforeLast(String str, String separator) {
        if (!isEmpty(str) && !isEmpty(separator)) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }
    /**string字符串全部转小写*/
    public static String strtoLowerCase(String str){
        return  str.toLowerCase();
    }
    public static void main(String[] args) {
        String zhi="eee";
        int pos=zhi.lastIndexOf(".");
        System.out.println(pos);
        System.out.println(zhi.substring(0,pos));
    }
}
