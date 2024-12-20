package com.example.desafio.adapters.jpa_database.repository

import com.example.desafio.domain.transaction.StatementHistory
import org.springframework.data.jpa.repository.JpaRepository

interface StatementOutputRepository : JpaRepository<StatementHistory, Long>