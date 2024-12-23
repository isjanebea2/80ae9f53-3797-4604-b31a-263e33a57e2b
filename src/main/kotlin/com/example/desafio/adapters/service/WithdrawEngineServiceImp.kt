package com.example.desafio.adapters.service

import com.example.desafio.application.ports.`in`.WithdrawEngineService
import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class WithdrawEngineServiceImp: WithdrawEngineService {
    override fun process(
        transaction: Transaction,
        accountAmount: AccountAmount
    ): Withdraw {
        val withdrawStatus = if (accountAmount.authorizeWithdraw(transaction.totalWithdrawalAmount)) {
            TransactionCodesEnum.APPROVED
        } else {
            TransactionCodesEnum.REJECT
        }

        return Withdraw(
            totalAmount = accountAmount.toWithdraw(transaction.totalWithdrawalAmount),
            merchantCategory = transaction.merchantCategory,
            accountAmount = accountAmount,
            status = withdrawStatus,
            pendingValue = accountAmount.toPendingBalance(transaction.totalWithdrawalAmount)
        )
    }

    override fun processForFallback(
        transaction: Transaction,
        account: Account,
        targetCategory: MerchantCategory,
        withdrawList: MutableList<Withdraw>
    ): Withdraw? {
        if (!transaction.merchantCategory.allowed(targetCategory)) {
            return null
        }

        val lastWithdraw: Withdraw? = withdrawList.lastOrNull()

        val accountAmount: AccountAmount = account.findAccountAmountByMcc(targetCategory) ?: return null

        if (accountAmount.value <= BigDecimal.ZERO) return null

        lastWithdraw?.let {
            if (!it.hasPendingAmount()) return null
        }

        val adjustedWithdrawalAmount = lastWithdraw?.pendingValue ?: transaction.totalWithdrawalAmount

        val applicableValue = if (accountAmount.authorizeWithdraw(adjustedWithdrawalAmount))
            adjustedWithdrawalAmount
        else
            accountAmount.value

        return Withdraw(
            totalAmount = applicableValue,
            merchantCategory = targetCategory,
            accountAmount = accountAmount,
            status = TransactionCodesEnum.REJECT,
            pendingValue = accountAmount.toPendingBalance(adjustedWithdrawalAmount)
        )
    }
}