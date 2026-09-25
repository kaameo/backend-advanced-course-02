package com.board.member.controller;

import com.board.member.dto.MemberRequestDto;
import com.board.member.dto.MemberResponseDto;
import com.board.member.entity.Member;
import com.board.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public List<MemberResponseDto> findAll(){
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> findById(@PathVariable Integer id){
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<MemberResponseDto> signUp(@RequestBody @Valid MemberRequestDto member){
        MemberResponseDto created = memberService.signUp(member);
        return ResponseEntity
                .created(URI.create("/api/v1/members/" + created.id()))
                .body(created);
    }
}
