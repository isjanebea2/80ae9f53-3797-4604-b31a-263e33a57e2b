package com.example.desafio.application

import com.example.desafio.application.ports.`in`.AccountService
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.application.ports.out.WithdrawResult
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.WithdrawProcess
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.springframework.stereotype.Service


@Service
class CardWithdrawUseCase(
    private val accountService: AccountService,
    private val statementHistoryService: StatementHistoryService
) {
    val logger: Log = LogFactory.getLog(CardWithdrawUseCase::class.java)

    fun execute(withdrawProcess: WithdrawProcess): WithdrawResult {

        val accountId = withdrawProcess.accountId
        logger.info("Executing transaction process for account $accountId and request  and operation = ${withdrawProcess.merchantCategory}")

        val account = accountService.findAccountById(accountId.toLong()) ?: return this.processUnavailableTransaction(
            withdrawProcess,
            TransactionCodesEnum.UNAVAILABLE_MAIN_ACCOUNT
        )

        if (account.isAvailableAccountsAmount()) {
            return this.processUnavailableTransaction(
                withdrawProcess,
                TransactionCodesEnum.UNAVAILABLE_EMPTY_ACCOUNTS_AMOUNT
            )
        }

        val targetAccountAmount = account.findAccountAmountByMcc(withdrawProcess.merchantCategory)
            ?: return this.processUnavailableTransaction(withdrawProcess, TransactionCodesEnum.UNAVAILABLE_MAIN_ACCOUNT)

        val withdraw = this.processWithdraw(withdrawProcess, targetAccountAmount)

        if (withdraw.status.isError()) {
            return this.processUnavailableTransaction(
                withdrawProcess,
                TransactionCodesEnum.UNAVAILABLE_MAIN_ACCOUNT
            )
        }

        if (!withdraw.status.isRejected()) {
            return saveWithdraw(withdraw, withdrawProcess)
        }

        processFallback(
            withdrawProcess.copy(merchantCategory = MerchantCategory.MEAL),
            account
        )?.let { return it }

        processFallback(
            withdrawProcess.copy(merchantCategory = MerchantCategory.CASH),
            account
        )?.let { return it }

        return this.processUnavailableTransaction(withdrawProcess, TransactionCodesEnum.REJECT)
    }

    private fun processFallback(withdrawProcess: WithdrawProcess, account: Account): WithdrawResult? {

        val targetAccountAmountFallback = account.findAccountAmountByMcc(withdrawProcess.merchantCategory)

        targetAccountAmountFallback?.let {
            val fallbackWithdraw = processWithdraw(
                withdrawProcess,
                accountAmount = it
            )
            val fallbackStatus = fallbackWithdraw.status

            withdrawProcess.changeStatus(fallbackStatus)

            logger.info("Transaction process fallback with mcc ${withdrawProcess.merchantCategory} status $fallbackStatus")

            if (fallbackStatus.isApproved()) {
                return saveWithdraw(fallbackWithdraw, withdrawProcess)
            }
        }

        return null
    }

    private fun processWithdraw(withdrawProcess: WithdrawProcess, accountAmount: AccountAmount): Withdraw {
        val withdrawStatus = if (accountAmount.authorizeWithdraw(withdrawProcess.totalWithdrawalAmount)) {
            TransactionCodesEnum.APPROVED
        } else {
            TransactionCodesEnum.REJECT
        }

        return Withdraw(
            totalAmount = withdrawProcess.totalWithdrawalAmount,
            merchantCategory = withdrawProcess.merchantCategory,
            accountAmount = accountAmount,
            status = withdrawStatus,
        )
    }

    private fun saveWithdraw(
        withdraw: Withdraw,
        withdrawProcess: WithdrawProcess
    ): WithdrawResult {
        val targetAccountAmountId = withdraw.accountAmount.id
        logger.info("Attempted to execute transaction process accountAmount $targetAccountAmountId")

        try {
            val status = accountService.withdraw(targetAccountAmountId, withdrawProcess)

            if (!status) {
                this.processUnavailableTransaction(withdrawProcess, TransactionCodesEnum.REJECT)
            }

            logger.info("Successfully executed transaction process for account $targetAccountAmountId with totalAmount ${withdraw.totalAmount} ")

            statementHistoryService.withdrawSave(targetAccountAmountId, withdrawProcess)

            withdrawProcess.changeStatus(withdraw.status)
        } catch (e: Exception) {
            this.processUnavailableTransaction(withdrawProcess, TransactionCodesEnum.INTERNAL_ERROR)
            this.logger.error("Occurred un error when persist withdraw of account $targetAccountAmountId", e)
        }

        return WithdrawResult(withdrawProcess.status)
    }

    private fun processUnavailableTransaction(
        withdrawProcess: WithdrawProcess,
        transactionCodesEnum: TransactionCodesEnum
    ): WithdrawResult {
        withdrawProcess.changeStatus(transactionCodesEnum)

        if (withdrawProcess.status.isError()) {
            this.logger.error("Error to process transaction for ${withdrawProcess.accountId} with code $transactionCodesEnum")
            this.logger.error(withdrawProcess)
        }

        return WithdrawResult(withdrawProcess.status)
    }

}