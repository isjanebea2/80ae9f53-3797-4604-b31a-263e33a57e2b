package com.example.desafio.domain.account

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "account_amount", schema = "caju")
data class AccountAmount(
    @Id
    @Column(name = "id", nullable = false)
    val id: Long,

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    val value: BigDecimal,

//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "account_id", nullable = false)
//    var account: Account,
    @Column(name = "account_id", nullable = false)
    val accountId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_type_id", nullable = false)
    val accountType: AccountType,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    val createdAt: Instant,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    val updatedAt: Instant,

    @Column(name = "deleted_at")
    val deletedAt: Instant? = null
) {
    fun withdraw(amount: BigDecimal) = this.copy(value = (value - amount))
}