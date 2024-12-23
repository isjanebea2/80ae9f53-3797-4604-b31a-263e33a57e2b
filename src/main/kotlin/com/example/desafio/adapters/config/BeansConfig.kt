package com.example.desafio.adapters.config

import com.example.desafio.adapters.service.AccountServiceImp
import com.example.desafio.adapters.service.StatementHistoryServiceImp
import com.example.desafio.application.CardWithdrawUseCase
import com.example.desafio.application.ports.`in`.WithdrawEngineService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeansConfig {
    @Bean
    fun cardWithdrawUseCase(
        accountServiceImp: AccountServiceImp,
        statementHistoryServiceImp: StatementHistoryServiceImp,
        withdrawEngineService: WithdrawEngineService
    ): CardWithdrawUseCase = CardWithdrawUseCase(
        accountService = accountServiceImp,
        statementHistoryService = statementHistoryServiceImp,
        withdrawEngineService = withdrawEngineService
    )
}