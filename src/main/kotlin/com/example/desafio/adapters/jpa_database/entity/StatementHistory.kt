package com.example.desafio.adapters.jpa_database.entity

import com.fasterxml.uuid.Generators
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "statement_output", schema = "caju")
data class StatementHistory(
    @Id
    var id: String = Generators.randomBasedGenerator().generate().toString(),

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    val value: BigDecimal,

    @Column(name = "merchant", nullable = false)
    val merchant: String,

    @Column(name = "account_amount_id", nullable = false)
    val accountAmountId: Long,

    @Column(name = "transaction_logs_id", nullable = false)
    val transactionLogId: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = Instant.now(),

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    val updatedAt: Instant? = Instant.now()
)