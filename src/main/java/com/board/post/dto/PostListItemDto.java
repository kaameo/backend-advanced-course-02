package com.board.post.dto;

import com.board.post.entity.Post;

import java.time.LocalDateTime;

public record PostListItemDto(
        Integer id,
        String title,
        String authorNickname,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PostListItemDto from(Post post) {
        return new PostListItemDto(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getNickname(),
                post.getComments().size(),
                post.getCreateDate(),
                post.getModifyDate()
        );
    }
}
