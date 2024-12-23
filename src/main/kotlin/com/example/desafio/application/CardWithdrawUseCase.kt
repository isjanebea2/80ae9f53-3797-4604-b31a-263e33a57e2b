package com.example.desafio.application

import com.example.desafio.application.ports.`in`.AccountService
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.application.ports.`in`.WithdrawEngineService
import com.example.desafio.application.ports.out.WithdrawResult
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.springframework.stereotype.Service

@Service
class CardWithdrawUseCase(
    private val accountService: AccountService,
    private val statementHistoryService: StatementHistoryService,
    private val withdrawEngineService: WithdrawEngineService
) {
    val logger: Log = LogFactory.getLog(CardWithdrawUseCase::class.java)

    fun execute(transaction: Transaction): WithdrawResult {

        val accountId = transaction.accountId


        logger.info("Executing transaction process for account $accountId and request  and operation = ${transaction.merchantCategory}")

        val account = accountService.findAccountById(accountId.toLong()) ?: return this.processUnavailableTransaction(
            transaction,
            TransactionCodesEnum.UNAVAILABLE_MAIN_ACCOUNT
        )

        if (account.notAvailableAccountsAmount()) {
            return this.processUnavailableTransaction(
                transaction,
                TransactionCodesEnum.UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT
            )
        }

        if (!account.isAvailableBalance(transaction.totalWithdrawalAmount)) {
            return this.processUnavailableTransaction(transaction, TransactionCodesEnum.REJECT)
        }

        account.findAccountAmountByMcc(transaction.merchantCategory)?.let {
            val withdraw = withdrawEngineService.process(transaction, it)

            if (!withdraw.status.isRejected()) {
                return withdrawCommon(withdraw, transaction)
            }
        }

        return processForFallback(transaction, account)
    }

    private fun withdrawCommon(
        withdraw: Withdraw,
        transaction: Transaction
    ): WithdrawResult {
        val targetAccountAmountId = withdraw.accountAmount.id
        logger.info("Attempted to execute transaction process accountAmount $targetAccountAmountId")

        try {
            val status = accountService.withdraw(withdraw)

            if (status) {
                this.processUnavailableTransaction(transaction, TransactionCodesEnum.REJECT)
            }

            logger.info("Successfully executed transaction process for account $targetAccountAmountId with totalAmount ${withdraw.totalAmount} ")

            statementHistoryService.withdrawSave(targetAccountAmountId, transaction)

            transaction.changeStatus(withdraw.status)
        } catch (e: Exception) {
            transaction.changeStatus(TransactionCodesEnum.INTERNAL_ERROR)
            this.processUnavailableTransaction(transaction, TransactionCodesEnum.INTERNAL_ERROR)
            this.logger.error("Occurred un error when persist withdraw of account $targetAccountAmountId", e)
        }

        return WithdrawResult(transaction.status)
    }

    private fun processForFallback(
        transaction: Transaction,
        account: Account
    ): WithdrawResult {
        val withdrawList = mutableListOf<Withdraw>()

        withdrawEngineService.processForFallback(
            transaction = transaction,
            targetCategory = MerchantCategory.FOOD,
            account = account,
            withdrawList = withdrawList
        )?.let {
            withdrawList.add(it)
        }

        withdrawEngineService.processForFallback(
            transaction = transaction,
            targetCategory = MerchantCategory.MEAL,
            account = account,
            withdrawList = withdrawList
        )?.let {
            withdrawList.add(it)
        }

        withdrawEngineService.processForFallback(
            transaction = transaction,
            targetCategory = MerchantCategory.CASH,
            account = account,
            withdrawList = withdrawList
        )?.let {
            withdrawList.add(it)
        }

        if (withdrawList.isEmpty()) {
            return this.processUnavailableTransaction(transaction, TransactionCodesEnum.REJECT)
        }

        val totalAmountOfEngine = withdrawList.map { it.totalAmount }
            .reduce { acc, balance -> acc + balance }


        fun isInvalidAmountOfEngine(): Boolean {
            val pendingValue = totalAmountOfEngine - transaction.totalWithdrawalAmount

            return pendingValue.toFloat() != 0.toFloat()
        }

        if (isInvalidAmountOfEngine()) {
            logger.error("Occurred error with engine to process total amount")
            return this.processUnavailableTransaction(transaction, TransactionCodesEnum.INTERNAL_ERROR)
        }

        try {
            val status = accountService.withdraw(withdrawList)

            if (status) {
                statementHistoryService.withdrawSave(transaction, withdrawList)

                return WithdrawResult(TransactionCodesEnum.APPROVED)
            }
        } catch (e: Exception) {
            this.logger.error(e.message, e)
            return this.processUnavailableTransaction(transaction, TransactionCodesEnum.INTERNAL_ERROR)
        }

        return this.processUnavailableTransaction(transaction, TransactionCodesEnum.REJECT)
    }


    private fun processUnavailableTransaction(
        transaction: Transaction,
        transactionCodesEnum: TransactionCodesEnum
    ): WithdrawResult {
        transaction.changeStatus(transactionCodesEnum)

        if (transaction.status.isError()) {
            this.logger.error("Error to process transaction for ${transaction.accountId} with code $transactionCodesEnum")
            this.logger.error(transaction)
        }

        return WithdrawResult(transaction.status)
    }

}