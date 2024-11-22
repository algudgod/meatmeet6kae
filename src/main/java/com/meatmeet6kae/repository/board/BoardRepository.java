package com.meatmeet6kae.repository.board;

import com.meatmeet6kae.entity.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {

    // 카테고리별로 최신 게시글 목록 조회 (DESC)
    List<Board> findByBoardCategoryOrderByBoardNoDesc(String boardCategory);

    // 게시글 번호로 게시글 찾기
    Optional<Board> findByBoardNo(int boardNo);

    /**
     * 특정 카테고리의 게시글을 ROW_NUMBER와 함께 조회 (User 테이블 조인)
     * @param boardCategory 게시판 카테고리
     * @return 게시글 정보와 카테고리별 번호 (Object 배열)
     */
    @Query(value = "SELECT " +
            "b.board_category, " +
            "ROW_NUMBER() OVER (PARTITION BY b.board_category ORDER BY b.board_no) AS categoryBoardNo, " +
            "b.board_no, " +
            "b.title, " +
            "b.content, " +
            "u.nickname, " +
            "b.create_date " +  // create_date 추가
            "FROM board b " +
            "JOIN user u ON b.login_id = u.login_id " + // User 테이블과 조인
            "WHERE b.board_category = :boardCategory",
            nativeQuery = true)
    List<Object[]> findByCategoryWithRowNumber(@Param("boardCategory") String boardCategory);

}
