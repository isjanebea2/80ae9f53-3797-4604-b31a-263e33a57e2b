package com.example.desafio.application.ports

import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.WithdrawProcess

interface AccountService {
    fun findAccountById(id: Long): HashMap<String, AccountAmount>
    fun withdraw(targetAccountAmountId: Long, transaction: WithdrawProcess): Boolean
}