package com.example.desafio.adapters.jpa_database.entity

import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Withdraw
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "account_amount", schema = "caju")
data class AccountAmountEntity(
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
    val accountTypeEntity: AccountTypeEntity,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    val createdAt: Instant,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    val updatedAt: Instant,

    @Column(name = "deleted_at")
    val deletedAt: Instant? = null
) {
    fun toDomain() = AccountAmount(
        id = id,
        value = value,
        accountTypeEntity = accountTypeEntity,
    )

    fun  withdraw(totalValue: BigDecimal): AccountAmountEntity {
        return copy(value =  value - totalValue)
    }
}