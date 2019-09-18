package com.fuq.demo.processor.send;

import com.fuq.demo.tool.bean.MessageBean;

@FunctionalInterface
public interface PreSendProcessor {

    void preSend(MessageBean message);

}
