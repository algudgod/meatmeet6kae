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

    // 특정 카테고리 값을 기준으로 필터링하여 게시글 내림차순 조회
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

        if(user != null && !board.getUser().getLoginId().equals(user.getLoginId()))
        board.setViewCount(board.getViewCount()+1);
    }

    // 게시글 카테고리 검증 및 기본값 설정
    // 목적: 특정 게시글 하나를 조회할 때, 해당 게시글의 카테고리가 null이면 FREE로 설정. Board 객체에만 적용.
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

    // 특정 카테고리 별로 고유한 번호가 매겨진 게시글을 조회
    public List<BoardDto> getBoardWithCategoryNumbers(String boardCategory){
        // 레포지토리에서 List<Object[]> 형태로 반환하는 findByCategoryWithRowNumber을 사용하기 위해 호출
        List<Object[]> rows = boardRepository.findByCategoryWithRowNumber(boardCategory);
        /* Object[] 배열로 담겨있는 여러개의 데이터를 필드명으로 명확하게 보기 위해 Dto 객체로 변환.
        변환된 객체를 한 곳에 모아 저장해야하는데, 빈 저장소를 만들어 준비.
        반복문으로 List<Object[]> 데이터를 하나씩 꺼내서 Dto로 변환. set으로 저장할 데이터 넣기.
            List<Object[]>는
               [0]: boardCategory
               [1]: categoryBoardNo (ROW_NUMBER로 생성된 카테고리별 글 번호)
               [2]: boardNo (전체 고유 글 번호)
               [3]: title
               [4]: content
        */
        List<BoardDto> boards = new ArrayList<>(); //빈 저장소
        for(Object[] row : rows) {
            //데이터 타입 한 번 확인
/*            System.out.println("row[0]: " + row[0].getClass().getName()); // boardCategory
            System.out.println("row[1]: " + row[1].getClass().getName()); // categoryBoardNo
            System.out.println("row[2]: " + row[2].getClass().getName()); // boardNo
            System.out.println("row[3]: " + row[3].getClass().getName()); // title
            System.out.println("row[4]: " + row[4].getClass().getName()); // content
            System.out.println("row[5]: " + row[5].getClass().getName()); // nickname*/

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
        // Dto객체 오름차순으로 정렬
        //sort()사용법. board2의 boardNo와 board1의 boardNo를 비교하여 board2가 board1보다 크면 (즉, 내림차순 정렬) 앞에 오도록 설정
        boards.sort((board1, board2) -> Integer.compare(board2.getBoardNo(), board1.getBoardNo()));
        return boards;
    }


}
