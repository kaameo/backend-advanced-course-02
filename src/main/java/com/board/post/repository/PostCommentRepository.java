package com.board.post.repository;

import com.board.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    List<PostComment> findByPostIdOrderByCreateDateAsc(int postId);
}
