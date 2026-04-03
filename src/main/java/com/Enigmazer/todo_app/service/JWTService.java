package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.RefreshTokenRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JWTService {


    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiry}")
    private Duration accessTokenExpiry;

    @Value("${jwt.refreshTokenExpiry}")
    private Duration refreshTokenExpiry;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey signingKey;

    @PostConstruct
    private void initKey() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public TokenPair generateTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        TokenPair tokenPair = new TokenPair(accessToken, refreshToken);
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        return tokenPair;
    }

    @Transactional
    private String generateRefreshToken(User user) {
        String token = generateSecureToken();
        Instant expiry = Instant.now().plus(refreshTokenExpiry);

        refreshTokenRepository.save(new RefreshToken(token, user, Instant.now(),expiry)); // store in DB
        return token;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateAccessToken(User user) {


        Map<String, Object> claims = Map.of(
                "roles", user.getRoles()
        );

        log.info("Generating JWT token for email: {}", user.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(accessTokenExpiry)))
                .signWith(signingKey)
                .compact();
    }

    public TokenPair renewTokens(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        return generateTokens(
                refreshTokenRepository.findUserByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("No user found with this token"))
                    .getEmail()
        );
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(rf -> rf.getExpiry().isAfter(Instant.now()) && !rf.isRevoked())
                .isPresent();
    }

    public boolean isAccessTokenValid(String token) {
        try {
            if (isTokenExpired(token)) {
                log.warn("Access token is expired");
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Empty JWT token: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Failed to parse claims from JWT: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        log.info("Invalidating the refresh token");
        try {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenEntity -> refreshTokenEntity.setRevoked(true));
        } catch (Exception e) {
            log.warn("Failed to invalidate refresh token: {}", e.getMessage());
        }
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Set<String> extractRole(String token) {
        return extractClaim(token, claims -> {
            Object rawRoles = claims.get("roles");
            if(rawRoles instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());
            }
            return Collections.emptySet();
        });
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

}
