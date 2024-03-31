package team.b2.bingojango.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsUtils
import team.b2.bingojango.global.security.jwt.JwtAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
    private val accessDeniedHandler: AccessDeniedHandler,
) {
    //모든 사용자에게 접근 허용
    private val allowedUrls = arrayOf(
        "/swagger-ui/**",
        "/v3/**",
        "/h2-console/**",
        "/oauth2/**",
        "/**" // TODO : 추후 삭제
    )

    //익명의 사용자만 접근 허용
    private val anonymousUrls = arrayOf(
        "/signup",
        "/login",
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .cors { it.disable() }
            .headers { it.frameOptions { foc -> foc.disable() } }
            .authorizeHttpRequests {
                it.requestMatchers(*allowedUrls).permitAll()
//                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    .requestMatchers(*anonymousUrls).anonymous()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}