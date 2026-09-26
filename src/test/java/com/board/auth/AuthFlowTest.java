package com.board.auth;

import com.board.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends IntegrationTestSupport {

    @Test
    @DisplayName("가입 → 로그인 → 토큰으로 내 정보 조회")
    void signUpLoginAndMe() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "password", "password123", "nickname", "테스터"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/members/me"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.password").doesNotExist());

        String token = login("user@test.com", "password123");

        mockMvc.perform(get("/api/v1/members/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("로그인 응답에 Bearer 토큰이 담긴다")
    void loginReturnsBearerToken() throws Exception {
        signUp("user@test.com", "password123", "테스터");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("이미 가입된 이메일로 가입하면 409")
    void duplicateEmail() throws Exception {
        signUp("dup@test.com", "password123", "첫번째");

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "dup@test.com", "password", "password123", "nickname", "두번째"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 400")
    void invalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "not-an-email", "password", "password123", "nickname", "테스터"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("비밀번호가 8자보다 짧으면 400")
    void shortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "password", "1234567", "nickname", "테스터"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("JSON이 아닌 Content-Type으로 로그인하면 500이 아니라 415")
    void unsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .content("email=user@test.com&password=password123"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    @DisplayName("틀린 비밀번호와 없는 이메일은 같은 401 메시지")
    void loginFailure() throws Exception {
        signUp("user@test.com", "password123", "테스터");

        String message = "이메일 또는 비밀번호가 올바르지 않습니다.";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(message));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("email", "nobody@test.com", "password", "password123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(message));
    }
}
