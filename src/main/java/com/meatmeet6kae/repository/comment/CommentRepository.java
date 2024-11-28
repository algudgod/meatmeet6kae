package com.meatmeet6kae.repository.comment;

import com.meatmeet6kae.entity.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    //게시판 번호 기준으로 댓글 조회
    List<Comment> findByBoard_BoardNo(int boardNo);
}
