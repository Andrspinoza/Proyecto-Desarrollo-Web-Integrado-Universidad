package com.utp.quesale.iam.infrastructure.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final byte[] secret;
    private final boolean enabled;
    @Autowired
    public JwtService(@Value("${app.auth.jwt-secret:}") String secret,
                      @Value("${app.auth.enabled:true}") boolean enabled) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.enabled = enabled;
    }
    public JwtService(String secret) { this(secret, true); }
    @PostConstruct public void validateConfiguration() { if (enabled) ensureKey(); }
    public String issue(Long studentId) {
        ensureKey();
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer("quesale-api")
                    .subject(studentId.toString()).issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(3600))).build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) { throw new IllegalStateException("Cannot issue token", e); }
    }
    public Long verify(String token) {
        ensureKey();
        if (token == null || token.length() > 4096) return null;
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret))) return null;
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!"quesale-api".equals(claims.getIssuer()) || claims.getExpirationTime() == null
                    || !claims.getExpirationTime().after(new Date())) return null;
            long id = Long.parseLong(claims.getSubject());
            return id > 0 ? id : null;
        } catch (Exception e) { return null; }
    }
    private void ensureKey() {
        if (secret.length < 32) throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
    }
}
