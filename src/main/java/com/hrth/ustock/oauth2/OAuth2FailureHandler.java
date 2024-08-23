package com.hrth.ustock.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.info(request.getRequestURI());
        String ERROR_PARAM_PREFIX = "error";
        String redirectUrl = UriComponentsBuilder
                .fromUriString("https://ustock.site")
//                .fromUriString("http://localhost:3000")
                .queryParam(ERROR_PARAM_PREFIX, exception.getLocalizedMessage())
                .build()
                .toUriString();

        log.info(redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
