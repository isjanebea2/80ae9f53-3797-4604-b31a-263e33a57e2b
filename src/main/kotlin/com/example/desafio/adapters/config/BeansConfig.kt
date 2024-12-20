package com.example.desafio.adapters.config

import com.example.desafio.adapters.service.AccountServiceImp
import com.example.desafio.adapters.service.StatementHistoryServiceImp
import com.example.desafio.application.CardWithdrawUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeansConfig {
    @Bean
    fun cardWithdrawUseCase(
        accountServiceImp: AccountServiceImp,
        statementHistoryServiceImp: StatementHistoryServiceImp
    ): CardWithdrawUseCase = CardWithdrawUseCase(
        accountService = accountServiceImp, statementHistoryService = statementHistoryServiceImp
    )
}