package com.fuq.demo.processor.receive;

import com.fuq.demo.tool.bean.MessageBean;

@FunctionalInterface
public interface PostReceiveProcessor {

    void onMessage(MessageBean message);

}
