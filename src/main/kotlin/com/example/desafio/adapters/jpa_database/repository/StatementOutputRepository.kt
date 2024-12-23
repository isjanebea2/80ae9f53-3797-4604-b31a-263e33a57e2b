package com.example.desafio.adapters.jpa_database.repository

import com.example.desafio.adapters.jpa_database.entity.StatementHistory
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*

interface StatementOutputRepository : JpaRepository<StatementHistory, String> {
    @Transactional
    @Modifying
    @Query(
        value = """
            INSERT INTO statement_output(
                id, value, merchant, account_amount_id, transaction_logs_id, created_at, updated_at
            ) VALUES (
                :id, :value, :merchant, :accountAmountId, :transactionLogId, :createdAt, :updatedAt
            )
        """,
        nativeQuery = true
    )
    fun customInsert(
        id: String,
        value: BigDecimal,
        merchant: String,
        accountAmountId: Long,
        transactionLogId: String,
        createdAt: Instant,
        updatedAt: Instant
    )
}