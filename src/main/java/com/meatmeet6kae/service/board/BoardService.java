package com.meatmeet6kae.service.board;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.board.BoardRepository;
import com.meatmeet6kae.repository.comment.CommentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class BoardService {

    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    public BoardService(BoardRepository boardRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    // 특정 카테고리의 게시글을 내림차순으로 조회
    public List<Board>getBoardsByCategory(String boardCategory){
        return boardRepository.findByBoardCategoryOrderByBoardNoDesc(boardCategory);
    }

    // 게시글 생성
    public void addBoard(BoardDto boardDto, User user){

        Board board = new Board();
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setBoardCategory(boardDto.getBoardCategory());
        board.setUser(user);
        boardRepository.save(board);
    }

    // 게시글의 존재 여부 검증한 후, 조회
    public Board getBoardByBoardNo(int boardNo) {
        return boardRepository.findByBoardNo(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    // 게시글 수정
    @Transactional
    public Board updateBoard(BoardDto boardDto) {

        Board board = getBoardByBoardNo(boardDto.getBoardNo());
        // String -> Enum 변환
        BoardCategory category = getBoardCategoryEnum(boardDto.getBoardCategory());
        System.out.println("변환된 카테고리: " + category);

        // 게시글 수정 내용
        board.setBoardCategory(category.name()); // Enum의 이름(String)으로 설정
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setUpdateDate(LocalDateTime.now());
        return boardRepository.save(board);
    }

    //게시글 삭제
    public void deleteBoard(int boardNo, User user){

        Board board = getBoardByBoardNo(boardNo);

        if (!isBoardOwner(board, user)) {
            throw new IllegalArgumentException("작성자가 아니므로 삭제할 수 없습니다.");
        }
        boardRepository.delete(board);
    }

    // 게시글 조회수 증가
    @Transactional
    public void updateViewCount(int boardNo, User user){

        Board board = getBoardByBoardNo(boardNo);

        if(user != null && !isBoardOwner(board,user)){
        board.setViewCount(board.getViewCount()+1);
        boardRepository.save(board);

        }
    }

    // 게시글 작성자와 로그인 사용자 검증
    public boolean isBoardOwner(Board board, User user){
        return board.getUser().getLoginId().equals(user.getLoginId());
    }

    // 특정 게시글 카테고리를 확인 후, 없다면 기본값 FREE로 설정(이미 존재하는 게시글에 적합)
    public Board getBoardDefaultCategory(int boardNo){

        Board board = getBoardByBoardNo(boardNo);

        if(board.getBoardCategory()== null || board.getBoardCategory().isEmpty()){
            board.setBoardCategory("FREE");
        }
        return board;
    }

    // 모든 게시글 카테고리를 Enum 반환
    public BoardCategory[] getAllBoardCategorys() {
        return BoardCategory.values();
    }

    // BoardCategory 변환 (String -> Enum)
    public BoardCategory getBoardCategoryEnum(String boardCategory) {
        if (boardCategory == null || boardCategory.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 값이 비어 있습니다.");
        }
        return BoardCategory.valueOf(boardCategory.toUpperCase());
    }

    // 특정 키테고리의 게시글 목록을 카테고리별 고유 번호와 함께 반환
    public List<BoardDto> getBoardWithCategoryNumbers(String boardCategory){

        List<Object[]> rows = boardRepository.findByCategoryWithRowNumber(boardCategory);

        List<BoardDto> boards = new ArrayList<>();
            for(Object[] row : rows) {
                BoardDto boardDto = new BoardDto();
                boardDto.setBoardCategory((String) row[0]);
                boardDto.setCategoryBoardNo(((Number) row[1]).intValue());
                boardDto.setBoardNo(((Number) row[2]).intValue());
                boardDto.setTitle((String) row[3]);
                boardDto.setContent((String) row[4]);
                boardDto.setNickname((String) row[5]);
                boardDto.setCreateDate(((Timestamp) row[6]).toLocalDateTime());

                // 댓글 수를 추가
                int commentCount = commentRepository.countByBoardBoardNo(boardDto.getBoardNo());
                boardDto.setCommentCount(commentCount);

                boards.add(boardDto);
            }
        boards.sort((board1, board2) -> Integer.compare(board2.getBoardNo(), board1.getBoardNo()));
        return boards;
    }

    // 오늘 날짜 새로운 게시글 수 계산
    public int countTodayBoardByCategory(String boardCategory){

        List<Board> boards = boardRepository.findByBoardCategoryOrderByBoardNoDesc(boardCategory);

        int todayBoardCount = 0;
        LocalDate today = LocalDate.now();

        for(Board board: boards){
            if(board.getCreateDate().toLocalDate().isEqual(today)){
                todayBoardCount++;
            }
        }
        return todayBoardCount;
    }

}
