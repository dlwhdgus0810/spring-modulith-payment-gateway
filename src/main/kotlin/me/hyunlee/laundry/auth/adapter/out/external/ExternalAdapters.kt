package me.hyunlee.laundry.auth.adapter.out.external

import me.hyunlee.laundry.auth.application.port.out.ClockPort
import me.hyunlee.laundry.auth.application.port.out.OtpCodeGeneratorPort
import me.hyunlee.laundry.auth.application.port.out.TokenCodecPort
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Instant
import java.util.*


@Component
class ClockSystemAdapter : ClockPort {
    override fun now(): Instant = Instant.now()
}

@Component
class OtpCodeGeneratorSimple : OtpCodeGeneratorPort {
    override fun generate(length: Int): String {
        val rnd = Random()
        val sb = StringBuilder()
        repeat(length) { sb.append(rnd.nextInt(10)) }
        return sb.toString()
    }
}

@Component
class TokenCodecSimpleAdapter(
    private val jwtEncoder: JwtEncoder
) : TokenCodecPort {

    override fun newAccessToken(
        userId: UUID,
        ttlSeconds: Long,
        claims: Map<String, Any>
    ): String {
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId.toString())
            .claims { it.putAll(claims) }
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttlSeconds))
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    override fun newRefreshToken(): String {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID().toString().replace("-", "")
    }

    override fun hash(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(token.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
