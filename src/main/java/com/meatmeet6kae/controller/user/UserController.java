package com.meatmeet6kae.controller.user;

import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    // Logger 설정
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping(value = "getUsers")
    public String getUsers(@RequestParam("loginId") String loginId, Model model, HttpSession session) {

        logger.debug("getUsers called with loginId: {}", loginId); // 로그 찍기

        User currentUser = (User) session.getAttribute("user"); //세션에 로그인된 사용자 정보

        Optional<User> user = userService.getUserByLoginId(loginId); //loginId 사용자 정보 조회
        model.addAttribute("currentUser", currentUser); // 현재 로그인한 사용자 정보 (관리자)
        model.addAttribute("user", user); // 조회된 유저 정보

        return "users/getUser";
    }

    @GetMapping(value = "getUser")
    public String getUser(Model model, HttpSession session) {


        User currentUser = (User) session.getAttribute("user"); //세션에 로그인된 사용자 정보
        if(currentUser == null){ //로그인 안 된 경우 home으로 return
            return "redirect:/home";
        }

        logger.debug("getUser called with loginId: {}", currentUser.getLoginId()); // 로그 찍기

        model.addAttribute("user", currentUser); // 현재 로그인된 사용자 정보

        return "users/getUser";
    }


}