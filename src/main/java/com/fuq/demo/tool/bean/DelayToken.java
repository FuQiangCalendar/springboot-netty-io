package com.fuq.demo.tool.bean;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DelayToken
 * @Description
 * @Author Wangjunkai
 * @Date 2019/9/3 9:05
 **/

public class DelayToken implements Delayed {

    private final String token;

    private final String username;

    private final long endTime;

    public DelayToken(String token, String username, long endTime) {
        this.token = token;
        this.username = username;
        this.endTime = endTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(endTime,TimeUnit.SECONDS) - unit.convert(System.currentTimeMillis(),TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this == o)
            return 1;
        if (o==null)
            return -1;
        long diff = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return diff<0?-1:(diff==0?0:1);
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}