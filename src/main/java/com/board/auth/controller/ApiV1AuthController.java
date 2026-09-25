package com.board.auth.controller;

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

    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody @Valid MemberRequestDto request) {
        MemberResponseDto created = memberService.signUp(request);
        return ResponseEntity
                .created(URI.create("/api/v1/members/" + created.id()))
                .body(created);
    }
}
