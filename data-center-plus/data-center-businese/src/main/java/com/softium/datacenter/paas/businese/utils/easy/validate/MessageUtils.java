package com.softium.datacenter.paas.web.utils.easy.validate;


import com.softium.datacenter.paas.web.utils.easy.input.Message;

import java.util.List;

/**
 * 2019/11/14
 *
 * @author paul
 */
public class MessageUtils {

    public static void addErrorMessage(List list, Message message) {
        if (list != null && message != null) {
            list.add(message);
        }
    }

    public static void addWarnMessage(List list, Message message) {
        if (list != null && message != null) {
            list.add(message);
        }
    }


}
