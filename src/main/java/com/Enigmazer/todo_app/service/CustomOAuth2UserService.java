package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.constants.CacheNameConstants;
import com.Enigmazer.todo_app.constants.RoleConstants;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.UserRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * CustomOAuth2UserService handles Oauth2 related operations like it loads
 * user from request, verifies them ,or register and verify them if they are new
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * loadUser register/verify the user and return the authenticated user
     *
     * @param request   incoming HTTP request
     * @return  OAuth2User authenticated oauth2 user
     * @throws OAuth2AuthenticationException threw when credentials
     * are wrong or not found
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = fetchOAuth2User(request);

        String provider = request.getClientRegistration().getRegistrationId(); // google or github
        String providerId =
                "google".equals(provider) ?
                        oAuth2User.getAttribute("sub") :
                        Objects.requireNonNull(oAuth2User.getAttribute("id")).toString();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // if no providerId found from provider then don't move ahead
        if(providerId == null) {
            log.info("Provider id is missing from the provider : {}", provider);
            throw new OAuth2AuthenticationException("Provider Id is missing");
        }

        // If email is missing and Oauth2 provider is GitHub, then try to fetch email manually
        if (email == null && provider.equals("github")) {
            email = fetchGitHubEmail(request);
        }

        // if still no email found from provider then don't move ahead
        if (email == null) {
            log.info("No email found from the provider : {}", provider);
            throw new OAuth2AuthenticationException("Email not found from " + provider);
        }

        // if no name found from provider then set email prefix as name
        if (name == null || name.isBlank()) {
            log.info("No name found from the provider {} setting email prefix as name", provider);
            name = getEmailPrefix(email);
        }

        log.info("OAuth2 login attempt via {}, email resolved to: {}", provider, email);

        // Validate user
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);

        // update email if needed, throw exception if email registered with another provider
        // or save if it's a new user

        if (user != null){
            if (!email.equals(user.getEmail())) {
                log.info("Updating email from {} to {}", user.getEmail(), email);
                user.setEmail(email);
                userRepository.save(user);
            }
        } else if ((user = userRepository.findByEmail(email).orElse(null)) != null) {
            log.info("email {} is registered with this provider : {}",user.getEmail(), provider);
            throw new OAuth2AuthenticationException("This email is already " +
                    "registered via another sign-in method. Try signing in using that method.");
        } else {
            log.info("adding new user to database using Oauth2 : {}", email);
            user = User.builder()
                    .name(name)
                    .email(email)
                    .provider(provider)
                    .providerId(providerId)
                    .roles(Set.of(RoleConstants.ROLE_USER))
                    .build();
            userRepository.save(user);
            log.info("user successfully added in the database : {}", user.getEmail());
        }

        if(!user.isEnabled()){
            throw new OAuth2AuthenticationException(new OAuth2Error("Inactive account"), "Account is not active");
        }

        log.info("OAuth2 login attempt via {} is successful now returning user " +
                "email : {} for creating jwt token.", provider, email);

        // Only return when we are sure data exists in attributes
        return new DefaultOAuth2User(
                user.getRoles().stream().map(SimpleGrantedAuthority::new).toList(),
                Collections.singletonMap(
                        "email", email
                ),
                "email"
        );
    }

    String getEmailPrefix(String email){
        if(email != null && email.contains("@")){
            return email.substring(0, email.indexOf('@'));
        }
        return "user"; // fallback
    }

    /**
     * Attempts to fetch the user's primary email from GitHub's API
     * when it's not available in the standard OAuth2User attributes.
     * also cache the email so we don't have to hit the api every time
     * and a rate limit to avoid calling the GitHub api too often if email isn't fetched
     */
    @Cacheable(value = CacheNameConstants.GITHUB_EMAIL_CACHE, key = "#request.accessToken.tokenValue")
    @RateLimiter(name = "githubApi")
    private String fetchGitHubEmail(OAuth2UserRequest request) {
        try {
            String token = request.getAccessToken().getTokenValue();

            var restTemplate = new org.springframework.web.client.RestTemplate();
            var headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
            var entity = new org.springframework.http.HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getBody() != null) {
                for (Map<String, Object> emailObj : response.getBody()) {
                    if (Boolean.TRUE.equals(emailObj.get("primary"))) {
                        return (String) emailObj.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch email from GitHub API: {}", e.getMessage());
        }
        return null;
    }

    protected OAuth2User fetchOAuth2User(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

}