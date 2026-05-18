package br.com.aulagv.authdemo

import android.util.Base64
import java.math.BigInteger
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Funções criptográficas simples para demonstrar conceitos de autenticação. */
object CryptoUtils {
    private val random = SecureRandom()

    /**
     * Hash SHA-256 de senha para exemplo didático.
     * Em produção, prefira Argon2id, bcrypt ou PBKDF2 com salt individual e custo alto.
     */
    fun sha256(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Gera string aleatória segura para state e code_verifier no OAuth com PKCE. */
    fun secureRandomBase64Url(bytes: Int = 32): String {
        val data = ByteArray(bytes)
        random.nextBytes(data)
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /** Calcula o code_challenge usado no fluxo Authorization Code + PKCE. */
    fun pkceChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(StandardCharsets.US_ASCII))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /** Codificação URL para montar endpoints OAuth sem bibliotecas externas. */
    fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

    /**
     * Implementação TOTP compatível com RFC 6238: HMAC-SHA1 + janela de 30s.
     * Representa a prova de posse: o usuário precisa possuir o gerador de token.
     */
    fun generateTotp(secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val counter = timeMillis / 1000L / 30L
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        val key = secret.toByteArray(StandardCharsets.UTF_8)
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(counterBytes)
        val offset = hash.last().toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)
        return (binary % 1_000_000).toString().padStart(6, '0')
    }

    /** Decodifica apenas o payload do JWT para visualização didática do ID Token. */
    fun decodeJwtPayload(jwt: String): String {
        val parts = jwt.split(".")
        if (parts.size < 2) return "JWT inválido ou ausente."
        val json = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        return json
    }
}
