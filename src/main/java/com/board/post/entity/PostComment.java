package com.board.post.entity;

import com.board.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POST_COMMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseIdAndTime {
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;


    private String content;
}
