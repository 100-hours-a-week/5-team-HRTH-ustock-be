package com.hrth.ustock.service;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.dto.oauth2.GoogleResponse;
import com.hrth.ustock.dto.oauth2.OAuth2Response;
import com.hrth.ustock.dto.oauth2.UserOauthDTO;
import com.hrth.ustock.entity.User;
import com.hrth.ustock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
            throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        String registrationId = oAuth2UserRequest
                .getClientRegistration()
                .getRegistrationId();

        // 6, 7번 과정(코드로토큰요청, 토큰발급)
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // 토큰 만들때 uuid값으로 변경, 필드명은 그대로 유지?
        // 오류 발생시 Provider+ProviderId로 조회하려면 지금 방식이 나을수도 있음(토큰 조회)
        log.info(oAuth2Response.toString());
        User existData = userRepository.findByProviderAndProviderId(
                oAuth2Response.getProvider(), oAuth2Response.getProviderId()
        );

        if (existData == null) {
            // 새로운 유저 저장(nickname, profileImage = null)
            User newUser = User.builder()
                    .providerName(oAuth2Response.getName())
                    .provider(oAuth2Response.getProvider())
                    .providerId(oAuth2Response.getProviderId())
                    .providerImage(oAuth2Response.getPicture())
                    .role("ROLE_USER")
                    .build();
            User saved = userRepository.save(newUser);

            // OAuth 유저정보 return
            UserOauthDTO userOauthDTO = UserOauthDTO.builder()
                    .userId(saved.getUserId())
                    .name(saved.getProviderName())
                    .provider(saved.getProvider())
                    .providerId(saved.getProviderId())
                    .role(saved.getRole())
                    .build();
            return new CustomOAuth2User(userOauthDTO);
        } else {
            // provider로부터 변경된 user 정보 update
            UserOauthDTO userOauthDTO = UserOauthDTO.builder()
                    .userId(existData.getUserId())
                    .provider(existData.getProvider())
                    .providerId(existData.getProviderId())
                    .name(oAuth2Response.getName())
                    .picture(oAuth2Response.getPicture())
                    .role(existData.getRole())
                    .build();
            existData.updateUserOAuth(userOauthDTO);

            // OAuth 유저정보 return
            return new CustomOAuth2User(userOauthDTO);
        }
    }
}
