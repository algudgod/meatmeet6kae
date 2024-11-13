package com.meatmeet6kae.controller.home;

import com.meatmeet6kae.entity.user.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 디버깅
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);


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
    public String boardMain(Model model){
        model.addAttribute("page","boardList");
        return "navigation/boardList";
    }

}
