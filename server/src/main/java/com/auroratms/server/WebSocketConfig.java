package com.auroratms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for Websockets implementation for pushing information from server to client
 */
@Configuration
@EnableWebSocketMessageBroker
//  when you use Spring Security’s authorization for messages, at present, you need to ensure that the authentication
//  ChannelInterceptor config is ordered ahead of Spring Security’s. This is best done by declaring the custom interceptor
//  in its own implementation of WebSocketMessageBrokerConfigurer that is marked with
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Value("${client.host.url}")
    private String clientHostUrl;

    // type of message broker to use
    @Value("${message.broker.type}")
    private String messageBroker;

    // values to use for RabbitMQ message broker
    @Value("${message.broker.rabbitmq.host}")
    private String rabbitMQHost;

    @Value("${message.broker.rabbitmq.port}")
    private Integer rabbitMQPort;

    @Value("${message.broker.rabbitmq.login}")
    private String rabbitMQLogin;

    @Value("${message.broker.rabbitmq.passcode}")
    private String rabbitMQPasscode;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
//        ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
//        te.setPoolSize(1);
//        te.setThreadNamePrefix("wss-heartbeat-thread-");
//        te.initialize();
//        messageBrokerRegistry.enableSimpleBroker("/topic")
//                .setHeartbeatValue(new long[]{10000, 20000})
//                .setTaskScheduler(te);
        messageBrokerRegistry.setApplicationDestinationPrefixes("/app");
        if (messageBroker.equals("inmemory")) {
            messageBrokerRegistry.enableSimpleBroker("/topic");
        } else if (messageBroker.equals("rabbitmq")) {
//            messageBrokerRegistry.setPathMatcher(new AntPathMatcher("."));
            messageBrokerRegistry.enableStompBrokerRelay("/topic")
                    .setRelayHost(rabbitMQHost)
                    .setClientLogin(rabbitMQLogin)
                    .setClientPasscode(rabbitMQPasscode)
                    .setSystemLogin(rabbitMQLogin)
                    .setSystemPasscode(rabbitMQPasscode);
        }
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint for initiating WebSocket handshake via https request
        registry.addEndpoint("/websocket-hs")
                .setAllowedOrigins(this.clientHostUrl)
                .withSockJS();
    }

    /**
     * Overriden to get authentication into the WebSocket request
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String accessToken = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
                    Jwt jwt = jwtDecoder.decode(accessToken);
                    Authentication authentication = jwtAuthenticationConverter.convert(jwt);
                    accessor.setUser(authentication);
                }
                return message;
            }
        });
    }
}
