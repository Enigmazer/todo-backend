package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.constants.CacheNameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenBlacklistServiceTest {

    private CacheManager cacheManager;
    private Cache cache;
    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache(CacheNameConstants.TOKEN_BLACKLIST_CACHE)).thenReturn(cache);

        service = new TokenBlacklistService(cacheManager);
    }

    @Test
    void blacklistToken_ShouldPutTokenInCache() {
        String tokenId = "token123";
        Date expiration = new Date();

        service.blacklistToken(tokenId, expiration);

        verify(cache).put(tokenId, true);
    }

    @Test
    void isTokenBlacklisted_ShouldReturnTrue_WhenTokenExistsInCache() {
        String tokenId = "token123";
        when(cache.get(tokenId, Boolean.class)).thenReturn(true);

        assertTrue(service.isTokenBlacklisted(tokenId));
    }

    @Test
    void isTokenBlacklisted_ShouldReturnFalse_WhenTokenDoesNotExistInCache() {
        String tokenId = "token123";
        when(cache.get(tokenId, Boolean.class)).thenReturn(null);

        assertFalse(service.isTokenBlacklisted(tokenId));
    }

    @Test
    void blacklistToken_ShouldNotFail_WhenCacheIsNull() {
        when(cacheManager.getCache(CacheNameConstants.TOKEN_BLACKLIST_CACHE)).thenReturn(null);

        assertDoesNotThrow(() -> service.blacklistToken("token123", new Date()));
    }

    @Test
    void isTokenBlacklisted_ShouldReturnFalse_WhenCacheIsNull() {
        when(cacheManager.getCache(CacheNameConstants.TOKEN_BLACKLIST_CACHE)).thenReturn(null);

        assertFalse(service.isTokenBlacklisted("token123"));
    }
}
