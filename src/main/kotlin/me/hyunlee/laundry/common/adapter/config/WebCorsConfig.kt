package me.hyunlee.laundry.common.adapter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { } // 전역 CORS 활성화
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                auth.requestMatchers("/actuator/**").permitAll()
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { rs ->
                rs.jwt { }
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val c = CorsConfiguration()
        c.allowedOrigins = listOf("http://localhost:3000")
        c.allowedMethods = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS")
        c.allowedHeaders = listOf("*")
        c.exposedHeaders = listOf("Location","Authorization")
        c.allowCredentials = true
        c.maxAge = 3600
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", c)
        }
    }
}