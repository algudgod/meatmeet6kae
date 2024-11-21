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

    // 카테고리별로 내림차순 정렬된 게시글 조회
    List<Board> findByBoardCategoryOrderByBoardNoDesc(String boardCategory);

    // 특정 게시글 번호로 조회
    Optional<Board> findByBoardNo(int boardNo);


    /* 카테고리별 ROW_NUMBER를 계산하여 동적으로 번호가 매겨진 게시글 정보를 가져옴.
       SELECT로 boardCategory를 가져오고, ROW_NUMBER로 각 행에 고유한 번호(categoryBoardNo)를 붙임.
       boardCategory 기준으로 데이터를 partition하고, 번호를 부여한 후, order by boardNo 순으로 정렬.
       부여된 번호를 categoryBoardNo으로 지정.
       boardCategory 파라미터 값이 :boardCategory에 대입되어 필터링.

       **JOIN 활용**:
       - board 테이블과 user 테이블을 조인하여 작성자 닉네임(nickname)을 가져옵니다.
       - board.user_id와 user.id를 연결(JOIN)하여 닉네임 정보를 가져옵니다.
       - 게시판의 작성자 정보를 표시하기 위해 JOIN이 필요합니다.

       **쿼리 필드 설명**:
       - b.boardCategory: 게시글의 카테고리
       - ROW_NUMBER() OVER(PARTITION BY b.boardCategory ORDER BY b.boardNo): 카테고리별로 순번(categoryBoardNo)을 생성
       - b.boardNo: 게시글 고유 번호
       - b.title: 게시글 제목
       - b.content: 게시글 내용
       - u.nickname: 작성자의 닉네임
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
    /* List<Object[]>는
        [0]: boardCategory
        [1]: categoryBoardNo (ROW_NUMBER로 생성된 카테고리별 글 번호)
        [2]: boardNo (전체 고유 글 번호)
        [3]: title
        [4]: content
        [5]: nickname

     JPA 매핑 규칙
     VARCHAR, TEXT -> String
     INT -> Integer
     ROW_NUMBER() -> Number
    */
}
