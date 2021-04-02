package com.softium.datacenter.paas.web.utils.fileCommon;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class MyFileUtils {
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    public static final String CLASS_EXT = ".class";
    public static final String JAR_FILE_EXT = ".jar";
    public static final String JAR_PATH_EXT = ".jar!";
    public static final String PATH_FILE_PRE = "file:";
    public static boolean isWindows() {
        return '\\' == File.separatorChar;
    }
    public static boolean isUnix() {
        return '/' == File.separatorChar;
    }
    public static String getSeparator() {
        return isWindows() ? "\\" : "/";
    }
    public static List<File> loopFiles(File file) {
        return loopFiles(file, (FileFilter)null);
    }
    public static List<File> loopFiles(File file, FileFilter fileFilter) {
        List<File> fileList = new ArrayList();
        if (null == file) {
            return fileList;
        } else if (!file.exists()) {
            return fileList;
        } else {
            if (file.isDirectory()) {
                File[] var3 = file.listFiles();
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    File tmp = var3[var5];
                    fileList.addAll(loopFiles(tmp, fileFilter));
                }
            } else if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }

            return fileList;
        }
    }
}
