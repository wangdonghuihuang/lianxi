package com.softium.datacenter.paas.web.utils.easy.validate;

/**
 * 2019/11/14
 *
 * @author paul
 */
public enum ValidateTypeEnum {

    FORMAT("format", "格式校验"),
    REQUIRED("Required", "必填校验"),
    BUSINESS("BUSINESS", "业务校验"),
    DUPLICATED("DUPLICATED", "重复校验");


    private String type;
    private String message;

    /**
     * @param type    类型
     * @param message 描述
     */
    ValidateTypeEnum(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
