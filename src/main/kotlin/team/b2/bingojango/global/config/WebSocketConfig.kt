package team.b2.bingojango.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import team.b2.bingojango.domain.chatting.handler.StompHandler

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val stompHandler: StompHandler
) : WebSocketMessageBrokerConfigurer {


    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // 클라이언트에서 보낸 메시지를 받을 prefix = 메시지 받음
        config.enableSimpleBroker("/sub")
        // 해당 주소를 구독sub 중인 클라이언트들에게 메시지 전달 = 메시지 보냄
        config.setApplicationDestinationPrefixes("/pub")
    }

    // 실제 발행 전 확인 절차
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompHandler)
    }

}