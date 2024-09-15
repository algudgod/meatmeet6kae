package com.meatmeet6kae.entity.verification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailVerificationId;  // 이메일 인증용 ID 필드
    private String token;
    private LocalDateTime tokenExpiration;  // 토큰 만료 시간
    private boolean verificationStatus = false;  // 이메일 인증 완료 여부

    public Long getEmailVerificationId() {
        return emailVerificationId;
    }

    public void setEmailVerificationId(Long emailVerificationId) {
        this.emailVerificationId = emailVerificationId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public boolean isVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(boolean verificationStatus) {
        this.verificationStatus = verificationStatus;
    }


}
