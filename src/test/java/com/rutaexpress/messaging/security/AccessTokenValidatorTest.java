package com.rutaexpress.messaging.security;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AccessTokenValidatorTest {
    private final String issuer = "https://issuer.example.test/pool";
    private final org.springframework.security.oauth2.core.OAuth2TokenValidator<Jwt> validator =
            new SecurityConfig().accessTokenValidator(issuer, "expected-client");
    @Test void acceptsOnlyConfiguredClientAccessToken() {
        assertFalse(validator.validate(token("access", "expected-client", issuer, Instant.now().plusSeconds(60))).hasErrors());
        assertTrue(validator.validate(token("id", "expected-client", issuer, Instant.now().plusSeconds(60))).hasErrors());
        assertTrue(validator.validate(token("access", "other-client", issuer, Instant.now().plusSeconds(60))).hasErrors());
        assertTrue(validator.validate(token("access", "expected-client", "https://other.example.test", Instant.now().plusSeconds(60))).hasErrors());
        assertTrue(validator.validate(token("access", "expected-client", issuer, Instant.now().minusSeconds(3600))).hasErrors());
    }
    private Jwt token(String type, String client, String tokenIssuer, Instant expiry) {
        return Jwt.withTokenValue("test").header("alg", "RS256").issuer(tokenIssuer).subject("user")
                .claim("token_use", type).claim("client_id", client).expiresAt(expiry).build();
    }
}
