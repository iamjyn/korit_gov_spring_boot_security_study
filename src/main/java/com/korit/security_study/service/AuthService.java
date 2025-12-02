package com.korit.security_study.service;

import com.korit.security_study.dto.ApiRespDto;
import com.korit.security_study.dto.SigninReqDto;
import com.korit.security_study.dto.SignupReqDto;
import com.korit.security_study.entity.User;
import com.korit.security_study.entity.UserRole;
import com.korit.security_study.repository.UserRepository;
import com.korit.security_study.repository.UserRoleRepository;
import com.korit.security_study.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public ApiRespDto<?> signup(SignupReqDto signupReqDto) {
        // username 중복확인
        Optional<User> foundUser = userRepository.getUserByUsername(signupReqDto.getUsername());
        if (foundUser.isPresent()) {
            return new ApiRespDto<>("failed", "이미 사용중인 username", null);
        }
        // 유저추가
        Optional<User> optionalUser = userRepository.addUser(signupReqDto.toEntity(bCryptPasswordEncoder));
        // 추가 후 userId로 userRole 추가
        UserRole userRole = UserRole.builder()
                .userId(optionalUser.get().getUserId())
                .roleId(3)
                .build();
        userRoleRepository.addUserRole(userRole);
        return new ApiRespDto<>("success", "회원가입 완료", optionalUser.get());
    }

    public ApiRespDto<?> signin(SigninReqDto signinReqDto) {
        // username을 가진 정보가 있는지 확인
        Optional<User> foundUser = userRepository.getUserByUsername(signinReqDto.getUsername());
        if (foundUser.isEmpty()) {
            return new ApiRespDto<>("failed", "사용자 정보를 다시 확인해주세요.", null);
        }
        User user = foundUser.get();
        if (!bCryptPasswordEncoder.matches(signinReqDto.getPassword(), user.getPassword())) {
            return new ApiRespDto<>("failed", "사용자 정보를 다시 확인해주세요.", null);
        }
        String token = jwtUtils.generateAccessToken(user.getUserId().toString());
        return new ApiRespDto<>("success", "로그인 성공", token);
    }

    public ApiRespDto<?> getUserByUsername(String username) {
        Optional<User> foundUser = userRepository.getUserByUsername(username);
        if (foundUser.isEmpty()) {
            return new ApiRespDto<>("failed", "해당 회원 없음", null);
        }
        return new ApiRespDto<>("success", "회원 조회 완료", foundUser.get());
    }
}
