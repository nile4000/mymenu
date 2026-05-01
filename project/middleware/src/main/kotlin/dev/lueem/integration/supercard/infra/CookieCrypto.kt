package dev.lueem.integration.supercard.infra

import dev.lueem.shared.config.SupercardProperties
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@ApplicationScoped
class CookieCrypto @Inject constructor(
    private val properties: SupercardProperties
) {
    private val secureRandom = SecureRandom()

    fun encrypt(plain: String): String {
        val key = loadKey()
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encrypted: String): String {
        val key = loadKey()
        val combined = Base64.getDecoder().decode(encrypted)
        require(combined.size > 12) { "Invalid encrypted payload" }

        val iv = combined.copyOfRange(0, 12)
        val payload = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(payload), Charsets.UTF_8)
    }

    private fun loadKey(): ByteArray {
        val raw = properties.encryptionKey.trim()
        require(raw.isNotBlank()) { "SUPERCARD_SESSION_ENC_KEY is required" }

        val decoded = Base64.getDecoder().decode(raw)
        require(decoded.size == 32) { "SUPERCARD_SESSION_ENC_KEY must be base64-encoded 32 bytes" }
        return decoded
    }
}
