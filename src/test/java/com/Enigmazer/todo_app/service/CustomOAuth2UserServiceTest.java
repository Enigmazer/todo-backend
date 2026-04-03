package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.enums.RoleType;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2User delegateOAuth2User;

    @InjectMocks
    private CustomOAuth2UserService service;

    @Test
    void loadUser_ShouldRegisterNewUser_WhenNotExists() {
        // mock request and client registration
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        var clientReg = mock(org.springframework.security.oauth2.client.registration.ClientRegistration.class);
        when(request.getClientRegistration()).thenReturn(clientReg);
        when(clientReg.getRegistrationId()).thenReturn("google");

        // mock attributes
        when(delegateOAuth2User.getAttribute("sub")).thenReturn("providerId123");
        when(delegateOAuth2User.getAttribute("email")).thenReturn("user@example.com");
        when(delegateOAuth2User.getAttribute("name")).thenReturn("User Name");

        // mock repository
        when(userRepository.findByProviderAndProviderId("google", "providerId123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // stub internal user fetch (avoid real call to parent loadUser)
        CustomOAuth2UserService spyService = spy(service);
        doReturn(delegateOAuth2User).when(spyService).fetchOAuth2User(request);

        OAuth2User result = spyService.loadUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getProvider()).isEqualTo("google");
        assertThat(saved.getRoles()).containsExactly(RoleType.USER.toString());
        assertThat(result).isInstanceOf(DefaultOAuth2User.class);
    }

    @Test
    void loadUser_ShouldThrow_WhenProviderIdMissing() {
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        var clientReg = mock(org.springframework.security.oauth2.client.registration.ClientRegistration.class);
        when(request.getClientRegistration()).thenReturn(clientReg);
        when(clientReg.getRegistrationId()).thenReturn("google");

        when(delegateOAuth2User.getAttribute("sub")).thenReturn(null);
        CustomOAuth2UserService spyService = spy(service);
        doReturn(delegateOAuth2User).when(spyService).fetchOAuth2User(request);

        assertThrows(OAuth2AuthenticationException.class, () -> spyService.loadUser(request));
    }

    @Test
    void loadUser_ShouldThrow_WhenEmailConflictWithExistingUser() {
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        var clientReg = mock(org.springframework.security.oauth2.client.registration.ClientRegistration.class);
        when(request.getClientRegistration()).thenReturn(clientReg);
        when(clientReg.getRegistrationId()).thenReturn("google");

        when(delegateOAuth2User.getAttribute("sub")).thenReturn("providerId123");
        when(delegateOAuth2User.getAttribute("email")).thenReturn("user@example.com");
        when(delegateOAuth2User.getAttribute("name")).thenReturn("User Name");

        User existingUser = User.builder().build();
        when(userRepository.findByProviderAndProviderId("google", "providerId123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        CustomOAuth2UserService spyService = spy(service);
        doReturn(delegateOAuth2User).when(spyService).fetchOAuth2User(request);

        assertThrows(OAuth2AuthenticationException.class, () -> spyService.loadUser(request));
    }

    @Test
    void getEmailPrefix_ShouldReturnPrefix() {
        String prefix = service.getEmailPrefix("abc@domain.com");
        assertThat(prefix).isEqualTo("abc");
    }

    @Test
    void getEmailPrefix_ShouldFallback() {
        String prefix = service.getEmailPrefix("abc");
        assertThat(prefix).isEqualTo("user");
    }
}
