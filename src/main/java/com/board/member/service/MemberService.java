package com.board.member.service;

import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Optional<Member> findById(Integer id) {
        return memberRepository.findById(id);
    }

    public Long count() {
        return memberRepository.count();
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
