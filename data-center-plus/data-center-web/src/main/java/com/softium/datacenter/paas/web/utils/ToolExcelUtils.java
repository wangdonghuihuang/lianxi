package com.softium.datacenter.paas.web.utils;

import com.softium.datacenter.paas.api.utils.ListMapCommonUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * excel-xls-xlsx解析工具类
 */
public class ToolExcelUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ToolExcelUtils.class);
    /**
     * 默认从第一个sheet开始读取（索引值为0）
     */
    private static final int READ_START_SHEET = 0;
    private static final int BUFFER_SIZE = 4096;
    /**
     * 自动根据文件扩展名，调用对应读取方法
     *
     * @param parseStream 文件输入流
     * @param fileType    文件类型
     * @param mapping     关系映射集合
     * @param isall       在xls文件内容中，有某列不能匹配映射关系，是否舍弃整个文件还是跳过这列继续下行,true 舍弃，fals 不舍弃
     */
//TODO  如果增加布尔参数控制是否全要，需要queryAccordMap方法中
    public static List<Map<String, Object>> readExcel(InputStream parseStream, String fileType, Map<String, Object> mapping, boolean isall) throws IOException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<Map<String, Object>> alllist = new ArrayList<>();
        try {
            InputStream inputStream=null;
            ByteArrayOutputStream arrayOutputStream=cloneInputStream(parseStream);
            if(arrayOutputStream.size()>0){
                inputStream=new ByteArrayInputStream(arrayOutputStream.toByteArray());
            }else{
                //小于等于0表示流内的文件为空，直接返回空集合
                return alllist;
            }

            if ("xls".equals(fileType)) { // 使用xls方式读取
                mapList = readExcelXls(inputStream, mapping, isall);
            } else if ("xlsx".equals(fileType)) { // 使用xlsx方式读取
                mapList = readExcelXlsx(inputStream, mapping, isall);
            } else if ("csv".equals(fileType)) {
                List<String> lista = CSVFileUtil.getLines(inputStream);
                List<Map<String, Object>> csvlist = CSVFileUtil.parseList(lista);
                mapList = parseCsvCommon(mapping, csvlist);
            }
        } catch (IOException e) {
            LOG.info("读取excel文件失败:" + e.getMessage());
        }
        //最后判断mapList如果不为空，再次做集合筛选分析，把key值为Null的，代表没有匹配到映射关系，移除不要
        //System.out.println("最后不处理空结果:"+mapList.toString());
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> stringObjectMap = mapList.get(i);
            com.softium.datacenter.paas.api.utils.ListMapCommonUtils.removeNullKey(stringObjectMap);
            Map<String, Object> calMap=stringObjectMap;
           int couNum= ListMapCommonUtils.removeNullValue(calMap);
            if(couNum>1){
                alllist.add(stringObjectMap);
            }
        }
        //System.out.println("处理完空key后结果:"+alllist.toString());
        return alllist;
    }

    /**
     * 读取Excel(97-03版，xls格式)
     */
    public static List<Map<String, Object>> readExcelXls(InputStream inputStream, Map<String, Object> mapping, boolean isall) throws IOException {
        HSSFWorkbook wb = null;// 用于Workbook级的操作，创建、删除Excel
        List<Map<String, Object>> listxls = new ArrayList<Map<String, Object>>();
        Map<String, Object> hamap = new HashMap<>();
        try {
            wb = new HSSFWorkbook(inputStream);
            //获取表头值
            Map<String, Object> map = getRowCellKey(wb);
            //调用判断是否存在表头方法
            boolean isaccord = queryAccordMap(mapping, map, isall);
            //调用映射关系匹配方法
            if (isaccord) {//如果符合则进行关系匹配并返回最终表头集合
                hamap = queryHandlerMap(mapping, map);
                listxls = readExcelCommon(wb, hamap);
            }

        } catch (Exception e) {
            LOG.error("解析excel失败:" + e.getMessage());
        } finally {
            if(wb!=null){
                wb.close();
            }
        }
        return listxls;
    }

    /**
     * 读取Excel 2007版，xlsx格式
     */
    public static List<Map<String, Object>> readExcelXlsx(InputStream inputStream, Map<String, Object> mapping, boolean isall) throws IOException {
        List<Map<String, Object>> listxlsx = new ArrayList<Map<String, Object>>();
        XSSFWorkbook wb = null;
        Map<String, Object> xlsxmap = new HashMap<>();
        try {
            wb = new XSSFWorkbook(inputStream);
            //获取表头值
            Map<String, Object> map = getRowCellKey(wb);
            //调用判断是否存在表头方法
            boolean isaccord = queryAccordMap(mapping, map, isall);
            //调用映射关系匹配方法
            if (isaccord) {//如果符合则进行关系匹配并返回最终表头集合
                xlsxmap = queryHandlerMap(mapping, map);
            }
            listxlsx = readExcelCommon(wb, xlsxmap);
        } catch (Exception e) {
            LOG.error("解析excel失败:" + e.getMessage());
        } finally {
            if(wb!=null){
                wb.close();
            }
        }
        return listxlsx;
    }

    /**
     * 读取第一行，作为表头，遍历列，作为表头数组，用做和映射关系匹配
     */
    public static Map<String, Object> getRowCellKey(Workbook work) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }
            //取第一行标题
            row = sheet.getRow(0);
            //遍历所有的列
            for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
                cell = row.getCell(k);
                map.put(String.valueOf(k), getCellValue(cell));
            }
        }
        return map;
    }

    /**
     * 映射关系筛查是否符合方法
     *
     * @param bigmap   映射关系集合
     * @param smallmap 所获取excel文件解析表头集合
     */
    public static boolean queryAccordMap(Map<String, Object> bigmap, Map<String, Object> smallmap, boolean isall) {
        boolean iscontain = false;
        if (isall) {
            //此遍历是判断excel文件中表头需要全部与映射关系匹配
            for (Map.Entry<String, Object> map : smallmap.entrySet()) {
                String value = String.valueOf(map.getValue());
                //判断映射关系中是否包含此value值
                iscontain = bigmap.containsValue(value);
                if (iscontain) {
                    continue;
                } else {
                    break;
                }
            }
        } else {
            //此遍历是只要文件中表头有任何一列符合映射关系，即为true
            for (Map.Entry<String, Object> map : smallmap.entrySet()) {
                String value = String.valueOf(map.getValue());
                iscontain = bigmap.containsValue(value);
                if (iscontain) {
                    break;
                }
            }
        }
        return iscontain;
    }

    /**
     * 分析映射关系，封装最终excel表头方法
     */
    public static Map<String, Object> queryHandlerMap(Map<String, Object> bigmap, Map<String, Object> smallmap) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> amap : smallmap.entrySet()) {
            String avalue = String.valueOf(amap.getValue());
            //遍历映射集合，如果表头符合，则进行匹配赋值
            for (Map.Entry<String, Object> bmap : bigmap.entrySet()) {
                String bvalue = String.valueOf(bmap.getValue());
                if (bvalue.equals(avalue)) {
                    //获取映射关系的key作为新的表头名
                    map.put(String.valueOf(bmap.getValue()), bmap.getKey());
                }
            }
        }
        return map;
    }

    /**
     * 读取excel公共方法
     */
    public static List<Map<String, Object>> readExcelCommon(Workbook work, Map<String, Object> mapping) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        //遍历excel中sheet,一般都是一个excel里面一个工作簿
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }
            //取第一行标题
            row = sheet.getRow(0);
            String title[] = null;//定义数组
            if (row != null) {
                //System.out.println("aa:"+row.getPhysicalNumberOfCells());
                title = new String[row.getPhysicalNumberOfCells()];
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    cell = row.getCell(j);
                    title[j] = String.valueOf(getCellValue(cell));
                }
            } else {
                continue;
            }
            //遍历当前sheet中的所有行
            for (int m = 1; m < sheet.getLastRowNum() + 1; m++) {
                row = sheet.getRow(m);
                if(row!=null&&row.getPhysicalNumberOfCells()!=0){
                Map<String, Object> objectMap = new HashMap<>();
                //遍历所有的列
                    /*todo 注释掉此，原采用excel行获取首列到尾列，问题是只有活动列3列，却读出30多列，
                       可能是由于读取是把非活动但是非空的列也读出来
                       此处直接采用上面title数组，里面存的就是所有列生成的数组*/
                   /* System.out.println("bb:"+row.getFirstCellNum());
                    System.out.println("cc:"+row.getPhysicalNumberOfCells());*/
                for (int y = row.getFirstCellNum(); y < title.length; y++) {
                    //for (int y = 0; y < title.length; y++) {
                    cell = row.getCell(y);
                    String key = title[y];
                    objectMap.put(String.valueOf(mapping.get(key)), getCellValue(cell));
                }
                objectMap.put("rowNum",m+1);
                mapList.add(objectMap);
                }
            }
        }
        return mapList;
    }

    /**
     * @Title: getCellValue
     */
    public static String getCellValue(Cell cell) {
        Object result = "";
        if (cell != null) {
            CellType type=cell.getCellTypeEnum();
            if(type==CellType.STRING){
                result = cell.getStringCellValue();
            }else if(type==CellType.NUMERIC){
                short format = cell.getCellStyle().getDataFormat();
                double value = cell.getNumericCellValue();
                SimpleDateFormat sdf = null;
                //时间格式
                if (format == 14 || format == 31 || format == 57 || format == 58 || (176 <= format && format <= 178)
                        || (182 <= format && format <= 196) || (210 <= format && format <= 213) || (208 == format)) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = DateUtil.getJavaDate(value);
                    if (null != date) {
                        result = sdf.format(date);
                    }
                } else if (format == 20 || format == 32 || format == 183 || (200 <= format && format <= 209)) {
                    sdf = new SimpleDateFormat("HH:mm");
                    Date date = DateUtil.getJavaDate(value);
                    if (null != date) {
                        result = sdf.format(date);
                    }
                } else {//非时间格式
                    int newValue = (int) value;
                    if (newValue == value) {
                        result = newValue;
                    } else {
                        result = value;
                    }
                }
            }else if(type==CellType.BOOLEAN){
                result = cell.getBooleanCellValue();
            }else if(type==CellType.FORMULA){
                result = cell.getCellFormula();
            }else if(type==CellType.ERROR){
                result = cell.getErrorCellValue();
            }
           /* switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    result = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    short format = cell.getCellStyle().getDataFormat();
                    double value = cell.getNumericCellValue();
                    SimpleDateFormat sdf = null;
                    //时间格式
                    if (format == 14 || format == 31 || format == 57 || format == 58 || (176 <= format && format <= 178)
                            || (182 <= format && format <= 196) || (210 <= format && format <= 213) || (208 == format)) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = DateUtil.getJavaDate(value);
                        if (null != date) {
                            result = sdf.format(date);
                        }
                    } else if (format == 20 || format == 32 || format == 183 || (200 <= format && format <= 209)) {
                        sdf = new SimpleDateFormat("HH:mm");
                        Date date = DateUtil.getJavaDate(value);
                        if (null != date) {
                            result = sdf.format(date);
                        }
                    } else {//非时间格式
                        int newValue = (int) value;
                        if (newValue == value) {
                            result = newValue;
                        } else {
                            result = value;
                        }
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    result = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    result = cell.getCellFormula();
                    break;
                case Cell.CELL_TYPE_ERROR:
                    result = cell.getErrorCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    break;
                default:
                    break;
            }*/
        }
        return result.toString();
    }

    /**
     * 筛洗csv文件数据，匹配映射关系统一处理方法
     */
    public static List<Map<String, Object>> parseCsvCommon(Map<String, Object> mapping, List<Map<String, Object>> mapList) {
        List<Map<String, Object>> arraylist = new ArrayList<>();
        Map<String, Object> biaotoumap = new HashMap<>();
        Map<String, Object> hamap = new HashMap<>();

        //遍历list<map>,获取map的key值作为新map集合的value值
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> mapa = mapList.get(i);
            for (Map.Entry<String, Object> mm : mapa.entrySet()) {
                String key = mm.getKey();
                biaotoumap.put(key, key);
            }
            break;
        }
        boolean zhi = queryAccordMap(mapping, biaotoumap, false);
        /*if (zhi) {
            //进行映射关系匹配，返回最终表头集合
            hamap = queryHandlerMap(mapping, biaotoumap);
            for (int k = 0; k < mapList.size(); k++) {
                Map<String, Object> kmap = mapList.get(k);
                for (Map.Entry<String, Object> fmmap : kmap.entrySet()) {
                    String keyStr = fmmap.getKey();
                    String valueStr = String.valueOf(fmmap.getValue());
                    for (Map.Entry<String, Object> hmap : hamap.entrySet()) {
                        String hkey = hmap.getKey();
                        String hvalue = String.valueOf(hmap.getValue());
                        if (keyStr.equals(hkey)) {
                            allmap.put(hvalue, valueStr);
                        } else {
                            //将null作为key值，最后可以在返回方法中，进行key为null的移除，返回最终要的列数据
                            allmap.put(null, valueStr);
                        }
                        arraylist.add(allmap);
                    }
                }
            }
        }*/
        if (zhi) {
            //进行映射关系匹配，返回最终表头集合
            hamap = queryHandlerMap(mapping, biaotoumap);
            for (int k = 0; k < mapList.size(); k++) {
                Map<String, Object> kmap = mapList.get(k);
                //作为最终新的集合
                Map<String, Object> allmap = new HashMap<>();
                for (Map.Entry<String, Object> fmmap : kmap.entrySet()){
                    //获取读取到文件中的集合map
                    String keyStr = fmmap.getKey();
                    String valueStr = String.valueOf(fmmap.getValue());
                    //根据key值从表头集合map获取value，替换
                    allmap.put(String.valueOf(hamap.get(keyStr)),valueStr);
                }
                allmap.put("rowNum",k+2);
                arraylist.add(allmap);
            }

        }
        return arraylist;
    }
    /**
     * inputstream流拷贝，缓存流
     * */
    public static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
