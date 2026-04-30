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
        get() = encryptionKeyValue.normalized()

    val dbUrl: String
        get() = dbUrlValue.normalized()

    val dbUser: String
        get() = dbUserValue.normalized()

    val dbPassword: String
        get() = dbPasswordValue.normalized()

    private fun Optional<String>.normalized(): String =
        orElse("")
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
}
