package com.utp.quesale.calls.application;

import com.utp.quesale.calls.application.dtos.IceServerDto;
import com.utp.quesale.calls.application.dtos.IceServersDto;
import com.utp.quesale.calls.application.handlers.queries.GetIceServersQueryHandler;
import com.utp.quesale.calls.application.queries.GetIceServersQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetIceServersQueryHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String[] STUN = {"stun:stun.l.google.com:19302"};
    private static final String[] TURN = {"turn:turn.quesale.example:3478?transport=udp", "turn:turn.quesale.example:3478?transport=tcp"};

    private static String expectedCredential(String secret, String username) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Con TURN configurado devuelve STUN más TURN con credenciales temporales firmadas")
    void stunAndTurnWithSignedCredentials() throws Exception {
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(CLOCK, STUN, TURN, "secreto-turn", 3600);

        IceServersDto result = handler.handle(new GetIceServersQuery(7L));

        assertEquals(2, result.iceServers().size());
        IceServerDto stun = result.iceServers().get(0);
        assertEquals(List.of(STUN), List.of(stun.urls().toArray(new String[0])));
        assertNull(stun.username());
        assertNull(stun.credential());

        IceServerDto turn = result.iceServers().get(1);
        String expectedUsername = (NOW.getEpochSecond() + 3600) + ":7";
        assertEquals(expectedUsername, turn.username());
        assertEquals(expectedCredential("secreto-turn", expectedUsername), turn.credential());
        assertEquals(2, turn.urls().size());
    }

    @Test
    @DisplayName("La credencial es distinta para cada estudiante")
    void credentialDependsOnStudent() {
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(CLOCK, STUN, TURN, "secreto-turn", 3600);

        IceServerDto first = handler.handle(new GetIceServersQuery(7L)).iceServers().get(1);
        IceServerDto second = handler.handle(new GetIceServersQuery(8L)).iceServers().get(1);

        assertNotEquals(first.username(), second.username());
        assertNotEquals(first.credential(), second.credential());
    }

    @Test
    @DisplayName("Sin secreto de TURN solo se devuelve STUN")
    void withoutSecretOnlyStun() {
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(CLOCK, STUN, TURN, "", 3600);

        IceServersDto result = handler.handle(new GetIceServersQuery(7L));

        assertEquals(1, result.iceServers().size());
        assertNull(result.iceServers().get(0).credential());
    }

    @Test
    @DisplayName("Sin URLs de TURN tampoco se devuelven credenciales")
    void withoutTurnUrlsOnlyStun() {
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(CLOCK, STUN, new String[0], "secreto-turn", 3600);

        assertEquals(1, handler.handle(new GetIceServersQuery(7L)).iceServers().size());
    }

    @Test
    @DisplayName("Sin nada configurado la lista queda vacía")
    void nothingConfigured() {
        GetIceServersQueryHandler handler = new GetIceServersQueryHandler(CLOCK, new String[0], new String[0], "", 3600);

        assertTrue(handler.handle(new GetIceServersQuery(7L)).iceServers().isEmpty());
    }
}
