package com.softium.datacenter.paas.web.utils.easy.input;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageModel {

    /**
     * 错误消息
     */
    public List<Message> error = new ArrayList<>(1);

    /**
     * 警告消息
     */
    public List<Message> warn = new ArrayList<>(1);


    public void copyModel(MessageModel resource) {
        if (resource != null) {

            //
            if (resource.error != null && !resource.error.isEmpty()) {
                error.addAll(resource.error);
            }

            //
            if (resource.warn != null && !resource.warn.isEmpty()) {
                warn.addAll(resource.warn);
            }

        }


    }
}
