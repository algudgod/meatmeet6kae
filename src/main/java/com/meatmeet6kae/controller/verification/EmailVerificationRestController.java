package com.meatmeet6kae.controller.verification;

import com.meatmeet6kae.entity.verification.EmailVerification;
import com.meatmeet6kae.service.user.UserService;
import com.meatmeet6kae.service.verification.EmailVerificationService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailVerificationRestController {

    // 로그를 남기기 위한 Logger 객체 설정: DEBUG
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationRestController.class);

    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private UserService userService;

    // 이메일 인증 요청
    @PostMapping("/send")
    public Map<String, Object> sendVerificationEmail(@RequestBody Map<String, String> emailData) {
        Map<String, Object> response = new HashMap<>(); // 응답을 저장할 해시맵 생성

        String email = emailData.get("email"); // 요청 본문에서 이메일 가져와서 email에 저장
        logger.debug("Received email verification request for email: {}", email); // DEBUG: 파라미터로 가져온 이메일 확인

        //이메일 중복 확인 로직 추가 -01리펙토링_0917
        if (userService.existsByEmail(email)) { // 이미 사용 중인 이메일인지 확인
            logger.debug("Email '{}' is already in use.", email); // DEBUG: 중복된 이메일 확인되면 콘솔에 출력
            response.put("message", "이미 가입된 이메일입니다."); // 중복된 이메일일 때 메시지 설정
            response.put("success", false); // 중복 시 성공 여부를 false로 설정
            return response; // 중복된 이메일이므로 메서드를 종료하고 응답을 반환
        }
        // 서비스에서 생성된 토큰 가져와서 token에 저장
        EmailVerification emailVerification = emailVerificationService.createEmailVerification();
        String token = emailVerification.getToken();

        //이메일 전송중에 발생할 수 있는 예외를 직접 처리하기 위해 try-catch 사용
        try {
            // 이메일 인증 링크를 포함하여 이메일을 전송
            emailVerificationService.sendVerification(email, token);
            response.put("message", "Verification email sent. Please check your inbox."); // 이메일 전송 성공 메시지 설정
            response.put("success", true); // 성공 여부를 true로 설정
            logger.debug("Verification email successfully sent to: {}", email); // DEBUG: 이메일 전송 성공
        } catch (MessagingException e) { // 이메일 전송 중 예외가 발생하면
            e.printStackTrace(); // 예외 스택 트레이스를 출력
            response.put("message", "Failed to send verification email."); // 이메일 전송 실패 메시지
            response.put("success", false); // 실패 여부를 false로 설정
        }
        // 최종 응답 반환
        return response;
    }

    // 토큰을 확인하고 이메일 인증을 완료하는 메서드
    @GetMapping("/confirm") // GET 요청을 "/email/confirm"으로 매핑
    public Map<String, Object> confirmEmail(@RequestParam String token) { // 요청 파라미터로 토큰 받기
        Map<String, Object> response = new HashMap<>(); // 응답을 저장할 맵 생성

        // DEBUG: 이메일 인증 확인 요청 수신
        logger.debug("Received email verification confirmation for token: {}", token);

        // 토큰을 검증하여 이메일 인증 시도
        boolean isVerified = emailVerificationService.verifyEmailToken(token);

        if (isVerified) { // 인증이 성공하면
            response.put("message", "Email verification successful."); // 응답 메시지에 성공 메시지 추가
            response.put("success", true); // 성공 여부를 응답에 추가

            logger.debug("Email verification successful for token: {}", token); // DEBUG: 이메일 인증 성공
        } else { // 인증이 실패하면
            response.put("message", "Email verification failed."); // 응답 메시지에 실패 메시지 추가
            response.put("success", false); // 실패 여부를 응답에 추가

            logger.debug("Email verification failed for token: {}", token); // DEBUG: 이메일 인증 실패
        }
        return response;
    }
}
