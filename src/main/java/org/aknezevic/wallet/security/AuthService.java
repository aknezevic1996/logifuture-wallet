package org.aknezevic.wallet.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    private static final String AUTH_HEADER = "X-API-KEY";
    private static String AUTH_TOKEN;

    @Value("${auth.token}")
    private void setToken(String token) {
        AUTH_TOKEN = token;
    }

    public static Authentication getAuth(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_HEADER);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }

}
