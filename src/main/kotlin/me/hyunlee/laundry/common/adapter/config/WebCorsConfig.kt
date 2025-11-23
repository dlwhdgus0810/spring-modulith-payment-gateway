package me.hyunlee.laundry.common.adapter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@ConfigurationProperties(prefix = "cors")
data class CorsConfigProps(
    var allowedOrigins: List<String> = emptyList(),
    var allowedMethods: List<String> = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS"),
    var allowedHeaders: List<String> = listOf("*"),
    var exposedHeaders: List<String> = listOf("Location","Authorization"),
    var allowCredentials: Boolean = true,
    var maxAge: Long = 3600
)

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsConfigProps::class)
class WebSecurityConfig(
    private val corsProps: CorsConfigProps
) {

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/v3/api-docs",
                "/v3/api-docs/",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
            )
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                auth.requestMatchers(
                    "/v3/api-docs",
                    "/v3/api-docs/",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()
                auth.requestMatchers("/actuator/**").permitAll()
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.requestMatchers("/api/places/**").permitAll()
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN")
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        CorsConfiguration().apply {
            allowedOrigins = corsProps.allowedOrigins
            allowedMethods = corsProps.allowedMethods
            allowedHeaders = corsProps.allowedHeaders
            exposedHeaders = corsProps.exposedHeaders
            allowCredentials = corsProps.allowCredentials
            maxAge = corsProps.maxAge
        }.let { config ->
            UrlBasedCorsConfigurationSource().apply {
                registerCorsConfiguration("/**", config)
            }
        }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("roles")
            setAuthorityPrefix("ROLE_")
        }

        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        }
    }
}