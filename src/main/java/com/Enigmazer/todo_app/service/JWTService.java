package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.RefreshTokenRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWTService handles all JWT-related operations like generating tokens,
 * extracting user details from tokens, and validating/invalidating them.
 */
@Service
@Slf4j
public class JWTService {

    @Value("${jwt.accessTokenExpiry}")
    private Duration accessTokenExpiry;

    @Value("${jwt.refreshTokenExpiry}")
    private Duration refreshTokenExpiry;

    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final KeyPair keyPair;
    private final RefreshTokenRepository refreshTokenRepository;


    /**
     * Constructs a new JWTService with the required dependencies.
     *
     * @param userRepository the user repository for user data access
     * @param tokenBlacklistService the service for managing blacklisted tokens
     * @param refreshTokenRepository the repository for managing refresh tokens
     * @param keyLoader the loader for cryptographic keys
     */
    @Autowired
    public JWTService(UserRepository userRepository,
                     TokenBlacklistService tokenBlacklistService,
                     RefreshTokenRepository refreshTokenRepository,
                     KeyLoader keyLoader) {
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.keyPair = keyLoader.loadKeyPair("public_key.pem", "private_key.pem");
    }

    /**
     * Generates a new pair of access and refresh tokens for the specified user.
     * Updates the user's last login timestamp.
     *
     * @param email the email of the user to generate tokens for
     * @return a TokenPair containing both access and refresh tokens
     * @throws UsernameNotFoundException if no user is found with the given email
     */
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

    /**
     * Generates a new refresh token for the specified user and stores it in the database.
     *
     * @param user the user to generate the refresh token for
     * @return the generated refresh token
     */
    private String generateRefreshToken(User user) {
        String token = generateSecureToken();
        Instant expiry = Instant.now().plus(refreshTokenExpiry);

        refreshTokenRepository.save(new RefreshToken(token, user, Instant.now(),expiry)); // store in DB
        return token;
    }

    /**
     * Generates a cryptographically secure random token.
     *
     * @return a URL-safe base64 encoded random string
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates a JWT for a given user email with roles included in the claims.
     *
     * @param user the user for whom we have to create the token
     * @return signed JWT token
     */
    private String generateAccessToken(User user) {

        // Add unique token ID for blacklisting
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = Map.of(
                "roles", user.getRoles()
        );

        log.info("Generating JWT token for email: {} with jti: {}", user.getEmail(), jti);

        return Jwts.builder()
                .id(jti)
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(accessTokenExpiry)))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Refreshes both access and refresh tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token to use for generating new tokens
     * @return a new TokenPair with fresh access and refresh tokens
     * @throws RuntimeException if the refresh token is invalid or no user is associated with it
     */
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

    /**
     * Checks if a refresh token is valid and not expired.
     *
     * @param refreshToken the refresh token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(rf -> rf.getExpiry().isAfter(Instant.now()) && !rf.isRevoked())
                .isPresent();
    }

    /**
     * Validates the access token by checking expiry, and blacklist status.
     *
     * @param token       JWT(access) token
     * @return true if the token is valid and not expired
     */
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            final String tokenId = claims.getId();

            if (tokenId == null){
                log.warn("Token Id is missing.");
                return false;
            }

            if (tokenBlacklistService.isTokenBlacklisted(tokenId)) {
                log.warn("Access token is blacklisted: {}", tokenId);
                return false;
            }

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

    /**
     * Extracts all claims (payload data) from the given JWT token.
     * This includes custom claims like roles, as well as standard claims like subject and expiration.
     *
     * @param token the JWT token
     * @return all claims contained in the token payload
     * @throws JwtException if the token is malformed, expired, or invalid
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Failed to parse claims from JWT: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Invalidates a JWT token by adding it to the blacklist.
     * This method also invalidates the corresponding refresh token.
     *
     * @param token JWT token to invalidate
     */
    @Transactional
    public void invalidateTokens(String token) {
        log.info("Invalidating the tokens");
        try {
            String tokenId = extractTokenId(token);
            Date expirationDate = extractExpirationDate(token);
            tokenBlacklistService.blacklistToken(tokenId, expirationDate);
            log.info("Access Token {} has been invalidated", tokenId);
            refreshTokenRepository.deleteByUser(getCurrentUser());  // logout from all the devices
            log.info("Refresh Token has been deleted");
        } catch (JwtException e) {
            log.warn("Failed to invalidate tokens: {}", e.getMessage());
        }
    }

    /**
     * Invalidates a refresh token by marking it as revoked in the database.
     *
     * @param refreshToken the refresh token to invalidate
     */
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

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return the currently authenticated user
     * @throws UsernameNotFoundException if no user is authenticated
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Extracts the username (email) from a JWT token.
     *
     * @param token the JWT token
     * @return the username (email) from the token's subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the roles from the given access token.
     *
     * @param token JWT token
     * @return roles
     */
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

    /**
     * Extracts the JWT ID (jti) from a token.
     *
     * @param token the JWT token
     * @return the token's unique ID
     */
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    /**
     * Extracts a specific claim from a JWT token using the provided claim resolver function.
     * This is a generic helper method used by other claim extraction methods to avoid code duplication.
     *
     * @param <T> the type of the claim to extract
     * @param token the JWT token from which to extract the claim
     * @param claimResolver a function that extracts the desired claim from the token's claims
     * @return the extracted claim value of type T
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

}
