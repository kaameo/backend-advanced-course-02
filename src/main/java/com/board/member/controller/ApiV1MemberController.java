package com.board.member.controller;

import com.board.member.entity.Member;
import com.board.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public List<Member> getMembers(){
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Member> getMember(@PathVariable Integer id){
        return memberService.findById(id);
    }

    @PostMapping("")
    public Member save(@RequestBody Member member){
        return memberService.save(member);
    }
}
