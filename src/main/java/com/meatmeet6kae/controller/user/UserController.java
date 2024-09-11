package com.meatmeet6kae.controller.user;

import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // 회원 가입 폼을 보여주는 메서드
    @GetMapping("/addUserForm")
    public String showAddUserForm() {
        return "users/addUserForm";  // 회원 가입 폼 페이지
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User newUser, Model model) {
        userService.addUser(newUser);
        model.addAttribute("user",newUser);
        return "users/addUser";
    }

    //로그인 폼을 보여주는 메서드
    @GetMapping("/login")
    public String showLoginForm() {
        return "users/login";  // templates/users/login.html 파일을 반환
    }

    @PostMapping("/login")
    public String login( @RequestParam("loginId") String loginId,
                         @RequestParam("password") String password,
                         HttpSession session, Model model) {

        User user = userService.login(loginId, password);  // UserService의 login 메서드 호출
        if (user != null) {
            // 로그인 성공 - 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            return "redirect:/home";  // 로그인 성공 후 홈으로 리다이렉트
        } else {
            // 로그인 실패 - 에러 메시지 전달
            model.addAttribute("error", "Invalid login ID or password");
            return "users/login";  // 로그인 폼으로 다시 이동
        }
    }

}