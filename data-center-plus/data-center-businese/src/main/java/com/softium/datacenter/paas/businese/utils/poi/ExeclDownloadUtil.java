package com.softium.datacenter.paas.web.utils.poi;
import com.softium.datacenter.paas.api.entity.FileHandleRule;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.awt.geom.RectangularShape;
import java.io.*;
import java.util.List;
import java.util.Map;

public class ExeclDownloadUtil {
    /**
     *
     * @param list 数据库中查询到的记录
     * @param out 输出流
     * @param sheetName 工作表名
     * @param headers 表头
     * @param propertyNames  记录所对应的实体类中的成员变量名称
     */
    public static void exportExcel(XSSFWorkbook wb, int sheetNum, String sheetName, List list, OutputStream out, String[] headers, String[] propertyNames, Map<String,List<FileHandleRule>> requireMap,Map<String,String> nameMap){
        XSSFSheet sheet = wb.createSheet();
        wb.setSheetName(sheetNum, sheetName);
        CellStyle style=wb.createCellStyle();
        Font font=wb.createFont();
        font.setColor(Font.COLOR_RED);
        style.setFont(font);
        Row row = sheet.createRow(0);
        sheet.setDefaultColumnWidth((short) 10);
        for(int i=0;i<headers.length;i++){
            String celValue="";
            Cell cell = row.createCell(i);
            //todo  再此根据值，去缓存对比，如果字段配置为必填，加上*符号
            /*headers是excel文件的表头，此表头是默认模板配置的，需要根据配置的名字，去获取对应数据中心的名称，如果数据中心的名称有配置为必填项，则标红*/
            if(requireMap.containsKey(nameMap.get(headers[i]))){
                celValue="*"+headers[i];
                cell.setCellValue(celValue);
                cell.setCellStyle(style);
            }else {
                celValue=headers[i];
                cell.setCellValue(celValue);
            }

        }
        if(list!=null&&list.size()>0){
            for(int i=0;i<list.size();i++){
                Object obj = list.get(i);
                Row row1 = sheet.createRow(i + 1);
                for(int j=0;j<headers.length;j++){
                    Cell cell = row1.createCell(j);//创建单元格
                    try {
                        String val = BeanUtils.getProperty(obj, propertyNames[j]);
                        cell.setCellValue(val);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    /**文件下载
     * @param filePath 文件上级目录
     * @param fileName 文件名
     * @param newName  下载的展示文件名
     * */
    public static ResponseEntity<InputStreamResource> downloadFile(String filePath, String fileName, String newName){
        ResponseEntity<InputStreamResource> response = null;
        try {
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);
            HttpHeaders headers=new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition",
                    "attachment; filename="
                            + new String(newName.getBytes("UTF-8"), "ISO-8859-1") + ".xlsx");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            response = ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(new InputStreamResource(inputStream));
        }catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return response;
    }
}
