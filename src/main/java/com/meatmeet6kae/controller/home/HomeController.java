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

    // 로그를 남기기 위한 Logger 객체 설정: DEBUG
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);


    @GetMapping("/")
    public String home(HttpSession session, Model model) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            // 로그인된 사용자 정보 전달
            model.addAttribute("user", currentUser);
        }
        logger.debug("home in");
        System.out.println("home in");
        return "home";  // home.html 템플릿 반환
    }


}
