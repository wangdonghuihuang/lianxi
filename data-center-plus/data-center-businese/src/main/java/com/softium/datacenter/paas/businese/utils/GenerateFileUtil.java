package com.softium.datacenter.paas.web.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * 类名称: CsvUtil
 * 类描述: 文件操作
 * 创建人:
 * Version 1.0.0
 */
public class GenerateFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(GenerateFileUtil.class);
    public static File makeTempCSV(String fileDir , String fileName, List heads, List<List> values) throws IOException {
         //创建文件
//        String path = ClassLoader.getSystemResource("").getPath();
        logger.info("创建文件开始");
        File file0 = new File(fileDir);
        logger.info("文件路径："+file0.getAbsolutePath());
        if(!file0.exists()){
            file0.mkdir();
        }
        File file = new File(file0.getAbsolutePath()+File.separator+ fileName);
        CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
        FileOutputStream fos =new FileOutputStream(file);
        byte[] uft8bom = {(byte)0xef,(byte)0xbb,(byte)0xbf};
        fos.write(uft8bom);
        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
        CSVPrinter printer =null;
        try {
            printer = new CSVPrinter(bufferedWriter, formator);
            //写入表头
            printer.printRecord(heads);
            //写入内容
            for (List value : values) {
                printer.printRecord(value);
            }
            printer.flush();
        }finally {
            printer.close();
            bufferedWriter.close();
        }
        return file;
    }

    public static void appendFileCsv(File file, List<List> content) {
        if(content !=null && !content.isEmpty()) {
            CSVPrinter printer = null;
            BufferedWriter bufferedWriter = null;
            try {
                CSVFormat formator = CSVFormat.DEFAULT.withRecordSeparator("\n");
                bufferedWriter =
                        new BufferedWriter(new OutputStreamWriter
                                (new FileOutputStream(file, true), "UTF-8"));
                printer = new CSVPrinter(new FileWriter(file, true), formator);
                //写入内容
                for (List value : content) {
                    printer.printRecord(value);
                }
                printer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    printer.close();
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

    }

}