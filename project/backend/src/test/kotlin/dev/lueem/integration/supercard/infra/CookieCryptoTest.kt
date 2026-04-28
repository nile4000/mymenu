package dev.lueem.integration.supercard.infra

import dev.lueem.shared.config.SupercardProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Base64

class CookieCryptoTest {

    @Test
    fun `encrypt and decrypt roundtrip`() {
        val properties = SupercardProperties().apply {
            encryptionKey = Base64.getEncoder().encodeToString(ByteArray(32) { 1 })
            dbUrl = ""
            dbUser = ""
            dbPassword = ""
        }
        val crypto = CookieCrypto(properties)

        val plain = "CMSSESSIONID=abc; TS01=def"
        val encrypted = crypto.encrypt(plain)
        val decrypted = crypto.decrypt(encrypted)

        assertEquals(plain, decrypted)
    }
}
