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
import java.time.LocalDateTime;
import java.util.List;


@Service
public class BoardService {

    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 전체 게시글 조회
    public  List<Board>getAllBoards(){
        return boardRepository.findAll();
    }

    public List<Board>getBoardsByCategory(String boardCategory){
        return boardRepository.findByBoardCategoryOrderByBoardNoDesc(boardCategory);
    }

    // 게시글 생성
    public void addBoard(BoardDto boardDto, User user){

        //boardDto를 엔티티로 변환하여 저장.
        Board board = new Board();
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setBoardCategory(boardDto.getBoardCategory());
        board.setUser(user);
        boardRepository.save(board);
    }

    public Board getBoardByBoardNo(int boardNo) {
        return boardRepository.findByBoardNo(boardNo).orElse(null);
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

        // 유효성 검사: 게시글이 없거나 작성자와 로그인한 사용자 비교
        if (board == null) {
            return;
        }
        if (!board.getUser().getLoginId().equals(user.getLoginId())){
            return;
        }
        boardRepository.delete(board);
    }

    // 게시글 조회수 계산
    @Transactional
    public void updateViewCount(int boardNo, User user){
        Board board = getBoardByBoardNo(boardNo);

        if(user == null || !board.getUser().getLoginId().equals(user.getLoginId()))
        board.setViewCount(board.getViewCount()+1);
    }

    // 게시글 카테고리 검증 및 기본값 설정
    public Board getBoardDefaultCategory(int boardNo){
        Board board = getBoardByBoardNo(boardNo);

        //카테고리 기본 값 설정
        if(board.getBoardCategory()== null || board.getBoardCategory().isEmpty()){
            board.setBoardCategory("FREE");
        }
        return board;
    }
    // 모든 Enum BoardCategory
    public BoardCategory[] getAllBoardCategorys() {
        return BoardCategory.values();
    }
    // BoardCategory 변환 (String -> Enum)
    public BoardCategory getBoardCategoryEnum(String boardCategory) {
        return BoardCategory.valueOf(boardCategory);
    }
}
