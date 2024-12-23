package com.example.desafio.adapters.service

import com.example.desafio.adapters.jpa_database.repository.StatementOutputRepository
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.adapters.jpa_database.entity.StatementHistory
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import com.fasterxml.uuid.Generators
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class StatementHistoryServiceImp(
    private val statementOutputRepository: StatementOutputRepository,
) : StatementHistoryService {
    override fun withdrawSave(accountAmountId: Long, transaction: Transaction) {
        this.statementOutputRepository.customInsert(
            value = transaction.totalWithdrawalAmount,
            merchant = transaction.merchant,
            accountAmountId = accountAmountId,
            transactionLogId = transaction.id.toString(),
            updatedAt = Instant.now(),
            createdAt = Instant.now(),
        )
    }

    override fun withdrawSave(transaction: Transaction, withdrawList: List<Withdraw>) {

        val transactionLogId = transaction.id.toString()

        withdrawList.forEach {
            this.statementOutputRepository.customInsert(
                value = it.totalAmount,
                merchant = transaction.merchant,
                accountAmountId = it.accountAmount.id,
                transactionLogId = transactionLogId,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
    }

    private fun generateId() = Generators.timeBasedGenerator().generate().toString()
}