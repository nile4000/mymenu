package dev.lueem.shared.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.Optional

@ApplicationScoped
class SupercardProperties {

    @ConfigProperty(name = "SUPERCARD_SESSION_ENC_KEY")
    var encryptionKeyValue: Optional<String> = Optional.empty()

    @ConfigProperty(name = "SUPERCARD_DB_URL")
    var dbUrlValue: Optional<String> = Optional.empty()

    @ConfigProperty(name = "SUPERCARD_DB_USER")
    var dbUserValue: Optional<String> = Optional.empty()

    @ConfigProperty(name = "SUPERCARD_DB_PASSWORD")
    var dbPasswordValue: Optional<String> = Optional.empty()

    val encryptionKey: String
        get() = normalize(encryptionKeyValue.orElse(""))

    val dbUrl: String
        get() = normalize(dbUrlValue.orElse(""))

    val dbUser: String
        get() = normalize(dbUserValue.orElse(""))

    val dbPassword: String
        get() = normalize(dbPasswordValue.orElse(""))

    private fun normalize(value: String): String {
        val trimmed = value.trim()
        if (trimmed.length >= 2) {
            val first = trimmed.first()
            val last = trimmed.last()
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return trimmed.substring(1, trimmed.length - 1).trim()
            }
        }
        return trimmed
    }
}
