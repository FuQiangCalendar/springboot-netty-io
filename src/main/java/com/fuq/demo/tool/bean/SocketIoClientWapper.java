package com.fuq.demo.tool.bean;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SocketIoClientWapper
 * @Description
 * @Author Wangjunkai
 * @Date 2019/9/3 8:46
 **/
@AllArgsConstructor
@Setter
@Getter
public class SocketIoClientWapper {
    private SocketIOClient socketIOClient;

    private String username;

    private String token;
}