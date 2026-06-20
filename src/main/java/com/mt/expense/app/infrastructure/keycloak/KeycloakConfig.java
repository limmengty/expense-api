package com.mt.expense.app.infrastructure.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for Keycloak admin client. */
@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloakAdminClient(KeycloakProperty props) {
        return KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .grantType("client_credentials")
                .build();
    }
}
