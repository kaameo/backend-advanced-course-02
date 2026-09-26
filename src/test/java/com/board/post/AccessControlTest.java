package com.board.post;

import com.board.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessControlTest extends IntegrationTestSupport {

    @Test
    @DisplayName("토큰 없이 글을 쓰면 401")
    void writePostWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("title", "제목", "content", "본문"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("토큰 없이 댓글을 쓰면 401")
    void writeCommentWithoutToken() throws Exception {
        String token = signUpAndLogin("author@test.com");
        int postId = createPost(token);

        mockMvc.perform(post("/api/v1/posts/" + postId + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("content", "댓글"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 토큰이면 401")
    void invalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("글과 댓글 읽기는 토큰 없이 된다")
    void readWithoutToken() throws Exception {
        String token = signUpAndLogin("author@test.com");
        int postId = createPost(token);
        createComment(token, postId);

        mockMvc.perform(get("/api/v1/posts")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/posts/" + postId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("남의 글을 수정·삭제하면 403이고 글은 그대로 남는다")
    void othersPost() throws Exception {
        String authorToken = signUpAndLogin("author@test.com");
        String otherToken = signUpAndLogin("other@test.com");
        int postId = createPost(authorToken);

        mockMvc.perform(put("/api/v1/posts/" + postId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("title", "바꾼 제목", "content", "바꾼 본문"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(delete("/api/v1/posts/" + postId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    @DisplayName("남의 댓글을 수정·삭제하면 403")
    void othersComment() throws Exception {
        String authorToken = signUpAndLogin("author@test.com");
        String otherToken = signUpAndLogin("other@test.com");
        int postId = createPost(authorToken);
        int commentId = createComment(authorToken, postId);

        mockMvc.perform(put("/api/v1/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("content", "바꾼 댓글"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("없는 글을 조회하면 404")
    void postNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("게시글이 없습니다. id=999999"));
    }

    @Test
    @DisplayName("없는 글의 댓글 목록을 조회하면 404")
    void commentsOfMissingPost() throws Exception {
        mockMvc.perform(get("/api/v1/posts/999999/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("없는 댓글을 수정하면 404")
    void commentNotFound() throws Exception {
        String token = signUpAndLogin("author@test.com");
        int postId = createPost(token);

        mockMvc.perform(put("/api/v1/posts/" + postId + "/comments/999999")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("content", "수정"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 글의 댓글 번호로 접근하면 404")
    void commentOfAnotherPost() throws Exception {
        String token = signUpAndLogin("author@test.com");
        int postA = createPost(token);
        int postB = createPost(token);
        int commentOfA = createComment(token, postA);

        mockMvc.perform(put("/api/v1/posts/" + postB + "/comments/" + commentOfA)
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("content", "수정"))))
                .andExpect(status().isNotFound());
    }
}
