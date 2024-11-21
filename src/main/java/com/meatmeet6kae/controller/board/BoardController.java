package com.meatmeet6kae.controller.board;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.controller.user.UserController;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.board.BoardService;
import jakarta.servlet.http.HttpSession;
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

    //BoardService 로직 호출
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시판 글쓰기 form을 보여주는 메서드
    @GetMapping("/addBoardForm")
    public String addBoardForm(@RequestParam("boardCategory")String boardCategory, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        //게시글 작성 폼에서 사용될 데이터를 저장하는 새로운 Dto 객체 생성
        BoardDto boardDto = new BoardDto();
        //(String -> Eunm으로 BoardCategory를 변환하여 categoryEnum에 담음.
        BoardCategory categoryEnum = boardService.getBoardCategoryEnum(boardCategory);
        //boardDto의 boardCategory가 String이기때문에 Enum을 직접 대입할 수 없어 toString() 또는 name()을 사용하여 문자열로 변환.
        boardDto.setBoardCategory(categoryEnum.toString());

        model.addAttribute("boardCategorys",boardService.getAllBoardCategorys()); // 전체 게시판 카테고리 목록
        model.addAttribute("boardCategory",categoryEnum); // 사용자가 요청한 카테고리를 Enum으로 변환.

        model.addAttribute("boardDto",boardDto);
        return "boards/addBoardForm";
    }

    @GetMapping("")
    public String getBoardList(@RequestParam("boardCategory")String boardCategory, HttpSession session, Model model){

        //세션에서 로그인 된 사용자의 정보를 가져옴(header의 동적 로그인/로그아웃)
        User user = (User)session.getAttribute("user");
        // 디버깅 로그 추가
        if (user != null) {
            logger.debug("Logged-in user: {}", user.getLoginId());
        } else {
            logger.debug("No user found in session");
        }
        model.addAttribute("user", user);

        logger.debug("boardCategory: {}", boardCategory);

        List<Board> boards = boardService.getBoardsByCategory(boardCategory);
        model.addAttribute("boards", boards);
        model.addAttribute(("boardCategory"), boardCategory);
        return "navigation/boardList";
    }

    @PostMapping("addBoard")
    public String addBoard(BoardDto boardDto, BindingResult result, HttpSession session, Model model) {

        // 1. 세션에서 로그인 된 사용자의 정보를 가져옴
        User user = (User) session.getAttribute("user");

        logger.debug("boardDto: {}", boardDto);
        logger.debug("boardCategory from BoardDto: {}", boardDto.getBoardCategory());

        // 2. 입력된 데이터에 오류가 있는지 검증
        if (result.hasErrors()) {
            logger.debug("result error: {}", result.getAllErrors());
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

        User user = (User)session.getAttribute("user");
        model.addAttribute("user",user);

        //게시글 상세 조회 작동 시, 조회수 +1;
        boardService.updateViewCount(boardNo,user);

        Board board = boardService.getBoardDefaultCategory(boardNo);
        model.addAttribute("board",board);

        model.addAttribute("boardCategorys",boardService.getAllBoardCategorys());
        model.addAttribute("boardCategory", boardService.getBoardCategoryEnum(board.getBoardCategory()));

        return "boards/boardDetail";
    }

    //게시글 수정 폼을 보여주는 메서드
    @GetMapping("editBoard/{boardNo}")
    public String editBoard(@PathVariable int boardNo, HttpSession session, Model model){

        User user = (User)session.getAttribute("user");
        model.addAttribute("user",user);

        // 예외: 사용자가 로그인 되지 않은 경우
        if (user == null) {
            return "redirect:/users/login";
        }

        // 게시글 조회
        Board boards = boardService.getBoardByBoardNo(boardNo);

        // 예외: 게시글이 없을 경우
        if (boards == null) {
            return "redirect:/boardList";
        }

        // 예외: 작성자와 세션의 loginId가 일치하지 않는 경우
        if (!user.getLoginId().equals(boards.getUser().getLoginId())) {
            return "redirect:/boardList";
        }

        model.addAttribute("boards",boards);

        return "boards/editBoard";
    }

    //게시글 수정 메서드
    @PostMapping("updateBoard")
    public String updateBoard(@ModelAttribute BoardDto boardDto, HttpSession session, Model model){

        User user = (User)session.getAttribute("user");
        // 유효성 검사: 사용자가 로그인하지 않은 경우
        if (user == null) {
            return "redirect:/users/login";
        }

        Board boards = boardService.getBoardByBoardNo(boardDto.getBoardNo());
        // 유효성 검사: 게시글이 없거나 작성자와 로그인한 사용자 비교
        if(boards ==null || !user.getLoginId().equals(boards.getUser().getLoginId())){
            return "redirect:/boardList";
        }

        boardService.updateBoard(boardDto);
        // 수정 완료 후 , 게시글 상세조회 메서드 호출, boardNo으로 바인딩
        return "redirect:/boards/"+boardDto.getBoardNo();
    }

    //게시글 삭제 메서드
    @PostMapping("/deleteBoard/{boardNo}")
    public String deleteBoard(@PathVariable int boardNo, HttpSession session){

        User user = (User)session.getAttribute("user");

        // 예외: 사용자가 로그인 되지 않은 경우
        if (user == null) {
            return "redirect:/users/login";
        }

        // 게시글 삭제
        boardService.deleteBoard(boardNo, user);
        // 게시글 삭제 성공 시 목록으로 리다이렉트
        return "redirect:/boardList";
    }

}
