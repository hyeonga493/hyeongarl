package com.hyeongarl.service;

import com.hyeongarl.entity.Token;
import com.hyeongarl.entity.User;
import com.hyeongarl.error.UserNotFoundException;
import com.hyeongarl.repository.TokenRepository;
import com.hyeongarl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public String login(User userRequest) {
        LocalDateTime now = LocalDateTime.now();

        // 사용자 정보 확인
        User user = userRepository.findByUserEmail(userRequest.getUserEmail())
                .orElseThrow(UserNotFoundException::new);;

        // 비밀번호 확인
        if(!user.getPassword().equals(userRequest.getPassword())) {
            throw new UserNotFoundException();
        }

        log.info("확인");
        if(tokenRepository.existsByUserIdAndExpiryDate(user.getUserId(), now)) {
            log.info("만료된 토큰 있음");
            tokenRepository.deleteExpiredTokenByUserIdAndExpiryDate(user.getUserId(), now);
        }

        Token validateToken = tokenRepository.findByUserIdAndExpiryDate(user.getUserId(), now);

        // 인증 토큰 없는 경우
        if(validateToken == null) {
            String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

            Token saveToken = Token.builder()
                    .token(token)
                    .userId(user.getUserId())
                    .build();

            tokenRepository.save(saveToken);
            return token;
        }
        return validateToken.getToken();
    }
}
