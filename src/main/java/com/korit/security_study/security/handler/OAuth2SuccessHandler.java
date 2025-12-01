package com.korit.security_study.security.handler;

import com.korit.security_study.entity.OAuth2User;
import com.korit.security_study.entity.User;
import com.korit.security_study.repository.OAuth2UserRepository;
import com.korit.security_study.repository.UserRepository;
import com.korit.security_study.security.jwt.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OAuth2UserRepository oAuth2UserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User 정보를 파싱
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String provider = defaultOAuth2User.getAttribute("provider");
        String providerUserId = defaultOAuth2User.getAttribute("providerUserId");
        String email = defaultOAuth2User.getAttribute("email");
        System.out.println(provider);
        System.out.println(providerUserId);
        System.out.println(email);

        // provider, providerUserId로 이미 연동된 사용자 정보고 있는지 DB에서 확인
        Optional<OAuth2User> foundOAuth2User = oAuth2UserRepository.getOAuth2UserByProviderAndProviderUserId(provider, providerUserId);

        // OAuth2 로그인을 통해 회원가입이 되어있지 않거나, 아직 연동되지 않은 상태
        if (foundOAuth2User.isEmpty()) {
            response.sendRedirect("http://localhost:3000/auth/oauth2?provider=" + provider + "&providerUserId=" + providerUserId + "&email=" + email);
            return;
        }

        // 연동된 사용자가 있다면? => userId를 통해 회원 정보를 조회
        Optional<User> foundUser = userRepository.getUserByUserId(foundOAuth2User.get().getUserId());
        String accessToken = null;
        if (foundUser.isPresent()) {
            accessToken = jwtUtils.generateAccessToken(Integer.toString(foundUser.get().getUserId()));
        }

        response.sendRedirect("http://localhost:3000/auth/oauth2/signin?accessToken=" + accessToken);
    }
}