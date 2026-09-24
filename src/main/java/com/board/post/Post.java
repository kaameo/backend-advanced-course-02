package com.board.post;

import com.board.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "POST")
public class Post extends BaseIdAndTime {

    private String title;
    private String content;

    private List<PostComment> comments;
}
