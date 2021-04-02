package com.softium.datacenter.paas.web.utils.easy.input;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 2019/11/11
 *
 * @author paul
 */
@Data
public class Message implements Comparable<Message> {


    private String type;
    private String typeMessage;
    private Integer rowIndex;
    private Integer columnIndex;
    private String message;

    public Message() {
    }

    public Message(String type, String typeMessage, Integer rowIndex, Integer columnIndex, String message) {
        this.type = type;
        this.typeMessage = typeMessage;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public Message(String typeMessage, Integer rowIndex, Integer columnIndex, String message) {
        this.typeMessage = typeMessage;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public Message(Integer rowIndex, Integer columnIndex, String message) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public Message(Integer rowIndex, String message) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public Message(String message) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.message = message;
    }

    public void appendMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            this.message = this.message + message;
        }
    }

    @Override
    public int compareTo(Message o) {

        if (o == null || o.getRowIndex() == null) {
            return -1;
        } else if (this.rowIndex == null) {
            return 1;
        }

        return this.rowIndex.compareTo(o.rowIndex);
    }
}
