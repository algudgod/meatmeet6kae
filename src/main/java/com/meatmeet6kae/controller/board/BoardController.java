package com.meatmeet6kae.controller.board;

import com.meatmeet6kae.controller.user.UserController;
import com.meatmeet6kae.dto.board.BoardDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.board.BoardService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String addBoardForm(@RequestParam("boardCode")String boardCode, Model model) {
        BoardDto boardDto = new BoardDto();
        boardDto.setBoardCode(boardCode);
        model.addAttribute("boardDto",boardDto);
        return "boards/addBoardForm";
    }

    @GetMapping("")
    public String getBoardList(@RequestParam("boardCode")String boardCode, HttpSession session, Model model){

        //세션에서 로그인 된 사용자의 정보를 가져옴(header의 동적 로그인/로그아웃)
        User user = (User)session.getAttribute("user");
        // 디버깅 로그 추가
        if (user != null) {
            logger.debug("Logged-in user: {}", user.getLoginId());
        } else {
            logger.debug("No user found in session");
        }
        model.addAttribute("user", user);

        logger.debug("boardCode: {}", boardCode);
        List<Board> boards = boardService.getBoardsByCode(boardCode);
        model.addAttribute("boards", boards);
        model.addAttribute(("boardCode"), boardCode);
        return "navigation/boardList";
    }

    @PostMapping("addBoard")
    public String addBoard(BoardDto boardDto, BindingResult result, HttpSession session, Model model){

        //1.세션에서 로그인 된 사용자의 정보를 가져옴
        User user = (User)session.getAttribute("user");
        //2. 로그인을 하지 않은 사용자에 대한 글쓰기 제한
        if (user == null) {
            return "redirect:/users/login"; // 로그인 페이지로 리다이렉트
        }

        logger.debug("boardDto: {}", boardDto);
        logger.debug("Board Code from BoardDto: {}", boardDto.getBoardCode());
        logger.debug(": {}", boardDto);

        if(result.hasErrors()) {
            logger.debug("result error: {}", result.getAllErrors());
            model.addAttribute("error","입력한 정보에 오류가 있습니다.");
            return "boards/addBoardForm";
        }
        // 입력된 데이터에 오류가 없을 시 입력된 정보를 저장 -> service
        boardService.addBoard(boardDto);
        return "navigation/boardList";
    }

}
