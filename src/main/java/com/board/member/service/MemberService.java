package com.board.member.service;

import com.board.global.exception.NotFoundException;
import com.board.member.dto.MemberResponseDto;
import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public MemberResponseDto findById(Integer id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id=" + id));
        return MemberResponseDto.from(member);
    }

    public Long count() {
        return memberRepository.count();
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
