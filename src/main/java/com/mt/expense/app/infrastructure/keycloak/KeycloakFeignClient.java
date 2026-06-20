package com.mt.expense.app.infrastructure.keycloak;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KeycloakFeignClient {

    private final RestClient keycloakRestClient;

    @SuppressWarnings("unchecked")
    public Map<String, Object> token(String basicAuth, MultiValueMap<String, String> formParams) {
        return keycloakRestClient
                .post()
                .uri("/protocol/openid-connect/token")
                .header("Authorization", basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formParams)
                .retrieve()
                .body(Map.class);
    }

    public void logout(String basicAuth, MultiValueMap<String, String> formParams) {
        keycloakRestClient
                .post()
                .uri("/protocol/openid-connect/logout")
                .header("Authorization", basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formParams)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> userinfo(String bearerToken) {
        return keycloakRestClient
                .get()
                .uri("/protocol/openid-connect/userinfo")
                .header("Authorization", bearerToken)
                .retrieve()
                .body(Map.class);
    }
}
