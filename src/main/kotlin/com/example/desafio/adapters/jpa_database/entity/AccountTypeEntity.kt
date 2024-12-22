package com.example.desafio.adapters.jpa_database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "account_type", schema = "caju")
data class AccountTypeEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: Int,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    var createdAt: Instant,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    var updatedAt: Instant,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)