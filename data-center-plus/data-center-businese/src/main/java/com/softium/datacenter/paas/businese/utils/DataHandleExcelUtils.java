package com.softium.datacenter.paas.web.utils;

import com.softium.datacenter.paas.api.dto.excel.ExcelJobDTO;
import com.softium.datacenter.paas.api.entity.ExcelTemplate;
import com.softium.datacenter.paas.api.entity.FieldMapping;
import com.softium.datacenter.paas.api.enums.TemplateExcelCode;
import com.softium.datacenter.paas.api.utils.ListMapCommonUtils;
import com.softium.datacenter.paas.web.common.Constats;
import com.softium.datacenter.paas.web.utils.fileCommon.MyStringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataHandleExcelUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ToolExcelUtils.class);
    /**
     * 默认从第一个sheet开始读取（索引值为0）
     */
    private static final int READ_START_SHEET = 0;
    private static final int BUFFER_SIZE = 4096;
    /**
     * 自动根据文件扩展名，调用对应读取方法
     *
     * @param jobDTO 文件信息封装实体类
     * @param mapping     关系映射集合
     * @param isall       在xls文件内容中，有某列不能匹配映射关系，是否舍弃整个文件还是跳过这列继续下行,true 舍弃，fals 不舍弃
     */
//TODO  如果增加布尔参数控制是否全要，需要queryAccordMap方法中
    public static Map<String,List<Map<String, Object>>> readExcel(ExcelJobDTO jobDTO, Map<String, List<ExcelTemplate>> mapping, boolean isall, Map<String,String> dataTypeMap) throws IOException {
        Map<String,List<Map<String, Object>>> mapList = new HashMap<>();
        Map<String,List<Map<String, Object>>> alllist = new HashMap<>();
        try {
            File file=new File(jobDTO.getLocalFileFull());
            InputStream parseStream=new FileInputStream(file);
            InputStream inputStream=null;
            String fileType=jobDTO.getFileType();
            ByteArrayOutputStream arrayOutputStream=cloneInputStream(parseStream);
            if(arrayOutputStream.size()>0){
                inputStream=new ByteArrayInputStream(arrayOutputStream.toByteArray());
            }else{
                //小于等于0表示流内的文件为空，直接返回空集合
                return alllist;
            }

            if (MyStringUtil.strtoLowerCase(fileType).equals(Constats.FILE_TYPE_XLS)) { // 使用xls方式读取
                mapList = readExcelXls(inputStream, mapping, isall,dataTypeMap,jobDTO);
            } else if (MyStringUtil.strtoLowerCase(fileType).equals(Constats.FILE_TYPE_XLSX)) { // 使用xlsx方式读取
                mapList = readExcelXlsx(inputStream, mapping, isall,dataTypeMap,jobDTO);
            } /*else if ("csv".equals(fileType)) {
                List<String> lista = CSVFileUtil.getLines(inputStream);
                List<Map<String, Object>> csvlist = CSVFileUtil.parseList(lista);
                mapList = parseCsvCommon(mapping, csvlist);
            }*/
        } catch (IOException e) {
            LOG.info("读取excel文件失败:" + e.getMessage());
        }
        //最后判断mapList如果不为空，再次做集合筛选分析，把key值为Null的，代表没有匹配到映射关系，移除不要
        //System.out.println("最后不处理空结果:"+mapList.toString());
        /*for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> stringObjectMap = mapList.get(i);
            ListMapCommonUtils.removeNullKey(stringObjectMap);
            Map<String, Object> calMap=stringObjectMap;
            int couNum= ListMapCommonUtils.removeNullValue(calMap);
            if(couNum>1){
                alllist.add(stringObjectMap);
            }
        }*/
        /*for(){

        }*/
        Set<Map.Entry<String, List<Map<String, Object>>>> entrySet = mapList.entrySet();
        Iterator<Map.Entry<String, List<Map<String, Object>>>> iter = entrySet.iterator();
        while (iter.hasNext())
        {
            List<Map<String, Object>> excelParseList = new ArrayList<>();
            Map.Entry<String, List<Map<String, Object>>> entry = iter.next();
            String key=entry.getKey();
            List<Map<String, Object>> value=entry.getValue();
            for(int i=0;i<value.size();i++){
                Map<String, Object> stringObjectMap = value.get(i);
                ListMapCommonUtils.removeNullKey(stringObjectMap);
                Map<String, Object> calMap=stringObjectMap;
                int couNum= ListMapCommonUtils.removeNullValue(calMap);
                if(couNum>1){
                    //此处专门做个标记，会造成数据累加，是因为不断覆盖累加
                    excelParseList.add(stringObjectMap);

                }
            }
            alllist.put(key,excelParseList);
        }
        /*Iterator<Map.Entry<String, List<Map<String, Object>>>> iterator = mapList.entrySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next().getKey();
            List<Map<String, Object>> value = iterator.next().getValue();
            for (int i = 0; i < value.size(); i++) {
                Map<String, Object> stringObjectMap = value.get(i);
                ListMapCommonUtils.removeNullKey(stringObjectMap);
                Map<String, Object> calMap=stringObjectMap;
                int couNum= ListMapCommonUtils.removeNullValue(calMap);
                if(couNum>1){
                    excelParseList.add(stringObjectMap);
                }
            }
            alllist.put(key,excelParseList);
        }*/
        //System.out.println("处理完空key后结果:"+alllist.toString());
        return alllist;
    }

    /**
     * 读取Excel(97-03版，xls格式)
     */
    public static Map<String,List<Map<String, Object>>> readExcelXls(InputStream inputStream, Map<String, List<ExcelTemplate>> mapping, boolean isall,Map<String,String> dataTypeMap,ExcelJobDTO jobDTO) throws IOException {
        HSSFWorkbook wb = null;// 用于Workbook级的操作，创建、删除Excel
        Map<String,List<Map<String, Object>>> listxls = new HashMap<>();
        Map<String, Object> hamap = new HashMap<>();
        try {
            wb = new HSSFWorkbook(inputStream);
            listxls = readExcelCommon(wb,mapping,isall,dataTypeMap,jobDTO);

        } /*catch (Exception e) {
            LOG.error("解析excel失败:" + e.getMessage());
        }*/ finally {
            if(wb!=null){
                wb.close();
            }
        }
        return listxls;
    }

    /**
     * 读取Excel 2007版，xlsx格式
     */
    public static Map<String,List<Map<String, Object>>> readExcelXlsx(InputStream inputStream, Map<String, List<ExcelTemplate>> mapping, boolean isall,Map<String,String> dataTypeMap,ExcelJobDTO jobDTO) throws IOException {
        Map<String,List<Map<String, Object>>> listxlsx = new HashMap<>();
        XSSFWorkbook wb = null;
        Map<String, Object> xlsxmap = new HashMap<>();
        try {
            wb = new XSSFWorkbook(inputStream);
           /* //调用判断是否存在表头方法
            boolean isaccord = queryAccordMap(mapping, map, isall);
            //调用映射关系匹配方法
            if (isaccord) {//如果符合则进行关系匹配并返回最终表头集合
                xlsxmap = queryHandlerMap(mapping, map);
            }*/
            listxlsx = readExcelCommon(wb,mapping, isall,dataTypeMap,jobDTO);
        } /*catch (Exception e) {
            LOG.error("解析excel失败:" + e.getMessage());
        }*/ finally {
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
    public static boolean queryAccordMap(Map<String, Object>  bigmap, Map<String, Object> smallmap, boolean isall,String type) {
        boolean iscontain = false;
        if (isall) {
            //此遍历是判断excel文件中表头需要全部与映射关系匹配
            for (Map.Entry<String, Object> map : smallmap.entrySet()) {
                String value = String.valueOf(map.getValue());
                value=value.replaceAll("[*]","");
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
                value=value.replaceAll("[*]","");
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
            avalue=avalue.replaceAll("[*]","");
            //遍历映射集合，如果表头符合，则进行匹配赋值
            for (Map.Entry<String, Object> bmap : bigmap.entrySet()) {
                String bvalue = String.valueOf(bmap.getValue());
                //这里判断是否包含，就需要判断string串中是否包含某个string字符串
                if (bvalue.equals(avalue)) {
                    //如果包含，也不再是用bmap的value作为key,因为bmap的value是多个，
                    // 数据中心字段一个对应文件的多个，所以如果包含，则还用上传的avalue作为map的key值
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
    public static Map<String,List<Map<String, Object>>> readExcelCommon(Workbook work,Map<String, List<ExcelTemplate>> fieldMapping,boolean isall,Map<String,String> dataTypeMap,ExcelJobDTO jobDTO) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        Map<String,List<Map<String, Object>>> allDataMapList=new HashMap<>();
        Map<String,String> sheetMap=new ConcurrentHashMap<>();
        //遍历excel中sheet,一般都是一个excel里面一个工作簿
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
            sheet = work.getSheetAt(i);
            if (sheet == null) {
                continue;
            }
            Map<String, Object> objectMap = new HashMap<>();
            //取第一行标题
            row = sheet.getRow(0);
            for(int k=row.getFirstCellNum();k<row.getLastCellNum();k++){
                cell=row.getCell(k);
                objectMap.put(String.valueOf(k),getCellValue(cell));
            }
            String lastSheetName=sheet.getSheetName();
            //如果dataType中不存在sheet名，表示此文件不符合格式，非系统下载模板所配置
            //if(dataTypeMap.containsKey(lastSheetName)){
            //通过sheet名从map中获取对应业务类型
            String sheetName= TemplateExcelCode.getValue(dataTypeMap.get(lastSheetName).toString());
            sheetMap.put(sheetName,lastSheetName);
            //todo  此处map缓存会引起原值覆盖，改为key为文件表id,value为 pd,sheet名
            //MapCache.set(sheetName,lastSheetName);//key为PD,ID业务类型，value为excel中sheet名
            //根据类型获取映射map
            Map<String,Object> objectMap1=getTitleName(fieldMapping,sheetName);
            //调用判断是否存在表头方法
            boolean isaccord = queryAccordMap(objectMap1, objectMap, isall,sheetName);
            Map<String, Object> hmap=new HashMap<>();
            //调用映射关系匹配方法
            if (isaccord) {//如果符合则进行关系匹配并返回最终表头集合
                hmap = queryHandlerMap(objectMap1, objectMap);
                String title[] = null;//定义数组
                if (row != null) {
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
                        Map<String, Object> objectMap2 = new HashMap<>();
                        //遍历所有的列
                    /*todo 注释掉此，原采用excel行获取首列到尾列，问题是只有活动列3列，却读出30多列，
                       可能是由于读取是把非活动但是非空的列也读出来
                       此处直接采用上面title数组，里面存的就是所有列生成的数组*/
                        for (int y = row.getFirstCellNum(); y < title.length; y++) {
                            cell = row.getCell(y);
                            String key = title[y];
                            key=key.replaceAll("[*]","");
                            //todo  此处标记下，excel文件中会有比如经销商名填在了单价下面，经销商名没有填，此处会造成excel文件任务解析map分组报错
                            objectMap2.put(String.valueOf(hmap.get(key)), getCellValue(cell));
                        }
                        objectMap2.put("rowNum",m+1);
                        mapList.add(objectMap2);
                    }
                }
                allDataMapList.put(sheetName,mapList);
            }
            // }

        }
        MapCache.set(jobDTO.getParseLogId(),sheetMap);
        return allDataMapList;
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
        }
        return result.toString();
    }

    /**
     * 筛洗csv文件数据，匹配映射关系统一处理方法
     */
   /* public static Map<String,List<Map<String, Object>>> parseCsvCommon(Map<String, List<FieldMapping>> mapping, List<Map<String, Object>> mapList) {
        Map<String,List<Map<String, Object>>> hashmapList=new HashMap<>();
        List<Map<String, Object>> arraylist = new ArrayList<>();
        Map<String, Object> biaotoumap = new HashMap<>();
        Map<String, Object> hamap = new HashMap<>();
        Map<String,Object> titleMap=getTitleName(mapping,"PD");
        //遍历list<map>,获取map的key值作为新map集合的value值
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> mapa = mapList.get(i);
            for (Map.Entry<String, Object> mm : mapa.entrySet()) {
                String key = mm.getKey();
                biaotoumap.put(key, key);
            }
            break;
        }
        boolean zhi = queryAccordMap(titleMap, biaotoumap, false);
        if (zhi) {
            //进行映射关系匹配，返回最终表头集合
            hamap = queryHandlerMap(titleMap, biaotoumap);
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
        hashmapList.put("csv",arraylist);
        return hashmapList;
    }*/
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
    /**根据业务类型获取配置映射字段*/
    public static Map<String,Object> getTitleName(Map<String, List<ExcelTemplate>> map,String type){
        Map<String,Object> objectMap=new HashMap<>();
        for (Map.Entry<String, List<ExcelTemplate>> entry : map.entrySet()) {
            String key=entry.getKey();
            List<ExcelTemplate> mappings=entry.getValue();
            if(key.equals(type)){
                objectMap=mappings.stream().collect(Collectors.toMap(ExcelTemplate::getColumnTitleName,ExcelTemplate::getColumnExcelName));
            }
        }
        return objectMap;
    }
}
