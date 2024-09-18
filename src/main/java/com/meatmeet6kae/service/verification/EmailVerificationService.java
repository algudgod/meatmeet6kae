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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    // 로그를 남기기 위한 Logger 객체 설정: DEBUG
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private JavaMailSender javaMailSender; // Spring Framework에서 제공하는 인터페이스 주입
    private SpringTemplateEngine springTemplateEngine;

    public EmailVerificationService(JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine) {
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    // EmailVerificationService 메서드 (생성 -> 전송 -> 검증 -> 확인)

    // 이메일을 인증하는 토큰을 생성하고 저장, 옵션 설정 메서드
    public EmailVerification createEmailVerification() {
        String token = UUID.randomUUID().toString(); // 고유한 토큰을 설정한다.
        logger.debug("Creating email verification with token: {}", token); // DEBUG: 생성된 토큰 확인

        // EmailVerification 인스턴스 생성 및 토큰 및 만료 시간 설정
        EmailVerification emailVerification = new EmailVerification(); // 이메일 인증 정보를 저장하기 위한 인스턴스 생성
        emailVerification.setToken(token); // 이메일 인증 링크를 클릭할 때 사용할 토큰
        emailVerification.setTokenExpiration(LocalDateTime.now().plusMinutes(3)); // 토큰의 만료 시간 설정 (3분 후)

        // EmailVerification 객체를 데이터베이스에 저장하고 반환
        return emailVerificationRepository.save(emailVerification);
    }

/*  // 이메일 인증 링크를 전송하는 메서드
    public void sendVerification(String email, String token) throws MessagingException {

        logger.debug("Sending verification email to: {}, with token: {}", email, token); // DEBUG: email과 token 재확인
        // 이메일의 제목, 인증 완료할 수 있는 토큰이 포함된 링크, 본문 생성
        String subject = "MeatMeet6kae의 이메일 인증을 위한 메일입니다.";
        String confirmationUrl = "이메일 인증에 필요한 토큰입니다. 해당 토큰을 인증창에 입력하세요: " + token;
        String message = "안녕하세요, \n" + confirmationUrl;

        logger.debug("Email subject: {}, confirmationUrl: {}", subject, confirmationUrl);
        // DEBUG: 이메일 메시지 구성 내용 출력 (제목과 인증 URL 포함)

        // MimeMessage 객체 생성: 이메일 메시지를 구성하는 객체
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        // MimeMessageHelper를 사용하여 이메일 내용 설정
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
        messageHelper.setTo(email); // 수신자의 이메일 주소를 설정
        messageHelper.setFrom("92mi_hyung@naver.com"); // 발신자의 이메일 주소를 설정
        messageHelper.setSubject(subject); // 이메일의 제목 설정
        messageHelper.setText(message, true); // 이메일 본문 설정 (HTML을 허용)

        logger.debug("send email to: {}", email); // DEBUG: 이메일 전송 시도

        // 이메일 전송
        javaMailSender.send(mimeMessage); // 구성된 이메일을 실제로 전송
        logger.debug("Email send success: {}", email); // DEBUG: 이메일 전송 성공
    } - HTML형식으로 메시지 설정을 위해 주석 처리*/

    // 이메일 인증 링크를 전송하는 메서드 -03리펙토링_0918
    public void sendVerification(String email, String token) throws MessagingException {

    //이메일의 제목
    String subject = "MeatMeet6kae의 이메일 인증을 위한 메일입니다.";

    //Thymeleaf context를 사용해서 변수 전달
    Context context = new Context();
    context.setVariable("token",token);

    //HTML 템플릿을 사용하여 이메일 내용 생성
    String htmlContent = springTemplateEngine.process("/email/verificationEmailText",context);

    logger.debug("Generated HTML content: {}", htmlContent); //DEBUG: 생성된 HTML 컨텐츠 확인

    // MimeMessage 객체 생성: 이메일 메시지를 구성하는 객체
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    // MimeMessageHelper를 사용하여 이메일 내용 설정
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
    messageHelper.setTo(email); // 수신자의 이메일 주소를 설정
    messageHelper.setFrom("92mi_hyung@naver.com"); // 발신자의 이메일 주소를 설정
    messageHelper.setSubject(subject); // 이메일의 제목 설정
    messageHelper.setText(htmlContent, true); // 이메일 본문 설정 (HTML을 허용)

    logger.debug("send email to: {}", email); // DEBUG: 이메일 전송 시도

    // 이메일 전송
    javaMailSender.send(mimeMessage); // 구성된 이메일을 실제로 전송
    logger.debug("Email send success: {}", email); // DEBUG: 이메일 전송 성공

    }

    // 토큰을 검증하고 이메일 인증 상태를 업데이트하는 메서드
    public boolean verifyEmailToken(String token) {

        logger.debug("Verifying email token: {}", token); // DEBUG: 메서드가 실행되며 전달받은 토큰 확인

        //전달 받은 토큰으로 EmailVerification에 있는지 검색, 없을 수도 있기 때문에 Optional<T>로 안전하게 null처리
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByToken(token);

        if (emailVerification.isPresent() && !emailVerification.get().isVerificationStatus()) {
            EmailVerification verification = emailVerification.get();
            //isPresent()로 값이 존재하는지 안전하게 확인, 있다면 isVerificationStatus으로 인증 여부 확인(boolean)
            //get()으로 실제 객체 반환

            // 토큰이 아직 만료되지 않은 경우
            if (verification.getTokenExpiration().isAfter(LocalDateTime.now())) {
                verification.setVerificationStatus(true); // 이메일 인증 상태를 true로 설정
                emailVerificationRepository.save(verification);
                logger.debug("Email verification successful for token: {}", token); // DEBUG: 이메일 인증 성공
                return true; // 인증 성공
            }
        } else {
            logger.debug("Token expired or already verified for token: {}", token); // DEBUG: 토큰 만료 또는 이미 인증된 경우
        }
        return false;
    }
}
