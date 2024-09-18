package com.meatmeet6kae.repository.verification;

import com.meatmeet6kae.entity.verification.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일 인증:
    // token으로 조회, 없을 경우(잘못된 토큰 사용, 토큰 만료 등)를 위해 Optional<T>로 안전하게 null 처리
    Optional<EmailVerification> findByToken(String token);

}