package com.meatmeet6kae.service.user;

import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    // Logger 설정
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // login_id로 사용자 조회
    public Optional<User> getUserByLoginId(String loginId) {
        logger.debug("getUserByLoginId called with loginId: {}", loginId); // DEBUG: getUserByLoginId 호출 여부 로그 출력

        Optional<User> user = userRepository.findByLoginId(loginId); //사용자 조회

        return user;
    }
    public User addUser(User user) {
        // 추가 로직
        return userRepository.save(user);  // Repository를 통해 사용자 저장
    }

}

