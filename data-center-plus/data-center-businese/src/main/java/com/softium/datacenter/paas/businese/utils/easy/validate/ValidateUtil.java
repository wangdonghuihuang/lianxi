package com.softium.datacenter.paas.web.utils.easy.validate;

import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.read.metadata.property.ExcelReadHeadProperty;
import com.softium.datacenter.paas.web.utils.easy.input.Message;
import org.apache.commons.lang3.StringUtils;

import javax.validation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/11/7
 *
 * @author paul
 */
public class ValidateUtil {

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private Validator validator;

    private ValidateUtil() {
        this.validator = validatorFactory.getValidator();
    }

    /**
     * 获取校验器
     *
     * @return
     */
    public static Validator validation() {
        return new ValidateUtil().validator;
    }

    /**
     * 获取校验信息
     *
     * @param constraintViolations
     * @return
     */
    public static String message(Set<ConstraintViolation<Object>> constraintViolations) {
        if (constraintViolations == null || constraintViolations.isEmpty()) {
            return null;
        }
        return constraintViolations.stream().map(a -> a.getMessage()).collect(Collectors.joining(","));
    }


    public static String getProperName(ConstraintViolation<Object> constraintViolation) {
        Path propertyPath = constraintViolation.getPropertyPath();
        Iterator<Path.Node> iterator = propertyPath.iterator();
        return iterator.next().getName();
    }

    public static String messageList(Set<ConstraintViolation<Object>> violations, int row, Map<String, Integer> indexMap, List<Message> messages) {

        StringBuilder stringBuilder = new StringBuilder();
        violations.forEach(a -> {
            String message = a.getMessage();
            if (StringUtils.isNotBlank(message)) {
                stringBuilder.append(message);
            }
            messages.add(new Message(row, indexMap.get(getProperName(a)), message));
        });

        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }

    public static Map<String, Integer> fileNameMap(Class t) {
        ExcelReadHeadProperty excelReadHeadProperty = new ExcelReadHeadProperty(null, t, null, null);
        Map<String, ExcelContentProperty> fieldNameContentPropertyMap = excelReadHeadProperty.getFieldNameContentPropertyMap();

        Map<String, Integer> stringIntegerHashMap = new HashMap<>();
        fieldNameContentPropertyMap.forEach((a, b) -> {
            stringIntegerHashMap.put(a, b.getHead().getColumnIndex());
        });
        return stringIntegerHashMap;
    }

    public static void main(String[] args) {

/*        Validator validation = validation();
        Set<ConstraintViolation<Object>> validate = validation.validate(new SaleExcelDTO());
        ArrayList<Message> objects = new ArrayList<>();
        String effectiveDate = messageList(validate, 1, Map.of("effectiveDate", 0), objects);
        System.out.println(effectiveDate);
        System.out.println(objects);*/
/*
        ExcelReadHeadProperty excelReadHeadProperty = new ExcelReadHeadProperty(null, SaleExcelDTO.class, null, null);
        Map<String, ExcelContentProperty> fieldNameContentPropertyMap = excelReadHeadProperty.getFieldNameContentPropertyMap();
*/

        /*BeanGenerator beanGenerator = new BeanGenerator();
        beanGenerator.setSuperclass(SqlExportDto.class);
        beanGenerator.addProperty("message", String.class);
*/
    }

}
