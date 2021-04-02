package com.softium.datacenter.paas.web.utils.fileCommon;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MyZipUtil {
    public static File unzip(String zipFilePath, String outFileDir) {
        return unzip(FileUtil.file(zipFilePath), FileUtil.mkdir(outFileDir));
    }
    public static File unzip(File zipFile, File outFile) throws UtilException{
        ZipFile zipFileObj = null;

        try {
            zipFileObj = autoCharsetZipFile(zipFile);
            Enumeration<ZipEntry> em = (Enumeration<ZipEntry>) zipFileObj.entries();
            ZipEntry zipEntry = null;
            File outItemFile = null;

            while(em.hasMoreElements()) {
                zipEntry = (ZipEntry)em.nextElement();
                outItemFile = new File(outFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    outItemFile.mkdirs();
                } else {
                    FileUtil.touch(outItemFile);
                    copy(zipFileObj, zipEntry, outItemFile);
                }
            }
        } catch (Exception var9) {
            throw new UtilException(var9);
        } finally {
            IoUtil.close(zipFileObj);
        }

        return outFile;
    }
    private static ZipFile autoCharsetZipFile(File zipFile) throws Exception {
        ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
        Enumeration entries = zip.entries();

        try {
            while(entries.hasMoreElements()) {
                entries.nextElement();
            }

            return zip;
        } catch (Exception var4) {
            zip.close();
            zip = new ZipFile(zipFile, Charset.forName("UTF-8"));
            return zip;
        }
    }
    private static void copy(ZipFile zipFile, ZipEntry zipEntry, File outItemFile) throws IOException {
        InputStream in = null;
        BufferedOutputStream out = null;

        try {
            in = zipFile.getInputStream(zipEntry);
            out = FileUtil.getOutputStream(outItemFile);
            IoUtil.copy(in, out);
        } finally {
            IoUtil.close(out);
            IoUtil.close(in);
        }

    }
}
