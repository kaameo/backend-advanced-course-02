package com.board.post.entity;

import com.board.global.jpa.entity.BaseIdAndTime;
import com.board.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POST_COMMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseIdAndTime {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Member author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PostComment parentComment;

    public PostComment(Post post, Member author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
    }

    public static PostComment reply(PostComment parent, Member author, String content) {
        PostComment reply = new PostComment(parent.getPost(), author, content);
        reply.parentComment = parent;
        return reply;
    }

    public boolean isReply() {
        return parentComment != null;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isAuthor(int memberId) {
        return author.getId() == memberId;
    }
}
