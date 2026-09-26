package com.board.global.initData;

import com.board.member.dto.MemberRequestDto;
import com.board.member.service.MemberService;
import com.board.post.dto.PostCommentRequestDto;
import com.board.post.dto.PostRequestDto;
import com.board.post.service.PostCommentService;
import com.board.post.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class DataInit {
    private final DataInit self;
    private final MemberService memberService;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public DataInit(@Lazy DataInit self, MemberService memberService, PostService postService, PostCommentService postCommentService) {
        this.self = self;
        this.memberService = memberService;
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @Bean
    public ApplicationRunner baseInitDataRunner() {
        return args -> {
            self.makeBaseData();
        };
    }

    @Transactional
    public void makeBaseData() {
        if (memberService.count() > 0) {
            return;
        }
        int user1 = memberService.signUp(new MemberRequestDto("example1@example.com", "12345678", "홍길동")).id();
        int user2 = memberService.signUp(new MemberRequestDto("example2@example.com", "12345678", "김철수")).id();
        int user3 = memberService.signUp(new MemberRequestDto("example3@example.com", "12345678", "이영희")).id();
        int[] authors = {user1, user2, user3};

        for (int i = 1; i <= 12; i++) {
            int authorId = authors[i % 3];
            int postId = postService.create(authorId, new PostRequestDto("테스트 글 " + i, "본문 " + i)).id();

            // 짝수 번째 글에만 댓글 3개 (댓글 0개인 글도 필요)
            if (i % 2 == 0) {
                int firstCommentId = 0;
                for (int j = 1; j <= 3; j++) {
                    int commentId = postCommentService.create(authors[j % 3], postId, new PostCommentRequestDto("댓글 " + j)).id();
                    if (j == 1) {
                        firstCommentId = commentId;
                    }
                }

                // 첫 댓글에 대댓글 2개
                for (int k = 1; k <= 2; k++) {
                    postCommentService.createReply(authors[(k + 1) % 3], postId, firstCommentId, new PostCommentRequestDto("대댓글 " + k));
                }
            }
        }
    }
}

