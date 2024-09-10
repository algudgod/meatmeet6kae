package com.meatmeet6kae.repository.user;

import com.meatmeet6kae.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // JPA상속, <엔티티클래스와 login_id의 데이터 타입>

    //login_id로 User 조회, 사용자 없을 시, null 처리 (Optional)
    Optional<User> findByLoginId(String loginId);


}
