package com.mt.expense.app.infrastructure.keycloak;

import com.mt.expense.app.application.port.out.KeycloakTokenService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Adapter implementation of KeycloakTokenService using Feign Client. Uses Basic Auth header for
 * client authentication (same pattern as HRM).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakTokenServiceImpl implements KeycloakTokenService {

    private final KeycloakFeignClient keycloakFeignClient;
    private final KeycloakProperty keycloakProperty;

    private String basicAuthHeader() {
        String credentials =
                keycloakProperty.getClientId() + ":" + keycloakProperty.getClientSecret();
        return "Basic "
                + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD);
        formData.add(OAuth2Constants.USERNAME, username);
        formData.add(OAuth2Constants.PASSWORD, password);
        formData.add(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID);

        try {
            return keycloakFeignClient.token(basicAuthHeader(), formData);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> refresh(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2Constants.GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);
        formData.add(OAuth2Constants.REFRESH_TOKEN, refreshToken);
        formData.add(OAuth2Constants.CLIENT_ID, keycloakProperty.getClientId());
        formData.add(OAuth2Constants.CLIENT_SECRET, keycloakProperty.getClientSecret());

        try {
            return keycloakFeignClient.token(basicAuthHeader(), formData);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public void logout(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2Constants.REFRESH_TOKEN, refreshToken);

        try {
            keycloakFeignClient.logout(basicAuthHeader(), formData);
        } catch (Exception e) {
            log.warn("Logout failed (token may already be invalid): {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> userInfo(String accessToken) {
        return keycloakFeignClient.userinfo("Bearer " + accessToken);
    }
}
