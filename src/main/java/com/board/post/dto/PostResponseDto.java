package com.board.post.dto;

import com.board.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Integer id,
        String title,
        String content,
        Integer authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getCreateDate(),
                post.getModifyDate()
        );
    }
}
