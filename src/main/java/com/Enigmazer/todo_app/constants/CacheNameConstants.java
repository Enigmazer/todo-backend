package com.Enigmazer.todo_app.constants;

import java.util.Set;

/**
 * Stores the constant for cache names to prevent typos
 */
public class CacheNameConstants {
    public static final String GITHUB_EMAIL_CACHE = "githubEmailCache";
    public static final String TOKEN_BLACKLIST_CACHE = "tokenBlacklistCache";

    /**
     *
     * @return list of all the CacheNameConstants
     * handled by caffeine cache manager
     */
    public static Set<String> getCaffeineCacheNamesSet() {
        return Set.of(
                GITHUB_EMAIL_CACHE
        );
    }

    /**
     *
     * @return list of all the CacheNameConstants handled by redis cache manager
     */
    public static Set<String> getRedisCacheNamesSet() {
        return Set.of(
                TOKEN_BLACKLIST_CACHE
        );
    }
}