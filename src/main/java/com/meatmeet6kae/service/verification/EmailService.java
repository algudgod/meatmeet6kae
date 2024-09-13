package com.meatmeet6kae.service.verification;

import com.meatmeet6kae.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

/*
    @Autowired
    private JavaMailSender javaMailSender; //이메일 전송 객체. build.gradle에 의존성 추가 후 사용 가능.

    public void senVerifivationEmail(User user, String token){
        SimpleMailMessage message = new SimpleMailMessage();//기본적인 텍스트 이메일 메시지 클래스, 수신자, 제목 등 설정.
        message.setTo(user.getEmail()); //이메일 주소 설정
        message.setSubject("MeatMeet6kae 인증을 위한 메일입니다."); //이메일 제목 설정
        message.setText("To verify your email, please click the link: "
                + "http://localhost:8080/users/verify?token="+token); //이메일 본문 내용 설정(이메일 인증링크 포함)
        javaMailSender.send(message);
    }*/
}
