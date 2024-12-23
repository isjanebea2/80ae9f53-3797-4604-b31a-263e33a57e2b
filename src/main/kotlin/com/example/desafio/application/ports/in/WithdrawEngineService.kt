package com.example.desafio.application.ports.`in`

import com.example.desafio.domain.account.Account
import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.enums.MerchantCategory

interface WithdrawEngineService {
    fun processForFallback(
        transaction: Transaction,
        account: Account,
        targetCategory: MerchantCategory,
        withdrawList: MutableList<Withdraw>
    ): Withdraw?
    fun process(transaction: Transaction, accountAmount: AccountAmount): Withdraw
}