package com.meatmeet6kae.service.board;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.imageFile.ImageFile;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.board.BoardRepository;
import com.meatmeet6kae.repository.comment.CommentRepository;
import com.meatmeet6kae.repository.imageFile.ImageFileRepository;
import com.meatmeet6kae.service.s3.S3Service;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final CommentRepository commnetRepository;
    private final ImageFileRepository imageFileRepository;
    private final S3Service s3Service;

    public BoardService(BoardRepository boardRepository, CommentRepository commnetRepository, ImageFileRepository imageFileRepository, S3Service s3Service)  {
        this.boardRepository = boardRepository;
        this.commnetRepository = commnetRepository;
        this.imageFileRepository = imageFileRepository;
        this.s3Service = s3Service;
    }

    // 특정 카테고리의 게시글을 내림차순으로 조회
    public List<Board>getBoardsByCategory(String boardCategory){
        return boardRepository.findByBoardCategoryOrderByBoardNoDesc(boardCategory);
    }

    // 게시글 생성
    public void addBoard(BoardDto boardDto, User user, List<MultipartFile> images) throws IOException {

        Board board = new Board();
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setBoardCategory(boardDto.getBoardCategory());
        board.setUser(user);
        boardRepository.save(board);

        // 이미지 개수 제한 검증
        if (images != null && images.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3개까지만 첨부할 수 있습니다.");
        }

        // 이미지 업로드 및 URL 저장
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(images); // S3에 업로드
            for (String imageUrl : imageUrls) {
                // ImageFile 엔티티 생성 및 저장
                ImageFile imageFile = new ImageFile();
                imageFile.setBoard(board); // 게시글과 연결
                imageFile.setImageUrl(imageUrl); // S3 URL 설정
                imageFileRepository.save(imageFile); // 데이터베이스에 저장
            }
        }

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

                int commentCount = commnetRepository.countByBoardBoardNo(boardDto.getBoardNo());
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
