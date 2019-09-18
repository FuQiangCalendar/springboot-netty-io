package com.fuq.demo.http;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fuq.demo.server.MessageProcessor;
import com.fuq.demo.tool.bean.MessageBean;
import com.fuq.demo.tool.bean.SocketIoClientWapper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName HttpsSendMessage
 * @Description 业务系统发送消息调用接口
 * @Author Wangjunkai
 * @Date 2019/8/27 15:39
 **/
@Controller
@Slf4j
@RequestMapping("/http/")
public class HttpsSendMessage {

    private ConcurrentHashMap<String, SocketIoClientWapper> sessionMap;

    @RequestMapping("/send_message")
    public void sendMessage(@RequestBody MessageBean messageBean){
        Map<String, String> receiveUserAndSessionId = messageBean.getReceiveUserAndSessionId();
        String message = messageBean.getMessage();
        if(Objects.nonNull(receiveUserAndSessionId) && Objects.nonNull(message)){
            receiveUserAndSessionId.entrySet().parallelStream().forEach(userAndId -> {
                try {
                    SocketIoClientWapper socketIOClient = sessionMap.get(userAndId.getValue());
                    if(Objects.equals(socketIOClient.getUsername(), userAndId.getKey())) {
                        MessageProcessor.sendMessage2User(messageBean, socketIOClient.getSocketIOClient());
                        log.info("用户{}向用户{}发送：{}",  messageBean.getSendUser(), userAndId.getKey(), message);
                    }else{
                        log.error("接收用户sessionId：{}与用户名：{}不匹配！", userAndId.getValue(), userAndId.getKey());
                    }
                } catch (IOException e) {
                    log.error("用户{}向用户{}发送：{} 失败！原因：{}", messageBean.getSendUser(), userAndId.getKey(), message, e.getMessage());
                }
            });
        }else{
            log.error("用户{}向用户{}发送：{} 失败！", messageBean.getSendUser(), receiveUserAndSessionId, message);
        }
    }

}