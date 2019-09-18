package com.fuq.demo.rest;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fuq.demo.config.NettyIOConfig;
import com.fuq.demo.service.system.UserInfoService;
import com.fuq.demo.tool.PasswordUtil;
import com.fuq.demo.tool.bean.Client;
import com.fuq.demo.tool.dto.UserInfo;
import com.fuq.demo.tool.redis.CacheManager;
import com.fuq.demo.tool.result.Result;

//@Controller
@RestController
public class HomeController {
	
	@Resource
    private UserInfoService userInfoService;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private NettyIOConfig nettyIOConfig;
	
    @RequestMapping(value = {"/","/index"}, method = RequestMethod.GET)
    public String index(){
        return"/index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(HttpServletRequest request, Map<String, Object> map) throws Exception{
        System.out.println("HomeController.login()");
        // 登录失败从request中获取shiro处理的异常信息。
        // shiroLoginFailure:就是shiro异常类的全类名.
        String exception = (String) request.getAttribute("shiroLoginFailure");
        System.out.println("exception=" + exception);
        String msg = "";
        if (exception != null) {
            if (UnknownAccountException.class.getName().equals(exception)) {
                System.out.println("UnknownAccountException -- > 账号不存在：");
                msg = "UnknownAccountException -- > 账号不存在：";
            } else if (IncorrectCredentialsException.class.getName().equals(exception)) {
                System.out.println("IncorrectCredentialsException -- > 密码不正确：");
                msg = "IncorrectCredentialsException -- > 密码不正确：";
            } else if ("kaptchaValidateFailed".equals(exception)) {
                System.out.println("kaptchaValidateFailed -- > 验证码错误");
                msg = "kaptchaValidateFailed -- > 验证码错误";
            } else {
                msg = "else >> "+exception;
                System.out.println("else -- >" + exception);
            }
        }
        map.put("msg", msg);
        if (msg.equals("")) {
        	String username = (String) request.getParameter("username");
        	UserInfo userInfo = userInfoService.findByUsername(username);
        	//存入redis缓存中
//            RedisUtil.hset(RedisUtil.AUTH, username, userInfo);
        	String token = PasswordUtil.getEncodeAuth(username, username, userInfo.getPassword());
        	String ip = InetAddress.getLocalHost().getAddress().toString();
        	String hostName = InetAddress.getLocalHost().getHostName();
        	Date loginTime = Calendar.getInstance().getTime();
        	Client userClient = Client.builder().token(token).ip(ip).hostName(hostName)
        	.loginTime(loginTime).username(username).truename(userInfo.getName()).build();
        	cacheManager.hSet(CacheManager.AUTH_PREFIX, token, userClient);
        	String socketIOUrl = "http://" + nettyIOConfig.hostname + ":" + nettyIOConfig.port 
        			+ "?username=" + username + "&token=" + token;
        	userInfo.setToken(token);
        	userInfo.setSocketIOUrl(socketIOUrl);
        	return Result.success(userInfo);
        }
        // 此方法不处理登录成功,由shiro进行处理
        return Result.fail("登录失败！");
//        return "/login";
    }

    @RequestMapping("/403")
    public String unauthorizedRole(){
        System.out.println("------没有权限-------");
        return "403";
    }

}