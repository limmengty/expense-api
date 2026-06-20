package com.mt.expense.app.infrastructure.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakFeignClientConfig {

    @Bean
    public RestClient keycloakRestClient(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm) {
        return RestClient.builder().baseUrl(serverUrl + "/realms/" + realm).build();
    }
}
