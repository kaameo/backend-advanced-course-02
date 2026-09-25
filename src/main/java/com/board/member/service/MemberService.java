package com.board.member.service;

import com.board.global.exception.DuplicateException;
import com.board.global.exception.NotFoundException;
import com.board.member.dto.MemberRequestDto;
import com.board.member.dto.MemberResponseDto;
import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<MemberResponseDto> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponseDto findById(Integer id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id=" + id));
        return MemberResponseDto.from(member);
    }

    @Transactional(readOnly = true)
    public Long count() {
        return memberRepository.count();
    }

    @Transactional
    public MemberResponseDto signUp(MemberRequestDto request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member saved = memberRepository.save(
                new Member(request.email(), encodedPassword, request.nickname())
        );
        return MemberResponseDto.from(saved);
    }
}
