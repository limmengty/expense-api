package com.mt.expense.app.infrastructure.keycloak;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Configuration properties for Keycloak connection. */
@ConfigurationProperties(prefix = "keycloak")
@Component
@Getter
@Setter
@Validated
public class KeycloakProperty {

    @NotNull @NotEmpty private String realm;

    @NotNull @NotEmpty private String serverUrl;

    @NotNull @NotEmpty private String clientId;

    @NotNull @NotEmpty private String clientSecret;

    @NotNull @NotEmpty private String issuerUri;
}
