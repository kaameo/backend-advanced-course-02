package com.board.post.repository;

import com.board.post.dto.PostListItemDto;
import com.board.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = """
            select new com.board.post.dto.PostListItemDto(
                p.id, p.title, m.nickname, count(c), p.createDate, p.modifyDate
            )
            from Post p
            join p.author m
            left join p.comments c
            group by p.id, p.title, m.nickname, p.createDate, p.modifyDate
            order by p.createDate desc
            """,
            countQuery = "select count(p) from Post p")
    Page<PostListItemDto> findPostList(Pageable pageable);
}
