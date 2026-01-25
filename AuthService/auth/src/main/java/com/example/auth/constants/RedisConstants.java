package com.example.auth.constants;

/**
 * Constants for Redis key prefixes used throughout the authentication service.
 */
public final class RedisConstants {

    private RedisConstants() {
    }

    public static final String SESSION_PREFIX = "session:";

    public static final String PASSWORD_RESET_TOKEN_PREFIX = "password_reset:";

    public static final String LOCKOUT_USER_PREFIX = "lockout:user:";
    public static final String LOCKOUT_LEVEL_PREFIX = "lockout:level:user:";
}
