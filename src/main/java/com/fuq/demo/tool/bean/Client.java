package com.fuq.demo.tool.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @ClassName Client
 * @Description
 * @Author Wangjunkai
 * @Date 2019/8/27 15:18
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String ip;

    private String hostName;

    private Date loginTime;

    private String username;

    private String truename;

/*    public static void main(String[] args) throws JsonProcessingException {
        Client client = new Client();
        client.setToken("123");
        client.setUsername("wjk");
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(client);
        System.out.println(s);
    }*/

}