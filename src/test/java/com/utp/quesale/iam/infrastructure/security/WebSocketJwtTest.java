package com.utp.quesale.iam.infrastructure.security;

import com.utp.quesale.shared.infrastructure.websocket.WebSocketConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketJwtTest {
    static class Registration extends ChannelRegistration {
        ChannelInterceptor interceptor() { return getInterceptors().getFirst(); }
    }
    private final JwtService jwt = new JwtService("a-secret-with-at-least-32-bytes-long");
    private ChannelInterceptor interceptor() {
        var config = new WebSocketConfiguration(new String[]{"https://example.com"});
        config.setAuth(jwt, true);
        var registration = new Registration();
        config.configureClientInboundChannel(registration);
        return registration.interceptor();
    }
    private Message<byte[]> frame(StompHeaderAccessor headers) {
        headers.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }
    @Test void connectRequiresSignedToken() {
        var headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        assertThrows(IllegalArgumentException.class, () -> interceptor().preSend(frame(headers), null));
        headers.setNativeHeader("Authorization", "Bearer " + jwt.issue(42L));
        interceptor().preSend(frame(headers), null);
        assertEquals("42", headers.getUser().getName());
    }
    @Test void cannotSubscribeToRawQueueOfAnotherStudent() {
        var headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headers.setUser(() -> "42"); headers.setDestination("/queue/messages-user99");
        assertThrows(IllegalArgumentException.class, () -> interceptor().preSend(frame(headers), null));
    }
}
