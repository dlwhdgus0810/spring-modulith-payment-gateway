package me.hyunlee.laundry.common.adapter.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

@ConfigurationProperties(prefix = "jwt")
data class JwtConfigProps(
    var hmacSecret: String
)

@Configuration
@EnableConfigurationProperties(JwtConfigProps::class)
class JwtConfig(
    private val jwtProps: JwtConfigProps
) {

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val key = SecretKeySpec(jwtProps.hmacSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val jwk = OctetSequenceKey.Builder(key).keyID("hmac-key-1").build()
        val jwkSet = JWKSet(jwk)
        return NimbusJwtEncoder(ImmutableJWKSet(jwkSet))
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(jwtProps.hmacSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).build()
    }
}
