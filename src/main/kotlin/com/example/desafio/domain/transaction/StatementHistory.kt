package com.example.desafio.domain.transaction

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "statement_output", schema = "caju")
data class StatementHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    var value: BigDecimal,

    @Column(name = "merchant", nullable = false)
    var merchant: String,

    @Column(name = "account_amount_id", nullable = false)
    var accountAmountId: Long,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = Instant.now(),

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    var updatedAt: Instant? = Instant.now() // Atualizado com a última modificação.
)