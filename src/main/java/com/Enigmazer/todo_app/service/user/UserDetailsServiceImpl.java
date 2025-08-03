package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.constants.CacheNameConstants;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserDetailsServiceImpl loads user from database and
 * map it to the UserDetails and return it So it can be
 * handled by the authentication provider.
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     *  loadUserByUsername method is normally called when a user logs in,
     *  but if they use oauth2 to log in, then this method will be not
     *  called until the first request with jwt token comes
     *
     * @param email the username identifying the user whose data is required.
     * @return UserDetails which is handled or supported by authentication provider for authentication
     * @throws UsernameNotFoundException if the email is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.debug("Trying to load user {} from the database", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        log.info("User {} is successfully loaded from the database", user.getEmail());

        // Convert roles from Set<String> to Set<GrantedAuthority>
        Set<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        log.debug("Mapped roles for {}: {}", email, authorities);

        // Avoid null password issue for OAuth2 users
        String safePassword = user.getPassword() == null ? "" : user.getPassword();

        log.info("Mapping the user {} to userDetails and returning it", user.getEmail());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(safePassword) // Prevent IllegalArgumentException
                .authorities(authorities)
                .disabled(!user.isEnabled()) // Correctly reflect 'enabled' field
                .build();
    }
}
