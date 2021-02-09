package com.github.dimsmith.restspringtemplate.common

import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.jose4j.jwt.consumer.ErrorCodes
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.HmacKey

object JWTFactory {

    private const val ALG = AlgorithmIdentifiers.HMAC_SHA256
    private const val ISSUER = "dimsmith"
    private const val AUDIENCE = "dimsmith-aud"
    private const val SECRET = "very_secret"
    private const val JWT_TOKEN_EXPIRES_TIME = 60 * 60 * 3 // 3 hours
    private val KEY = HmacKey(SECRET.toByteArray())

    fun produce(subject: String, claims: Map<String, Any>): String {
        val jwtClaims = JwtClaims()
        jwtClaims.issuer = ISSUER
        jwtClaims.subject = subject
        jwtClaims.setAudience(AUDIENCE)
        jwtClaims.expirationTime = NumericDate.fromSeconds(NumericDate.now().value + JWT_TOKEN_EXPIRES_TIME)
        jwtClaims.setGeneratedJwtId()
        claims.map { (key, value) ->
            jwtClaims.setClaim(key, value)
        }

        val jws = JsonWebSignature()
        jws.keyIdHeaderValue = System.currentTimeMillis().toString()
        jws.isDoKeyValidation = false
        jws.payload = jwtClaims.toJson()
        jws.key = KEY
        jws.algorithmHeaderValue = ALG

        return jws.compactSerialization
    }

    fun claim(jwt: String): MutableMap<String, Any> {
        val jwtConsumer = JwtConsumerBuilder()
            .setRequireExpirationTime()
            .setRequireJwtId()
            .setRequireSubject()
            .setExpectedIssuer(ISSUER)
            .setExpectedAudience(AUDIENCE)
            .setVerificationKey(KEY)
            .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256)
            .setRelaxVerificationKeyValidation()
            .build()

        try {
            val jwtClaims = jwtConsumer.processToClaims(jwt)
            val claim = jwtClaims.claimsMap
            if (claim["api_key"] != AppConfig.API_KEY) throw Exception("Invalid API Key")
            return jwtClaims.claimsMap
        } catch (ex: Exception) {
            when(ex) {
                is InvalidJwtException -> {
                    if (ex.hasExpired()) {
                        throw JWTException("JWT expired at ${ex.jwtContext.jwtClaims.expirationTime}")
                    }
                    if (ex.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)) {
                        throw JWTException("JWT had wrong audience: ${ex.jwtContext.jwtClaims.audience}")
                    }
                    if (ex.hasErrorCode(ErrorCodes.ISSUER_INVALID)) {
                        throw JWTException("JWT had wrong issuer: ${ex.jwtContext.jwtClaims.audience}")
                    }
                    throw JWTException(ex.errorDetails.joinToString { it.errorMessage })
                }
                else -> {
                    throw JWTException(ex.message ?: "")
                }
            }

        }
    }
}