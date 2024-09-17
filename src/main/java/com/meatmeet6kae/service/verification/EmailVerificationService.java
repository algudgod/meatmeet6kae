package com.meatmeet6kae.service.verification;

import com.meatmeet6kae.entity.verification.EmailVerification;
import com.meatmeet6kae.repository.verification.EmailVerificationRepository;
import com.meatmeet6kae.service.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class); // Logger 설정

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private JavaMailSender javaMailSender; // JavaMailSender 의존성 주입

    // 이메일 인증 항목을 생성하고 저장하는 메서드
    public EmailVerification createEmailVerification(String token) {
        logger.debug("Creating email verification with token: {}", token);

        // EmailVerification 인스턴스 생성 및 토큰 및 만료 시간 설정
        EmailVerification emailVerification = new EmailVerification(); // 이메일 인증 정보를 저장하기 위한 인스턴스 생성
        emailVerification.setToken(token); // 이메일 인증 링크를 클릭할 때 사용할 토큰
        emailVerification.setTokenExpiration(LocalDateTime.now().plusMinutes(3)); // 토큰의 만료 시간 설정 (3분 후)

        // EmailVerification 객체를 데이터베이스에 저장하고 반환
        return emailVerificationRepository.save(emailVerification);
    }

    // 토큰을 검증하고 이메일 인증 상태를 업데이트하는 메서드
    public boolean verifyEmailToken(String token) {
        logger.debug("Verifying email token: {}", token);

        // 토큰으로 EmailVerification 검색
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByToken(token);

        if (emailVerification.isPresent() && !emailVerification.get().isVerificationStatus()) {
            EmailVerification verification = emailVerification.get();

            // 토큰이 아직 만료되지 않은 경우
            if (verification.getTokenExpiration().isAfter(LocalDateTime.now())) {
                // 이메일 인증 상태를 true로 설정
                verification.setVerificationStatus(true);
                emailVerificationRepository.save(verification);
            }
            logger.debug("Email verification successful for token: {}", token);
            return true; // 인증 성공
        } else {
            // 로그: 토큰 만료됨 또는 이미 인증된 경우
            logger.debug("Token expired or already verified for token: {}", token);
        }
        return false;
    }

    // 이메일 인증 링크를 전송하는 메서드
    public void sendVerification(String email, String token) throws MessagingException {

        logger.debug("Sending verification email to: {}, with token: {}", email, token);

        // 이메일의 제목, 인증 완료할 수 있는 토큰이 포함된 링크, 본문 생성
        String subject = "MeatMeet6kae의 이메일 인증을 위한 메일입니다.";
        String confirmationUrl = "이메일 인증에 필요한 토큰입니다. 해당 토큰을 인증창에 입력하세요: " + token;
        String message = "안녕하세요, \n" + confirmationUrl;

        logger.debug("Email subject: {}, confirmationUrl: {}", subject, confirmationUrl);
        // 로그: 이메일 메세지 구성

        MimeMessage mimeMessage = javaMailSender.createMimeMessage(); // 이메일 메세지를 구성할 객체 생성
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setTo(email); // 수신자의 이메일 주소를 설정
        helper.setFrom("92mi_hyung@naver.com"); //발신자 설정,
        helper.setSubject(subject);
        helper.setText(message, true); // true는 HTML을 허용하는 것이며 메세지 본문에 HTML 태그를 포함할 수 있다.

        logger.debug("send email to: {}", email); // 로그: 이메일 전송 시도
        javaMailSender.send(mimeMessage); // 완성된 이메일을 전송
        logger.debug("Email send success: {}", email); // 로그: 이메일 전송 성공
    }

    // 이메일 인증 여부를 확인하는 메서드
    public boolean isEmailVerified(String token) {
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByToken(token);
        return emailVerification.isPresent() && emailVerification.get().isVerificationStatus();
    }
}
