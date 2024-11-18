package com.meatmeet6kae.service.board;

import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.board.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        return boardRepository.findByBoardCategory(boardCategory);
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
        return boardRepository.findByBoardNo(boardNo).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    public Board updateBoard(Board board) {

        Board boards = getBoardByBoardNo(board.getBoardNo());
        boards.setTitle(board.getTitle());
        boards.setContent(board.getContent());
        boards.setUpdateDate(LocalDateTime.now());
        return boardRepository.save(boards);
    }

    public void deleteBoard(int boardNo){
        Board board = getBoardByBoardNo(boardNo);
        boardRepository.delete(board);
    }
}
