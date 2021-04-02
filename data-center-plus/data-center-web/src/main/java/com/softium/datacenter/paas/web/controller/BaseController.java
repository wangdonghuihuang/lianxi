package com.softium.datacenter.paas.web.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.softium.framework.common.dto.ActionResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * @author sjb02
 */
@RestController
public class BaseController {

    /**
     * @param response
     */
    public static void setExcelContentType(HttpServletResponse response, String fileName) {
        fileName = LocalDate.now().toString() + "-" + fileName + ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("filename", fileName);
        response.setHeader("Content-disposition", "attachment;filename=\"" + fileName + "\"");
    }


    public ActionResult success(String message) {
        return new ActionResult(message);
    }

    public ActionResult error(String message) {
        return new ActionResult(false,message);
    }

    /**
     * 下载文件
     *
     * @param date
     * @param temple
     * @param fileName
     * @return
     */
    protected ResponseEntity<byte[]> downloadExcel(List date, Class temple, String fileName) {
        return responseEntity(body(date, temple, null), httpHeaders(fileName), HttpStatus.OK);
    }


    protected ResponseEntity<byte[]> downloadExcel(byte[] bytes, String fileName) {
        return responseEntity(bytes, httpHeaders(fileName), HttpStatus.OK);
    }

    protected ResponseEntity<byte[]> downloadExcel(List date, Class temple, String fileName, String sheetName) {
        return responseEntity(body(date, temple, sheetName), httpHeaders(fileName), HttpStatus.OK);
    }

    /**
     * 生成下载
     *
     * @param body
     * @param httpHeaders
     * @param statusCode
     * @return
     */
    protected ResponseEntity<byte[]> responseEntity(byte[] body, HttpHeaders httpHeaders, HttpStatus statusCode) {
        return new ResponseEntity<>(body, httpHeaders, statusCode);
    }

    /**
     * 配置文件名称
     *
     * @param fileName
     * @return
     */
    protected HttpHeaders httpHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set("filename", fileName);
        // 设置文件名
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return headers;

    }

    /**
     * 生成文件
     *
     * @param list
     * @param clazz
     * @return
     */
    protected byte[] body(List list, Class clazz, String sheetName) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(outputStream, clazz).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
        if (sheetName != null) {
            excelWriterBuilder.sheet(sheetName).doWrite(list);
        } else {
            excelWriterBuilder.sheet().doWrite(list);
        }

        return outputStream.toByteArray();
    }
}
