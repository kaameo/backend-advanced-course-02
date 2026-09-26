package com.board.post.repository;

import com.board.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
    @Query("""
                select c from PostComment c
                join fetch c.author
                where c.post.id = :postId
                order by c.createDate asc
            """)
    List<PostComment> findAllWithAuthorByPostId(@Param("postId") int postId);

    @Modifying
    @Query("delete from PostComment c where c.post.id = :postId")
    void deleteAllByPostId(@Param("postId") int postId);

    @Modifying
    @Query("delete from PostComment c where c.parentComment.id = :parentCommentId")
    void deleteAllByParentId(@Param("parentCommentId") int parentId);

}
