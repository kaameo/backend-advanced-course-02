package com.board.post;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "POST_COMMENT")
public class PostComment {
    private String comment;

}
