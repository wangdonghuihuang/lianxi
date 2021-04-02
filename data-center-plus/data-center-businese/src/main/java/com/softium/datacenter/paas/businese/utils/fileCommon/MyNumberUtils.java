package com.softium.datacenter.paas.web.utils.fileCommon;

public class MyNumberUtils {
    public static boolean isBlankChar(char c) {
        return isBlankChar((int)c);
    }

    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }
}
