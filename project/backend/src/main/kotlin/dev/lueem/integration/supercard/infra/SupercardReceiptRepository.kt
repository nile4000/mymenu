package dev.lueem.integration.supercard.infra

import dev.lueem.extraction.api.ReceiptResponse
import dev.lueem.shared.config.SupercardProperties
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.math.BigDecimal
import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ApplicationScoped
class SupercardReceiptRepository @Inject constructor(
    private val properties: SupercardProperties
) {

    fun ensureSchema() {
        withConnection { conn ->
            conn.createStatement().use { st ->
                st.execute("alter table receipt add column if not exists \"External_Source\" text")
                st.execute("alter table receipt add column if not exists \"External_Receipt_Id\" text")
                st.execute(
                    """
                    create unique index if not exists ux_receipt_external_source_id
                    on receipt ("External_Source", "External_Receipt_Id")
                    where "External_Source" is not null and "External_Receipt_Id" is not null
                    """.trimIndent()
                )
            }
        }
    }

    fun existsByExternalId(externalSource: String, supercardExternalReceipt: String): Boolean = withConnection { conn ->
        conn.prepareStatement(
            """
            select 1
            from receipt
            where "External_Source" = ? and "External_Receipt_Id" = ?
            limit 1
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, externalSource)
            ps.setString(2, supercardExternalReceipt)
            ps.executeQuery().use { rs -> rs.next() }
        }
    }

    fun findExistingExternalIds(externalSource: String, supercardExternalReceipts: Collection<String>): Set<String> {
        if (supercardExternalReceipts.isEmpty()) {
            return emptySet()
        }

        return withConnection { conn ->
            supercardExternalReceipts
                .distinct()
                .chunked(500)
                .flatMapTo(linkedSetOf()) { chunk ->
                    val placeholders = chunk.joinToString(",") { "?" }
                    conn.prepareStatement(
                        """
                        select "External_Receipt_Id"
                        from receipt
                        where "External_Source" = ?
                          and "External_Receipt_Id" in ($placeholders)
                        """.trimIndent()
                    ).use { ps ->
                        ps.setString(1, externalSource)
                        chunk.forEachIndexed { index, id ->
                            ps.setString(index + 2, id)
                        }
                        ps.executeQuery().use { rs ->
                            buildList {
                                while (rs.next()) {
                                    add(rs.getString(1))
                                }
                            }
                        }
                    }
                }
        }
    }

    fun insertReceiptWithArticles(
        extraction: ReceiptResponse,
        externalSource: String,
        supercardExternalReceipt: String,
        purchaseDateOverride: String? = null,
        totalOverride: java.math.BigDecimal? = null
    ) {
        withConnection { conn ->
            conn.autoCommit = false
            try {
                val receiptId = insertReceipt(conn, extraction, externalSource, supercardExternalReceipt, purchaseDateOverride, totalOverride)
                insertArticles(conn, receiptId, extraction, purchaseDateOverride)
                conn.commit()
            } catch (e: Exception) {
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = true
            }
        }
    }

    private fun insertReceipt(
        conn: java.sql.Connection,
        extraction: ReceiptResponse,
        externalSource: String,
        supercardExternalReceipt: String,
        purchaseDateOverride: String? = null,
        totalOverride: java.math.BigDecimal? = null
    ): Long {
        conn.prepareStatement(
            """
            insert into receipt ("Created_At", "Corp", "Total_Receipt", "Purchase_Date", "Uuid", "Total_R_Open_Ai", "Total_R_Extract", "External_Source", "External_Receipt_Id")
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            on conflict ("External_Source", "External_Receipt_Id") where "External_Source" is not null and "External_Receipt_Id" is not null do nothing
            returning "Id"
            """.trimIndent()
        ).use { ps ->
            ps.setTimestamp(1, Timestamp.from(Instant.now()))
            ps.setString(2, extraction.corp)
            ps.setBigDecimal(3, totalOverride ?: extraction.total)
            ps.setDate(4, Date.valueOf(purchaseDateOverride?.let { parseDate(it) } ?: parseDate(extraction.purchaseDate)))
            ps.setString(5, extraction.uid)
            ps.setLong(6, extraction.metadata.openAiArticleCount.toLong())
            ps.setLong(7, extraction.metadata.extractedTotalRow.toLong())
            ps.setString(8, externalSource)
            ps.setString(9, supercardExternalReceipt)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getLong(1)
                }
            }
        }

        conn.prepareStatement(
            """
            select "Id" from receipt
            where "External_Source" = ? and "External_Receipt_Id" = ?
            limit 1
            """.trimIndent()
        ).use { ps ->
            ps.setString(1, externalSource)
            ps.setString(2, supercardExternalReceipt)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getLong(1)
                }
            }
        }

        throw IllegalStateException("Failed to resolve receipt id after insert")
    }

    private fun insertArticles(conn: java.sql.Connection, receiptId: Long, extraction: ReceiptResponse, purchaseDateOverride: String? = null) {
        conn.prepareStatement(
            """
            insert into article ("Created_At", "Name", "Price", "Quantity", "Discount", "Total", "Category", "Purchase_Date", "Receipt_Id")
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { ps ->
            for (article in extraction.articles) {
                ps.clearParameters()
                ps.setTimestamp(1, Timestamp.from(Instant.now()))
                ps.setString(2, article.name)
                ps.setBigDecimal(3, article.price)
                ps.setBigDecimal(4, article.quantity)
                ps.setBigDecimal(5, article.discount)
                ps.setBigDecimal(6, article.total)
                ps.setString(7, article.category)
                ps.setDate(8, Date.valueOf(purchaseDateOverride?.let { parseDate(it) } ?: parseDate(extraction.purchaseDate)))
                ps.setLong(9, receiptId)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun parseDate(value: String): LocalDate {
        val trimmed = value.trim()
        return runCatching { LocalDate.parse(trimmed) }
            .recoverCatching { LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }
            .recoverCatching { LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd.MM.yy")) }
            .getOrElse { throw IllegalArgumentException("Unsupported date format: '$value'") }
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
