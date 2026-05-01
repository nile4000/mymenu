package dev.lueem.integration.supercard.infra

import dev.lueem.shared.config.SupercardProperties
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.Instant

@ApplicationScoped
class SupercardConfigStore @Inject constructor(
    private val properties: SupercardProperties
) {

    fun ensureSchema() {
        withConnection { conn ->
            conn.createStatement().use { st ->
                st.execute(
                    """
                    create table if not exists integration_config (
                      key text primary key,
                      value_text text null,
                      value_encrypted text null,
                      updated_at timestamptz not null default now()
                    )
                    """.trimIndent()
                )
            }
        }
    }

    fun upsertText(key: String, value: String?) {
        withConnection { conn ->
            conn.prepareStatement(
                """
                insert into integration_config (key, value_text, updated_at)
                values (?, ?, ?)
                on conflict (key)
                do update set value_text = excluded.value_text, updated_at = excluded.updated_at
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, key)
                ps.setString(2, value)
                ps.setTimestamp(3, Timestamp.from(Instant.now()))
                ps.executeUpdate()
            }
        }
    }

    fun upsertEncrypted(key: String, value: String?) {
        withConnection { conn ->
            conn.prepareStatement(
                """
                insert into integration_config (key, value_encrypted, updated_at)
                values (?, ?, ?)
                on conflict (key)
                do update set value_encrypted = excluded.value_encrypted, updated_at = excluded.updated_at
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, key)
                ps.setString(2, value)
                ps.setTimestamp(3, Timestamp.from(Instant.now()))
                ps.executeUpdate()
            }
        }
    }

    fun getText(key: String): String? = withConnection { conn ->
        conn.prepareStatement("select value_text from integration_config where key = ?").use { ps ->
            ps.setString(1, key)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    fun getEncrypted(key: String): String? = withConnection { conn ->
        conn.prepareStatement("select value_encrypted from integration_config where key = ?").use { ps ->
            ps.setString(1, key)
            ps.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    fun getUpdatedAt(key: String): Instant? = withConnection { conn ->
        conn.prepareStatement("select updated_at from integration_config where key = ?").use { ps ->
            ps.setString(1, key)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    rs.getTimestamp(1)?.toInstant()
                } else {
                    null
                }
            }
        }
    }

    private fun <T> withConnection(block: (java.sql.Connection) -> T): T {
        val url = properties.dbUrl.trim()
        require(url.isNotBlank()) {
            "Missing Supercard DB config. Set SUPERCARD_DB_URL, SUPERCARD_DB_USER and SUPERCARD_DB_PASSWORD."
        }
        Class.forName("org.postgresql.Driver")
        DriverManager.getConnection(url, properties.dbUser, properties.dbPassword).use { conn ->
            return block(conn)
        }
    }
}
