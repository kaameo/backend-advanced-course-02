package com.board.global.initData;

import com.board.member.dto.MemberRequestDto;
import com.board.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class DataInit {
    private final DataInit self;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public DataInit(@Lazy DataInit self, MemberService memberService, PasswordEncoder passwordEncoder) {
        this.self = self;
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
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
        makeMember("example1@example.com","12345678", "홍길동");
        makeMember("example2@example.com","12345678", "홍길동");
        makeMember("example3@example.com","12345678", "홍길동");
        makeMember("example4@example.com","12345678", "홍길동");
        makeMember("example5@example.com","12345678", "홍길동");
        makeMember("example6@example.com","12345678", "홍길동");
        makeMember("example7@example.com","12345678", "홍길동");
        makeMember("example8@example.com","12345678", "홍길동");
        makeMember("example9@example.com","12345678", "홍길동");
        makeMember("example10@example.com","12345678", "홍길동");
    }

    private void makeMember(
            String email,
            String password,
            String nickname
    ) {
        memberService.signUp(new MemberRequestDto(email, password, nickname));
    }
}

