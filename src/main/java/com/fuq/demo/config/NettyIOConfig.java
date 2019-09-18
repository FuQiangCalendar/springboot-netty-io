package com.fuq.demo.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.fuq.demo.tool.bean.SocketIoClientWapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName NettyIOConfig
 * @Description
 * @Author Wangjunkai
 * @Date 2019/9/2 10:01
 **/
@Configuration
public class NettyIOConfig implements ApplicationListener<WebServerInitializedEvent> {

    public static String HTTPURL;

    private static final String url = "/http/send_message";

    @Value("${socketio.hostname}")
    public String hostname;

    @Value("${socketio.port}")
    public int port;

    @Bean
    public SocketIOServer socketIOServer(){
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setHostname(hostname);
        configuration.setPort(port);
        return new SocketIOServer(configuration);
    }

    /**
     * @MethodName: springAnnotationScanner
     * @Description: socketIO相关注解扫描
     * @Param:  * @param
     * @Return: com.corundumstudio.socketio.annotation.SpringAnnotationScanner
     * @Author: wangjunkai
     * @Date: 2019/9/2 10:15
    **/
    @Bean
    public SpringAnnotationScanner springAnnotationScanner() {
        return new SpringAnnotationScanner(socketIOServer());
    }

    @Bean
    public ConcurrentMap<String, SocketIoClientWapper> websocketServerMap() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {
        String localHost = null;
        try {
            localHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int port = webServerInitializedEvent.getWebServer().getPort();
        StringBuilder builder = new StringBuilder("http://");
        HTTPURL = builder.append(localHost).append(":").append(port).append(url).toString();
    }
}