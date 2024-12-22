package com.example.desafio.adapters.jpa_database.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "account", schema = "caju")
data class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long,

    @Column(name = "status", nullable = false, length = 20)
    val status: String,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    val createdAt: Instant,

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    val updatedAt: Instant,

    @Column(name = "deleted_at")
    val deletedAt: Instant,
)