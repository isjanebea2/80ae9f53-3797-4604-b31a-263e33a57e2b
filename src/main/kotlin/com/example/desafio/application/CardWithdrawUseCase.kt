package com.example.desafio.application

import com.example.desafio.adapters.rest.dto.TransactionRequest
import com.example.desafio.application.ports.AccountService
import com.example.desafio.application.ports.StatementHistoryService
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.Mcc
import com.example.desafio.domain.transaction.WithdrawProcess
import com.example.desafio.domain.transaction.enums.TransactionCode
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.springframework.stereotype.Service


@Service
class CardWithdrawUseCase(
    private val accountService: AccountService,
    private val statementHistoryService: StatementHistoryService
) {
    val logger: Log = LogFactory.getLog(CardWithdrawUseCase::class.java)

    fun execute(request: TransactionRequest): TransactionCode {

        val currentMccProcess = Mcc.fromString(request.mcc)

        logger.info("Executing transaction process for account ${request.accountId} and request (TODO REQUEST ID) and operation = $currentMccProcess")

        val account = accountService.findAccountById(request.accountId.toLong())

        val withdrawProcessed = WithdrawProcess(
            account,
            request.totalAmount.toBigDecimal(),
            currentMccProcess,
            request.merchant
        )

        val withdrawResult = processWithdraw(withdrawProcessed, currentMccProcess)

        if (withdrawResult.isError()) {
            logger.error("Error to process transaction with mcc $currentMccProcess status ${withdrawResult.status}")
            return withdrawResult.status
        }

        if (!withdrawResult.isRejected()) {
            return saveWithdraw(withdrawResult, withdrawProcessed)
        }

        fun processFallback(mcc: Mcc): TransactionCode? {
            val fallbackResult = processWithdraw(withdrawProcessed, mcc)
            logger.info("Transaction process fallback with mcc $mcc status ${withdrawResult.status}")
            return if (!fallbackResult.isRejected()) {
                saveWithdraw(fallbackResult, withdrawProcessed.copy(mcc = fallbackResult.mcc))
            } else null
        }

        processFallback(Mcc.MEAL)?.let { return it }

        processFallback(Mcc.CASH)?.let { return it }

        return withdrawResult.status
    }


    private fun processWithdraw(withdrawProcess: WithdrawProcess, mcc: Mcc): Withdraw {

        val result = withdrawProcess.copy(mcc = mcc).toResult()

        if (result.status.isRejected()) {
            if (result.status.isError()) {
                this.logger.error("Error to process transaction with status ${result.status}")
            }
        }

        return result
    }

    private fun saveWithdraw(
        withdraw: Withdraw,
        withdrawProcess: WithdrawProcess
    ): TransactionCode {
        logger.info("Attempted to execute transaction process accountAmount ${withdraw.accountAmountTargetId}")

        val status = accountService.withdraw(withdraw.accountAmountTargetId, withdrawProcess)

        if (!status) return TransactionCode.REJECT

        logger.info("Successfully executed transaction process for account ${withdraw.accountAmountTargetId} with totalAmount ${withdraw.totalAmount} ")

        statementHistoryService.withdrawSave(withdraw.accountAmountTargetId, withdrawProcess)

        return withdraw.status
    }
}