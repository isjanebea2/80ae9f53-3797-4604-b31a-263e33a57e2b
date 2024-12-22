package com.example.desafio.application.ports.`in`

import com.example.desafio.domain.account.Account
import com.example.desafio.domain.transaction.WithdrawProcess

interface AccountService {
    fun findAccountById(id: Long): Account?
    fun withdraw(targetAccountAmountId: Long, transaction: WithdrawProcess): Boolean
}