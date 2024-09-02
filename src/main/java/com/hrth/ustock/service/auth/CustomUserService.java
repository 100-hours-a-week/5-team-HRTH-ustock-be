package com.hrth.ustock.service.auth;

import com.hrth.ustock.dto.oauth2.CustomOAuth2User;
import com.hrth.ustock.exception.domain.user.UserException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.hrth.ustock.exception.domain.user.UserExceptionType.USER_NOT_FOUND;

@Service
public class CustomUserService {

    public CustomOAuth2User getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User) {
            return (CustomOAuth2User) authentication.getPrincipal();
        } else {
            throw new UserException(USER_NOT_FOUND);
        }

    }
}
