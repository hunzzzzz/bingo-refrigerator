package team.b2.bingojango.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import team.b2.bingojango.global.oauth.api.oauth2login.converter.OAuth2ProviderConverter

@Configuration
class WebConfig(
    @Value("\${root.uri}") private val rootURI: String,
) : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(OAuth2ProviderConverter())
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // 모든 경로에 대해
            .allowedOrigins("http://localhost:9090", rootURI) // 이 출처로부터의 요청만 허용
            .allowedMethods("*") // 모든 HTTP 메소드 허용
            .allowCredentials(true) // 쿠키를 포함한 요청 허용
    }
}