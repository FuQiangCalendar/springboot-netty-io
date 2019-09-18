package com.fuq.demo.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuq.demo.config.NettyIOConfig;
import com.fuq.demo.processor.receive.PostReceiveProcessor;
import com.fuq.demo.processor.send.PreSendProcessor;
import com.fuq.demo.tool.bean.Client;
import com.fuq.demo.tool.bean.DelayToken;
import com.fuq.demo.tool.bean.MessageBean;
import com.fuq.demo.tool.bean.SocketIoClientWapper;
import com.fuq.demo.tool.redis.CacheManager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @ClassName MessageProcessor
 * @Description
 * @Author Wangjunkai
 * @Date 2019/9/2 14:57
 **/
@Slf4j
@Component
public class MessageProcessor  {

    @Autowired
    private SocketIOServer socketIoServer;

    private static ApplicationContext applicationContext;

    public static ConcurrentMap<String, SocketIoClientWapper> sessionMap;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final static DelayQueue DELAY_QUEUE = new DelayQueue();

    private final static long delayTime = TimeUnit.SECONDS.toMillis(60);

    private static CacheManager cacheManager;

    static {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> {
            DelayToken token = null;
            try {
                token = (DelayToken) DELAY_QUEUE.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(sessionMap.containsKey(token.getToken())){
                log.info("延迟队列触发，token:{}， 用户名:{}已经重新连接！", token.getToken(), token.getUsername());
            }else {
                cacheManager.hDelete(CacheManager.AUTH_PREFIX, token.getToken());
                log.info("延迟队列触发，删除token:{}， 用户名:{}", token.getToken(), token.getUsername());
            }
        }, 60, TimeUnit.SECONDS);
    }
    /**
     * 	客户端连接的时候触发 请求格式uri?p1=?&p2=?
     *
     * @param client
     */
    @OnConnect
    public void onConnect(SocketIOClient client) throws IOException {
        String username = client.getHandshakeData().getSingleUrlParam("username");
        String token = client.getHandshakeData().getSingleUrlParam("token");
        Client userclient = new Client();
        userclient.setToken("123");
        userclient.setUsername("wjk");
        System.out.println(token);
        cacheManager.hSet(CacheManager.AUTH_PREFIX, token, userclient);
        Client userClient = ((Client) cacheManager.hGet(CacheManager.AUTH_PREFIX, token));
        if(Objects.nonNull(userClient) && Objects.equals(username, userClient.getUsername())){
            String sessionId = client.getSessionId().toString();
            cacheManager.hSet(CacheManager.defaultPrefix, token, NettyIOConfig.HTTPURL + "?sessionId=" + sessionId);
            sessionMap.put(sessionId, new SocketIoClientWapper(client, username, token));
            sendMessage2User(MessageBean.builder().sendType(MessageBean.SendMessageType.LOGIN_SUCCESS).message("认证信息校验成功！").build(), client);
            log.info("用户{}登录成功！token:{}", username, token);
        }else {
            sendMessage2User(MessageBean.builder().sendType(MessageBean.SendMessageType.LOGIN_FAIL).message("认证信息校验未通过！").build(), client);
            log.info("用户{}登录失败！token:{}", username, token);
        }
    }

   /**
    * @MethodName: onDisconnect
    * @Description: 断开连接：1、正常退出，2、关闭浏览器，3、异常断开，2、31分钟内没有重连上就移除用户信息
    * @Param:  * @param client
    * @Return: void
    * @Author: wangjunkai
    * @Date: 2019/9/3 8:49
   **/
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        SocketIoClientWapper swapperClient = sessionMap.remove(client.getSessionId());
        String token = swapperClient.getToken();
        try {
            cacheManager.hDelete(CacheManager.defaultPrefix, token);
            Client userClient = ((Client) cacheManager.hGet(CacheManager.AUTH_PREFIX, token));
            if (Objects.nonNull(userClient)) {
                DELAY_QUEUE.add(new DelayToken(token, swapperClient.getUsername(), System.currentTimeMillis() + delayTime));
            } else {
                cacheManager.hDelete(CacheManager.AUTH_PREFIX, token);
            }
        } catch (Exception e) {
            log.error("移除redis中用户{}信息失败, token:{}！", swapperClient.getUsername(), token, e);
            throw e;
        }
        log.info("用户{}退出成功！", swapperClient.getUsername());
    }

     /**
      * @MethodName: onEvent
      * @Description: 接收消息
      * @Param:  * @param client
      * @param messageBean
      * @Return: void
      * @Author: wangjunkai
      * @Date: 2019/9/3 8:53
     **/
    @OnEvent(value = "messageevent")
    public void onEvent(SocketIOClient client, MessageBean messageBean) throws IOException {
        PostReceiveProcessor postReceiveProcessor = (PostReceiveProcessor) applicationContext.getBean(messageBean.getReceiveProcessor());
        postReceiveProcessor.onMessage(messageBean);
    }



    public static void sendMessage2User(MessageBean messageBean, SocketIOClient client) throws IOException {
        if(Objects.nonNull(messageBean.getSendProcessor())) {
            PreSendProcessor preSendProcessor = (PreSendProcessor) applicationContext.getBean(messageBean.getSendProcessor());
            preSendProcessor.preSend(messageBean);
        }
        String message = objectMapper.writeValueAsString(messageBean);
        client.sendEvent(messageBean.getSendType(), message);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        MessageProcessor.applicationContext = applicationContext;
    }

    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        MessageProcessor.cacheManager = cacheManager;
    }

    @Autowired
    public void setSessionMap(ConcurrentMap<String, SocketIoClientWapper> sessionMap) {
        MessageProcessor.sessionMap = sessionMap;
    }
}