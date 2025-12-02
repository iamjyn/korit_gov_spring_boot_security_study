package com.korit.security_study.service;

import com.korit.security_study.dto.ApiRespDto;
import com.korit.security_study.dto.SendMailReqDto;
import com.korit.security_study.entity.User;
import com.korit.security_study.entity.UserRole;
import com.korit.security_study.repository.UserRepository;
import com.korit.security_study.repository.UserRoleRepository;
import com.korit.security_study.security.jwt.JwtUtils;
import com.korit.security_study.security.model.Principal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MailService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JavaMailSender javaMailSender;

    public ApiRespDto<?> sendMail(SendMailReqDto sendMailReqDto, Principal principal) {
        if (!sendMailReqDto.getEmail().equals(principal.getEmail())) {
            return new ApiRespDto<>("failed", "잘못된 접근입니다.", null);
        }

        Optional<User> foundUser = userRepository.getUserByEmail(sendMailReqDto.getEmail());
        if (foundUser.isEmpty()) {
            return new ApiRespDto<>("failed", "잘못된 접근입니다.", null);
        }
        User user = foundUser.get();
        boolean hasTempRole = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRoleId() == 3);

        if (!hasTempRole) {
            return new ApiRespDto<>("failed", "인증이 필요한 계정이 아닙니다.", null);
        }

        String token = jwtUtils.generateVerifyToken(user.getUserId().toString());
//        System.out.println(11);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendMailReqDto.getEmail()); // 메일 수신자
        message.setSubject("[이메일 인증]이메일 인증해주세요."); // 메일 제목
        message.setText("링크를 클릭해 인증을 완료해주세요. :" +
                "http://localhost:8080/mail/verify?verifyToken=" + token); // 메일 내용
        javaMailSender.send(message);
//        System.out.println(22);
        return new ApiRespDto<>("success", "인증 메일이 전송되었습니다. 메일을 확인하세요.",null);
    }

    public Map<String, Object> verify(String token) {
        Claims claims = null;
        Map<String, Object> resultMap = null;

        try {
            claims = jwtUtils.getClaims(token);
            String subject = claims.getSubject();

            if (!"VerifyToken".equals(subject)) {
                return resultMap = Map.of(
                        "status", "failed",
                        "message", "잘못된 접근입니다."
                );
            }

            Integer userId = Integer.parseInt(claims.getId());
            Optional<User> foundUser = userRepository.getUserByUserId(userId);
            if (foundUser.isEmpty()) {
                return Map.of(
                        "status", "failed",
                        "message", "존재하지 않는 사용자입니다."
                );
            }

            List<UserRole> userRoles = foundUser.get().getUserRoles();
            Optional<UserRole> tempUserRole = userRoles.stream()
                    .filter(userRole -> userRole.getRoleId() == 3)
                    .findFirst();
            if (tempUserRole.isEmpty()) {
                return Map.of(
                        "status", "failed",
                        "message", "이미 인증이 완료된 메일입니다."
                );
            }
            UserRole userRole = tempUserRole.get();
            userRole.setRoleId(2);
            userRoleRepository.updateUserRole(userRole);

            return Map.of(
                    "status", "success",
                    "message", "이메일 인증 완료"
            );
        } catch (ExpiredJwtException e) {
            resultMap = Map.of(
                    "status", "failed",
                    "message", "만료된 인증 요청입니다.\n인증 메일을 다시 요청하세요"
            );
        } catch (JwtException e) {
            resultMap = Map.of(
                    "status", "failed",
                    "message", "문제가 발생했습니다.\n인증 메일을 다시 요청하세요"
            );
        }
        return resultMap;
    }
}
