package com.utp.quesale.calls.application.handlers.queries;

import com.utp.quesale.calls.application.dtos.IceServerDto;
import com.utp.quesale.calls.application.dtos.IceServersDto;
import com.utp.quesale.calls.application.queries.GetIceServersQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GetIceServersQueryHandler {

    private static final String HMAC_ALGORITHM = "HmacSHA1";

    private final Clock clock;
    private final String[] stunUrls;
    private final String[] turnUrls;
    private final String turnSecret;
    private final long ttlSeconds;

    public GetIceServersQueryHandler(Clock clock,
                                     @Value("${app.calls.stun-urls:stun:stun.l.google.com:19302}") String[] stunUrls,
                                     @Value("${app.calls.turn-urls:}") String[] turnUrls,
                                     @Value("${app.calls.turn-secret:}") String turnSecret,
                                     @Value("${app.calls.turn-ttl-seconds:3600}") long ttlSeconds) {
        this.clock = clock;
        this.stunUrls = stunUrls;
        this.turnUrls = turnUrls;
        this.turnSecret = turnSecret;
        this.ttlSeconds = ttlSeconds;
    }

    public IceServersDto handle(GetIceServersQuery query) {
        List<IceServerDto> servers = new ArrayList<>();
        if (stunUrls.length > 0) {
            servers.add(new IceServerDto(List.of(stunUrls), null, null));
        }
        if (turnUrls.length > 0 && !turnSecret.isBlank()) {
            long expiresAt = clock.instant().plusSeconds(ttlSeconds).getEpochSecond();
            String username = expiresAt + ":" + query.studentId();
            servers.add(new IceServerDto(List.of(turnUrls), username, sign(username)));
        }
        return new IceServersDto(servers);
    }

    private String sign(String username) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(turnSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not sign TURN credentials", ex);
        }
    }
}
