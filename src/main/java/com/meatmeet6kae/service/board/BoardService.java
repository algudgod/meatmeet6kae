package com.meatmeet6kae.service.board;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.board.BoardRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class BoardService {

    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final BoardRepository boardRepository;
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
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
    public Board updateBoard(BoardDto boardDto) {

        Board boards = getBoardByBoardNo(boardDto.getBoardNo());

        // 게시글 수정 내용
        boards.setTitle(boardDto.getTitle());
        boards.setContent(boardDto.getContent());
        boards.setUpdateDate(LocalDateTime.now());
        return boardRepository.save(boards);
    }

    //게시글 삭제
    public void deleteBoard(int boardNo, User user){

        Board board = getBoardByBoardNo(boardNo);

        // 작성자와 로그인 사용자 검증
        if (!board.getUser().getLoginId().equals(user.getLoginId())){
            return;
        }
        boardRepository.delete(board);
    }

    // 게시글 조회수 증가
    @Transactional
    public void updateViewCount(int boardNo, User user){

        Board board = getBoardByBoardNo(boardNo);

        if(user != null && !board.getUser().getLoginId().equals(user.getLoginId())){
        board.setViewCount(board.getViewCount()+1);
        }
    }

    // 게시글 카테고리를 기본값 FREE로 설정
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
        return BoardCategory.valueOf(boardCategory);
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

                boards.add(boardDto);
            }
        boards.sort((board1, board2) -> Integer.compare(board2.getBoardNo(), board1.getBoardNo()));
        return boards;
    }


}
