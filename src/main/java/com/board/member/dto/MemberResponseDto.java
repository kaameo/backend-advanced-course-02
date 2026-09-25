package com.board.member.dto;

import com.board.member.entity.Member;

import java.time.LocalDateTime;

public record MemberResponseDto(
        Integer id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getCreateDate(),
                member.getModifyDate()
        );
    }
}
