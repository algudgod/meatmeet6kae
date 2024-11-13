package com.meatmeet6kae.service.board;

import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.repository.board.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    /* 디버깅:
       로직의 흐름을 추적하고 특정 연산의 결과를 확인하고자 함. */
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);
    //
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }


    public List<Board>getBoardsByCode(String boardCode){

        return boardRepository.findByBoardCode(boardCode);
    }

    // 게시글 생성
    public void addBoard(BoardDto boardDto){

        //boardDto를 엔티티로 변환하여 저장.
        Board board = new Board();
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setBoardCode(boardDto.getBoardCode());
        boardRepository.save(board);
    }



}
