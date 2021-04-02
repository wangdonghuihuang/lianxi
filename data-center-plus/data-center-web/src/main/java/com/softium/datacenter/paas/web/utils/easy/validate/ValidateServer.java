package com.softium.datacenter.paas.web.utils.easy.validate;

import com.softium.datacenter.paas.web.utils.easy.input.BaseExcelReadModel;
import com.softium.datacenter.paas.web.utils.easy.input.Message;
import com.softium.datacenter.paas.web.utils.easy.input.MessageModel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 验证 暂不支持 自定义注解
 *
 * @param <T>
 */
@Data
public class ValidateServer<T extends BaseExcelReadModel> {
    Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ERROR_KEY = "error.";
    public static final String WARN_KEY = "warn.";
    public static final String VALIDATE_TYPE_KEY = "validateType";
    public static final Class EXCEL_GROUP = ValidateFilterGroup.class;
    public Map<String, Integer> fileNameIndexMap;
    public Validator validator;
    public MessageModel messageModel;

    public static final Map<Class<?>, ValidateTypeEnum> validateTypeEnumHashMap = new HashMap<>(13);

    static {
        ValidateTypeEnum REQUIRED = ValidateTypeEnum.REQUIRED;
        // 必填校验
        validateTypeEnumHashMap.put(Null.class, REQUIRED);
        validateTypeEnumHashMap.put(NotNull.class, REQUIRED);
        validateTypeEnumHashMap.put(NotNull.class, REQUIRED);
        validateTypeEnumHashMap.put(AssertTrue.class, REQUIRED);
        validateTypeEnumHashMap.put(AssertFalse.class, REQUIRED);

        // 格式校验
        ValidateTypeEnum FORMAT = ValidateTypeEnum.FORMAT;
        validateTypeEnumHashMap.put(Min.class, FORMAT);
        validateTypeEnumHashMap.put(Max.class, FORMAT);
        validateTypeEnumHashMap.put(DecimalMin.class, FORMAT);
        validateTypeEnumHashMap.put(DecimalMax.class, FORMAT);
        validateTypeEnumHashMap.put(Size.class, FORMAT);
        validateTypeEnumHashMap.put(Digits.class, FORMAT);
        validateTypeEnumHashMap.put(Past.class, FORMAT);
        validateTypeEnumHashMap.put(Future.class, FORMAT);
        validateTypeEnumHashMap.put(Pattern.class, FORMAT);


    }


    public ValidateServer(Class<T> clazz) {
        this.validator = ValidateUtil.validation();
        this.messageModel = new MessageModel();
        this.fileNameIndexMap = ValidateUtil.fileNameMap(clazz);
    }

    /**
     * @param t
     * @param rowIndex
     * @return
     */
    public MessageModel validate(T t, Integer rowIndex) {

        if (logger.isDebugEnabled()) {
            logger.debug(" rowIndex [{}] data [{}] ", rowIndex, t);
        }

        final MessageModel validateMessageModel = new MessageModel();

        Set<ConstraintViolation<T>> violations = validator.validate(t, EXCEL_GROUP);
        StringBuilder stringBuilder = new StringBuilder();
        violations.forEach(a -> {

            Annotation annotation = a.getConstraintDescriptor().getAnnotation();
            ValidateTypeEnum validateTypeEnum = validateTypeEnumHashMap.get(annotation.annotationType());
            if (validateTypeEnum == null) {
                validateTypeEnum = ValidateTypeEnum.REQUIRED;
            }

            String message = a.getMessage();
            if (message.startsWith(ERROR_KEY)) {
                message = message.replaceFirst(ERROR_KEY, "");
                Message message1 = new Message(validateTypeEnum.getType(), validateTypeEnum.getMessage(), rowIndex, fileNameIndexMap.get(getProperName(a)), message);
                messageModel.error.add(message1);
                validateMessageModel.error.add(message1);

            } else {
                //(message.startsWith(WARN_KEY))
                message = message.replaceFirst(WARN_KEY, "");
                Message message1 = new Message(validateTypeEnum.getType(), validateTypeEnum.getMessage(), rowIndex, fileNameIndexMap.get(getProperName(a)), message);
                messageModel.warn.add(message1);
                validateMessageModel.warn.add(message1);
            }
            if (StringUtils.isNotBlank(message)) {
                stringBuilder.append(message);
            }
        });

        String s = stringBuilder.length() == 0 ? null : stringBuilder.toString();
        t.setMessage(s);

        return validateMessageModel;

    }


    public String getProperName(ConstraintViolation<T> constraintViolation) {
        Path propertyPath = constraintViolation.getPropertyPath();
        Iterator<Path.Node> iterator = propertyPath.iterator();
        return iterator.next().getName();
    }

}

