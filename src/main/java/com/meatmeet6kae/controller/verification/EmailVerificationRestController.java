package com.meatmeet6kae.controller.verification;

import com.meatmeet6kae.service.user.UserService;
import com.meatmeet6kae.service.verification.EmailVerificationService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/email")
public class EmailVerificationRestController {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationRestController.class); // Logger 설정

    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private UserService userService;

    // 이메일 인증 요청
    @PostMapping("/send")
    public Map<String, Object> sendVerificationEmail(@RequestBody Map<String, String> request) { // @RequestBody를 사용하여 이메일주소 가져오기
        Map<String, Object> response = new HashMap<>(); // 응답을 저장할 해시맵 생성

        String email = request.get("email"); // 요청 본문에서 이메일 가져오기
        logger.debug("Received email verification request for email: {}", email); //확인 완료.

        //이메일 중복 확인 로직 추가 -0917-addUserForm의 ajax와 연결
        if (userService.existsByEmail(email)) { // 이미 사용 중인 이메일인지 확인
            logger.debug("Email '{}' is already in use. 여기도 메일 확인", email); // 로그 추가
            response.put("message", "이미 가입된 이메일입니다."); // 중복된 이메일인 경우 메시지 설정
            response.put("success", false); // 중복 시 성공 여부를 false로 설정
            return response; // 중복된 이메일이므로 메서드를 종료하고 응답을 반환합니다.
        }

        // 토큰 생성 및 저장
        String token = UUID.randomUUID().toString(); // 고유한 토큰을 생성하고
        emailVerificationService.createEmailVerification(token); // 생성된 토큰을 이메일 인증 정보에 생성 및 저장한다.

        // 이메일 전송
        try {
            emailVerificationService.sendVerification(email, token);
            response.put("message", "Verification email sent. Please check your inbox.");
            response.put("success", true); // 성공 여부

            // 로그: 이메일 전송 성공
            logger.debug("Verification email successfully sent to: {}", email);
        } catch (MessagingException e) { // 이메일 전송 중 예외가 발생하면
            e.printStackTrace(); // 예외 스택 트레이스를 출력
            response.put("message", "Failed to send verification email."); // 이메일 전송 실패 메시지
            response.put("success", false); // 실패 여부
        }
        return response;
    }

    // 토큰을 확인하고 이메일 인증을 완료
    @GetMapping("/confirm") // GET 요청을 "/email/confirm"로 매핑합니다.
    public Map<String, Object> confirmEmail(@RequestParam String token) { // 요청 파라미터로 토큰받기
        Map<String, Object> response = new HashMap<>(); // 응답을 저장할 해시맵을 생성

        // 로그: 이메일 인증 확인 요청 수신
        logger.debug("Received email verification confirmation for token: {}", token);

        boolean isVerified = emailVerificationService.verifyEmailToken(token); // 토큰을 검증하여 이메일 인증 시도
        if (isVerified) { // 인증이 성공하면
            response.put("message", "Email verification successful."); // 성공 메시지
            response.put("success", true); // 성공 여부를 응답에 추가합니다.

            // 로그: 이메일 인증 성공
            logger.debug("Email verification successful for token: {}", token);
        } else { // 인증이 실패하면
            response.put("message", "Email verification failed."); // 실패 메시지
            response.put("success", false); // 실패 여부

            // 로그: 이메일 인증 실패
            logger.debug("Email verification failed for token: {}", token);
        }
        return response;
    }
}
