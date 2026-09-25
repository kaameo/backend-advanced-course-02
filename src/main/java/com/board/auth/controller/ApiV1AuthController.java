package com.board.auth.controller;

import com.board.auth.dto.LoginRequestDto;
import com.board.auth.dto.TokenResponseDto;
import com.board.auth.service.AuthService;
import com.board.member.dto.MemberRequestDto;
import com.board.member.dto.MemberResponseDto;
import com.board.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ApiV1AuthController {
    private final MemberService memberService;
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody @Valid MemberRequestDto request) {
        MemberResponseDto created = memberService.signUp(request);
        return ResponseEntity
                .created(URI.create("/api/v1/members/" + created.id()))
                .body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }
}
