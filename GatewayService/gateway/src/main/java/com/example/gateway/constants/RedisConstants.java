package com.example.gateway.constants;


public final class RedisConstants {

    private RedisConstants() {
    }

    public static final String RATE_LOGIN_IP_PREFIX = "rate:login:ip:";
    public static final String RATE_REGISTER_IP_PREFIX = "rate:register:ip:";
    public static final String LOCKOUT_IP_PREFIX = "lockout:ip:";
}
