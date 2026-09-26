package com.board.post.dto;

import com.board.post.entity.PostComment;

import java.time.LocalDateTime;

public record PostCommentResponseDto(
        Integer id,
        Integer postId,
        String content,
        Integer authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PostCommentResponseDto from(PostComment postComment) {
        return new PostCommentResponseDto(
                postComment.getId(),
                postComment.getPost().getId(),
                postComment.getContent(),
                postComment.getAuthor().getId(),
                postComment.getAuthor().getNickname(),
                postComment.getCreateDate(),
                postComment.getModifyDate()
        );
    }
}
