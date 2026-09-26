package com.board.member.controller;

import com.board.member.dto.MemberResponseDto;
import com.board.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public List<MemberResponseDto> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> me(@AuthenticationPrincipal Integer memberId) {
        return ResponseEntity.ok(memberService.findById(memberId));
    }
}
