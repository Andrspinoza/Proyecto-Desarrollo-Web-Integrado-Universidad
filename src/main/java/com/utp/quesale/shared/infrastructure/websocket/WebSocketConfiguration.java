package com.utp.quesale.shared.infrastructure.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.utp.quesale.iam.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;
    private JwtService jwt;
    private boolean authEnabled = true;

    @Autowired
    public void setAuth(JwtService jwt, @Value("${app.auth.enabled:true}") boolean enabled) {
        this.jwt = jwt; this.authEnabled = enabled;
    }

    public WebSocketConfiguration(@Value("${app.websocket.allowed-origins:*}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(authEnabled ? new DefaultHandshakeHandler() : new StudentHandshakeHandler())
                .setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setUserDestinationPrefix("/user");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
                if (!authEnabled) return message;
                StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (headers == null || headers.getCommand() == null) return message;
                if (StompCommand.CONNECT.equals(headers.getCommand())) {
                    String token = headers.getFirstNativeHeader("Authorization");
                    Long studentId = token != null && token.startsWith("Bearer ") ? jwt.verify(token.substring(7)) : null;
                    if (studentId == null) throw new IllegalArgumentException("Unauthorized websocket");
                    headers.setUser(() -> studentId.toString());
                } else if (StompCommand.SEND.equals(headers.getCommand()) || StompCommand.SUBSCRIBE.equals(headers.getCommand())) {
                    if (headers.getUser() == null) throw new IllegalArgumentException("Unauthorized websocket");
                    String destination = headers.getDestination();
                    if (destination == null || (StompCommand.SUBSCRIBE.equals(headers.getCommand()) && !destination.startsWith("/user/queue/"))
                            || (StompCommand.SEND.equals(headers.getCommand()) && !destination.startsWith("/app/")))
                        throw new IllegalArgumentException("Forbidden destination");
                }
                return message;
            }
        });
    }
}
