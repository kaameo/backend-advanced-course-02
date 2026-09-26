package com.board.post.dto;

import com.board.post.entity.PostComment;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentResponseDto(
        Integer id,
        Integer postId,
        Integer parentId,
        String content,
        Integer authorId,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<PostCommentResponseDto> replies
) {
    public static PostCommentResponseDto from(PostComment comment) {
        return from(comment, List.of());
    }

    public static PostCommentResponseDto from(PostComment comment, List<PostCommentResponseDto> replies) {
        return new PostCommentResponseDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.isReply() ? comment.getParentComment().getId() : null,
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getCreateDate(),
                comment.getModifyDate(),
                replies
        );
    }
}
