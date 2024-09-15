package com.meatmeet6kae.repository.verification;

import com.meatmeet6kae.entity.verification.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 토큰을 사용하여 인증 항목을 찾는 메서드
    Optional<EmailVerification> findByToken(String token);
}