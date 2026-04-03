package com.Enigmazer.todo_app.service.auth;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;

public interface AuthService {

    TokenPair login(UserLoginRequest users);

    void logout(String token);

    void changePassword(UserLoginRequest request);

    String getLatestRefreshTokenForUser();
}
