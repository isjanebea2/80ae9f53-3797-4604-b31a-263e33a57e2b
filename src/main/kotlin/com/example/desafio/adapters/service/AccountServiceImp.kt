package com.example.desafio.adapters.service

import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.adapters.jpa_database.repository.AccountAmountRepository
import com.example.desafio.application.ports.AccountService
import com.example.desafio.domain.transaction.WithdrawProcess
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import kotlin.jvm.optionals.getOrNull

@Service
class AccountServiceImp(
    val accountAmountRepository: AccountAmountRepository,
    private val transactionManager: PlatformTransactionManager
) : AccountService {
    val logger: Log = LogFactory.getLog(AccountServiceImp::class.java)

    override fun findAccountById(id: Long): HashMap<String, AccountAmount> {
        val accountAmounts = accountAmountRepository.findByAccountId(id)
        return this.convertToMap(accountAmounts)
    }

    private fun convertToMap(accountsAmount: MutableList<AccountAmount>): HashMap<String, AccountAmount> {
        return accountsAmount.associateBy { it.accountType.name }.toMutableMap() as HashMap<String, AccountAmount>
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun withdraw(targetAccountAmountId: Long, transaction: WithdrawProcess): Boolean {
        val transactionDefinition = DefaultTransactionDefinition()
        transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED)
        val transactionStatus: TransactionStatus = transactionManager.getTransaction(transactionDefinition)

        try {
            val targetAccountAmount: AccountAmount = accountAmountRepository.findById(targetAccountAmountId).getOrNull()
                ?: throw Exception("Account $targetAccountAmountId not found")


            val currentTransaction =
                transaction.copy(availableAccounts = this.convertToMap(mutableListOf(targetAccountAmount)))

            val result = currentTransaction.toResult()

            if (result.status.isRejected()) {
                throw Exception("Account ${targetAccountAmount.accountId} conflict")
            }

            val entity = targetAccountAmount.copy(value = targetAccountAmount.value - result.totalAmount)

            this.accountAmountRepository.save(entity)

            transactionManager.commit(transactionStatus)

            return true;

        } catch (e: Exception) {
            transactionManager.rollback(transactionStatus)
            this.logger.error(e.message, e)
            return false
        }
    }
}