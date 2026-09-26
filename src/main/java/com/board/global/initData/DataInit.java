package com.board.global.initData;

import com.board.member.dto.MemberRequestDto;
import com.board.member.dto.MemberResponseDto;
import com.board.member.service.MemberService;
import com.board.post.dto.PostRequestDto;
import com.board.post.dto.PostResponseDto;
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

    public DataInit(@Lazy DataInit self, MemberService memberService, PostService postService) {
        this.self = self;
        this.memberService = memberService;
        this.postService = postService;
    }

    @Bean
    public ApplicationRunner baseInitDataRunner() {
        return args -> {
            self.makeBaseMembers();
        };
    }

    @Transactional
    public void makeBaseMembers() {
        if (memberService.count() > 0) {
            return;
        }
        int member1Id = makeMember("example1@example.com", "12345678", "홍길동1");
        int member2Id = makeMember("example2@example.com", "12345678", "홍길동2");
        int member3Id = makeMember("example3@example.com", "12345678", "홍길동3");
        int member4Id = makeMember("example4@example.com", "12345678", "홍길동4");
        int member5Id = makeMember("example5@example.com", "12345678", "홍길동5");

        int post1Id = makePost(member1Id, "title1", "content1");
        int post2Id = makePost(member2Id, "title2", "content2");
        int post3Id = makePost(member2Id, "title3", "content3");
        int post4Id = makePost(member3Id, "title4", "content4");
        int post5Id = makePost(member3Id, "title5", "content5");
        int post6Id = makePost(member3Id, "title6", "content5");

    }

    private int makeMember(
            String email,
            String password,
            String nickname
    ) {
        MemberResponseDto member = memberService.signUp(new MemberRequestDto(email, password, nickname));
        return member.id();
    }

    private int makePost(
            int authorId,
            String title,
            String content
    ) {
        PostResponseDto post = postService.create(authorId, new PostRequestDto(title, content));
        return post.id();
    }
}

