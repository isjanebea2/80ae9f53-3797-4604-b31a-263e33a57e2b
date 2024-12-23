package com.example.desafio.adapters.service

import com.example.desafio.adapters.jpa_database.entity.AccountAmountEntity
import com.example.desafio.adapters.jpa_database.repository.AccountAmountRepository
import com.example.desafio.adapters.jpa_database.repository.AccountRepository
import com.example.desafio.application.ports.`in`.AccountService
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Withdraw
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import kotlin.jvm.optionals.getOrNull

@Service
class AccountServiceImp(
    private val accountAmountRepository: AccountAmountRepository,
    private val accountRepository: AccountRepository,
    private val transactionManager: PlatformTransactionManager
) : AccountService {
    val logger: Log = LogFactory.getLog(AccountServiceImp::class.java)

    override fun findAccountById(id: Long): Account? {
        val account = accountRepository.findByIdOrNull(id) ?: return null

        val accountAmounts = accountAmountRepository.findByAccountId(id)

        return Account(
            availableAccountsAmount = this.convertToMap(accountAmounts),
            id = id,
            status = account.status
        )
    }

    private fun convertToMap(accountsAmount: MutableList<AccountAmountEntity>): HashMap<String, AccountAmount> {

        return accountsAmount
            .map { it.toDomain() }
            .associateBy { it.accountTypeEntity.name }.toMutableMap() as HashMap<String, AccountAmount>
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun withdraw(withdraw: Withdraw): Boolean {
        val transactionDefinition = DefaultTransactionDefinition()
        transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED)
        val transactionStatus: TransactionStatus = transactionManager.getTransaction(transactionDefinition)

        try {
            val targetAccountAmountEntity: AccountAmountEntity =
                accountAmountRepository.findById(withdraw.accountAmount.id).getOrNull()
                    ?: throw Exception("Account ${withdraw.accountAmount.id} not found")

            val accountAmount = targetAccountAmountEntity.toDomain()

            if (!accountAmount.authorizeWithdraw(withdraw.totalAmount)) {
                throw Exception("Account ${targetAccountAmountEntity.accountId} conflict")
            }

            val entity =
                targetAccountAmountEntity.copy(value = accountAmount.value - withdraw.totalAmount)

            this.accountAmountRepository.save(entity)

            transactionManager.commit(transactionStatus)

            return true;

        } catch (e: Exception) {
            transactionManager.rollback(transactionStatus)
            this.logger.error(e.message, e)
            return false
        }
    }

    override fun withdraw(withdrawList: List<Withdraw>): Boolean {
        val transactionStatus = createTransaction()
        try {
            val accountsAmountEntity = accountAmountRepository.findAllById(
                withdrawList.map { it.accountAmount.id }
            ).associateBy { it.id }

            val accountsAmount = accountsAmountEntity.mapValues {
                it.value.toDomain()
            }.toMutableMap() as HashMap<Long, AccountAmount>

            if (accountsAmount.size != withdrawList.size) {
                throw Exception("Account ${withdrawList.size} not matching")
            }

            val pendingAccountsAmount = mutableListOf<AccountAmountEntity>()

            withdrawList.forEach { withdraw ->
                val currentAccountAmount = accountsAmount[withdraw.accountAmount.id]
                currentAccountAmount?.let {
                    run {
                        if (!it.authorizeWithdraw(withdraw.totalAmount)) {
                            throw Exception("Account ${withdraw.accountAmount.id} conflict")
                        }

                        accountsAmountEntity[it.id]?.let { entity ->
                            run {
                                pendingAccountsAmount.add(entity.withdraw(withdraw.totalAmount))
                            }
                        }
                    }
                }
            }

            this.accountAmountRepository.saveAll(pendingAccountsAmount)

            transactionManager.commit(transactionStatus)

            return true;

        } catch (e: Exception) {
            transactionManager.rollback(transactionStatus)
            this.logger.error(e.message, e)
            return false
        }
    }

    private fun createTransaction(): TransactionStatus {
        val transactionDefinition = DefaultTransactionDefinition().apply {
            isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED
        }
        return transactionManager.getTransaction(transactionDefinition)
    }
}