package com.utp.quesale.iam.infrastructure.security;

import org.junit.jupiter.api.Test;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private final JwtService service = new JwtService("a-secret-with-at-least-32-bytes-long");
    @Test void roundtrip() { assertEquals(42L, service.verify(service.issue(42L))); }
    @Test void tamperedTokenIsRejected() {
        String token = service.issue(42L);
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].charAt(0) == 'A' ? "B" : "A") + parts[2].substring(1);
        assertNull(service.verify(String.join(".", parts)));
    }
    @Test void weakKeyIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short").issue(1L));
    }
    @Test void expiredTokenIsRejected() throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer("quesale-api").subject("42")
                .expirationTime(new Date(System.currentTimeMillis() - 1000)).build();
        var signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signed.sign(new MACSigner("a-secret-with-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8)));
        assertNull(service.verify(signed.serialize()));
    }
}
