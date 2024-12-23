package com.example.desafio.application.ports.`in`

import com.example.desafio.domain.account.Account
import com.example.desafio.domain.transaction.Withdraw

interface AccountService {
    fun findAccountById(id: Long): Account?
    fun withdraw(withdraw: Withdraw): Boolean
    fun withdraw(withdrawList: List<Withdraw>): Boolean
}