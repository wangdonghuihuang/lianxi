package com.softium.datacenter.paas.web.utils.poi;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Slf4j
public class ExportExcelUtils {
    private static String fileWritePath="datalogDownload";
    /*生成Excel表格*/
    public static File expoerDataExcel(ArrayList<String> titleKeyList,
                                Map<String, String> titleMap, List<Map<String, Object>> srcList,String fileName) {
        Date date = new Date();
        FileOutputStream out =null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
        String xlsFileName = fileName + ".xlsx";

        Workbook wb = new SXSSFWorkbook(100);   //创建Excel文件
        Sheet sheet = null;     //工作表对象
        Row nRow = null;        //行对象
        Cell nCell = null;      //列对象

        int rowNo = 0;      //总行号
        int pageRowNo = 0;  //页行号

        for (int k = 0; k < srcList.size(); k++) {
            Map<String, Object> srcMap = srcList.get(k);
            //写入300000条后切换到下个工作表
            if (rowNo % 100000 == 0) {
                wb.createSheet("质检报告" + (rowNo / 100000));//创建新的sheet对象
                sheet = wb.getSheetAt(rowNo / 100000);        //动态指定当前的工作表
                pageRowNo = 0;      //新建了工作表,重置工作表的行号为0

                nRow = sheet.createRow(pageRowNo++);  // 定义表头
                // 列数 titleKeyList.size()
                for (int i = 0; i < titleKeyList.size(); i++) {
                    Cell cell_tem = nRow.createCell(i);
                    cell_tem.setCellValue(titleMap.get(titleKeyList.get(i)));
                }
                rowNo++;

            }
            rowNo++;
            nRow = sheet.createRow(pageRowNo++);    //新建行对象

            // 行，获取cell值
            for (int j = 0; j < titleKeyList.size(); j++) {
                nCell = nRow.createCell(j);
                //从数据集map中根据key获取值，若无则设置为空,titlelist有无有序无所谓，只是利用map获取key值
                if (srcMap.get(titleKeyList.get(j)) != null) {
                    nCell.setCellValue(srcMap.get(titleKeyList.get(j)).toString());
                } else {
                    nCell.setCellValue("");
                }
            }
        }
        File file0 = new File(fileWritePath);
        if(!file0.exists()){
            file0.mkdir();
        }
        File writeFile = new File(file0.getAbsolutePath()+File.separator+ xlsFileName);
        try {
            writeFile.createNewFile();
             out = new FileOutputStream(writeFile);
            wb.write(out);
            wb.close();
            out.flush();
            out.close();
        }catch (Exception e){
            log.error("质检报告异常:"+e.getMessage());
        }finally {
            try {
                if(wb!=null){
                    wb.close();
                }
                if (out != null ){
                    out.close();
                }
            }catch (Exception e){
                log.error("关闭异常:"+e.getMessage());
            }
        }
        return writeFile;
    }
}
