package dev.lueem.integration.supercard.infra

import dev.lueem.shared.config.SupercardProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Base64
import java.util.Optional

class CookieCryptoTest {

    @Test
    fun `encrypt and decrypt roundtrip`() {
        val properties = SupercardProperties().apply {
            encryptionKeyValue = Optional.of(Base64.getEncoder().encodeToString(ByteArray(32) { 1 }))
            dbUrlValue = Optional.empty()
            dbUserValue = Optional.empty()
            dbPasswordValue = Optional.empty()
        }
        val crypto = CookieCrypto(properties)
        val plain = "CMSSESSIONID=abc; TS01=def"

        val decrypted = crypto.decrypt(crypto.encrypt(plain))

        assertEquals(plain, decrypted)
    }
}
