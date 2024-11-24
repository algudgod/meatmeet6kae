package com.meatmeet6kae.controller.board;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.controller.user.UserController;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.board.BoardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {
    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시판 글쓰기 form을 보여주는 메서드
    @GetMapping("/addBoardForm")
    public String addBoardForm(@RequestParam("boardCategory")String boardCategory, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        BoardCategory categoryEnum = boardService.getBoardCategoryEnum(boardCategory);

        BoardDto boardDto = new BoardDto();
        boardDto.setBoardCategory(categoryEnum.toString());

        model.addAttribute("boardCategorys",boardService.getAllBoardCategorys());
        model.addAttribute("boardDto",boardDto);
        return "boards/addBoardForm";
    }

    @GetMapping("")
    public String getBoardList(@RequestParam("boardCategory")String boardCategory, HttpSession session, Model model){

        User user = (User)session.getAttribute("user");
        model.addAttribute("user", user);

        logger.debug("boardCategory: {}", boardCategory);
        model.addAttribute("boardCategory", boardCategory);

        List<Board> boards = boardService.getBoardsByCategory(boardCategory);
        model.addAttribute("boards", boards);

        return "navigation/boardList";
    }

    @PostMapping("addBoard")
    public String addBoard(@Valid BoardDto boardDto, BindingResult result, HttpSession session, Model model) {

        // 1. 세션에서 로그인 된 사용자의 정보를 가져옴
        User user = (User) session.getAttribute("user");

        logger.debug("boardDto: {}", boardDto);
        logger.debug("boardCategory from BoardDto: {}", boardDto.getBoardCategory());

        // 2. 입력된 데이터에 오류가 있는지 검증
        if (result.hasErrors()) {
            logger.debug("result error: {}", result.getAllErrors());
            model.addAttribute("boardCategorys", boardService.getAllBoardCategorys());
            model.addAttribute("user",user);
            model.addAttribute("error", "입력한 정보에 오류가 있습니다.");
            return "boards/addBoardForm";
        }

        // 3. boardCategory가 없으면 "FREE"로 설정
        if (boardDto.getBoardCategory() == null || boardDto.getBoardCategory().isEmpty()) {
            boardDto.setBoardCategory("FREE");
        }

        // 4. Service 레이어를 통해 게시글 저장
        boardService.addBoard(boardDto, user);

        // 5. 게시글 등록 후 해당 카테고리로 리다이렉트
        return "redirect:/boardList?boardCategory=" + boardDto.getBoardCategory();
    }

    //게시글 상세 조회
    @GetMapping("{boardNo}")
    public String getBoardDetail(@PathVariable int boardNo, HttpSession session, Model model){
        try {
            User user = (User)session.getAttribute("user");
            model.addAttribute("user",user);

            // 조회수 증가
            boardService.updateViewCount(boardNo,user);
            //게시글 정보 가져오기
            Board board = boardService.getBoardDefaultCategory(boardNo);
            model.addAttribute("board",board);
            // 카테고리 정보 추가
            model.addAttribute("boardCategorys",boardService.getAllBoardCategorys());
            model.addAttribute("boardCategory", boardService.getBoardCategoryEnum(board.getBoardCategory()));

            return "boards/boardDetail";

        }catch (IllegalArgumentException e){
            model.addAttribute("errorMessage",e.getMessage());

            return "errors/boardNullErrorPage";
        }
    }

    // 게시글 수정 폼을 보여주는 메서드
    @GetMapping("/editBoardForm/{boardNo}")
    public String editBoardForm(@PathVariable int boardNo, HttpSession session, Model model) {
        // 1. 사용자 세션 확인
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login"; // 로그인되지 않은 사용자는 로그인 페이지로 이동
        }

        // 2. 게시글 조회
        Board board = boardService.getBoardByBoardNo(boardNo);
        if (board == null) {
            return "redirect:/boardList"; // 게시글이 없으면 게시판 목록으로 이동
        }

        // 3. 작성자 확인
        if (!user.getLoginId().equals(board.getUser().getLoginId())) {
            return "redirect:/boardList"; // 작성자가 아니면 목록으로 이동
        }

        // 4. View에 데이터 전달
        BoardDto boardDto = new BoardDto();
        boardDto.setBoardNo(board.getBoardNo());
        boardDto.setTitle(board.getTitle());
        boardDto.setContent(board.getContent());
        boardDto.setBoardCategory(board.getBoardCategory());

        String boardCategoryName = BoardCategory.valueOf(board.getBoardCategory()).getBoardCategoryName();
        model.addAttribute("boardCategoryName", boardCategoryName); // 카테고리 이름 전달

        model.addAttribute("board", boardDto);
        model.addAttribute("boardCategory", boardService.getAllBoardCategorys());

        return "boards/editBoardForm"; // 수정 폼 페이지
    }

    // 게시글 수정 메서드
    @PostMapping("/updateBoard")
    public String updateBoard(@Valid @ModelAttribute BoardDto boardDto, BindingResult result, HttpSession session, Model model) {
        // 1. 사용자 세션 확인
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/login"; // 로그인되지 않은 사용자는 로그인 페이지로 이동
        }

        // 2. 게시글 조회 및 작성자 확인
        Board board = boardService.getBoardByBoardNo(boardDto.getBoardNo());
        if (board == null || !user.getLoginId().equals(board.getUser().getLoginId())) {
            return "redirect:/boardList"; // 게시글이 없거나 작성자가 아니면 목록으로 이동
        }

        // 3. 유효성 검사
        if (result.hasErrors()) {
            logger.debug("result error: {}", result.getAllErrors());
            System.out.println("vail failllllllllllllllll"+ boardDto.toString());
            model.addAttribute("user", user); // 로그인 사용자 정보 유지
            model.addAttribute("board", boardDto); // 유효성 검사가 실패하면 폼 데이터 유지
            model.addAttribute("boardCategory", boardService.getAllBoardCategorys());
            model.addAttribute("errors", result.getAllErrors()); // 에러를 모델에 추가

            // 카테고리 이름을 다시 모델에 추가
            String boardCategoryName = BoardCategory.valueOf(boardDto.getBoardCategory()).getBoardCategoryName();
            model.addAttribute("boardCategoryName", boardCategoryName);

            return "boards/editBoardForm"; // 수정 폼으로 다시 이동
        }

        // 4. 게시글 수정
        System.out.println("updateBoard good: " + boardDto);
        boardService.updateBoard(boardDto);

        // 5. 수정 완료 후 상세보기 페이지로 이동
        return "redirect:/boards/" + boardDto.getBoardNo();
    }

    //게시글 삭제 메서드
    @PostMapping("/deleteBoard/{boardNo}")
    public String deleteBoard(@PathVariable int boardNo, HttpSession session, Model model){

        try {
            User user = (User) session.getAttribute("user");

            // 예외: 사용자가 로그인 되지 않은 경우
            if (user == null) {
                return "redirect:/users/login";
            }

            // 게시글 삭제
            boardService.deleteBoard(boardNo, user);
            // 게시글 삭제 성공 시 목록으로 리다이렉트
            return "redirect:/boardList";
        }catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "errors/boardNullErrorPage";
        }
    }


}
