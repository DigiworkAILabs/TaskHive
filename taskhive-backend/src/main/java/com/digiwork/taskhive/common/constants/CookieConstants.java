package com.digiwork.taskhive.common.constants;

public final class CookieConstants {

    private CookieConstants() {
    }

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final String USER_ROLE_COOKIE = "userRole";

    // REFRESH_TOKEN_PATH is now configurable via app.cookie.refresh-token-path property (see CookieUtil)
    // Cookie paths are now configurable via properties (see CookieUtil)

    public static final int ACCESS_TOKEN_MAX_AGE = 900; // 15 minutes
    public static final int REFRESH_TOKEN_MAX_AGE = 604800; // 7 days
    public static final int USER_ROLE_MAX_AGE = 604800; // 7 days
}
