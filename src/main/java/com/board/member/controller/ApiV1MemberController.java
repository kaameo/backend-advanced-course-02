package com.board.member.controller;

import com.board.member.dto.MemberResponseDto;
import com.board.member.entity.Member;
import com.board.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public List<Member> findAll(){
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> findById(@PathVariable Integer id){
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PostMapping("")
    public Member save(@RequestBody Member member){
        return memberService.save(member);
    }
}
