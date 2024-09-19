package com.meatmeet6kae.service.user;

import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    // 로그를 남기기 위한 Logger 객체 설정: DEBUG
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //사용자 조회 메서드
    //로그인 시, 입력한 loginId로 사용자를 조회하거나 인증 과정을 수행,
    //관리자가 특정 사용자의 정보를 조회할 때,
    //사용자가 자신의 프로필 정보를 조회 등을 할 때.
    public Optional<User> getUserByLoginId(String loginId) {
        logger.debug("getUserByLoginId called with loginId: {}", loginId); // DEBUG: getUserByLoginId 호출 확인
        Optional<User> user = userRepository.findByLoginId(loginId);

        return user;
    }

    //사용자 추가 메서드
    public User addUser(User user) {
        //새로운 사용자를 추가하기 전에 아이디 중복 확인 -04리펙토링_0918
        if(userRepository.existsByLoginId((user.getLoginId()))){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        //새로운 사용자를 추가하기 전에 이메일 중복을 확인 -01리펙토링_0917
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        return userRepository.save(user);  //Repository를 통해 사용자 저장
    }

    //loginId가 이미 사용 중인지 확인하는 메서드 -04리펙토링_0918
    public boolean existsByLoginId(String loginId){
        return userRepository.existsByLoginId(loginId);
    }

    // 이메일이 이미 사용 중인지 확인하는 메서드
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    //사용자 로그인 메서드
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

    //사용자 수정 메서드
    public User updateUser(User user) {
        // 데이터베이스에 사용자 정보 업데이트
        return userRepository.save(user);
    }

    //사용자 탈퇴 메서드
    public void deactivateUser(String loginId) {
        //레포지토리에서 사용자를 loginId로 검색해서 찾고 반환한다.
        //람다(매개변수)->{실행코드, 한줄이면 중괄호 생략가능}를 사용해서 예외를 생성하고 메세지를 설정하고 던져본다.
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("Y".equals(user.getUseYn())) {
            user.setUseYn("N");
            user.setWithdrawDate(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}

