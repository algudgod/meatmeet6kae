package com.meatmeet6kae.controller.user;

import com.meatmeet6kae.dto.user.UserDTO;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.service.user.UserService;
import com.meatmeet6kae.service.verification.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller //Spring MVC 패턴을 사용한 사용자 관리 컨트롤러
@RequestMapping("/users") //모든 메서드는 url이 /users로 매핑되어 시작한다.
public class UserController {

    // 로그를 남기기 위한 Logger 객체 설정: DEBUG
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    //UserService 주입: 서비스 계층 로직 호출
    @Autowired
    private UserService userService;
    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping(value = "getUsers")
    public String getUsers(@RequestParam("loginId") String loginId, Model model, HttpSession session) {

        logger.debug("getUsers called with loginId: {}", loginId); // DEBUG:

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
            return "redirect:/";
        }
        logger.debug("getUser called with loginId: {}", currentUser.getLoginId()); // 로그 찍기

        model.addAttribute("user", currentUser); // 현재 로그인된 사용자 정보

        return "users/getUser";
    }

    // 회원 가입 폼을 보여주는 메서드
    @GetMapping("/addUserForm")
    public String showAddUserForm() {
        return "users/addUserForm";  // 회원 가입 폼.html
    }

    // 사용자를 추가하는 메서드 -05리펙토링_0918
    @PostMapping("/addUser")
    public String addUser(@ModelAttribute @Valid UserDTO userDTO, BindingResult bindingResult, Model model,
                          @RequestParam Map<String, String> params) { //html에 있는 우편정보 가져오기 위한 Map_0919

        // 데이터 확인을 위해 로그 추가
        logger.debug("UserDTO: {}", userDTO);
        logger.debug("Params: {}", params);

        if (bindingResult.hasErrors()) {
            logger.debug("BindingResult errors: {}", bindingResult.getAllErrors());

            // 검증 실패한 필드와 메시지를 모델에 추가하여 화면에 표시
            model.addAttribute("error", "입력한 정보에 오류가 있습니다.");
            // 유효성 검사에 실패한 경우, 오류 메시지와 함께 가입 폼으로 다시 이동
            return "users/addUserForm";
        }
        try {
            // DTO를 엔티티로 변환
            User newUser = new User();
            newUser.setLoginId(userDTO.getLoginId());
            newUser.setPassword(userDTO.getPassword());
            newUser.setName(userDTO.getName());
            newUser.setEmail(userDTO.getEmail()); //생성한 이메일 주소를 설정

            //존재하는 addr을 위해 @RequestParam 하여 값 불러오기
            newUser.setAddr(params.get("addr")); //주소
            String postcode = params.get("postcode"); //우편번호
            String addrDetail = params.get("addrDetail"); //상세주소
            String addrExtraAddress = params.get("addrExtraAddress"); // 참고항목

            // 로그로 확인
            logger.debug("Addr: {}", newUser.getAddr());
            logger.debug("Postcode: {}", postcode);
            logger.debug("AddrDetail: {}", addrDetail);
            logger.debug("AddrExtraAddress: {}", addrExtraAddress);

            // 사용자 추가 메서드 호출
            User savedUser = userService.addUser(newUser);
            model.addAttribute("user", savedUser); // 추가된 사용자 정보를 모델에 추가
            model.addAttribute("postcode", postcode);
            model.addAttribute("addrDetail", addrDetail);
            model.addAttribute("addrExtraAddress", addrExtraAddress);

            logger.debug("User added successfully: {}", savedUser);
            logger.debug("Model attributes set: user={}, postcode={}, addrDetail={}, addrExtraAddress={}",
                    savedUser, postcode, addrDetail, addrExtraAddress);


            return "users/addUser"; // 회원가입 완료 페이지로 이동
        } catch (IllegalArgumentException e) {
            // 이메일 중복 등으로 인해 오류 발생 시
            model.addAttribute("error", e.getMessage()); // 에러 메시지를 모델에 추가
            return "users/addUserForm"; // 가입 폼으로 다시 이동
        }
    }

    // 로그인 폼을 보여주는 메서드
    @GetMapping("/login")
    public String showLoginForm() {
        return "users/login";  // templates/users/login.html 파일을 반환
    }

    // 로그인
    @PostMapping("/login")
    public String login( @RequestParam("loginId") String loginId,
                         @RequestParam("password") String password,
                         HttpSession session, Model model) {

        User user = userService.login(loginId, password);  // UserService의 login 메서드 호출
        if (user != null) {
            // loginId 사용여부를 확인
            if("N".equals(user.getUseYn())) {
                logger.debug("Login deactivated user [{}]", loginId);
                model.addAttribute("error", "비활성화된 계정입니다. 관리자에게 문의하세요.");
                return "users/login";
            }
            // 로그인 성공 - 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            return "redirect:/";  // 로그인 성공 후 홈으로 리다이렉트
        } else {
            // 로그인 실패 - 에러 메시지 전달
            model.addAttribute("error", "ID 또는 PASSWORD가 잘못 입력 되었습니다.");
            return "users/login";  // 로그인 폼으로 다시 이동
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 현재 로그인된 사용자 정보 확인
        User currentUser = (User) session.getAttribute("user");

        // 로그아웃 전 사용자 정보를 로그로 찍어보기.
        if (currentUser != null) {
            logger.debug("User [{}] is logging out.", currentUser.getLoginId());
        } else {
            logger.debug("Not found user");
        }

        // 세션 무효화 (로그아웃 처리)
        session.invalidate();
        // 로그아웃 후 세션 무효화 로그
        logger.debug("Session invalidated. logout.");

        return "redirect:/";  // 로그아웃 후 홈 페이지로 리다이렉트
    }

    //내 정보 보기 폼을 보여주는 메서드
    @GetMapping("/showUserInfo")
    public String showUserInfo(HttpSession session, Model model, HttpServletRequest request) {

        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user",user);
        model.addAttribute("currentUrl", request.getRequestURI());
        return "users/showUserInfo";  // templates/users/showUserInfo.html 파일을 반환
    }

    // 내 정보 수정 폼을 보여주는 메서드
    @GetMapping("/updateUserForm")
    public String updateUserForm(HttpSession session, Model model){
        User user = (User) session.getAttribute("user");
        if(user == null){
            return "redirect:/users/login";
        }
        model.addAttribute("user",user);
        return "users/updateUserForm";  // templates/users/updateUserForm.html 파일을 반환
    }

    // 내 정보 수정
    @PostMapping("/updateUser")
    public String updateUser(@ModelAttribute User updatedUser, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        // 폼에서 받아온 데이터 로그 확인 (loginId)
        logger.debug("폼에서 받은 데이터 - loginId: {}", updatedUser.getLoginId());

        if (currentUser == null) {
            logger.warn("현재 세션에 사용자가 존재하지 않습니다.");
            return "redirect:/users/login";
        }
        // 폼에서 받아온 데이터를 로그로 출력
        logger.debug("updatedUser.getName()", updatedUser.getName());
        logger.debug("updatedUser.getAddr()", updatedUser.getAddr());
        logger.debug("updatedUser.getGender()", updatedUser.getGender());
        logger.debug("updatedUser.getEmail()", updatedUser.getEmail());
        logger.debug("updatedUser.getLoginId()", updatedUser.getLoginId());

        // 사용자 정보를 업데이트
        currentUser.setName(updatedUser.getName());
        currentUser.setGender(updatedUser.getGender());
        currentUser.setAddr(updatedUser.getAddr());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setEmailYn(updatedUser.getEmailYn());

        // DB에 업데이트
        User updated = userService.updateUser(currentUser);

        // 세션에 변경된 사용자 정보 반영
        session.setAttribute("user", updated);
        logger.debug("updated", session.getAttribute("user"));

        return "redirect:/users/showUserInfo";
    }

    //아이디 사용여부 변경
    @PostMapping("/deactivate")
    public String deactivateUser(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            return "redirect:/users/login";
        }
/*      서비스로 로직 이동
        // 탈퇴 처리: useYn을 'N'으로 설정
        currentUser.setUseYn("N");
        userService.updateUser(currentUser); // DB에 업데이트*/
        userService.deactivateUser(currentUser.getLoginId());

        // 세션에서 사용자 정보 제거 (로그아웃 처리)
        session.invalidate();

        return "redirect:/";  // 탈퇴 후 홈으로 리다이렉트
    }

    // Id 중복 확인
    @GetMapping("/checkLoginId")
    @ResponseBody
    public boolean checkLoginId(@RequestParam("loginId") String loginId) {
        return userService.existsByLoginId(loginId);
    }

    //ID찾기 폼을 보여주는 메서드
    @GetMapping("/findUserIdForm")
    public String findUserIdForm() {
        return "users/findUserId";
    }

}