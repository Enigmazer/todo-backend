package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.constants.CacheNameConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Handle work related to token blacklisting
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final CacheManager cacheManager;

    /**
     * Adds a token to the blacklist.
     *
     * @param tokenId unique identifier of the token (jti claim)
     * @param expirationTime when the token should be expired (not used here)
     */
    public void blacklistToken(String tokenId, Date expirationTime) {
        Cache cache = cacheManager.getCache(CacheNameConstants.TOKEN_BLACKLIST_CACHE);
        if (cache != null) {
            log.info("Blacklisting token ID: {}, expires at: {}", tokenId, expirationTime);
            cache.put(tokenId, true);
        } else {
            log.warn("Token blacklist cache not found. Token not blacklisted!");
        }
    }

    /**
     * Checks whether a token is blacklisted or not.
     *
     * @param tokenId unique identifier of the token
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String tokenId) {
        Cache cache = cacheManager.getCache(CacheNameConstants.TOKEN_BLACKLIST_CACHE);
        if (cache != null) {
            Boolean isBlacklisted = cache.get(tokenId, Boolean.class);
            return Boolean.TRUE.equals(isBlacklisted);
        }
        return false;
    }
}
