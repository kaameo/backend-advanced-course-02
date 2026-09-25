package com.board.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String tokenType
) {
    public static TokenResponseDto bearer(String accessToken) {
        return new TokenResponseDto(accessToken, "Bearer");
    }
}
