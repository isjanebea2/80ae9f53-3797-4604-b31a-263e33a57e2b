package com.example.desafio.adapters.jpa_database.repository

import com.example.desafio.adapters.jpa_database.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<AccountEntity, Long>