package com.gz.xg.service

import com.gz.xg.domain.entity.SysUser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-minutes}") private val expirationMinutes: Long
) {

    fun generateToken(user: SysUser): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(expirationMinutes * 60)

        return Jwts.builder()
            .subject(user.id)
            .claim("username", user.username)
            .claim("realName", user.realName)
            .claim("type", user.type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey())
            .compact()
    }

    fun getExpiresAt(): Long {
        return Instant.now().plusSeconds(expirationMinutes * 60).toEpochMilli()
    }

    fun getExpiresIn(): Long {
        return expirationMinutes * 60
    }

    private fun signingKey(): SecretKey {
        val keyBytes = secret.toByteArray(StandardCharsets.UTF_8)
        require(keyBytes.size >= 32) {
            "jwt.secret must be at least 32 bytes for HS256"
        }
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
