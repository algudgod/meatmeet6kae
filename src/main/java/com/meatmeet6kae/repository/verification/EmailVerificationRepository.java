package com.meatmeet6kae.repository.verification;

import com.meatmeet6kae.entity.verification.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification,Long> {
    EmailVerification findByToken(String token); // 토큰으로 검색하는 메소드

}
