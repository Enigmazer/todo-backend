package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Set<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // Avoid null password issue for OAuth2 users
        String safePassword = user.getPassword() == null ? "" : user.getPassword();

        log.info("Mapping the user {} to userDetails and returning it", user.getEmail());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(safePassword)
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}
