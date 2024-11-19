package com.meatmeet6kae.controller.home;

import com.meatmeet6kae.common.enums.BoardCategory;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.board.BoardService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public final BoardService boardService;

    public HomeController(BoardService boardService) {
        this.boardService = boardService;
    }


    @GetMapping("/")
    public String home(HttpSession session, Model model) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null){
            //로그인된 사용자 정보 전달
            model.addAttribute("user",currentUser);
        }

        //동적 active클래스 적용을 위한 변수
        model.addAttribute("page","home");

        logger.debug("home in");
        System.out.println("home in");

        return "home";
    }

    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null){
            //로그인된 사용자 정보 전달
            model.addAttribute("user",currentUser);
        }

        model.addAttribute("page","menu");
        return "navigation/menu";
    }

    @GetMapping("/location")
    public String location(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null){
            //로그인된 사용자 정보 전달
            model.addAttribute("user",currentUser);
        }
        model.addAttribute("page","location");
        return "navigation/location";
    }

    @GetMapping("/boardList")
    public String boardList(@RequestParam(value = "boardCategory", required = false)String boardCategory, HttpSession session, Model model){

        User user = (User)session.getAttribute("user");
        model.addAttribute("user",user);

        // 모든 열거형(ENUM 반환)
        BoardCategory[] boardCategories = BoardCategory.values();
        System.out.println("boardCategories"+boardCategories);
        System.out.println("Arrays.toString().boardCategories="+ Arrays.toString(boardCategories));
        for(BoardCategory boardCategorys: boardCategories){
            System.out.println("name="+boardCategorys.name()+", ordinal"+boardCategorys.ordinal());
        } // 모든 열거형 뷰로 전달
        model.addAttribute("boardCategories",boardCategories);

        // boardCategory가 없다면 기본값으로 FREE 설정
        if(boardCategory == null|| boardCategory.isEmpty()){
            boardCategory = "FREE";
        }
        // 선택된 카테고리는 String으로 넘어오기 때문에, valueOf로 Enum 변환(오류방지)
        BoardCategory category = BoardCategory.valueOf(boardCategory);
        model.addAttribute("category",category);

        List<Board> boards = boardService.getBoardsByCategory(boardCategory);

        // 새글
        int todayBoardCount = 0; //오늘의 게시글 초기화
        LocalDate today = LocalDate.now(); //오늘 날짜 가져오기
        for(Board board: boards) { // 게시글 전체 목록
            if (board.getCreateDate().toLocalDate().isEqual(today)) {
                todayBoardCount++;
            }
        }

        model.addAttribute("boards", boards);
        //model.addAttribute("boardCategory", boardCategory);
        model.addAttribute("boardCategoryName",category.getBoardCategoryName());
        model.addAttribute("boardDescription",category.getBoardDescription());
        model.addAttribute("page","boardList");
        model.addAttribute("todayBoardCount",todayBoardCount);
        model.addAttribute("totalBoardCount",boards.size());
        return "navigation/boardList";
    }

}
