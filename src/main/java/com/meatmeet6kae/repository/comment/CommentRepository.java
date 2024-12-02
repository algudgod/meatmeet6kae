package com.meatmeet6kae.repository.comment;

import com.meatmeet6kae.entity.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByBoard_BoardNo(int boardNo); //게시판 번호 기준으로 댓글 전체조회
    int countByBoardBoardNo(int boardNo); // 게시글 ID로 댓글 수를 카운트

}
