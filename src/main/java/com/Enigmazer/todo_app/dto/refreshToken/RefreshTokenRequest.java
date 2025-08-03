package com.Enigmazer.todo_app.dto.refreshToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
