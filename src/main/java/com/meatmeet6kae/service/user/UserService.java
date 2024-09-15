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

    //login_id로 사용자 조회
    public Optional<User> getUserByLoginId(String loginId) {
        logger.debug("getUserByLoginId called with loginId: {}", loginId); // DEBUG: getUserByLoginId 호출 여부 로그 출력

        Optional<User> user = userRepository.findByLoginId(loginId); //Optional 사용한 사용자 조회

        return user;
    }
    //사용자 추가 메서드
    public User addUser(User user) {
        return userRepository.save(user);  // Repository를 통해 사용자 저장
    }

    //Optional<User>를 사용해 로그인 처리
    public User login(String loginId, String password) {
        Optional<User> loginUser = getUserByLoginId(loginId);  //

        if (loginUser.isPresent()) {
            User user = loginUser.get();
            if (user.getPassword().equals(password)) {
                return user;  // 로그인 성공
            }
        }
        return null;  // 로그인 실패 시 null 반환
    }

    public User updateUser(User user) {
        // 데이터베이스에 사용자 정보 업데이트
        return userRepository.save(user);
    }
}

